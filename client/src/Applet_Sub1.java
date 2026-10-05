/* Applet_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import com.GameClient;
import jagex3.jagmisc.jagmisc;
import net.runelite.api.Skill;
import net.runelite.api.hooks.Callbacks;
import net.runelite.client.RuneLite;

import java.applet.Applet;
import java.applet.AppletContext;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public abstract class Applet_Sub1 extends GameClient implements Runnable, FocusListener, WindowListener {
    static int anInt1;
    static int anInt2;
    static int anInt3;
    static int anInt4;
    static int anInt5;
    static int anInt6;
    static int anInt7;
    static int anInt8;
    static int anInt9;
    static int anInt10;
    static int anInt11;
    static int anInt12;
    static int anInt13;
    static int anInt14;
    static int anInt15;
    static int anInt16;
    private boolean aBoolean17 = false;
    static int anInt18;
    static int anInt19;
    static Class324 aClass324_20;
    static int anInt21;
    static int anInt22;
    static int anInt23;
    static int anInt24;
    static int anInt25;
    static int anInt26;
    private boolean aBoolean27 = false;
    static int anInt28;
    static int anInt29;
    static int anInt30;
    static int anInt31;
    static int anInt32;
    static int anInt33;
    static int anInt34;
    static int anInt35;
    static int anInt36;
    static int anInt37;
    static int[] anIntArray38;
    static int anInt39;
    static int anInt40;
    public static boolean aBoolean41;
    public static int anInt42;
    public static int anInt43;
    public static int anInt44;
    public static int anInt45;
    public static boolean aBoolean46;
    public static boolean aBoolean47;
    public static boolean aBoolean48;
    public static boolean aBoolean49;
    public static boolean aBoolean50;
    public static boolean aBoolean51;
    public static boolean aBoolean52;
    public static Thread currentThread;
    private static Applet parameterApplet;
    private boolean animationSmoothingEnabled;
    private static BufferedImage runeLiteOverlayImage;
    private static int[] runeLiteOverlayPixels;
    private static int runeLiteOverlayWidth;
    private static int runeLiteOverlayHeight;
    public static int anInt53;
    public static boolean aBoolean54;
    public static int anInt55;
    public static boolean aBoolean56;
    public static int anInt57;
    public static boolean aBoolean58;
    public static int anInt59;

    abstract void method80(int i);

    public final void stop() {
        anInt24++;
        if (this == Class348_Sub40_Sub9.anApplet_Sub1_9169 && !Class26.aBoolean384) Class113.aLong1739 = Class62.method599(-73) - -4000L;
    }

    public final String getParameter(String string) {
        anInt3++;
        if (parameterApplet != null) return parameterApplet.getParameter(string);
        if (Class52.aFrame4904 != null) return null;
        if (Class93.anApplet1530 != null && Class93.anApplet1530 != this) return Class93.anApplet1530.getParameter(string);
        return super.getParameter(string);
    }

    String method81(byte i) {
        anInt1++;
        if (i <= 40) run();
        return null;
    }

    final void method82(int i, String string) {
        int i_0_ = 88 / ((i - -5) / 54);
        anInt22++;
        if (!aBoolean27) {
            aBoolean27 = true;
            System.out.println("error_game_" + string);
            try {
                Class224.method1617((byte) 125, Class93.anApplet1530, "loggedout");
            } catch (Throwable throwable) {
                /* empty */
            }
            try {
                getAppletContext().showDocument(new URL(getCodeBase(), ("error_game_" + string + ".ws")), "_top");
            } catch (Exception exception) {
                /* empty */
            }
        }
    }

    public final void windowActivated(WindowEvent windowevent) {
        anInt16++;
    }

    final boolean method83(boolean bool) {
        if (bool != true) getDocumentBase();
        anInt5++;
        return Class348_Sub40_Sub19.method3098(-30282, "jagmisc");
    }

    public final void focusLost(FocusEvent focusevent) {
        anInt9++;
        Class348_Sub40_Sub16.aBoolean9229 = false;
    }

    private final void method84(int i) {
        anInt8++;
        long l = Class62.method599(i + -88);
        long l_1_ = Class328_Sub2_Sub1.aLongArray8800[Class244.anInt4613];
        Class328_Sub2_Sub1.aLongArray8800[Class244.anInt4613] = l;
        Class244.anInt4613 = 0x1f & 1 + Class244.anInt4613;
        if (l_1_ != 0L && l > l_1_) {
            /* empty */
        }
        synchronized (this) {
            Class175.aBoolean2329 = Class348_Sub40_Sub16.aBoolean9229;
        }
        // The game cycle and the frame both run on this thread (DetachedRenderer is a pacer, not
        // a second thread), so a cycle can never overlap a frame: it advances game state only.
        method99((byte) 93);
        if (i != -1) aBoolean27 = true;
    }

    public final void update(Graphics graphics) {
        anInt34++;
        paint(graphics);
    }

    static final void method85(int i, Js5Archive Js5Archive) {
        anInt32++;
        Class369_Sub3.aClass45_8601 = Js5Archive;
        if (i != 0) anInt37 = 101;
    }

    public final void windowClosing(WindowEvent windowevent) {
        anInt15++;
        destroy();
    }

    public final URL getDocumentBase() {
        anInt30++;
        if (parameterApplet != null) return parameterApplet.getDocumentBase();
        if (Class52.aFrame4904 != null) return null;
        if (Class93.anApplet1530 != null && this != Class93.anApplet1530) return Class93.anApplet1530.getDocumentBase();
        return super.getDocumentBase();
    }

    static final boolean method86(String string, int i) {
        anInt13++;
        if (i != 0) return true;
        return Class275.aHashtable3548.containsKey(string);
    }

    public final void windowDeactivated(WindowEvent windowevent) {
        anInt12++;
    }

    synchronized void method87(byte i) {
        if (i > -11) paint(null);
        if (Class305.aCanvas3869 != null) {
            Class305.aCanvas3869.removeFocusListener(this);
            Class305.aCanvas3869.getParent().setBackground(Color.black);
            Class305.aCanvas3869.getParent().remove(Class305.aCanvas3869);
        }
        anInt7++;
        Container container;
        if (Class34.aFrame476 == null) {
            if (Class52.aFrame4904 == null) {
                if (Class93.anApplet1530 == null) container = Class348_Sub40_Sub9.anApplet_Sub1_9169;
                else container = Class93.anApplet1530;
            } else container = Class52.aFrame4904;
        } else container = Class34.aFrame476;
        container.setLayout(null);
        Class305.aCanvas3869 = new Canvas_Sub1(this);
        Class305.aCanvas3869.setIgnoreRepaint(true);
        container.add(Class305.aCanvas3869);
        applyCanvasSize();
        Class305.aCanvas3869.setVisible(true);
        if (container == Class52.aFrame4904) {
            Insets insets = Class52.aFrame4904.getInsets();
            applyCanvasLocation(container, (insets.left + Class348_Sub48.anInt7129), insets.top - -Class335.anInt4167);
        } else applyCanvasLocation(container, Class348_Sub48.anInt7129, Class335.anInt4167);
        Class305.aCanvas3869.addFocusListener(this);
        Class305.aCanvas3869.requestFocus();
        Class348_Sub40_Sub16.aBoolean9229 = true;
        Class175.aBoolean2329 = true;
        Class49.aBoolean4726 = true;
        Class203.aBoolean2674 = false;
        Class348_Sub12.aLong6748 = Class62.method599(-106);
    }

    public final void windowOpened(WindowEvent windowevent) {
        anInt39++;
    }

    public final synchronized void paint(Graphics graphics) {
        anInt18++;
        if (this == Class348_Sub40_Sub9.anApplet_Sub1_9169 && !Class26.aBoolean384) {
            Class49.aBoolean4726 = true;
            if (Class367_Sub4.aBoolean7320 && -Class348_Sub12.aLong6748 + Class62.method599(-57) > 1000) {
                Rectangle rectangle = graphics.getClipBounds();
                if (rectangle == null || (rectangle.width >= Class272.anInt3473 && (Class348_Sub22.anInt6857 <= rectangle.height))) Class203.aBoolean2674 = true;
            }
        }
    }

    private final void method88(int i) {
        anInt2++;
        long l = Class62.method599(-119);
        long l_2_ = Packet.aLongArray7206[Class152.anInt2071];
        Packet.aLongArray7206[Class152.anInt2071] = l;
        if (l_2_ != 0L && l_2_ < l) {
            int i_3_ = (int) (l - l_2_);
            Class239_Sub5.anInt5891 = (32000 + (i_3_ >> 1)) / i_3_;
        }
        Class152.anInt2071 = Class152.anInt2071 - -1 & 0x1f;
        // Everything here already runs on the client thread - the pacer draws frames here too - so
        // a resize simply can never tear down a surface mid-draw. The frame prep, the draw and the
        // AWT bookkeeping share this one thread exactly as they always relied on.
        if (Class159.anInt2127++ > 50) {
            Class159.anInt2127 -= 50;
            Class49.aBoolean4726 = true;
            applyCanvasSize();
            Class305.aCanvas3869.setVisible(true);
            if (Class52.aFrame4904 != null && Class34.aFrame476 == null) {
                Insets insets = Class52.aFrame4904.getInsets();
                applyCanvasLocation(Class52.aFrame4904, (insets.left - -Class348_Sub48.anInt7129), (insets.top + Class335.anInt4167));
            } else applyCanvasLocation(Class305.aCanvas3869.getParent(), Class348_Sub48.anInt7129, Class335.anInt4167);
        }
        client.handleDisplayResize();
        if (DetachedRenderer.consumeCanvasRefreshRequest()) {
            // A live renderer toggle: rebuild the canvas and GL surface on the client thread, with
            // no frame in flight, before the next frame draws. This is the graceful canvas
            // transition the switch needs.
            Class367_Sub11.method3556(false);
            Class49.aBoolean4726 = true;
        }
        // The frame is drawn here, on the client thread, whether the pacer is on or off. With it
        // off this is the vanilla one-frame-per-cycle draw; with it on the loop calls this once
        // per paced frame instead. Either way the RuneLite overlay callbacks (which draw()) run on
        // the client thread, so plugin callback affinity is preserved without any hand-off. The
        // plugin client-tick loop is fired by the game loop once per cycle, not here, so it keeps
        // the cycle rate when frames come faster.
        method93(-11018);
        if (i > -107) method90(true, true);
    }

    private void fireRuneLiteClientLoop() {
        try {
            if (RuneLite.getInjector() != null) {
                RuneLite.getInjector().getInstance(Callbacks.class).clientMainLoop();
            }
        } catch (Throwable ignored) {
        }
    }

    static void prepareRuneLiteOverlays() {
        try {
            if (RuneLite.getInjector() == null) return;
            int width = Math.max(1, Class321.anInt4017);
            int height = Math.max(1, Class348_Sub42_Sub8_Sub2.anInt10432);
            ensureRuneLiteOverlayBuffer(width, height);

            Graphics2D graphics = runeLiteOverlayImage.createGraphics();
            try {
                graphics.setComposite(AlphaComposite.Clear);
                graphics.fillRect(0, 0, width, height);
                graphics.setComposite(AlphaComposite.SrcOver);
                graphics.setClip(0, 0, width, height);
                RuneLite.getInjector().getInstance(Callbacks.class).draw(null, graphics, 0, 0);
            } finally {
                graphics.dispose();
            }
        } catch (Throwable failure) {
            if (Loader.trace) failure.printStackTrace();
        }
    }

    static void drawRuneLiteOverlays(ha renderer) {
        try {
            if (renderer == null) return;
            // Frames are paced but drawn on the client thread, so the callbacks and the composite
            // both run here: plugin callback thread affinity is preserved with no hand-off.
            prepareRuneLiteOverlays();
            if (!hasRuneLiteOverlayPixels()) return;
            int width = runeLiteOverlayWidth;
            int height = runeLiteOverlayHeight;
            Class105 sprite = renderer.method3711(runeLiteOverlayPixels, 0, width, width, height, false);
            sprite.method970(0, 0, width, height, 1, -1, 1);
        } catch (Throwable failure) {
            if (Loader.trace) failure.printStackTrace();
        }
    }

    private static void ensureRuneLiteOverlayBuffer(int width, int height) {
        if (runeLiteOverlayImage != null && runeLiteOverlayWidth == width && runeLiteOverlayHeight == height) {
            return;
        }

        runeLiteOverlayWidth = width;
        runeLiteOverlayHeight = height;
        runeLiteOverlayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        runeLiteOverlayPixels = ((DataBufferInt) runeLiteOverlayImage.getRaster().getDataBuffer()).getData();
    }

    private static boolean hasRuneLiteOverlayPixels() {
        for (int pixel : runeLiteOverlayPixels) {
            if ((pixel >>> 24) != 0) {
                return true;
            }
        }
        return false;
    }

    public final void windowDeiconified(WindowEvent windowevent) {
        anInt35++;
    }

    public final void focusGained(FocusEvent focusevent) {
        anInt23++;
        Class348_Sub40_Sub16.aBoolean9229 = true;
        Class49.aBoolean4726 = true;
    }

    public final void windowClosed(WindowEvent windowevent) {
        anInt33++;
    }

    final boolean method89(int i) {
        anInt40++;
        return true;
        /*if (i <= 19) return true;
        String string = getDocumentBase().getHost().toLowerCase();
        if (string.equals("jagex.com") || string.endsWith(".jagex.com")) return true;
        if (string.equals("runescape.com") || string.endsWith(".runescape.com")) return true;
        if (string.equals("stellardawn.com") || string.endsWith(".stellardawn.com")) return true;
        if (string.endsWith("127.0.0.1")) return true;
        for (*//**//*; string.length() > 0 && string.charAt(-1 + string.length()) >= 48; string = string.substring(0, string.length() - 1)) {
            if (string.charAt(string.length() - 1) > 57) {
                break;
            }
        }
        if (string.endsWith("192.168.1.")) return true;
        method82(53, "invalidhost");
        return false;*/
    }

    private final void method90(boolean bool, boolean bool_4_) {
        anInt26++;
        synchronized (this) {
            if (Class26.aBoolean384) return;
            Class26.aBoolean384 = true;
        }
        System.out.println("Shutdown start - clean:" + bool);
        DetachedRenderer.shutdown();
        if (Class93.anApplet1530 != null) Class93.anApplet1530.destroy();
        if (bool_4_ != false) aBoolean17 = false;
        try {
            method80(0);
        } catch (Exception exception) {
            /* empty */
        }
        if (aBoolean17) {
            try {
                jagmisc.quit();
            } catch (Throwable throwable) {
                /* empty */
            }
            aBoolean17 = false;
        }
        Class257.method1945((byte) -128, true);
        Class228.method1629(!bool_4_);
        if (Class305.aCanvas3869 != null) {
            try {
                Class305.aCanvas3869.removeFocusListener(this);
                Class305.aCanvas3869.getParent().remove(Class305.aCanvas3869);
            } catch (Exception exception) {
                /* empty */
            }
        }
        if (Class348_Sub23_Sub1.aClass297_8992 != null) {
            try {
                Class348_Sub23_Sub1.aClass297_8992.method2234((byte) 103);
            } catch (Exception exception) {
                /* empty */
            }
        }
        method91((byte) 108);
        if (Class52.aFrame4904 != null) {
            Class52.aFrame4904.setVisible(false);
            Class52.aFrame4904.dispose();
            Class52.aFrame4904 = null;
        }
        System.out.println("Shutdown complete - clean:" + bool);
    }

    public static final void provideLoaderApplet(Applet applet) {
        anInt11++;
        parameterApplet = applet;
        Class93.anApplet1530 = applet;
    }

    public void supplyApplet(Applet applet) {
        parameterApplet = applet;
        Class93.anApplet1530 = this;
    }

    abstract void method91(byte i);

    public final AppletContext getAppletContext() {
        anInt19++;
        if (Class52.aFrame4904 != null) return null;
        if (Class93.anApplet1530 != null && this != Class93.anApplet1530) return Class93.anApplet1530.getAppletContext();
        return super.getAppletContext();
    }

    public final URL getCodeBase() {
        anInt29++;
        if (parameterApplet != null) return parameterApplet.getCodeBase();
        if (Class52.aFrame4904 != null) return null;
        if (Class93.anApplet1530 != null && this != Class93.anApplet1530) return Class93.anApplet1530.getCodeBase();
        return super.getCodeBase();
    }

    public abstract void init();

    public final void destroy() {
        anInt21++;
        if (Class348_Sub40_Sub9.anApplet_Sub1_9169 == this && !Class26.aBoolean384) {
            Class113.aLong1739 = Class62.method599(-108);
            Class286_Sub5.method2161((byte) 77, 5000L);
            Class231.aClass297_2993 = null;
            method90(false, false);
        }
    }

    public final void run() {
        currentThread = Thread.currentThread();
        anInt28++;
        do {
            try {
                if (Class297.aString3782 != null) {
                    String string = Class297.aString3782.toLowerCase();
                    if (string.indexOf("sun") != -1 || string.indexOf("apple") != -1) {
                        String string_5_ = Class297.aString3796;
                        if (string_5_.equals("1.1") || string_5_.startsWith("1.1.") || string_5_.equals("1.2") || string_5_.startsWith("1.2.")) {
                            method82(-119, "wrongjava");
                            break;
                        }
                    } else if (string.indexOf("ibm") != -1 && (Class297.aString3796 == null || Class297.aString3796.equals("1.4.2"))) {
                        method82(81, "wrongjava");
                        break;
                    }
                }
                if (Class297.aString3796 != null && Class297.aString3796.startsWith("1.")) {
                    int i = 2;
                    int i_6_ = 0;
                    while (Class297.aString3796.length() > i) {
                        int i_7_ = Class297.aString3796.charAt(i);
                        if (i_7_ < 48 || i_7_ > 57) break;
                        i++;
                        i_6_ = 10 * i_6_ - (-i_7_ + 48);
                    }
                    if (i_6_ >= 5) Class367_Sub4.aBoolean7320 = true;
                }
                Applet applet = Class348_Sub40_Sub9.anApplet_Sub1_9169;
                if (Class93.anApplet1530 != null) applet = Class93.anApplet1530;
                Method method = Class297.aMethod3786;
                if (method != null) {
                    try {
                        method.invoke(applet, Boolean.TRUE);
                    } catch (Throwable throwable) {
                        /* empty */
                    }
                }
                aa_Sub3.method168((byte) 103);
                Class127_Sub1.method1119(false);
                method87((byte) -97);
                method92(28740);
                Class348_Sub8.aClass241_6660 = Class229.method1631(false);
                // The loop is paced exactly like open592's: each pass polls the cycle timer without
                // sleeping, runs the cycles that are due, draws a frame if the pacer says one is due,
                // then waits for whichever comes first - the next frame or the next cycle. The logic
                // therefore keeps its own 20 ms rate (Class73.aLong4783) while frames can come faster;
                // nothing in the frame advances game time, so extra frames cannot speed the game up.
                // With the pacer off this is the vanilla loop unchanged: the timer sleeps to the next
                // cycle and one frame is drawn per pass.
                while (Class113.aLong1739 == 0L || (Class62.method599(-124) < Class113.aLong1739)) {
                    boolean detached = DetachedRenderer.isActive();
                    Class101_Sub2.anInt5744 = detached
                        ? Class348_Sub8.aClass241_6660.pollCycles(0, Class73.aLong4783)
                        : Class348_Sub8.aClass241_6660.method1861(0, Class73.aLong4783);
                    for (int i = 0; Class101_Sub2.anInt5744 > i; i++)
                        method84(-1);

                    long frameNow = System.nanoTime();
                    if (!detached || DetachedRenderer.frameDue(frameNow)) {
                        if (detached) DetachedRenderer.frameDrawn(frameNow);
                        method88(-119);
                    }
                    // The RuneLite plugin loop is a client-tick callback: it has to fire at the game
                    // cycle rate (50 Hz), not the frame rate. Firing it from the frame (once per
                    // drawn frame) made every CLIENT_TICK-driven plugin animation run 3.6x too fast
                    // once the pacer raised the frame rate. A pass with no due cycle has no client
                    // tick to report, so it is skipped; the vanilla loop always runs a cycle.
                    if (Class101_Sub2.anInt5744 > 0)
                        fireRuneLiteClientLoop();
                    Class369_Sub3_Sub1.method3578((byte) -42, Class305.aCanvas3869, (Class348_Sub23_Sub1.aClass297_8992));

                    if (detached) {
                        long wait = DetachedRenderer.millisUntilNextFrame(frameNow);
                        long cycleWait = (Class348_Sub8.aClass241_6660.nanosToNextCycle() + 999999L) / 1000000L;
                        if (cycleWait < wait) wait = cycleWait;
                        // NB: -127, not -42: Class286_Sub5.method2161 evaluates 70 % ((i + 52) / 32)
                        // and any i whose (i + 52) / 32 is 0 divides by zero.
                        Class286_Sub5.method2161((byte) -127, wait < 1L ? 1L : wait);
                    }
                }
            } catch (Throwable throwable) {
                Class156.method1242(method81((byte) 109), throwable, 15004);
                method82(123, "crash");
            } finally {
                method90(true, false);
            }
        } while (false);
    }

    static Dimension getActiveCanvasSize() {
        return new Dimension(Class321.anInt4017, Class348_Sub42_Sub8_Sub2.anInt10432);
    }

    static boolean shouldScaleCanvasFrame() {
        return false;
    }

    static boolean shouldScaleOpenGLFrame() {
        return false;
    }

    static boolean useFastCanvasScaling() {
        return false;
    }

    static void applyCanvasSize() {
        if (Class305.aCanvas3869 == null) {
            return;
        }

        Dimension size = getActiveCanvasSize();
        Applet_Sub1 applet = Class348_Sub40_Sub9.anApplet_Sub1_9169;
        if (!size.equals(Class305.aCanvas3869.getPreferredSize())) {
            Class305.aCanvas3869.setPreferredSize(size);
        }
        if (!size.equals(Class305.aCanvas3869.getSize())) {
            Class305.aCanvas3869.setSize(size);
        }
    }

    static void applyCanvasLocation(Container container, int x, int y) {
        if (Class305.aCanvas3869 == null) {
            return;
        }

        Class305.aCanvas3869.setLocation(x, y);
    }

    static void refreshCanvasAfterDisplayChange() {
        Canvas canvas = Class305.aCanvas3869;
        if (canvas == null) {
            return;
        }

        try {
            applyCanvasSize();
            revalidateDisplayTree(canvas);
        } catch (Throwable throwable) {
            revalidateDisplayTree(canvas);
        }
    }

    private static void pulseWindowAfterDisplayChange(final Canvas canvas) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Container container = canvas.getParent();
                    while (container != null && !(container instanceof Frame)) {
                        container = container.getParent();
                    }

                    if (!(container instanceof Frame)) {
                        repaintDisplayTree(canvas);
                        return;
                    }

                    final Frame frame = (Frame) container;
                    final int state = frame.getExtendedState();
                    final boolean maximized = (state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH;

                    if (maximized) {
                        frame.setExtendedState(state & ~Frame.MAXIMIZED_BOTH);
                        repaintDisplayTree(canvas);
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    frame.setExtendedState(state);
                                } catch (Throwable ignored) {
                                }
                                repaintDisplayTree(canvas);
                                scheduleRepaintPass(canvas);
                            }
                        });
                        return;
                    }

                    Rectangle bounds = frame.getBounds();
                    if (bounds.width > 1 && bounds.height > 1) {
                        frame.setBounds(bounds.x, bounds.y, bounds.width - 1, bounds.height);
                        frame.setBounds(bounds);
                    }
                    repaintDisplayTree(canvas);
                    scheduleRepaintPass(canvas);
                } catch (Throwable throwable) {
                    repaintDisplayTree(canvas);
                }
            }
        });
    }

    private static void scheduleRepaintPass(final Canvas canvas) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                repaintDisplayTree(canvas);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        repaintDisplayTree(canvas);
                    }
                });
            }
        });
    }

    private static void repaintDisplayTree(Canvas canvas) {
        try {
            canvas.invalidate();
            canvas.validate();
            canvas.repaint();

            Container container = canvas.getParent();
            while (container != null) {
                container.invalidate();
                container.validate();
                container.doLayout();
                container.repaint();
                container = container.getParent();
            }

            Applet_Sub1 applet = Class348_Sub40_Sub9.anApplet_Sub1_9169;
            if (applet != null) {
                applet.invalidate();
                applet.validate();
                applet.repaint();
            }

            canvas.getToolkit().sync();
        } catch (Throwable ignored) {
            try {
                canvas.repaint();
            } catch (Throwable ignoredAgain) {
            }
        }
    }

    private static void revalidateDisplayTree(Canvas canvas) {
        try {
            canvas.invalidate();
            canvas.validate();

            Container container = canvas.getParent();
            while (container != null) {
                container.invalidate();
                container.validate();
                container.doLayout();
                container = container.getParent();
            }

            Applet_Sub1 applet = Class348_Sub40_Sub9.anApplet_Sub1_9169;
            if (applet != null) {
                applet.invalidate();
                applet.validate();
            }
        } catch (Throwable ignored) {
        }
    }

    static int scaleMouseX(int x) {
        return x;
    }

    static int scaleMouseY(int y) {
        return y;
    }

    @Override
    public Canvas getCanvas() {
        return Class305.aCanvas3869;
    }

    @Override
    public int getCanvasWidth() {
        return Class321.anInt4017;
    }

    @Override
    public int getCanvasHeight() {
        return Class348_Sub42_Sub8_Sub2.anInt10432;
    }

    @Override
    public Dimension getRealDimensions() {
        Canvas canvas = getCanvas();
        if (canvas != null && canvas.getWidth() > 0 && canvas.getHeight() > 0) {
            return canvas.getSize();
        }
        return new Dimension(Class321.anInt4017, Class348_Sub42_Sub8_Sub2.anInt10432);
    }

    @Override
    public void invalidateStretching(boolean resize) {
        if (!resize) {
            repaint();
            return;
        }

        if (currentThread != null && Thread.currentThread() == currentThread) {
            try {
                Class367_Sub11.method3556(false);
            } catch (Throwable ignored) {
            }
        }

        Dimension size = new Dimension(Class321.anInt4017, Class348_Sub42_Sub8_Sub2.anInt10432);
        Canvas canvas = getCanvas();
        if (canvas != null) {
            if (!size.equals(canvas.getPreferredSize())) {
                canvas.setPreferredSize(size);
            }
            if (!size.equals(canvas.getSize())) {
                canvas.setSize(size);
                canvas.invalidate();
            }
            if (Class348_Sub8.aHa6654 != null) {
                try {
                    Class348_Sub8.aHa6654.method3669(canvas, Class321.anInt4017, Class348_Sub42_Sub8_Sub2.anInt10432);
                } catch (Throwable ignored) {
                }
            }
        }

        Container parent = getParent();
        if (parent != null) {
            parent.invalidate();
            applyCanvasLocation(parent, Class348_Sub48.anInt7129, Class335.anInt4167);
        }
    }

    @Override
    public void setAnimationSmoothingEnabled(boolean state) {
        animationSmoothingEnabled = state;
        Class28.aBoolean5002 = state;
    }

    @Override
    public boolean isAnimationSmoothingEnabled() {
        return animationSmoothingEnabled;
    }

    @Override
    public void setDetachedRendererEnabled(boolean enabled) {
        DetachedRenderer.requestActive(enabled);
    }

    @Override
    public void setDetachedRendererFpsTarget(int fps) {
        DetachedRenderer.setFpsTarget(fps);
    }

    @Override
    public void setAfkSaverEnabled(boolean enabled) {
        GameTuning.afkSaverEnabled = enabled;
    }

    @Override
    public void setAfkSaverGracePackets(int packets) {
        GameTuning.afkGracePackets = packets;
    }

    @Override
    public void setShowFpsOverlay(boolean show) {
        Class298.aBoolean3811 = show;
    }

    @Override
    public void setRenderDistance(int tiles) {
        GameTuning.setRenderDistance(tiles);
    }

    @Override
    public void setFogScale(int percent) {
        GameTuning.setFogScale(percent);
    }

    @Override
    public void setCullingDisabled(boolean disabled) {
        GameTuning.setCullingDisabled(disabled);
    }

    @Override
    public void setCullingDistance(int tiles) {
        GameTuning.setCullingDistance(tiles);
    }

    @Override
    public void setDrawRadiusScale(int percent) {
        GameTuning.setDrawRadiusScale(percent);
    }

    @Override
    public void setClampFogToBuiltMap(boolean clamp) {
        GameTuning.setClampFogToBuiltMap(clamp);
    }

    @Override
    public void setViewCullingDisabled(boolean disabled) {
        GameTuning.setViewCullingDisabled(disabled);
    }

    @Override
    public void setHideUpperFloors(boolean hide) {
        GameTuning.setHideUpperFloors(hide);
    }

    @Override
    public void setZoom(int offset) {
        GameTuning.setZoom(offset);
    }

    @Override
    public void setFogColour(int rgb) {
        GameTuning.setFogColour(rgb);
    }

    @Override
    public int getViewDistance() {
        return GameTuning.engagedViewDistance();
    }

    @Override
    public int getZoomOffset() {
        return GameTuning.zoomOffset();
    }

    @Override
    public int getFogScale() {
        return GameTuning.fogScalePercent();
    }

    @Override
    public int getDrawRadiusScale() {
        return GameTuning.drawRadiusScalePercent();
    }

    @Override
    public int getCullingDistance() {
        return GameTuning.cullingDistance();
    }

    @Override
    public boolean isCullingDisabled() {
        return GameTuning.cullingDisabled();
    }

    @Override
    public boolean isViewCullingDisabled() {
        return GameTuning.viewCullingDisabled();
    }

    @Override
    public boolean isHideUpperFloors() {
        return GameTuning.hideUpperFloors();
    }

    @Override
    public boolean isClampFogToBuiltMap() {
        return GameTuning.clampFogToBuiltMap();
    }

    @Override
    public int getFogColour() {
        return GameTuning.fogColour();
    }

    @Override
    public int getFpsTarget() {
        return DetachedRenderer.getFpsTarget();
    }

    @Override
    public boolean isShowFpsOverlay() {
        return Class298.isFpsOverlayShown();
    }

    @Override
    public void snapRendererTuning() {
        GameTuning.snapViewTuning();
    }

    @Override
    public void resetWheelTuning() {
        GameTuning.resetWheelTuning();
    }

    @Override
    public void setTuningChangeListener(Runnable listener) {
        GameTuning.setTuningChangeListener(listener);
    }

    @Override
    public void forceSceneRebuild() {
        GameTuning.forceSceneRebuild();
    }

    @Override
    public void resetSceneSize() {
        GameTuning.resetSceneSize();
    }

    @Override
    public int getLocalPlayerLocalX() {
        return Class132.aPlayer_1907 == null ? -1 : Class132.aPlayer_1907.x;
    }

    @Override
    public int getLocalPlayerLocalY() {
        return Class132.aPlayer_1907 == null ? -1 : Class132.aPlayer_1907.y;
    }

    @Override
    public int getLocalPlayerSceneX() {
        return Class132.aPlayer_1907 == null ? -1 : Class132.aPlayer_1907.anIntArray10320[0];
    }

    @Override
    public int getLocalPlayerSceneY() {
        return Class132.aPlayer_1907 == null ? -1 : Class132.aPlayer_1907.anIntArray10317[0];
    }

    @Override
    public int getLocalPlayerSize() {
        return Class132.aPlayer_1907 == null ? 1 : Class132.aPlayer_1907.method2436((byte) 72);
    }

    @Override
    public int getLocalPlayerCombatLevel() {
        return Class132.aPlayer_1907 == null ? 0 : Class132.aPlayer_1907.anInt10516;
    }

    @Override
    public boolean hasLocalPlayer() {
        return Class132.aPlayer_1907 != null;
    }

    @Override
    public int getCameraX() {
        return Class286_Sub4.anInt6246;
    }

    @Override
    public int getCameraY() {
        return Class305.anInt3855;
    }

    @Override
    public int getCameraZ() {
        return Class59_Sub2_Sub2.anInt8685;
    }

    @Override
    public int getCameraPitch() {
        return Class348_Sub42_Sub19.anInt9701;
    }

    @Override
    public int getCameraYaw() {
        return Class5.anInt4638;
    }

    @Override
    public int getCameraScale() {
        return Class132.anInt1906;
    }

    @Override
    public int getTileHeight(int localX, int localY, int plane) {
        return Class275.method2064(localX << 2, plane, 11219, localY << 2);
    }

    @Override
    public net.runelite.api.Point projectLocalToCanvas(int localX, int localY, int height) {
        if (Class348_Sub8.aHa6654 == null) {
            return null;
        }

        int[] point = new int[3];
        int worldX = localX << 2;
        int worldY = localY << 2;
        if (!Class305.aBoolean3870) {
            Class348_Sub8.aHa6654.da(worldX, height, worldY, point);
        } else {
            Class348_Sub8.aHa6654.HA(worldX, height, worldY, Math.max(1, Class132.anInt1906), point);
        }

        if (point[0] < 0 || point[1] < 0) {
            return null;
        }

        if (point[0] > Class321.anInt4017 || point[1] > Class348_Sub42_Sub8_Sub2.anInt10432) {
            return null;
        }

        return new net.runelite.api.Point(point[0], point[1]);
    }

    @Override
    public boolean hasNativeProjection() {
        return Class348_Sub8.aHa6654 != null;
    }

    @Override
    public List<GroundItemInfo> getGroundItems() {
        if (ChatMessage.aClass357ArrayArrayArray2029 == null || Exception_Sub1.aClass255_112 == null) {
            return Collections.emptyList();
        }

        List<GroundItemInfo> items = new ArrayList<>();
        int planes = ChatMessage.aClass357ArrayArrayArray2029.length;
        for (int plane = 0; plane < planes; plane++) {
            Class357[][] planeTiles = ChatMessage.aClass357ArrayArrayArray2029[plane];
            if (planeTiles == null) {
                continue;
            }

            for (int sceneX = 0; sceneX < planeTiles.length; sceneX++) {
                Class357[] column = planeTiles[sceneX];
                if (column == null) {
                    continue;
                }

                for (int sceneY = 0; sceneY < column.length; sceneY++) {
                    Class357 tile = column[sceneY];
                    if (tile == null || !(tile.aClass318_Sub1_Sub2_4408 instanceof Class318_Sub1_Sub2_Sub1)) {
                        continue;
                    }

                    Class318_Sub1_Sub2_Sub1 pile = (Class318_Sub1_Sub2_Sub1) tile.aClass318_Sub1_Sub2_4408;
                    addGroundItem(items, pile.anInt10181, pile.anInt10185, pile.x >> 2, pile.y >> 2, plane);
                    if (pile.anInt10189 != -1) {
                        addGroundItem(items, pile.anInt10189, pile.anInt10190, pile.x >> 2, pile.y >> 2, plane);
                    }
                    if (pile.anInt10180 != -1) {
                        addGroundItem(items, pile.anInt10180, pile.anInt10186, pile.x >> 2, pile.y >> 2, plane);
                    }
                }
            }
        }
        return items;
    }

    private void addGroundItem(List<GroundItemInfo> items, int id, int quantity, int localX, int localY, int plane) {
        if (id < 0) {
            return;
        }

        ObjType definition = Exception_Sub1.aClass255_112.getItemDefinitions(-115, id);
        String name = definition == null || definition.name == null ? "Item " + id : definition.name;
        int price = definition == null ? 0 : Math.max(0, definition.cost);
        items.add(new GroundItemInfo(id, quantity, name, price, localX, localY, plane));
    }

    @Override
    public List<NpcInfo> getNpcs() {
        if (Class282.aClass356_3654 != null) {
            int count = Class282.aClass356_3654.method3474(1);
            if (count > 0) {
                if (Class348_Sub40_Sub23.aClass348_Sub22Array9319 == null || Class348_Sub40_Sub23.aClass348_Sub22Array9319.length < count) {
                    Class348_Sub40_Sub23.aClass348_Sub22Array9319 = new Class348_Sub22[Math.max(1024, count)];
                }
                Class348_Sub32.anInt6930 = Class282.aClass356_3654.method3477(3, Class348_Sub40_Sub23.aClass348_Sub22Array9319);
            }
        }

        if (Class348_Sub40_Sub23.aClass348_Sub22Array9319 == null) {
            return Collections.emptyList();
        }

        List<NpcInfo> npcs = new ArrayList<>();
        int count = Math.min(Class348_Sub32.anInt6930, Class348_Sub40_Sub23.aClass348_Sub22Array9319.length);
        for (int i = 0; i < count; i++) {
            Class348_Sub22 entry = Class348_Sub40_Sub23.aClass348_Sub22Array9319[i];
            if (entry == null || entry.aNpc_6859 == null || entry.aNpc_6859.definition == null) {
                continue;
            }

            Npc npc = entry.aNpc_6859;
            NPCType definition = npc.definition.multinpcs == null ? npc.definition : npc.definition.method794(Class318_Sub1_Sub3_Sub3.aClass170_10209, -1);
            if (definition == null || definition.name == null || definition.name.length() == 0 || "null".equalsIgnoreCase(definition.name)) {
                continue;
            }

            int size = Math.max(1, definition.anInt1399);
            int height = Math.max(128, npc.method2426(200));
            int trueLocalX = (npc.anIntArray10320 != null && npc.anIntArray10320.length > 0)
                ? ((npc.anIntArray10320[0] << 9) + (size << 8)) >> 2
                : npc.x >> 2;
            int trueLocalY = (npc.anIntArray10317 != null && npc.anIntArray10317.length > 0)
                ? ((npc.anIntArray10317[0] << 9) + (size << 8)) >> 2
                : npc.y >> 2;
            int localX = npc.x <= 0 ? trueLocalX : npc.x >> 2;
            int localY = npc.y <= 0 ? trueLocalY : npc.y >> 2;

            npcs.add(new NpcInfo(
                npc.anInt10290,
                definition.id,
                definition.name,
                localX,
                localY,
                trueLocalX,
                trueLocalY,
                npc.plane,
                height,
                size,
                definition.combatLevel,
                definition.isFollower,
                false
            ));
        }
        return npcs;
    }

    @Override
    public String getMinimapDiagnostics() {
        return MinimapDebug.warning();
    }

    public NpcHullInfo getNpcHull(int npcIndex) {
        return NpcHullHooks.get(npcIndex);
    }

    @Override
    public List<SkillSnapshot> getSkillSnapshots() {
        if (Class161.anIntArray2145 == null || Class256.anIntArray3295 == null || Class186.anIntArray2497 == null) {
            return Collections.emptyList();
        }

        Skill[] skills = Skill.values();
        int count = Math.min(Math.min(Class161.anIntArray2145.length, Class256.anIntArray3295.length), Class186.anIntArray2497.length);
        count = Math.min(count, skills.length);

        List<SkillSnapshot> snapshots = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            Skill skill = skills[index];
            if (skill == Skill.OVERALL) {
                continue;
            }

            snapshots.add(new SkillSnapshot(
                skill,
                Math.max(0, Class186.anIntArray2497[index]),
                Math.max(0, Class256.anIntArray3295[index]),
                Math.max(0, Class161.anIntArray2145[index])
            ));
        }
        return snapshots;
    }

    @Override
    public void addMenuEntry(String option, String target, int type, int identifier, int param0, int param1) {
        Class50_Sub3.method466(false, target == null ? "" : target, 0, (byte) -93, false, param0, -1, true, type, identifier, option == null ? "" : option, identifier, -1);
    }

    @Override
    public byte[][][] getTileSettings() {
        return new byte[0][][];
    }

    @Override
    public int[][][] getTileHeights() {
        return new int[0][][];
    }

    @Override
    public int getPlane() {
        return Class132.aPlayer_1907 == null ? Class348_Sub40_Sub12.anInt9200 : Class132.aPlayer_1907.plane;
    }

    @Override
    public int getBaseX() {
        return za_Sub2.regionTileX;
    }

    @Override
    public int getBaseY() {
        return Class90.regionTileY;
    }

    @Override
    public boolean isInInstancedRegion() {
        return false;
    }

    @Override
    public int[][][] getInstanceTemplateChunks() {
        return new int[0][][];
    }

    @Override
    public int[][] getCollisionMaps(int plane) {
        return new int[0][];
    }

    @Override
    public boolean isClientThread() {
        return Thread.currentThread() == currentThread;
    }

    @Override
    public boolean isShiftPressed() {
        try {
            return Class182.aClass346_2449 != null && Class182.aClass346_2449.method2696(81, -121);
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public boolean isShiftClickEnabled() {
        return Class116.shiftClick;
    }

    abstract void method92(int i);

    abstract void method93(int i);

    static final void set(String string) {
        Class363.aString4461 = string;
        Class348_Sub38.anInt7006 = string.length();
    }

    static final void method94(String string, int i) {
        anInt6++;
        if (Class286_Sub1.aStringArray6200 == null) Class14_Sub3.method249(2);
        Class286_Sub3.aCalendar6221.setTime(new Date(Class62.method599(-102)));
        int i_8_ = Class286_Sub3.aCalendar6221.get(11);
        int i_9_ = Class286_Sub3.aCalendar6221.get(12);
        int i_10_ = Class286_Sub3.aCalendar6221.get(13);
        String string_11_ = (Integer.toString(i_8_ / 10) + i_8_ % 10 + ":" + i_9_ / 10 + i_9_ % 10 + ":" + i_10_ / 10 + i_10_ % 10);
        String[] strings = Class348_Sub40_Sub23.method3113('\n', true, string);
        for (int i_12_ = 0; i_12_ < strings.length; i_12_++) {
            for (int i_13_ = Class369_Sub2.anInt8587; i_13_ > 0; i_13_--)
                Class286_Sub1.aStringArray6200[i_13_] = Class286_Sub1.aStringArray6200[-1 + i_13_];
            Class286_Sub1.aStringArray6200[0] = string_11_ + ": " + strings[i_12_];
            if (Class299_Sub1.aFileOutputStream6323 != null) {
                try {
                    Class299_Sub1.aFileOutputStream6323.write(Class348_Sub24.method2992(((Class286_Sub1.aStringArray6200[0]) + "\n"), (byte) -20));
                } catch (java.io.IOException ioexception) {
                    /* empty */
                }
            }
            if (-1 + Class286_Sub1.aStringArray6200.length > Class369_Sub2.anInt8587) {
                Class369_Sub2.anInt8587++;
                if (Class284.anInt3676 > 0) Class284.anInt3676++;
            }
        }
        int i_14_ = 85 / ((i - -1) / 52);
    }

    final void method95(int i, int i_15_, int i_16_, int i_17_, int i_18_, String string, int i_19_) {
        anInt25++;
        try {
            if (Class348_Sub40_Sub9.anApplet_Sub1_9169 == null) {
                Class348_Sub48.anInt7129 = 0;
                Class348_Sub22.anInt6857 = Class348_Sub42_Sub8_Sub2.anInt10432 = i_16_;
                Class272.anInt3473 = Class321.anInt4017 = i;
                Class335.anInt4167 = 0;
                Class348_Sub1_Sub3.anInt8818 = i_15_;
                Class348_Sub40_Sub9.anApplet_Sub1_9169 = this;
                Class348_Sub8.anApplet6662 = Class93.anApplet1530;
                Class231.aClass297_2993 = Class348_Sub23_Sub1.aClass297_8992 = new Class297(i_17_, string, i_18_, Class93.anApplet1530 != null);
                Class144 class144 = Class348_Sub23_Sub1.aClass297_8992.method2236(this, -10240, 1);
                if (i_19_ != 50) anInt37 = -13;
                while (class144.anInt1997 == 0) Class286_Sub5.method2161((byte) -126, 10L);
            } else {
                Class348_Sub51.anInt7252++;
                if (Class348_Sub51.anInt7252 >= 3) method82(112, "alreadyloaded");
                else getAppletContext().showDocument(getDocumentBase(), "_self");
            }
        } catch (Throwable throwable) {
            Class156.method1242(null, throwable, 15004);
            method82(52, "crash");
        }
    }

    final void method96(int i, int i_20_, boolean bool, int i_21_, int i_22_, String string, int i_23_, int i_24_) {
        try {
            if (i_23_ != 23499) return;
            Class272.anInt3473 = Class321.anInt4017 = i_20_;
            Class348_Sub22.anInt6857 = Class348_Sub42_Sub8_Sub2.anInt10432 = i_24_;
            Class348_Sub40_Sub9.anApplet_Sub1_9169 = this;
            Class335.anInt4167 = 0;
            Class348_Sub48.anInt7129 = 0;
            Class348_Sub1_Sub3.anInt8818 = i_21_;
            Class348_Sub8.anApplet6662 = null;
            Class52.aFrame4904 = new Frame();
            Class52.aFrame4904.setTitle("Jagex");
            Class52.aFrame4904.setResizable(true);
            Class52.aFrame4904.addWindowListener(this);
            Class52.aFrame4904.setVisible(true);
            Class52.aFrame4904.toFront();
            Insets insets = Class52.aFrame4904.getInsets();
            Class52.aFrame4904.setSize(insets.right + (insets.left + Class272.anInt3473), (insets.bottom + (Class348_Sub22.anInt6857 + insets.top)));
            Class231.aClass297_2993 = Class348_Sub23_Sub1.aClass297_8992 = new Class297(i, string, i_22_, true);
            Class144 class144 = Class348_Sub23_Sub1.aClass297_8992.method2236(this, i_23_ + -33739, 1);
            while (class144.anInt1997 == 0) Class286_Sub5.method2161((byte) 21, 10L);
        } catch (Exception exception) {
            Class156.method1242(null, exception, i_23_ + -8495);
        }
        anInt31++;
    }

    public final void windowIconified(WindowEvent windowevent) {
        anInt14++;
    }

    final boolean method97(int i) {
        if (i != -1) method88(-104);
        anInt4++;
        return Class348_Sub40_Sub19.method3098(-30282, "jaclib");
    }

    public static void method98(int i) {
        anIntArray38 = null;
        aClass324_20 = null;
        if (i != 32717) method86(null, 65);
    }

    public final void start() {
        anInt36++;
        if (this == Class348_Sub40_Sub9.anApplet_Sub1_9169 && !Class26.aBoolean384) Class113.aLong1739 = 0L;
    }

    abstract void method99(byte i);

    final boolean method100(int i) {
        anInt10++;
        if (i != 10) return true;
        return Class348_Sub40_Sub19.method3098(-30282, "jagtheora");
    }
}
