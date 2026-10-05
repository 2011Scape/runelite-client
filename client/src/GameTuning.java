/**
 * Wheel-driven camera tuning: ctrl + wheel moves the camera zoom and alt + wheel moves the view
 * distance (the fog horizon with it), in the shape the open592 client uses. This mirrors that
 * client's {@code ClientConfig} and its {@code client.handleGameZoomScroll} /
 * {@code handleGameViewDistanceScroll} / {@code updateViewDistance} / {@code updateDrawRadius} /
 * {@code updateScene} / {@code getDrawDistance}, so the wheel feels the same as it does there.
 * <p>
 * This client's wheel events travel through the input queue, which does not carry keyboard
 * modifiers, so the listener records the modifiers here at enqueue time
 * ({@link #captureWheelModifiers}) and the per-cycle drain asks this class what the latest
 * wheel event carried. A wheel event is rare next to the frames between enqueue and drain,
 * so the latest event's modifiers are the right ones to use.
 * <p>
 * Both features are targets the display eases towards a fifth of the remaining distance each
 * client cycle ({@link #update()}), so a spin of the wheel glides instead of jumping: the view
 * distance moves 8 tiles a notch and then eases, and the far plane follows the eased value, which
 * is what makes terrain fade in and out as the wheel turns. Zoom is the client's own FOV zoom
 * offset ({@code Class320.zoomStep}), so every clamp the projection already applies stays in force.
 * <p>
 * The view distance is a distance in tiles, exactly the open592 extended view distance, read
 * through {@link #getDrawDistance()}. Unlike open592, which clamps the horizon to the scene the
 * server has actually built, this client lets the wheel move the horizon itself so the fog rolls
 * immediately rather than waiting for a map rebuild. It still never draws past the built map - the
 * drawn radius stays the client's own per-scene-size table radius (see {@link #drawRadius}) - so
 * the lower plane floors cannot bleed through the floor above; the horizon can simply sit past the
 * far map edge until the server answers with a larger region.
 * <p>
 * The scene itself is asked for once, not per scroll: the first alt + wheel scroll requests the
 * largest map size the client can build ({@link #SCENE_SIZES}) and holds it. Every later scroll, in
 * either direction, is far plane movement alone, and a map region is only sent again by the server
 * on the vanilla on-demand path. Asking per scroll made the server rebuild the region while the
 * wheel turned and again when it was taken back, so it is asked once and remembered.
 * <p>
 * Everything here is read and written on the client thread except the two entry points the
 * AWT event thread reaches ({@link #captureWheelModifiers} from the listener), which only
 * touch volatile fields.
 */
final class GameTuning {

	// ------------------------------------------------------------------ camera zoom (ctrl + wheel)

	/** Zoom offset the client starts at. 0 leaves the server-driven zoom untouched. */
	static final int ZOOM_DEFAULT = 0;
	/** How far one wheel notch moves the zoom offset. Matches the offset step this client already used. */
	static final int ZOOM_STEP = 15;
	/** The fraction of the remaining distance an eased value travels each cycle, shared by both wheels. */
	static final int WHEEL_EASE_DIVISOR = 5;

	// ------------------------------------------------------------------ view distance (alt + wheel)

