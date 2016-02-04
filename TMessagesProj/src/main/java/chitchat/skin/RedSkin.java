package chitchat.skin;

/**
 * Created by RaminBT on 24/01/2016.
 */
public class RedSkin extends Skin {
    @Override
    public int id() {
        return 0;
    }

    @Override
    public int actionbarColor() {
        return 0xffF44336;
    }

    @Override
    public int drawerNamesColor() {
        return 0xffD32F2F;
    }

    @Override
    public int onlineStatusColor() {
        return 0;
    }

    @Override
    public int unreadEyeColor() {
        return 0xff0BBCC9;
    }

    @Override
    public int light() {
        return 0xffFFCDD2;
    }
}
