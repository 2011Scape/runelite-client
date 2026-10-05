/* Class241 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

abstract class Class241 {
    static int anInt3148;
    static int anInt3149;
    static Class351 aClass351_3150 = new Class351(71, 2);

    abstract void method1856(byte i);

    static final long method1857(byte i) {
        anInt3148++;
        if (i != -45) return -58L;
        return Class348_Sub8.aClass241_6660.method1862(-18931);
    }

    abstract long method1858(int i);

    abstract int method1859(int i, long l);

    public static void method1860(byte i) {
        aClass351_3150 = null;
        int i_0_ = -52 / ((-22 - i) / 55);
    }

    public Class241() {
        /* empty */
    }

    final int method1861(int i, long l) {
        try {
            anInt3149++;
            long l_1_ = method1858(-73);
            if ((long) i < l_1_) Class286_Sub5.method2161((byte) 61, l_1_);
            return method1859(71, l);
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, "tb.H(" + i + ',' + l + ')');
        }
    }

    /**
     * Non-blocking counterpart of {@link #method1861}: advances the cycle clock by the time that
     * has passed and returns how many cycles are now due, without ever sleeping. The game loop
     * calls this every pass and does its own waiting (Applet_Sub1.run), which is what lets the
     * renderer be paced separately instead of being dragged along by the timer's sleep.
     * <p>
     * A cycle that is not due yet must report zero. The vanilla {@link #method1859} returns 1 from
     * its "not due" branch because the sleeping loop only ever reaches it after the sleep has
     * brought the cycle due; called every frame without that sleep, it would report a cycle on
     * every frame and run the game logic at the frame rate. {@link #cycleDue()} is exactly that
     * branch's condition, so the two can never disagree about when a cycle has arrived.
     */
    final int pollCycles(int i, long l) {
        anInt3149++;
        method1858(-73);
        if (!cycleDue()) return 0;
        return method1859(71, l);
    }

    /** Whether the next cycle is due at the clock reading just advanced by {@link #method1858}. */
    abstract boolean cycleDue();

    /**
     * Nanoseconds left until the next cycle is due, or 0 when one is due right now. The loop uses
     * this to wait for whichever comes first: the next paced frame or the next cycle.
     */
    abstract long nanosToNextCycle();

    abstract long method1862(int i);
}