	/**
	 * The map sizes, in tiles per side, this client can build, in the order of
	 * {@code Class73.anIntArray4780}: the vanilla 104x104 region and the three extended sizes.
	 * Staying in that table keeps the requested size one the client and server both know.
	 */
	static final int[] SCENE_SIZES = {104, 120, 136, 168};
	/** The largest scene index, asked for on the first alt + wheel scroll and then held. */
	static final int LARGE_SCENE_INDEX = SCENE_SIZES.length - 1;
	/** The vanilla scene index, the smallest region the server always builds, used by the reset. */
	static final int VANILLA_SCENE_INDEX = 0;
	/** The distance a fresh session starts at: the vanilla scene. */
	static final int MIN_VIEW_DISTANCE = SCENE_SIZES[0];
	/** The furthest the wheel can push the view distance. */
	static final int MAX_VIEW_DISTANCE = SCENE_SIZES[LARGE_SCENE_INDEX];
	/** How far one wheel notch moves the target, in tiles, the same step open592 uses. */
	static final int VIEW_DISTANCE_STEP = 8;
	/**
	 * World units of far plane per tile of view distance (the stock zFar coefficient) and the scale
	 * open592 applies to it. open592 derives its drawn radius from this far plane, so the fog always
	 * ends just inside the radius and visibly rolls with the wheel; the vanilla client instead used
	 * {@code <<= 2} (~3.2x farther), which left the fog beyond the drawn radius and effectively
	 * invisible. This client uses open592's scale so the horizon sits at the same place it does there.
	 */
	static final double FAR_PLANE_PER_TILE = 34.46;
	/** The scale open592 applies to the per-tile far plane ({@code ClientConfig.FAR_PLANE_SCALE}). */
	static final double FAR_PLANE_SCALE = 1.0;
	/** The far-plane nudge the software renderer gets on top of the sum (open592's {@code + 128}). */
	static final int SOFTWARE_FAR_PLANE_EXTRA = 128;

	// ------------------------------ experimental renderer settings (runtime, config-driven)

	/** Fog scale, in percent, the experimental renderer's slider starts at: the far plane unchanged. */
	static final int FOG_SCALE_DEFAULT = 285;
	/** Smallest and largest fog scale the slider allows, in percent. */
	static final int FOG_SCALE_MIN = 25;
	static final int FOG_SCALE_MAX = 400;
	/** Largest culling distance the slider allows, in tiles. */
	static final int CULL_DISTANCE_MAX = 168;
	/** Camera-radius scale bounds, in percent of the vanilla per-scene-size radius. */
	static final int RADIUS_SCALE_MIN = 25;
	static final int RADIUS_SCALE_MAX = 400;
	static final int RADIUS_SCALE_DEFAULT = 100;

	/** The modifiers the latest wheel event carried, read back on the drain side. */
	private static volatile boolean wheelControlDown;
	private static volatile boolean wheelAltDown;

	/**
	 * Told whenever the wheel moves the view distance or zoom. The experimental renderer uses it to
	 * write the new value back to config almost at once, so an open config panel follows the wheel
	 * instead of waiting for the periodic sweep. Null until the renderer registers one.
	 */
	private static volatile Runnable tuningChangeListener;

	/** The zoom offset target the wheel writes and the cycle eases towards. */
	private static volatile int targetZoom = ZOOM_DEFAULT;
	/** The view distance target, in tiles, the wheel writes and the cycle eases towards. */
	private static volatile int targetViewDistance = MIN_VIEW_DISTANCE;

	/** Fog scale, in percent, applied to the far plane by the experimental renderer's slider. */
	private static volatile int fogScalePercent = FOG_SCALE_DEFAULT;
	/** When true, culling inside {@link #cullingDistance} is disabled (experimental renderer). */
	private static volatile boolean cullingDisabled;
	/** When true, tiles outside the camera view (frustum) are not culled either (experimental renderer). */
	private static volatile boolean viewCullingDisabled;
	/** When true, only the player's plane and the ones below it are drawn (experimental renderer). */
	private static volatile boolean hideUpperFloors;
	/** Distance, in tiles, out to which culling is disabled (experimental renderer). */
	private static volatile int cullingDistance;
	/** Fog colour override (0xRRGGBB), or -1 to leave the client's own fog colour alone. */
	private static volatile int fogColour = -1;
	/**
	 * When true the fog horizon and the far plane never run past the map the server has actually built,
	 * so no geometry is drawn beyond the built edge. The void client used to let the horizon outrun the
	 * map so the fog rolled immediately, but the tiles drawn past the built edge expose the lower
	 * plane's floors, so this defaults to clamping.
	 */
	private static volatile boolean clampFogToBuiltMap = true;
	/** Camera radius scale, in percent of the vanilla per-scene-size radius (experimental renderer). */
	private static volatile int drawRadiusScalePercent = RADIUS_SCALE_DEFAULT;
	/** Set when the fog scale changed, so the projection is rebuilt with the new fog plane. */
	private static volatile boolean fogDirty;
	/** The eased view distance the far plane follows. */
	private static int viewDistance = MIN_VIEW_DISTANCE;

