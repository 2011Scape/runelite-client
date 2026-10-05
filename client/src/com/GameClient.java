package com;

import net.runelite.client.RuneLite;
import net.runelite.api.MenuEntry;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.client.game.GameEventBridgeHooks;

import java.applet.Applet;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public abstract class GameClient extends Applet {


    public static String local = "144.217.10.71"; // live


    public static void setParams(String ip) {
        local = ip;
        setParams();
    }

    /**
     * Connection settings
     */
    public static final String IP = local;
    public static final String LOBBY_IP = local;

    public static final String JVM_ARGS = "-Xmx1024m";

    public static final Properties client_parameters = new Properties();

    static void setParams() {
        client_parameters.put("separate_jvm", "true");
        client_parameters.put("boxbgcolor", "black");
        client_parameters.put("image", "http://www.runescape.com/img/game/splash2.gif");
        client_parameters.put("centerimage", "true");
        client_parameters.put("boxborder", "false");
        client_parameters.put("java_arguments", JVM_ARGS+ " -Xss2m -Dsun.java2d.noddraw=true -XX:CompileThreshold=1500 -Xincgc -XX:+UseConcMarkSweepGC -XX:+UseParNewGC");
        client_parameters.put("27", "0");
        client_parameters.put("1", "0");
        client_parameters.put("16", "false");
        client_parameters.put("17", "false");
        client_parameters.put("21", "1"); // WORLD ID
        client_parameters.put("30", "false"); //frame
        client_parameters.put("20", LOBBY_IP);
        client_parameters.put("29", "");
        client_parameters.put("11", "true");
        client_parameters.put("25", "1378752098");
        client_parameters.put("28", "0");
        client_parameters.put("8", ".runescape.com");
        client_parameters.put("23", "false");
        client_parameters.put("32", "0");
        client_parameters.put("15", "wwGlrZHF5gKN6D3mDdihco3oPeYN2KFybL9hUUFqOvk");
        client_parameters.put("0", "IjGJjn4L3q5lRpOR9ClzZQ");
        client_parameters.put("2", "");
        client_parameters.put("4", "1"); // WORLD ID
        client_parameters.put("14", "");
        client_parameters.put("5", "8194");
        client_parameters.put("-1", "QlwePyRU5GcnAn1lr035ag");
        client_parameters.put("6", "0");
        client_parameters.put("24", "true,false,0,43,200,18,0,21,354,-15,Verdana,11,0xF4ECE9,candy_bar_middle.gif,candy_bar_back.gif,candy_bar_outline_left.gif,candy_bar_outline_right.gif,candy_bar_outline_top.gif,candy_bar_outline_bottom.gif,loadbar_body_left.gif,loadbar_body_right.gif,loadbar_body_fill.gif,6");
        client_parameters.put("3", "hAJWGrsaETglRjuwxMwnlA/d5W6EgYWx");
        client_parameters.put("12", "false");
        client_parameters.put("13", "0");
        client_parameters.put("26", "0");
        client_parameters.put("9", "77");
        client_parameters.put("22", "false");
        client_parameters.put("18", "false");
        client_parameters.put("33", "");
        client_parameters.put("haveie6", "false");
    }

    public static GameClient getClient() {
        return (GameClient) RuneLite.clientA;
    }

    public String getParameter(String string) {
        return (String) client_parameters.get(string);
    }

    public URL getDocumentBase() {
        return getCodeBase();
    }

    public URL getCodeBase() {
        try {
            return new URL("http://"+local);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public static void openURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public abstract byte[][][] getTileSettings();

    public abstract int[][][] getTileHeights();

    public abstract int getPlane();

    public abstract int getBaseX();

    public abstract int getBaseY();

    public abstract boolean isInInstancedRegion();

    public abstract int[][][] getInstanceTemplateChunks();

    public abstract int[][] getCollisionMaps(int plane);

    public abstract Canvas getCanvas();

    public abstract int getCanvasWidth();

    public abstract int getCanvasHeight();

    public abstract Dimension getRealDimensions();

    public abstract void invalidateStretching(boolean resize);

    public abstract void setAnimationSmoothingEnabled(boolean state);

    public abstract boolean isAnimationSmoothingEnabled();

    /** Turns the detached renderer (DetachedRenderer) on or off. Backs the experimental renderer plugin. */
    public abstract void setDetachedRendererEnabled(boolean enabled);

    /** Sets the detached renderer's fps cap (0 = uncapped). Backs the experimental renderer plugin's config. */
    public abstract void setDetachedRendererFpsTarget(int fps);

    /** Turns the AFK idle distance drop on or off. Backs the AFK performance saver plugin. */
    public abstract void setAfkSaverEnabled(boolean enabled);

    /** Sets how many consecutive idle packets are required before the AFK distance drop engages. */
    public abstract void setAfkSaverGracePackets(int packets);

    /** Turns the displayfps overlay on or off. Backs the experimental renderer plugin's show-fps config. */
    public abstract void setShowFpsOverlay(boolean show);

    /** Pins the view distance (render-distance slider); 0 or less returns control to the wheel and the built scene. */
    public abstract void setRenderDistance(int tiles);

    /** Sets the far-plane fog scale in percent (fog slider). */
    public abstract void setFogScale(int percent);

    /** Turns the disable-culling option on or off. Backs the experimental renderer plugin. */
    public abstract void setCullingDisabled(boolean disabled);

    /** Sets the distance, in tiles, out to which culling is disabled. Backs the experimental renderer plugin. */
    public abstract void setCullingDistance(int tiles);

    /** Sets the camera-radius culling scale in percent of the vanilla per-scene-size radius. */
    public abstract void setDrawRadiusScale(int percent);

    /** Sets whether the fog horizon is clamped to the map the server has built (stops lower-plane bleed past the built edge). */
    public abstract void setClampFogToBuiltMap(boolean clamp);

    /** Turns off camera-view (frustum) culling of tiles. Backs the experimental renderer's culling tab. */
    public abstract void setViewCullingDisabled(boolean disabled);

    /** When on, hides ground planes above the player's current plane. Backs the experimental renderer's culling tab. */
    public abstract void setHideUpperFloors(boolean hide);

    /** Sets the camera zoom offset (zoom slider). Backs the experimental renderer plugin. */
    public abstract void setZoom(int offset);

    /** Sets the fog colour override (0xRRGGBB), or a negative value to leave the client's own. */
    public abstract void setFogColour(int rgb);

    /** The wheel-driven view distance in tiles, or 0 when the built scene is still in charge. */
    public int getViewDistance()
    {
        return 0;
    }

    /** The wheel-driven camera zoom offset. */
    public int getZoomOffset()
    {
        return 0;
    }

    /** The far-plane fog scale in percent currently in force. */
    public int getFogScale()
    {
        return 100;
    }

    /** The camera-radius culling scale in percent currently in force. */
    public int getDrawRadiusScale()
    {
        return 100;
    }

    /** The distance, in tiles, out to which culling is disabled. */
    public int getCullingDistance()
    {
        return 0;
    }

    /** True while distance culling is disabled. */
    public boolean isCullingDisabled()
    {
        return false;
    }

    /** True while camera-view (frustum) culling is disabled. */
    public boolean isViewCullingDisabled()
    {
        return false;
    }

    /** True while ground planes above the player's current plane are hidden. */
    public boolean isHideUpperFloors()
    {
        return false;
    }

    /** True while the fog horizon is clamped to the map the server has built. */
    public boolean isClampFogToBuiltMap()
    {
        return true;
    }

    /** The fog colour override (0xRRGGBB), or -1 when the client's own fog colour is used. */
    public int getFogColour()
    {
        return -1;
    }

    /** The detached renderer's fps cap (0 = uncapped). */
    public int getFpsTarget()
    {
        return 180;
    }

    /** True while the displayfps overlay is shown. */
    public boolean isShowFpsOverlay()
    {
        return false;
    }

    /**
     * Snaps the wheel-eased view distance and zoom straight to their targets, so settings applied
     * at start-up are already in place on the first frame instead of gliding in from the defaults.
     */
    public void snapRendererTuning()
    {
    }

    /**
     * Cancels any in-flight wheel ease and returns the view distance and zoom to their defaults,
     * handing the horizon back to the built scene. Backs the experimental renderer's reset button.
     */
    public void resetWheelTuning()
    {
    }

    /**
     * Registers the listener told when the wheel moves the view distance or zoom; null clears it.
     * Backs the experimental renderer's fast config round-trip, so an open config panel follows the
     * wheel instead of waiting for the periodic sweep. Runs on the client thread.
     */
    public void setTuningChangeListener(Runnable listener)
    {
    }

    /** Re-requests the current map region so the client runs its scene rebuild. Backs the force-rebuild action. */
    public abstract void forceSceneRebuild();

    /**
     * Re-requests the vanilla scene size, so the horizon and fog shrink back to the default view
     * distance after an alt + wheel session rather than staying at the larger built region. Backs the
     * experimental renderer's reset button. Runs on the client thread.
     */
    public void resetSceneSize()
    {
    }

    public abstract int getLocalPlayerLocalX();

    public abstract int getLocalPlayerLocalY();

    public abstract int getLocalPlayerSceneX();

    public abstract int getLocalPlayerSceneY();

    public abstract int getLocalPlayerSize();

    public int getLocalPlayerCombatLevel()
    {
        return 0;
    }

    public abstract boolean hasLocalPlayer();

    public int getCameraX()
    {
        return 0;
    }

    public int getCameraY()
    {
        return 0;
    }

    public int getCameraZ()
    {
        return 0;
    }

    public int getCameraPitch()
    {
        return 0;
    }

    public int getCameraYaw()
    {
        return 0;
    }

    public int getCameraScale()
    {
        return 7000;
    }

    public int getTileHeight(int localX, int localY, int plane)
    {
        return 0;
    }

    public Point projectLocalToCanvas(int localX, int localY, int height)
    {
        return null;
    }

    public boolean hasNativeProjection()
    {
        return false;
    }

    public List<GroundItemInfo> getGroundItems()
    {
        return Collections.emptyList();
    }

    public List<NpcInfo> getNpcs()
    {
        return Collections.emptyList();
    }

    public NpcHullInfo getNpcHull(int npcIndex)
    {
        return null;
    }

    /**
     * One line describing what the minimap last drew and why, or null when there is
     * nothing worth reporting. Backed by {@code MinimapDebug}, written from the minimap's
     * own render path. Displayed by the dev-tools overlay.
     */
    public String getMinimapDiagnostics()
    {
        return null;
    }

    public List<SkillSnapshot> getSkillSnapshots()
    {
        return Collections.emptyList();
    }

    public MenuEntry[] getMenuEntries()
    {
        return GameEventBridgeHooks.getMenuEntries();
    }

    public void addMenuEntry(String option, String target, int type, int identifier, int param0, int param1)
    {
    }

    public boolean isShiftPressed()
    {
        return false;
    }

    public boolean isShiftClickEnabled()
    {
        return false;
    }

    public Point getMouseCanvasPosition()
    {
        Canvas canvas = getCanvas();
        if (canvas == null)
        {
            return new Point(0, 0);
        }

        java.awt.Point point = canvas.getMousePosition();
        if (point == null)
        {
            return new Point(0, 0);
        }

        return new Point(point.x, point.y);
    }

    public abstract boolean isClientThread();

    public static final class GroundItemInfo
    {
        private final int id;
        private final int quantity;
        private final String name;
        private final int price;
        private final int localX;
        private final int localY;
        private final int plane;

        public GroundItemInfo(int id, int quantity, String name, int localX, int localY, int plane)
        {
            this(id, quantity, name, 0, localX, localY, plane);
        }

        public GroundItemInfo(int id, int quantity, String name, int price, int localX, int localY, int plane)
        {
            this.id = id;
            this.quantity = quantity;
            this.name = name;
            this.price = price;
            this.localX = localX;
            this.localY = localY;
            this.plane = plane;
        }

        public int getId()
        {
            return id;
        }

        public int getQuantity()
        {
            return quantity;
        }

        public String getName()
        {
            return name;
        }

        public int getPrice()
        {
            return price;
        }

        public int getStackValue()
        {
            return Math.max(1, quantity) * Math.max(0, price);
        }

        public int getLocalX()
        {
            return localX;
        }

        public int getLocalY()
        {
            return localY;
        }

        public int getPlane()
        {
            return plane;
        }
    }

    public static final class NpcInfo
    {
        private final int index;
        private final int id;
        private final String name;
        private final int localX;
        private final int localY;
        private final int trueLocalX;
        private final int trueLocalY;
        private final int plane;
        private final int height;
        private final int size;
        private final int combatLevel;
        private final boolean follower;
        private final boolean dying;

        public NpcInfo(int id, String name, int localX, int localY, int plane, int height)
        {
            this(-1, id, name, localX, localY, localX, localY, plane, height, 1, -1, false, false);
        }

        public NpcInfo(int index, int id, String name, int localX, int localY, int plane, int height, int size, int combatLevel, boolean follower, boolean dying)
        {
            this(index, id, name, localX, localY, localX, localY, plane, height, size, combatLevel, follower, dying);
        }

        public NpcInfo(int index, int id, String name, int localX, int localY, int trueLocalX, int trueLocalY, int plane, int height, int size, int combatLevel, boolean follower, boolean dying)
        {
            this.index = index;
            this.id = id;
            this.name = name;
            this.localX = localX;
            this.localY = localY;
            this.trueLocalX = trueLocalX;
            this.trueLocalY = trueLocalY;
            this.plane = plane;
            this.height = height;
            this.size = size;
            this.combatLevel = combatLevel;
            this.follower = follower;
            this.dying = dying;
        }

        public int getIndex()
        {
            return index;
        }

        public int getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public int getLocalX()
        {
            return localX;
        }

        public int getLocalY()
        {
            return localY;
        }

        public int getTrueLocalX()
        {
            return trueLocalX;
        }

        public int getTrueLocalY()
        {
            return trueLocalY;
        }

        public int getPlane()
        {
            return plane;
        }

        public int getHeight()
        {
            return height;
        }

        public int getSize()
        {
            return size;
        }

        public int getCombatLevel()
        {
            return combatLevel;
        }

        public boolean isFollower()
        {
            return follower;
        }

        public boolean isDying()
        {
            return dying;
        }
    }

    public static final class SkillSnapshot
    {
        private final Skill skill;
        private final int xp;
        private final int level;
        private final int boostedLevel;

        public SkillSnapshot(Skill skill, int xp, int level, int boostedLevel)
        {
            this.skill = skill;
            this.xp = xp;
            this.level = level;
            this.boostedLevel = boostedLevel;
        }

        public Skill getSkill()
        {
            return skill;
        }

        public int getXp()
        {
            return xp;
        }

        public int getLevel()
        {
            return level;
        }

        public int getBoostedLevel()
        {
            return boostedLevel;
        }
    }

    public static final class NpcHullInfo
    {
        private final int cycle;
        private final HullSegment[] segments;

        public NpcHullInfo(int cycle, HullSegment[] segments)
        {
            this.cycle = cycle;
            this.segments = segments;
        }

        public int getCycle()
        {
            return cycle;
        }

        public HullSegment[] getSegments()
        {
            return segments;
        }
    }

    public static final class HullSegment
    {
        private final int x1;
        private final int y1;
        private final int x2;
        private final int y2;
        private final int radius;

        public HullSegment(int x1, int y1, int x2, int y2, int radius)
        {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.radius = radius;
        }

        public int getX1()
        {
            return x1;
        }

        public int getY1()
        {
            return y1;
        }

        public int getX2()
        {
            return x2;
        }

        public int getY2()
        {
            return y2;
        }

        public int getRadius()
        {
            return radius;
        }
    }
}
