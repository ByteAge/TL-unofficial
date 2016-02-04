package chitchat.skin;

/**
 * Created by RaminBT on 24/01/2016.
 */
public class BrownSkin extends Skin{
    @Override
    public int id() {
        return 8;
    }

    @Override
    public int actionbarColor() {
        return 0xff795548;
    }

    @Override
    public int drawerNamesColor() {
        return 0xff5D4037;
    }

    @Override
    public int onlineStatusColor() {
        return 0;
    }

    @Override
    public int unreadEyeColor() {
        return 0xff86AAB7;
    }

    @Override
    public int light() {
        return 0xffD7CCC8;
    }
}