	/** True once alt + wheel has taken control of the view distance. */
	private static boolean viewDistanceEngaged;

	/** The largest scene index has been asked of the server and an answer is outstanding. */
	private static boolean sceneRequestPending;
	/** The server answered with a size below the one asked for, so it will not build it. */
	private static boolean sceneSizeRefused;
	/** The scene index the server has actually built, so the largest one is only asked for once. */
	private static int heldSceneIndex = -1;

	/** True while the idle packet has parked the view distance at the lowest gated value. */
	private static boolean afk;
	/** The view distance to glide back to when the player interacts again. */
	private static int afkSavedViewDistance;
	/**
	 * Idle packets in a row (no activity between them) required before the distance drops. The
	 * vanilla idle system re-arms its flag on every sign of activity, so "consecutive" means
	 * the ping fired this many times without the player doing anything in between — a grace
	 * period against dropping the distance on the first hiccup. Settable from the AFK saver
	 * plugin's config.
	 */
	static volatile int afkGracePackets = 3;
	/** Master switch for the idle distance drop; the AFK saver plugin owns it. */
	static volatile boolean afkSaverEnabled = true;
	/** Idle packets counted since the last sign of activity. */
	private static int idlePacketStreak;

	private GameTuning() {
	}

	/** Records what modifiers the wheel event being enqueued carried. Called on the AWT event thread. */
	static void captureWheelModifiers(boolean controlDown, boolean altDown) {
		wheelControlDown = controlDown;
		wheelAltDown = altDown;
	}

	/**
	 * Registers the listener told when the wheel moves the view distance or zoom; null clears it.
	 * Called on the client thread at start-up, but a volatile write so it is safe from any thread.
	 */
	static void setTuningChangeListener(Runnable listener) {
		tuningChangeListener = listener;
	}

	/**
	 * Tells the listener the wheel moved a target. Called on the client thread inside the client
	 * cycle, so the listener must return at once and must not throw: an exception is swallowed here
	 * rather than being allowed to abort the cycle. The handler only schedules later work, so this is
	 * a cheap notification, not the write itself.
	 */
	private static void notifyTuningChanged() {
		Runnable listener = tuningChangeListener;
		if (listener == null) return;
		try {
			listener.run();
		} catch (Throwable failure) {
			if (Loader.trace) failure.printStackTrace();
		}
	}

	/** True while alt + wheel holds the view distance and the far plane it drives. */
	static boolean engaged() {
		return viewDistanceEngaged;
	}

	/**
	 * The view distance the wheel has taken control of, in tiles, or 0 when it has not engaged and
	 * the built scene is still in charge. Read by the experimental renderer so it can write the
	 * value back to its config, which restores the horizon on the next launch.
	 */
	static int engagedViewDistance() {
		if (!viewDistanceEngaged) return 0;
		// While the AFK saver has parked the distance at the lowest gated value, report the distance
		// the wheel last chose, not the parked one, so persisting it does not flap the saved value.
		return afk ? afkSavedViewDistance : targetViewDistance;
	}

	/** The wheel-driven zoom offset, read back so the experimental renderer can persist it. */
	static int zoomOffset() {
		return targetZoom;
	}

	/**
	 * One cycle's worth of accumulated wheel rotation, dispatched the way the open592 client
	 * dispatches it: alt + wheel moves the view distance, ctrl + wheel moves the camera zoom.
	 * The accumulated rotation of a cycle is a sum of same-modifier notches in practice, so
	 * the latest event's modifiers stand for the whole of it.
	 */
	static void handleGameWheelScroll(int moved) {
		if (wheelAltDown) handleViewDistanceScroll(moved);
		else if (wheelControlDown) handleZoomScroll(moved);
	}

	/**
	 * One notch of ctrl + wheel, in this client's own direction: scrolling up lowers the
	 * rotation and raises the zoom offset, exactly what the offset did before the wheel
	 * carried it, so the zoom keeps the feel it already had, only eased.
	 */
	static void handleZoomScroll(int moved) {
		int before = targetZoom;
		if (moved < 0) targetZoom += ZOOM_STEP;
		else if (moved > 0) targetZoom -= ZOOM_STEP;
		if (targetZoom != before) notifyTuningChanged();
	}

