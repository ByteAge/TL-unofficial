package chitchat.skin;

/**
 * Created by RaminBT on 24/01/2016.
 */
public class GraySkin extends Skin {
    @Override
    public int id() {
        return 13;
    }

    @Override
    public int actionbarColor() {
        return 0xff9E9E9E;
    }

    @Override
    public int drawerNamesColor() {
        return 0xff616161;
    }

    @Override
    public int onlineStatusColor() {
        return 0;
    }

    @Override
    public int unreadEyeColor() {
        return 0xff616161;
    }

    @Override
    public int light() {
        return 0xffF5F5F5;
    }
}
