/* Class290 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class290 {
    static boolean aBoolean3706;
    static int anInt3707;
    Interface5_Impl1 anInterface5_Impl1_3708;
    boolean aBoolean3709;
    static float aFloat3710;
    Interface5_Impl1 anInterface5_Impl1_3711;
    static int anInt3712;
    static int anInt3713 = 0;
    boolean aBoolean3714;
    static int anInt3715;
    static int anInt3716;
    static int anInt3717 = 1338;
    static int anInt3718;

    final void method2195(boolean bool) {
        if (this.anInterface5_Impl1_3708 != null) this.anInterface5_Impl1_3708.method21(23315);
        anInt3715++;
        this.aBoolean3714 = bool;
    }

    static final void method2196(byte i) {
        // The vanilla far plane was (int)(scene * 34.46) << 2; GameTuning.farPlane builds it from
        // the same coefficient with open592's scale instead, so the fog horizon is where open592's
        // is (just inside the drawn radius) and the wheel visibly moves it.
        Class239_Sub19.anInt6043 = GameTuning.farPlane(Class367_Sub4.anInt7319);
        Class348_Sub33.anInt6964 = 200;
        anInt3716++;
        if (i == -9) {
            Class226.method1626(1, false);
        }
    }

    final boolean method2197(byte i) {
        if (i >= -4) method2195(true);
        anInt3718++;
        return this.aBoolean3714 && !this.aBoolean3709;
    }

    Class290(boolean bool) {
        this.aBoolean3709 = bool;
    }
}