	/**
	 * The idle packet went out: save where the view distance was heading and drop it to the
	 * lowest gated value, so an idle client eases its fog horizon in and stops drawing tiles
	 * nobody is looking at. The next sign of activity ({@code aBoolean10174} re-arming, which is
	 * exactly what the vanilla idle system re-arms on) glides it back. A client that never engaged
	 * alt + wheel is already at the lowest gated value, so there is nothing to save and change.
	 * Called from the packet encoder where the idle packet is written (Class348_Sub24).
	 */
	static void onIdlePacketSent() {
		if (!afkSaverEnabled || afk || !viewDistanceEngaged) return;
		// Grace: only drop the distance once this many idle packets have gone out in a row.
		idlePacketStreak++;
		if (idlePacketStreak < afkGracePackets) return;
		afk = true;
		afkSavedViewDistance = targetViewDistance;
		targetViewDistance = MIN_VIEW_DISTANCE;
	}

	/** The player interacted again: hand the saved view distance back to the ease. */
	private static void restoreFromAfk() {
		afk = false;
		targetViewDistance = afkSavedViewDistance;
	}

	/**
	 * One notch of alt + wheel: scrolling down pushes the horizon further out and scrolling up
	 * pulls it in, the direction the open592 client uses. The first scroll starts from the scene
	 * the server already built, so taking control of the view distance never takes view away from
	 * the player.
	 */
	static void handleViewDistanceScroll(int moved) {
		// Scrolling is interaction: leave AFK mode first so the wheel target wins over the saved one.
		if (afk) restoreFromAfk();
		boolean changed = false;
		if (!viewDistanceEngaged) {
			viewDistanceEngaged = true;
			viewDistance = targetViewDistance = clampViewDistance(Class367_Sub4.anInt7319);
			// Taking control changes the reported distance from "built scene" (0) to a real value.
			changed = true;
		}
		int before = targetViewDistance;
		if (moved > 0) targetViewDistance = clampViewDistance(targetViewDistance + VIEW_DISTANCE_STEP);
		else if (moved < 0) targetViewDistance = clampViewDistance(targetViewDistance - VIEW_DISTANCE_STEP);
		if (changed || targetViewDistance != before) notifyTuningChanged();
	}

	/**
	 * Eases both wheels one step towards their targets. Called once per client cycle. Each step the
	 * eased view distance takes re-runs the projection ({@code Class226.method1626}), which is what
	 * keeps the far plane and the fog in step with the distance, and then keeps the drawn radius in
	 * step and makes sure the largest scene has been asked for.
	 */
	static void update() {
		// aBoolean10174 re-arms to true on every sign of activity and only goes false when the idle
		// packet is sent, so true here means the player did something: the grace streak restarts
		// and an engaged AFK drop glides back to the saved distance.
		if (Class369_Sub3_Sub1.aBoolean10174) {
			idlePacketStreak = 0;
			if (afk) restoreFromAfk();
		}
		Class320.zoomStep = easeTowards(Class320.zoomStep, targetZoom);
		int eased = easeTowards(viewDistance, targetViewDistance);
		if (eased != viewDistance) {
			// The far plane follows the eased distance on every step it takes, so the fog and the
			// terrain fading into it move with the wheel instead of popping at a scene size. If the
			// renderer is not up yet, defer it: fogDirty is retried once it is.
			viewDistance = eased;
			fogDirty = true;
		}
		// The renderer is null before the display exists (menu/login), so hold the projection rebuild
		// until it is up rather than dereferencing it here; fogDirty is retried once it is.
		if (fogDirty && Class348_Sub8.aHa6654 != null) {
			// A fog-slider change or an eased distance step: rebuild the projection and fog now.
			fogDirty = false;
			Class226.method1626(1, false);
		}
		if (!viewDistanceEngaged) {
			return;
		}
		updateScene();
	}

