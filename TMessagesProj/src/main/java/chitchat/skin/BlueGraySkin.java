package chitchat.skin;

/**
 * Created by RaminBT on 24/01/2016.
 */
public class BlueGraySkin extends Skin {
    @Override
    public int id() {
        return 10;
    }

    @Override
    public int actionbarColor() {
        return 0xff607D8B;
    }

    @Override
    public int drawerNamesColor() {
        return 0xff455A64;
    }

    @Override
    public int onlineStatusColor() {
        return 0;
    }

    @Override
    public int unreadEyeColor() {
        return 0xff9F8274;
    }

    @Override
    public int light() {
        return 0xffCFD8DC;
    }
}
