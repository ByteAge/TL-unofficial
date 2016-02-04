package chitchat.skin;

/**
 * Created by RaminBT on 22/01/2016.
 */
public class IndigoSkin extends Skin {
    @Override
    public int id() {
        return 2;
    }

    @Override
    public int actionbarColor() {
        return 0xff3F51B5;
    }

    @Override
    public int drawerNamesColor() {
        return 0xff303F9F;
    }

    @Override
    public int onlineStatusColor() {
        return 0;
    }

    @Override
    public int unreadEyeColor() {
        return 0xffC0AE4A;
    }

    @Override
    public int light() {
        return 0xffC5CAE9;
    }
}
