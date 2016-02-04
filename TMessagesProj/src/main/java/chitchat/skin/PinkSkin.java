package chitchat.skin;

/**
 * Created by RaminBT on 22/01/2016.
 */
public class PinkSkin extends Skin {
    @Override
    public int id() {
        return 1;
    }

    @Override
    public int actionbarColor() {
        return 0xffE91E63;
    }

    @Override
    public int drawerNamesColor() {
        return 0xffC2185B;
    }

    @Override
    public int onlineStatusColor() {
        return 0;
    }

    @Override
    public int unreadEyeColor() {
        return 0xff16E19C;
    }

    @Override
    public int light() {
        return 0xffF8BBD0;
    }
}
