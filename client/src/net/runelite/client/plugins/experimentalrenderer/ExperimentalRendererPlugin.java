package net.runelite.client.plugins.experimentalrenderer;

import com.GameClient;
import com.google.inject.Provides;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.ConfigButtonProvider;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Experimental renderer",
	description = "Detaches the renderer from the game loop and draws the frame on its own thread, paced to a configurable fps target. Also exposes render distance, fog, culling, zoom, and colour controls.",
	tags = {"renderer", "fps", "experimental", "performance"},
	enabledByDefault = false,
	loadWhenOutdated = true
)
public class ExperimentalRendererPlugin extends Plugin implements ConfigButtonProvider
{
	private static final String GROUP = "experimentalrenderer";
	/** Quiet time after the last culling/camera-radius change before the scene rebuild runs. */
	private static final long REBUILD_DEBOUNCE_MS = 500L;
	/**
	 * How often the runtime settings are written back to config. The view distance and zoom move on
	 * the wheel and the rest can be changed outside the panel, so persisting them here is what
	 * carries a change across a reboot.
	 */
	private static final long PERSIST_INTERVAL_MS = 1000L;
	/**
	 * How long a wheel-driven write waits before it fires. An open config panel follows the wheel
	 * almost at once instead of up to a full {@link #PERSIST_INTERVAL_MS} later, while a burst of
	 * notches coalesces into a handful of writes rather than one per notch.
	 */
	private static final long WHEEL_PERSIST_THROTTLE_MS = 150L;

	/** Every config key this plugin owns, so the reset button can clear them all in one pass. */
	private static final String[] ALL_SETTING_KEYS =
	{
		"fpsTarget", "showFps", "renderDistance", "clampFog", "fogScale", "zoom",
		"cullingDistance", "disableCulling", "disableViewCulling", "hideUpperFloors",
		"cameraRadius", "fogColour"
	};

	@Inject
	private GameClient client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ScheduledExecutorService scheduledExecutorService;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ExperimentalRendererConfig config;

	/** The pending debounced rebuild, replaced (and the old one cancelled) on each culling change. */
	private ScheduledFuture<?> pendingRebuild;

	/** The periodic task that writes the wheel-driven view distance and zoom back to config. */
	private ScheduledFuture<?> persistenceTask;

	/** Guards {@link #wheelPersist}. */
	private final Object wheelPersistLock = new Object();
	/** The wheel-driven write waiting out {@link #WHEEL_PERSIST_THROTTLE_MS}, or null if none is pending. */
	private ScheduledFuture<?> wheelPersist;

