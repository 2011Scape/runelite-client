package net.runelite.client.plugins.experimentalrenderer;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("experimentalrenderer")
public interface ExperimentalRendererConfig extends Config
{
	@ConfigSection(
		name = "Pacing",
		description = "Frame pacing for the detached renderer.",
		position = 0
	)
	String pacingSection = "pacingSection";

	@ConfigSection(
		name = "View",
		description = "Render distance, fog horizon, and camera zoom.",
		position = 1
	)
	String viewSection = "viewSection";

	@ConfigSection(
		name = "Culling",
		description = "Disable far-plane and scene culling out to a distance.",
		position = 2
	)
	String cullingSection = "cullingSection";

	@ConfigSection(
		name = "Colour",
		description = "Fog and sky colour override.",
		position = 3
	)
	String colourSection = "colourSection";

	@Range(
		min = 0,
		max = 300
	)
	@ConfigItem(
		keyName = "fpsTarget",
		name = "FPS target",
		description = "Upper limit for redraws per second while the renderer is detached. 0 = uncapped.",
		section = pacingSection
	)
	default int fpsTarget()
	{
		return 180;
	}

	@ConfigItem(
		keyName = "showFps",
		name = "Show FPS readout",
		description = "Turns on the displayfps overlay (fps, memory, network, and the detached renderer status) without typing the displayfps command. Turns off when the plugin is disabled.",
		section = pacingSection
	)
	default boolean showFps()
	{
		return false;
	}

	@Range(
		min = 0,
		max = 168
	)
	@ConfigItem(
		keyName = "renderDistance",
		name = "Render distance (tiles) REQUIRES RESTART",
		description = "Pins the view distance, in tiles, instead of leaving it to alt + wheel. 0 leaves the wheel in charge. Larger distances need the server to build a larger scene.",
		section = viewSection
	)
	default int renderDistance()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "clampFog",
		name = "Clamp fog to built map",
		description = "Keeps the fog horizon and far plane inside the map the server has actually built, so nothing is drawn past its edge (this is where the lower plane's floors show through). Turn off to let the fog roll past the built map, at the cost of that bleed.",
		section = viewSection
	)
	default boolean clampFog()
	{
		return true;
	}

	@Range(
		min = 25,
		max = 400
	)
	@ConfigItem(
		keyName = "fogScale",
		name = "Fog distance (%)",
		description = "Scales the far plane the fog horizon sits on. 100 leaves it where the client puts it; higher pushes the fog out, lower pulls it in.",
		section = viewSection
	)
	default int fogScale()
	{
		return 100;
	}

	@Range(
		min = -200,
		max = 200
	)
	@ConfigItem(
		keyName = "zoom",
		name = "Zoom offset",
		description = "Camera zoom offset, the same value ctrl + wheel moves. 0 leaves the server-driven zoom untouched.",
		section = viewSection
	)
	default int zoom()
	{
		return 0;
	}

	@Range(
		min = 0,
		max = 168
	)
	@ConfigItem(
		keyName = "cullingDistance",
		name = "Culling distance (tiles)",
		description = "The distance, in tiles, that 'Disable culling' covers. Also the distance geometry is kept for.",
		section = cullingSection
	)
	default int cullingDistance()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "disableCulling",
		name = "Disable culling",
		description = "Stops the client culling tiles and geometry out to the culling distance above, so the far plane and the drawn radius both reach it.",
		section = cullingSection
	)
	default boolean disableCulling()
	{
		return false;
	}

	@ConfigItem(
		keyName = "disableViewCulling",
		name = "Disable camera-view culling",
		description = "Removes camera-view culling: tiles that fall outside the camera's view and the per-face screen-space frustum test. Also treats the culling distance as fully disabled, so this single toggle turns off scene culling. Costly: far more geometry is kept and drawn. Tile culling takes effect on the next scene build.",
		section = cullingSection
	)
	default boolean disableViewCulling()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideUpperFloors",
		name = "Hide upper floors",
		description = "Draws only the ground planes up to your current plane, so the floors of higher planes stop showing through while you stand on a lower one. Turn off to draw every plane again.",
		section = cullingSection
	)
	default boolean hideUpperFloors()
	{
		return false;
	}

	@Range(
		min = 25,
		max = 400
	)
	@ConfigItem(
		keyName = "cameraRadius",
		name = "Camera radius (%)",
		description = "Scales the radius the client visits and draws tiles, NPCs, and objects inside. The vanilla value culls everything past ~32-47 tiles even when the fog is pushed out; raise this to keep it. Takes effect on the next scene build (or the Force scene rebuild button).",
		section = cullingSection
	)
	default int cameraRadius()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "fogColour",
		name = "Fog / sky colour",
		description = "Recolours the fog (which this client also uses for the sky at the horizon). Uncheck to leave the client's own colour alone.",
		section = colourSection
	)
	default Color fogColour()
	{
		return null;
	}
}