	/**
	 * Keeps the drawn radius in step with the view distance the way open592's {@code updateDrawRadius}
	 * does: opening grows it straight away, so terrain is there at the fog boundary as the fog reveals
	 * it, while taking it back waits for the wheel to stop. This client rebuilds its visibility grids
	 * only with a scene, so the radius is the client's own per-scene-size table radius for the scene
	 * that is built ({@link #drawRadius}); the clamp in {@link #getDrawDistance()} is what keeps the
	 * wheel from asking the fog to draw past that scene.
	 * <p>
	 * This radius is also the scene-level culling knob: the experimental renderer's "disable culling"
	 * option grows it to {@link #cullingDistance} so the client visits and draws tiles out to that
	 * distance. The radius is read while a scene is built (it sizes the visibility grid), so a change
	 * takes effect on the next scene build - use the renderer's force-rebuild button to apply it now.
	 */
	static int drawRadius(int vanillaRadius) {
		// The camera-radius knob: scale the client's per-scene-size radius. Terrain and objects are only
		// visited and drawn inside this radius, so the vanilla value culls everything past it (~32-47
		// tiles) even when the fog horizon has been pushed out - this is the camera radius culling.
		int radius = (int) ((long) vanillaRadius * drawRadiusScalePercent / 100L);
		if (radius < 1) radius = 1;
		// When distance culling is off, the radius is grown to the whole culling distance so every tile
		// inside it is visited. The grid it sizes grows with it, so the larger radius is consistent.
		if (distanceCullingDisabled()) {
			int wanted = effectiveCullingDistance();
			if (wanted > radius) radius = wanted;
		}
		return radius;
	}

	/**
	 * Declares the scene size the client keeps, the open592 {@code updateScene}: the largest scene the
	 * client can build is asked for once, on the first alt + wheel scroll, and held. An answer below the
	 * size asked for is all this server builds, and asking again would rebuild the region every tick, so
	 * it is remembered and never asked for twice; asking per scroll is what made the server rebuild the
	 * region while the wheel turned.
	 */
	private static void updateScene() {
		int builtIndex = currentSceneIndex();
		if (builtIndex != heldSceneIndex) {
			if (sceneRequestPending) {
				sceneRequestPending = false;
				if (builtIndex < LARGE_SCENE_INDEX) sceneSizeRefused = true;
			} else {
				sceneSizeRefused = false;
			}
			heldSceneIndex = builtIndex;
		}
		if (sceneRequestPending || sceneSizeRefused || heldSceneIndex >= LARGE_SCENE_INDEX) return;
		sceneRequestPending = true;
		requestSceneSize(LARGE_SCENE_INDEX);
	}

	/**
	 * Asks the server for a map size by setting this client's build-area setting and sending the
	 * settings packet that carries it ({@code Class14_Sub2.method243}, opcode 24 - the same request the
	 * {@code ::setba} command makes). The server answers with a fresh map region at that size, which
	 * runs the client's loading rebuild, and {@link #currentSceneIndex()} then reports the new size.
	 */
	private static void requestSceneSize(int sceneIndex) {
		try {
			Class348_Sub51 settings = Class316.aClass348_Sub51_3959;
			if (settings == null || settings.aClass239_Sub6_7226 == null) return;
			settings.method3429((byte) 74, settings.aClass239_Sub6_7226, sceneIndex);
			Class14_Sub2.method243(37);
		} catch (Throwable failure) {
			if (Loader.trace) failure.printStackTrace();
		}
	}

	/**
	 * The map size index the client is currently built for, mapped back to its index in
	 * {@link #SCENE_SIZES}. This is the client's read of the scene the server gave it.
	 */
	private static int currentSceneIndex() {
		int sceneSize = Class367_Sub4.anInt7319;
		for (int index = SCENE_SIZES.length - 1; index >= 0; index--) {
			if (SCENE_SIZES[index] <= sceneSize) return index;
		}
		return 0;
	}

	/** Clamps a view distance to the wheel's range. */
	private static int clampViewDistance(int distance) {
		if (distance < MIN_VIEW_DISTANCE) return MIN_VIEW_DISTANCE;
		if (distance > MAX_VIEW_DISTANCE) return MAX_VIEW_DISTANCE;
		return distance;
	}

