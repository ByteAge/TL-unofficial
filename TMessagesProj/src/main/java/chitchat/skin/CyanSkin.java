package chitchat.skin;

/**
 * Created by RaminBT on 24/01/2016.
 */
public class CyanSkin extends Skin {
    @Override
    public int id() {
        return 11;
    }

    @Override
    public int actionbarColor() {
        return 0xff00BCD4;
    }

    @Override
    public int drawerNamesColor() {
        return 0xff0097A7;
    }

    @Override
    public int onlineStatusColor() {
        return 0;
    }

    @Override
    public int unreadEyeColor() {
        return 0xffFF432B;
    }

    @Override
    public int light() {
        return 0xffB2EBF2;
    }
}
