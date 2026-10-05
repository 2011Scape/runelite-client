import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import net.runelite.api.hooks.Callbacks;
import net.runelite.client.RuneLite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routes the client's own AWT input handlers into the RuneLite
 * {@link Callbacks} interface.
 * <p>
 * The client installs its own listeners on the game canvas - {@code Class373_Sub1}
 * and {@code Class373_Sub2} for mouse input and {@code Class346_Sub1} for keyboard
 * input - and translates what they receive into records the game loop drains each
 * cycle. Those classes live in the unnamed package, and a class in a named package
 * cannot import the unnamed package, so this bridge lives here alongside them and
 * is called directly. It is the same arrangement as {@code NpcHullHooks}.
 * <p>
 * Without this, {@code Hooks} implements all eleven input callbacks and nothing
 * ever invokes them, which leaves {@code MouseManager}, {@code KeyManager} and
 * everything registered with them - including {@code OverlayRenderer}'s
 * overlay-managing mode and {@code InventoryGridPlugin}'s drag preview - inert.
 * <p>
 * Two guarantees matter to the callers:
 * <ul>
 *   <li><b>An event is never replaced by {@code null}.</b> A listener that returns
 *       null (a real possibility, see {@code OverlayRenderer.mouseDragged}) yields
 *       the original event instead, so the client's handler always has something to
 *       inspect.</li>
 *   <li><b>A listener can never throw into the client.</b> Input dispatch runs on
 *       the AWT event thread; an exception escaping a plugin would abort handling
 *       of that event. Failures are logged a bounded number of times and the event
 *       continues unmodified.</li>
 * </ul>
 * Consumption is preserved as documented: callers check
 * {@link MouseEvent#isConsumed()} and skip their own handling if a listener
 * cancelled the event.
 */
final class InputHooks
{
	private static final Logger LOGGER = LoggerFactory.getLogger(InputHooks.class);

	/**
	 * Bounds listener-failure logging so a repeat offender cannot flood the log
	 * from the AWT thread.
	 */
	private static final int MAX_REPORTED_FAILURES = 5;

	private static final AtomicInteger REPORTED_FAILURES = new AtomicInteger();

	/**
	 * Resolved once and reused. Mouse moves arrive continuously, so an injector
	 * lookup per event would be wasteful.
	 */
	private static volatile Callbacks callbacks;

	/**
	 * Set only when the injector exists but cannot supply a {@link Callbacks}
	 * binding, which is a permanent condition for the lifetime of the process.
	 * Deliberately not set while the injector is still absent, because the client
	 * constructs its listeners before RuneLite finishes starting up.
	 */
	private static volatile boolean bindingsUnavailable;

	private InputHooks()
	{
	}

	private static Callbacks callbacks()
	{
		Callbacks resolved = callbacks;
		if (resolved != null)
		{
			return resolved;
		}

		if (bindingsUnavailable)
		{
			return null;
		}

		try
		{
			if (RuneLite.getInjector() == null)
			{
				return null;
			}

			resolved = RuneLite.getInjector().getInstance(Callbacks.class);
		}
		catch (Throwable ex)
		{
			// Covers a missing binding and any linkage failure raised while
			// resolving RuneLite itself. Input dispatch runs on the AWT thread,
			// so nothing may escape here.
			bindingsUnavailable = true;
			LOGGER.warn("No Callbacks binding available; client input will not reach RuneLite", ex);
			return null;
		}

		callbacks = resolved;
		return resolved;
	}

	/**
	 * Whether input is currently being routed. Diagnostics only.
	 */
	static boolean isRouting()
	{
		return callbacks() != null;
	}

	private static void report(String hook, Throwable ex)
	{
		if (REPORTED_FAILURES.incrementAndGet() <= MAX_REPORTED_FAILURES)
		{
			LOGGER.warn("Listener threw from {}; event passed through unmodified", hook, ex);
		}
	}

	// ------------------------------------------------------------------
	// mouse
	// ------------------------------------------------------------------

	private static MouseEvent dispatchMouse(String hook, MouseEvent event, Function<Callbacks, MouseEvent> invocation)
	{
		final Callbacks resolved = callbacks();
		if (resolved == null || event == null)
		{
			return event;
		}

		try
		{
			final MouseEvent result = invocation.apply(resolved);
			return result == null ? event : result;
		}
		catch (Throwable ex)
		{
			report(hook, ex);
			return event;
		}
	}

	static MouseEvent pressed(MouseEvent event)
	{
		return dispatchMouse("mousePressed", event, handler -> handler.mousePressed(event));
	}

	static MouseEvent released(MouseEvent event)
	{
		return dispatchMouse("mouseReleased", event, handler -> handler.mouseReleased(event));
	}

	static MouseEvent clicked(MouseEvent event)
	{
		return dispatchMouse("mouseClicked", event, handler -> handler.mouseClicked(event));
	}

	static MouseEvent entered(MouseEvent event)
	{
		return dispatchMouse("mouseEntered", event, handler -> handler.mouseEntered(event));
	}

	static MouseEvent exited(MouseEvent event)
	{
		return dispatchMouse("mouseExited", event, handler -> handler.mouseExited(event));
	}

	static MouseEvent dragged(MouseEvent event)
	{
		return dispatchMouse("mouseDragged", event, handler -> handler.mouseDragged(event));
	}

	static MouseEvent moved(MouseEvent event)
	{
		return dispatchMouse("mouseMoved", event, handler -> handler.mouseMoved(event));
	}

	static MouseWheelEvent wheel(MouseWheelEvent event)
	{
		final Callbacks resolved = callbacks();
		if (resolved == null || event == null)
		{
			return event;
		}

		try
		{
			final MouseWheelEvent result = resolved.mouseWheelMoved(event);
			return result == null ? event : result;
		}
		catch (Throwable ex)
		{
			report("mouseWheelMoved", ex);
			return event;
		}
	}

	// ------------------------------------------------------------------
	// keyboard
	// ------------------------------------------------------------------

	/**
	 * The Callbacks key methods return void, so a key listener cancels the event by
	 * consuming it. The return value here is therefore the consumption state, which
	 * callers use to decide whether to enqueue the key.
	 */
	private static boolean dispatchKey(String hook, KeyEvent event, Consumer<Callbacks> invocation)
	{
		final Callbacks resolved = callbacks();
		if (resolved == null || event == null)
		{
			return false;
		}

		try
		{
			invocation.accept(resolved);
		}
		catch (Throwable ex)
		{
			report(hook, ex);
			return false;
		}

		return event.isConsumed();
	}

	static boolean keyPressed(KeyEvent event)
	{
		return dispatchKey("keyPressed", event, handler -> handler.keyPressed(event));
	}

	static boolean keyReleased(KeyEvent event)
	{
		return dispatchKey("keyReleased", event, handler -> handler.keyReleased(event));
	}

	static boolean keyTyped(KeyEvent event)
	{
		return dispatchKey("keyTyped", event, handler -> handler.keyTyped(event));
	}
}