	/**
	 * The distance, in tiles, the fog horizon follows: the scene extent until alt + wheel takes the
	 * view distance over, and the eased view distance after that. By default it is clamped to the scene
	 * the server has actually built (the open592 behaviour), so no geometry is drawn past the built
	 * edge - that is where the lower plane's floor tiles otherwise show through. Turning
	 * {@link #setClampFogToBuiltMap} off lets the wheel move the horizon on its own so the fog rolls
	 * even on a server that never rebuilds a larger map, at the cost of drawing past the built edge.
	 */
	static int getDrawDistance() {
		int built = Class367_Sub4.anInt7319;
		if (!viewDistanceEngaged) return built;
		// Clamp to the built map so the horizon cannot run past its edge, where the tiles the renderer
		// would otherwise draw there show the lower plane through. Once the server builds a larger map
		// the clamp follows it up and the fog rolls again.
		if (clampFogToBuiltMap && viewDistance > built) return built;
		return viewDistance;
	}

	/**
	 * The far plane the projection is built from: the drawn distance's, engaged or not (the open592
	 * {@code method2070}). Because it is used before alt + wheel is ever touched too, the fog end is
	 * already where open592 puts it, and taking the wheel on the first time eases the horizon rather
	 * than jumping it - the fog end rides {@code getDrawDistance()}, which starts at the built scene
	 * and only ever moves as the ease moves it.
	 */
	static int appliedFarPlane() {
		return farPlane(getDrawDistance());
	}

	/**
	 * The far plane the projection and clipping use (the open592 {@code method2070} second argument),
	 * separated from {@link #appliedFarPlane()} so far-plane culling can be tuned without moving the
	 * fog end. It is the fog plane by default; the experimental renderer's "disable culling" option
	 * raises it to cover {@link #cullingDistance}, so geometry out to that distance is no longer
	 * clipped. This renderer derives fog from the same plane, so a raised clip also carries the fog
	 * end out with it - the two are only fully independent once fog has its own plane.
	 */
	static int clipFarPlane() {
		// The clip plane is independent of the fog scale: it starts from the unscaled distance plane,
		// is widened to cover a fog end that the fog slider pushed past it, and widened again to the
		// distance the disable-culling option keeps.
		int clip = baseFarPlane(getDrawDistance());
		int fog = appliedFarPlane();
		if (fog > clip) clip = fog;
		if (distanceCullingDisabled()) {
			int cull = baseFarPlane(effectiveCullingDistance());
			if (cull > clip) clip = cull;
		}
		return clip;
	}

	/**
	 * The distance the camera can see, in the renderer's own far-plane units, for a draw distance
	 * in tiles - the open592 {@code ClientConfig.farPlane}: per-tile coefficient scaled by
	 * {@link #FAR_PLANE_SCALE}, plus the software renderer's nudge. The fog end rides this plane,
	 * so this is what carries the fog horizon with the wheel.
	 */
	static int farPlane(int drawDistance) {
		return (int) (baseFarPlane(drawDistance) * ((double) fogScalePercent / 100.0));
	}

	/**
	 * The distance the camera can see, in the renderer's own far-plane units, for a draw distance in
	 * tiles, before the fog slider scales it - the open592 {@code ClientConfig.farPlane}: per-tile
	 * coefficient scaled by {@link #FAR_PLANE_SCALE}, plus the software renderer's nudge. The clip
	 * plane and the culling distance both build on this, so they do not move with the fog.
	 */
	static int baseFarPlane(int drawDistance) {
		int farPlane = (int) ((double) drawDistance * FAR_PLANE_PER_TILE * FAR_PLANE_SCALE);
		if (Class348_Sub8.aHa6654 != null && Class348_Sub8.aHa6654.method3670()) farPlane += SOFTWARE_FAR_PLANE_EXTRA;
		return farPlane;
	}

	// --------------------------------------------- experimental renderer settings (setters)

	/**
	 * Pins the view distance, in tiles, from the experimental renderer's render-distance slider; a
	 * value of 0 or less leaves the wheel and the built scene in charge. The value engages the view
	 * distance the same way alt + wheel does, so the far plane and fog follow it through the ease.
	 */
	static void setRenderDistance(int tiles) {
		if (tiles <= 0) {
			// 0 hands the horizon back to the built scene, the same state a fresh session starts in.
			// Without this the wheel could engage once and never be released (the persisted value
			// would also keep rewriting the slider), so the slider's 0 would not mean what it says.
			viewDistanceEngaged = false;
			return;
		}
		viewDistanceEngaged = true;
		targetViewDistance = clampViewDistance(tiles);
	}

