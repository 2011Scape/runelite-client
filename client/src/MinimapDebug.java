/**
 * What the minimap last drew, and why it drew it.
 * <p>
 * The minimap is the one piece of the interface whose content is not sent by the server:
 * it is a 512x512 image of the whole 104x104 region, rendered by the client whenever the
 * scene it describes changes ({@code Class348_Sub14.method2808}, driven from
 * {@code Class348_Sub46.method3319}). Every gap between a scene change and that render
 * used to be filled with black - {@code Class107.method1007} painted the widget's own
 * silhouette in it - which is indistinguishable from a map that is simply switched off,
 * and there was nothing on screen or in the console to say which one it was.
 * <p>
 * This class is the record of that: the reason the last frame used, how long it has stood,
 * how far the region gate is from letting the render through, and the two facts the reason
 * has to be read against - the server's map state ({@code Class259.anInt3306}) and the
 * plane the image was rendered for ({@code Class334.anInt4155}) versus the player's.
 * <p>
 * It is written from the render path (once per minimap frame) and read from two places:
 * the developer overlay, through {@code com.GameClient.getMinimapDiagnostics()}, and the
 * console, when {@code Loader.debug} is on and the reason changes. Both only fire on a
 * reason that is worth looking at, so an idle client says nothing.
 * <p>
 * The counters are deliberately plain statics rather than anything threaded through the
 * render path: this is diagnostic state, read on the game thread in the same frame it is
 * written, and the cost of it has to be a couple of assignments.
 */
final class MinimapDebug
{
	/** The map drew from the region image it was rendered for. */
	static final int NONE = 0;

	/** The region image is gone; the last good one stood in for it. */
	static final int STALE_IMAGE = 1;

	/** There is no region image at all, not even a stale one. */
	static final int NO_IMAGE = 2;

	/** The server's map state asked for a blank map, and it was honoured. */
	static final int BLACKOUT_STATE = 3;

	/** The widget has no texture to draw the map into. */
	static final int NO_WIDGET_TEXTURE = 4;

	/** The reason the last frame drew what it drew. */
	static int reason = NONE;

	/** Frames the current reason has stood for. */
	static int reasonFrames;

	/** Frames drawn from the last good image since the client started. */
	static int staleFrames;

	/** Frames drawn with no region image at all since the client started. */
	static int missingFrames;

	/** Frames blanked out by the server's map state since the client started. */
	static int blackoutFrames;

	/** Passes the region gate has deferred the render for, reset when one is let through. */
	static int gateAttempts;

	/** Tiles the region gate was still waiting on at its last pass. */
	static int gateUnresolvedTiles;

	/** The server's map state, as of the last frame. */
	static int serverMapState = -1;

	/** The player's plane, and the plane the region image was rendered for. */
	static int playerPlane = -1;
	static int scenePlane = -1;

	/** Bounds the console line so a reason that flaps cannot flood the log. */
	private static long lastLog;

	private MinimapDebug()
	{
	}

	/**
	 * Records the reason one frame of the minimap drew what it drew. Called from
	 * {@code Class107.method1007} on the game thread.
	 *
	 * @param i one of {@link #NONE}, {@link #STALE_IMAGE}, {@link #NO_IMAGE},
	 *          {@link #BLACKOUT_STATE} or {@link #NO_WIDGET_TEXTURE}
	 */
	static void record(int i)
	{
		if (i == reason)
		{
			reasonFrames++;
		}
		else
		{
			reason = i;
			reasonFrames = 0;
			maybeLog("minimap state changed: " + describe());
		}

		if (i == STALE_IMAGE) staleFrames++;
		else if (i == NO_IMAGE) missingFrames++;
		else if (i == BLACKOUT_STATE) blackoutFrames++;

		playerPlane = Class132.aPlayer_1907 == null ? -1 : Class132.aPlayer_1907.plane;
		scenePlane = Class334.anInt4155;
		serverMapState = Class259.anInt3306;
	}

	/**
	 * Records how the region gate spent a pass: how often it has now deferred the render,
	 * and how many tiles it was waiting on. Called from {@code Class348_Sub14.method2808},
	 * which owns both numbers.
	 *
	 * @param i the passes deferred so far, zero once a render was let through
	 * @param i_0_ the tiles that were still unloaded
	 */
	static void recordGate(int i, int i_0_)
	{
		gateAttempts = i;
		gateUnresolvedTiles = i_0_;
		if (i > 0)
		{
			maybeLog("minimap gate deferred (" + i + "), " + i_0_ + " tiles unresolved");
		}
	}

	/**
	 * The line the developer overlay shows, or null while the map is drawing normally.
	 * The overlay asks this every frame, so an idle client draws nothing.
	 */
	static String warning()
	{
		return reason == NONE ? null : describe();
	}

	/**
	 * The measured state, as one line. Never null: the counters and the two planes are
	 * what make a reason actionable, so they travel with it.
	 */
	static String describe()
	{
		StringBuilder line = new StringBuilder(reasonText(reason));
		if (reason != NONE)
		{
			line.append(" for ").append(reasonFrames).append(reasonFrames == 1 ? " frame" : " frames");
		}

		line.append("; gate ").append(gateAttempts).append(" deferred, ").append(gateUnresolvedTiles).append(" tiles unresolved");
		line.append("; map state ").append(serverMapState);
		line.append("; plane ").append(playerPlane).append(" (image for ").append(scenePlane).append(')');
		if (staleFrames > 0 || missingFrames > 0 || blackoutFrames > 0)
		{
			line.append("; since login: ").append(staleFrames).append(" stale, ").append(missingFrames)
				.append(" empty, ").append(blackoutFrames).append(" blanked");
		}
		return line.toString();
	}

	/** One phrase per reason, so the line reads as a sentence without a lookup table of its own. */
	private static String reasonText(int i)
	{
		switch (i)
		{
			case STALE_IMAGE:
				return "the minimap is drawing its last good region image";
			case NO_IMAGE:
				return "the minimap has no region image at all";
			case BLACKOUT_STATE:
				return "the server asked for a blank minimap";
			case NO_WIDGET_TEXTURE:
				return "the minimap widget has no texture";
			default:
				return "the minimap is drawing its region image";
		}
	}

	/**
	 * The console side, and the only thing that limits it: {@code Loader.debug}, plus one
	 * line a second at most so a reason that flaps between frames cannot flood the log.
	 */
	private static void maybeLog(String string)
	{
		if (!Loader.debug) return;

		long l = System.currentTimeMillis();
		if (l - lastLog < 1000L) return;

		lastLog = l;
		System.out.println(string);
	}
}