	@Provides
	ExperimentalRendererConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExperimentalRendererConfig.class);
	}

	@Override
	protected void startUp()
	{
		applyFpsTarget();
		client.setShowFpsOverlay(config.showFps());
		applyViewSettings();
		applyCullingSettings();
		applyColour();
		// Snap the restored view distance and zoom into place so the horizon, fog, and zoom are
		// already correct on the first frame rather than gliding in from the defaults.
		client.snapRendererTuning();
		client.setDetachedRendererEnabled(true);
		// Follow the wheel immediately: a notch writes the new value (throttled) instead of waiting
		// for the periodic sweep.
		client.setTuningChangeListener(this::onWheelTuningChanged);
		startPersistingTuning();
	}

	@Override
	protected void shutDown()
	{
		// Stop following the wheel before the final write, so a notch cannot schedule a write that
		// races the one below.
		client.setTuningChangeListener(null);
		cancelPendingWheelPersist();
		// Final write first, while the runtime state still reflects the user's choices; turning the
		// overlay off below would otherwise be captured as the persisted show-fps value.
		stopPersistingTuning();
		cancelPendingRebuild();
		client.setDetachedRendererEnabled(false);
		client.setShowFpsOverlay(false);
	}

	@Override
	public List<JButton> getConfigButtons()
	{
		List<JButton> buttons = new ArrayList<>();
		JButton rebuild = new JButton("Force scene rebuild");
		rebuild.setToolTipText("<html>Force scene rebuild:<br>Re-requests the current map region so the client runs its scene rebuild, applying a camera-radius or culling change now.</html>");
		rebuild.addActionListener(e -> client.forceSceneRebuild());
		buttons.add(rebuild);
		JButton reset = new JButton("Reset to defaults");
		reset.setToolTipText("<html>Reset to defaults:<br>Clears every stored experimental renderer setting and applies the defaults now.</html>");
		reset.addActionListener(e -> confirmReset(reset));
		buttons.add(reset);
		return buttons;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!GROUP.equals(event.getGroup()))
		{
			return;
		}
		switch (event.getKey())
		{
			case "fpsTarget":
				applyFpsTarget();
				break;
			case "showFps":
				client.setShowFpsOverlay(config.showFps());
				break;
			case "renderDistance":
			case "fogScale":
			case "clampFog":
			case "zoom":
				applyViewSettings();
				break;
			case "disableCulling":
			case "disableViewCulling":
			case "hideUpperFloors":
			case "cullingDistance":
			case "cameraRadius":
				// Apply the value now, but rebuild once the slider settles - not on every tick.
				applyCullingSettings();
				scheduleRebuild();
				break;
			case "fogColour":
				applyColour();
				break;
			default:
				break;
		}
	}

	/**
	 * Rebuilds the scene once the culling and camera-radius sliders settle. Each change reschedules and
	 * cancels the previous one, so dragging a slider produces a single rebuild after
	 * {@link #REBUILD_DEBOUNCE_MS} of quiet instead of one per tick. The rebuild itself is handed to the
	 * client thread, since it writes the build-area request packet.
	 */
	private synchronized void scheduleRebuild()
	{
		if (pendingRebuild != null)
		{
			pendingRebuild.cancel(false);
		}
		pendingRebuild = scheduledExecutorService.schedule(() ->
		{
			synchronized (ExperimentalRendererPlugin.this)
			{
				pendingRebuild = null;
			}
			clientThread.invokeLater(() -> client.forceSceneRebuild());
		}, REBUILD_DEBOUNCE_MS, TimeUnit.MILLISECONDS);
	}

	private synchronized void cancelPendingRebuild()
	{
		if (pendingRebuild != null)
		{
			pendingRebuild.cancel(false);
			pendingRebuild = null;
		}
	}

	/**
	 * The wheel moved the view distance or zoom. Schedules the write that carries the new value to
	 * config after a short throttle, so an open config panel follows the wheel almost at once rather
	 * than up to a full {@link #PERSIST_INTERVAL_MS} later, while a spin coalesces into a handful of
	 * writes instead of one per notch. Called on the client thread, so it only schedules: the write
	 * itself runs on the shared scheduler, keeping the client cycle free.
	 */
	private void onWheelTuningChanged()
	{
		synchronized (wheelPersistLock)
		{
			// A write is already waiting out the throttle; it will pick up this value too, so a burst
			// of notches produces one write per throttle window, not one per notch.
			if (wheelPersist != null)
			{
				return;
			}
			wheelPersist = scheduledExecutorService.schedule(() ->
			{
				// Clear before reading, so a notch that lands while this runs schedules a fresh write
				// rather than being folded into the value already being read.
				synchronized (wheelPersistLock)
				{
					wheelPersist = null;
				}
				persistRuntimeTuning();
			}, WHEEL_PERSIST_THROTTLE_MS, TimeUnit.MILLISECONDS);
		}
	}

	private void cancelPendingWheelPersist()
	{
		synchronized (wheelPersistLock)
		{
			if (wheelPersist != null)
			{
				wheelPersist.cancel(false);
				wheelPersist = null;
			}
		}
	}

	private void applyFpsTarget()
	{
		int target = Math.max(0, Math.min(300, config.fpsTarget()));
		client.setDetachedRendererFpsTarget(target);
	}

	private void applyViewSettings()
	{
		client.setRenderDistance(config.renderDistance());
		client.setFogScale(config.fogScale());
		client.setClampFogToBuiltMap(config.clampFog());
		client.setZoom(config.zoom());
	}

	private void applyCullingSettings()
	{
		client.setDrawRadiusScale(config.cameraRadius());
		client.setCullingDistance(config.cullingDistance());
		client.setCullingDisabled(config.disableCulling());
		client.setViewCullingDisabled(config.disableViewCulling());
		client.setHideUpperFloors(config.hideUpperFloors());
	}

	private void applyColour()
	{
		Color colour = config.fogColour();
		client.setFogColour(colour == null ? -1 : colour.getRGB() & 0xffffff);
	}

	/**
	 * Starts writing the runtime settings back to config, and does one write straight away so a value
	 * changed outside the panel is already safely stored before the next change. The task lives on
	 * the shared scheduler because the values only move on the client thread; reading the volatile
	 * snapshot from here keeps the client thread free.
	 */
	private synchronized void startPersistingTuning()
	{
		persistRuntimeTuning();
		if (persistenceTask == null || persistenceTask.isCancelled() || persistenceTask.isDone())
		{
			persistenceTask = scheduledExecutorService.scheduleWithFixedDelay(
				this::persistRuntimeTuning, PERSIST_INTERVAL_MS, PERSIST_INTERVAL_MS, TimeUnit.MILLISECONDS);
		}
	}

	private synchronized void stopPersistingTuning()
	{
		if (persistenceTask != null)
		{
			persistenceTask.cancel(false);
			persistenceTask = null;
		}
		// One last write so a change made since the previous tick is not lost on shutdown.
		persistRuntimeTuning();
	}

	/**
	 * Writes every experimental renderer setting back to config when the value in force differs from
	 * the value config already resolves to, so the panel and the client always agree. Most settings
	 * are only changed from the panel, but the view distance and zoom move on the wheel, and the fps
	 * readout, fps target and fog colour can be changed by the client's own commands, none of which
	 * would otherwise survive a reboot. A write re-enters {@link #onConfigChanged}, which re-applies
	 * the same value, so the round trip settles at once.
	 */
	private void persistRuntimeTuning()
	{
		try
		{
			persistIfChanged("fpsTarget", client.getFpsTarget(), config.fpsTarget());
			persistIfChanged("showFps", client.isShowFpsOverlay(), config.showFps());
			// 0 means "leave the wheel and the built scene in charge", so it is not a distance to store.
			int viewDistance = client.getViewDistance();
			if (viewDistance > 0)
			{
				persistIfChanged("renderDistance", viewDistance, config.renderDistance());
			}
			persistIfChanged("zoom", client.getZoomOffset(), config.zoom());
			persistIfChanged("fogScale", client.getFogScale(), config.fogScale());
			persistIfChanged("clampFog", client.isClampFogToBuiltMap(), config.clampFog());
			persistIfChanged("cameraRadius", client.getDrawRadiusScale(), config.cameraRadius());
			persistIfChanged("cullingDistance", client.getCullingDistance(), config.cullingDistance());
			persistIfChanged("disableCulling", client.isCullingDisabled(), config.disableCulling());
			persistIfChanged("disableViewCulling", client.isViewCullingDisabled(), config.disableViewCulling());
			persistIfChanged("hideUpperFloors", client.isHideUpperFloors(), config.hideUpperFloors());
			persistFogColour();
		}
		catch (Exception ex)
		{
			// A settings snapshot must never take the client down; the next tick tries again.
		}
	}

	/**
	 * Writes a setting only when the value in force differs from what config already resolves to (the
	 * stored override, or the item's default when none is stored). Comparing against the resolved
	 * value instead of the raw stored value means a setting left at its default stays unstored, so
	 * the reset button's cleared overrides are not written straight back, while a panel change that
	 * already matches never triggers a redundant write.
	 */
	private void persistIfChanged(String key, int value, int configured)
	{
		if (value != configured)
		{
			configManager.setConfiguration(GROUP, key, value);
		}
	}

	private void persistIfChanged(String key, boolean value, boolean configured)
	{
		if (value != configured)
		{
			configManager.setConfiguration(GROUP, key, value);
		}
	}

	/**
	 * Asks before wiping a customised renderer setup. A reset is not reversible (the stored overrides
	 * are unset), so an accidental click must not apply it silently; the dialog defaults to Cancel.
	 */
	private void confirmReset(JButton button)
	{
		int result = JOptionPane.showOptionDialog(button,
			"Reset every experimental renderer setting to its default?\n"
				+ "This clears your customised fog, render distance, culling, zoom, and colour.",
			"Reset to defaults", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
			null, new String[]{"Reset", "Cancel"}, "Cancel");
		if (result == JOptionPane.YES_OPTION)
		{
			resetAllSettings();
		}
	}

	/**
	 * Clears every setting this plugin owns and reapplies the defaults, so one click returns the
	 * renderer to stock. Unsetting each key removes the stored override and fires the config-changed
	 * events that reapply them; the defaults are applied explicitly too, because a key that was never
	 * stored fires no event. Any queued rebuild is dropped and the wheel ease is cancelled with its
	 * targets cleared, so the reset lands at once instead of gliding back to a stale distance; the
	 * vanilla scene size is then re-requested, which shrinks the map back to the default view distance
	 * and, on its reload, re-runs the scene build with the reset camera radius and culling defaults.
	 */
	private void resetAllSettings()
	{
		// Drop the rebuild a slider drag may have queued, so it cannot fire against a half-reset state
		// and re-request the large region the wheel had held, undoing the shrink asked for below.
		cancelPendingRebuild();
		for (String key : ALL_SETTING_KEYS)
		{
			configManager.unsetConfiguration(GROUP, key);
		}
		applyFpsTarget();
		client.setShowFpsOverlay(config.showFps());
		applyViewSettings();
		applyCullingSettings();
		applyColour();
		// Kill the wheel ease and clear its targets, so the horizon does not glide back towards the
		// distance the wheel was last heading for and no AFK-saved distance is restored over it.
		client.resetWheelTuning();
		// Ask the server for the vanilla scene size, not just a rebuild at the size currently built.
		// After an alt + wheel session the built region is the large one, so a plain rebuild would
		// leave the horizon and fog out where the wheel put them; the vanilla request shrinks the map
		// back and its reload re-runs the scene build with the reset culling settings applied. Sent on
		// the client thread like forceSceneRebuild, since it writes the build-area request packet.
		clientThread.invokeLater(() -> client.resetSceneSize());
	}

	/**
	 * Keeps the fog-colour config in step with the colour in force. A negative value means the client
	 * keeps its own colour, so the item is cleared; otherwise it is stored as the same opaque RGB the
	 * picker writes, so the panel and the client agree on what is applied.
	 */
	private void persistFogColour()
	{
		int rgb = client.getFogColour();
		Color stored = config.fogColour();
		if (rgb < 0)
		{
			if (stored != null)
			{
				configManager.unsetConfiguration(GROUP, "fogColour");
			}
			return;
		}
		if (stored == null || (stored.getRGB() & 0xffffff) != rgb)
		{
			configManager.setConfiguration(GROUP, "fogColour", new Color(rgb).getRGB());
		}
	}
}