	/** Sets the far-plane fog scale, in percent, from the experimental renderer's fog slider. */
	static void setFogScale(int percent) {
		if (percent < FOG_SCALE_MIN) percent = FOG_SCALE_MIN;
		if (percent > FOG_SCALE_MAX) percent = FOG_SCALE_MAX;
		fogScalePercent = percent;
		// The projection and fog are rebuilt by the cycle, so a fog change lands without a rebuild.
		fogDirty = true;
	}

	/** Sets whether the fog horizon is clamped to the map the server has built. */
	static void setClampFogToBuiltMap(boolean clamp) {
		clampFogToBuiltMap = clamp;
		fogDirty = true;
	}

	/** Sets the camera-radius scale, in percent, from the experimental renderer's camera-radius slider. */
	static void setDrawRadiusScale(int percent) {
		if (percent < RADIUS_SCALE_MIN) percent = RADIUS_SCALE_MIN;
		if (percent > RADIUS_SCALE_MAX) percent = RADIUS_SCALE_MAX;
		drawRadiusScalePercent = percent;
	}

	/** Turns the experimental renderer's disable-culling option on or off. */
	static void setCullingDisabled(boolean disabled) {
		cullingDisabled = disabled;
	}

	/**
	 * True while distance culling is off. The disable-culling toggle turns it off directly; the
	 * disable-camera-view-culling toggle implies it too, so a single toggle removes all scene culling.
	 */
	private static boolean distanceCullingDisabled() {
		return cullingDisabled || viewCullingDisabled;
	}

	/**
	 * The distance distance-culling is off out to: the culling-distance slider, but never less than the
	 * map the server has built (so the whole scene is kept when the slider is left at 0).
	 */
	private static int effectiveCullingDistance() {
		int distance = cullingDistance;
		int built = Class367_Sub4.anInt7319;
		if (distance < built) distance = built;
		return distance > 0 ? distance : CULL_DISTANCE_MAX;
	}

	/**
	 * Turns off camera-view (frustum) culling of tiles: the visibility grid then marks every tile in the
	 * drawn radius as visible instead of only the ones the camera frustum reaches. Backs the experimental
	 * renderer's culling tab.
	 */
	static void setViewCullingDisabled(boolean disabled) {
		viewCullingDisabled = disabled;
	}

	/** True while tiles outside the camera view are not being culled. */
	static boolean viewCullingDisabled() {
		return viewCullingDisabled;
	}

	/**
	 * True while the per-face screen-space frustum test is off. Backed by the same toggle as {@link
	 * #viewCullingDisabled()} so one option removes camera-view culling. Note this does not touch
	 * {@code ha.method3667}: that is a model clip-mask cache check, not a visibility cull, and forcing
	 * it makes callers reuse stale models.
	 */
	static boolean frustumCullingDisabled() {
		return viewCullingDisabled;
	}

	/**
	 * Turns the "hide upper floors" option on or off. When on, the ground planes drawn are capped to the
	 * player's current plane (and below), so the floors of higher planes do not show through while standing
	 * on a lower one. Backs the experimental renderer's culling tab.
	 */
	static void setHideUpperFloors(boolean hide) {
		hideUpperFloors = hide;
	}

	/** True while ground planes above the player's current plane are hidden. */
	static boolean hideUpperFloors() {
		return hideUpperFloors;
	}

	/** Sets the distance, in tiles, out to which culling is disabled by the experimental renderer. */
	static void setCullingDistance(int tiles) {
		if (tiles < 0) tiles = 0;
		if (tiles > CULL_DISTANCE_MAX) tiles = CULL_DISTANCE_MAX;
		cullingDistance = tiles;
	}

	/** Sets the camera zoom offset from the experimental renderer's zoom slider (0 = untouched). */
	static void setZoom(int offset) {
		targetZoom = offset;
	}

