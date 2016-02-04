package chitchat.skin;

/**
 * Created by RaminBT on 22/01/2016.
 */
public class PurpleSkin extends Skin {
    @Override
    public int id() {
        return 5;
    }

    @Override
    public int actionbarColor() {
        return 0xff512DA8;
    }

    @Override
    public int drawerNamesColor() {
        return 0xff412485;
    }

    @Override
    public int onlineStatusColor() {
        return 0;
    }

    @Override
    public int unreadEyeColor() {
        return 0xffAED257;
    }

    @Override
    public int light() {
        return 0xffE1BEE7;
    }
}
