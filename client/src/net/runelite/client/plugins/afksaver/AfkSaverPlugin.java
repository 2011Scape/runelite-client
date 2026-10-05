package net.runelite.client.plugins.afksaver;

import com.GameClient;
import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "AFK performance saver",
	description = "Drops the fog and render distances to the lowest gated value after repeated idle packets, and restores them when the player interacts again.",
	tags = {"afk", "idle", "performance", "fog", "render"},
	enabledByDefault = true,
	loadWhenOutdated = true
)
public class AfkSaverPlugin extends Plugin
{
	@Inject
	private GameClient client;

	@Inject
	private AfkSaverConfig config;

	@Provides
	AfkSaverConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AfkSaverConfig.class);
	}

	@Override
	protected void startUp()
	{
		applyGrace();
		client.setAfkSaverEnabled(true);
	}

	@Override
	protected void shutDown()
	{
		client.setAfkSaverEnabled(false);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if ("afksaver".equals(event.getGroup()) && "idleGracePackets".equals(event.getKey()))
		{
			applyGrace();
		}
	}

	private void applyGrace()
	{
		int grace = Math.max(1, Math.min(10, config.idleGracePackets()));
		client.setAfkSaverGracePackets(grace);
	}
}