	/** Sets the fog colour override (0xRRGGBB), or a negative value to leave the client's own. */
	static void setFogColour(int rgb) {
		fogColour = rgb < 0 ? -1 : rgb & 0xffffff;
	}

	/** The fog colour override (0xRRGGBB), or -1 when the client's own fog colour is used. */
	static int fogColour() {
		return fogColour;
	}

	/** The far-plane fog scale, in percent, currently in force. */
	static int fogScalePercent() {
		return fogScalePercent;
	}

	/** True while distance culling is disabled by the experimental renderer. */
	static boolean cullingDisabled() {
		return cullingDisabled;
	}

	/** The distance, in tiles, out to which the experimental renderer disables culling. */
	static int cullingDistance() {
		return cullingDistance;
	}

	/** The camera-radius scale, in percent of the vanilla per-scene-size radius. */
	static int drawRadiusScalePercent() {
		return drawRadiusScalePercent;
	}

	/** True while the fog horizon is clamped to the map the server has built. */
	static boolean clampFogToBuiltMap() {
		return clampFogToBuiltMap;
	}

	/**
	 * Drops the wheel-ease and snaps the view distance and zoom straight to their targets. Called
	 * once the experimental renderer has applied its stored settings at start-up, so the horizon,
	 * fog, and zoom are already in place on the first frame instead of gliding in from the defaults
	 * over the following cycles.
	 */
	static void snapViewTuning() {
		viewDistance = targetViewDistance;
		Class320.zoomStep = targetZoom;
		fogDirty = true;
	}

	/**
	 * Cancels any wheel ease in progress and returns the view distance and zoom to their defaults:
	 * the horizon is handed back to the built scene and the zoom to the server's own value. Used by
	 * the experimental renderer's reset, so the reset does not keep gliding towards the distance the
	 * wheel was last heading for, and the AFK saver cannot pull a saved distance back in over it.
	 */
	static void resetWheelTuning() {
		viewDistanceEngaged = false;
		afk = false;
		afkSavedViewDistance = MIN_VIEW_DISTANCE;
		idlePacketStreak = 0;
		targetViewDistance = MIN_VIEW_DISTANCE;
		viewDistance = MIN_VIEW_DISTANCE;
		targetZoom = ZOOM_DEFAULT;
		Class320.zoomStep = ZOOM_DEFAULT;
		fogDirty = true;
	}

	/**
	 * Re-requests the built map region at its current size so the client runs its loading rebuild -
	 * the experimental renderer's force-rebuild button. Re-sends the same build-area setting the wheel
	 * uses ({@code Class14_Sub2.method243}, opcode 24), which makes the server resend the region and
	 * the client re-run the scene build, applying any culling-radius change.
	 */
	static void forceSceneRebuild() {
		requestSceneSize(currentSceneIndex());
	}

	/**
	 * Re-requests the built map region at the vanilla scene size, so the horizon and the fog shrink
	 * back to the default view distance instead of staying at the larger extent the wheel previously
	 * held. The reset button's counterpart to {@link #forceSceneRebuild}: that one re-requests whatever
	 * size is built, which after an alt + wheel session is the large region, so alone it would leave
	 * the horizon out where the wheel put it.
	 * <p>
	 * The once-only bookkeeping is cleared as well. Without that a later alt + wheel would see this
	 * below-large reply and record it as the server refusing the large scene ({@link #sceneSizeRefused}),
	 * suppressing the large request for the rest of the session; clearing {@link #heldSceneIndex} lets
	 * {@link #updateScene} treat the vanilla reply as a fresh state and ask for the large scene again
	 * when the wheel next engages.
	 */
	static void resetSceneSize() {
		sceneRequestPending = false;
		sceneSizeRefused = false;
		heldSceneIndex = -1;
		requestSceneSize(VANILLA_SCENE_INDEX);
	}

	/** Eases an int a fifth of the way towards its target each call. */
	private static int easeTowards(int current, int target) {
		int difference = target - current;
		if (difference == 0) return current;
		int step = difference / WHEEL_EASE_DIVISOR;
		if (step == 0) step = difference > 0 ? 1 : -1;
		return current + step;
	}
}
