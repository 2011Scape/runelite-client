/**
 * Experimental renderer pacing, ported from the open592 client: the renderer is not a second
 * thread. The client's own loop runs the game cycles it is due for, then draws a frame if the
 * frame pacer says one is due, and then waits for whichever comes first - the next frame or the
 * next cycle. The world is therefore drawn at the frame target while the logic keeps its own,
 * lower, 50 Hz rate, and nothing in the draw advances game time, so the extra frames cannot speed
 * the game up.
 * <p>
 * Keeping the draw on the client thread is the whole point: these backends bind their native
 * context to the thread that created it, and moving frames to another thread is what used to
 * black-screen at login and freeze on the toggle. Here there is nothing to transfer - the game
 * cycle and the frame run one after the other on the same thread, exactly as they always relied
 * on, just more frames between cycles.
 * <p>
 * The plugin toggle only flips {@link #requestActive}: on, the loop paces extra frames (and skips
 * the vanilla per-frame sleep, because it waits itself); off, the loop draws exactly one frame per
 * iteration like the vanilla client. Neither blocks.
 */
final class DetachedRenderer {

    /** Frames per second the pacer aims for. */
    static volatile int fpsTarget = 180;

    /** The plugin's intent: true while the decoupled renderer owns frame pacing. */
    private static volatile boolean active;
    /** Set when a live toggle changed the mode; the loop re-syncs the canvas when it sees it. */
    private static volatile boolean canvasRefreshRequested;

    /** Nanoseconds between frames, recomputed whenever the target changes. */
    private static volatile long frameInterval = intervalFor(fpsTarget);

    /** The current fps target, read back so the experimental renderer can persist it. */
    static int getFpsTarget() {
        return fpsTarget;
    }
    /** The clock reading, in nanoseconds, the next frame is due at. */
    private static long nextFrame;

    private static int frameTally;
    private static long frameMark;
    private static volatile int framesPerSecond;

    private DetachedRenderer() {
    }

    /**
     * A plugin toggle flipped the mode. Never blocks: it only records the request and schedules the
     * first frame immediately, so the change takes effect on the loop's next iteration.
     */
    static void requestActive(boolean on) {
        active = on;
        canvasRefreshRequested = true;
        nextFrame = 0L;
        if (!on) {
            frameTally = 0;
            frameMark = 0L;
            framesPerSecond = 0;
        }
    }

    /** Whether the decoupled renderer is the mode the plugin asked for. */
    static boolean isActive() {
        return active;
    }

    /** Whether a live toggle asked the loop to re-sync the canvas. */
    static boolean consumeCanvasRefreshRequest() {
        boolean refresh = canvasRefreshRequested;
        canvasRefreshRequested = false;
        return refresh;
    }

    /** Whether a frame should be drawn at the given clock reading. */
    static boolean frameDue(long now) {
        return frameInterval <= 0L || now >= nextFrame;
    }

    /**
     * Records that a frame was drawn now and schedules the next one, keeping to the limit without
     * running a backlog: a frame that took longer than its interval, or a clock that was reset,
     * starts from now rather than trying to catch up on the frames it missed.
     */
    static void frameDrawn(long now) {
        if (frameInterval <= 0L) {
            return;
        }
        nextFrame += frameInterval;
        if (nextFrame < now || nextFrame - now > frameInterval) {
            nextFrame = now + frameInterval;
        }
    }

    /** How long, in whole milliseconds, until the next frame is due; 0 when it is due already. */
    static long millisUntilNextFrame(long now) {
        long wait = nextFrame - now;
        if (frameInterval <= 0L || wait <= 0L) {
            return 0L;
        }
        return (wait + 999999L) / 1000000L;
    }

    /** Applies a new frame target from the plugin config. */
    static void setFpsTarget(int fps) {
        fpsTarget = fps;
        frameInterval = intervalFor(fps);
    }

    private static long intervalFor(int fps) {
        return fps > 0 ? 1_000_000_000L / fps : 0L;
    }

    /** Counts one presented frame towards the reported rate. */
    static void noteFrame() {
        frameTally++;
        long now = System.currentTimeMillis();
        long window = now - frameMark;
        if (window >= 1000L) {
            framesPerSecond = (int) ((long) frameTally * 1000L / window);
            frameTally = 0;
            frameMark = now;
        }
    }

    /** The most recent measured frame rate, in frames per second. */
    static int framesPerSecond() {
        return framesPerSecond;
    }

    /** Nothing to stop now that frames run on the client thread; kept for the shutdown path. */
    static void shutdown() {
        active = false;
    }
}
