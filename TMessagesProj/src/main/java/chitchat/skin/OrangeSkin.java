package chitchat.skin;

/**
 * Created by RaminBT on 22/01/2016.
 */
public class OrangeSkin extends Skin {
    @Override
    public int id() {
        return 3;
    }

    @Override
    public int actionbarColor() {
        return 0xffFF9800;
    }

    @Override
    public int drawerNamesColor() {
        return 0xffF57C00;
    }

    @Override
    public int onlineStatusColor() {
        return 0;
    }

    @Override
    public int unreadEyeColor() {
        return 0xff0067FF;
    }

    @Override
    public int light() {
        return 0xffFFE0B2;
    }
}
