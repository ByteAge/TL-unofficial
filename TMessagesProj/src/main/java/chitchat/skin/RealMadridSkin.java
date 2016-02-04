package chitchat.skin;

import com.tisad.chitchat2.R;

/**
 * Created by RaminBT on 26/01/2016.
 */
public class RealMadridSkin extends Skin {
    @Override
    public int id() {
        return 23;
    }

    @Override
    public int actionbarColor() {
        return 0xffB5962E;
    }

    @Override
    public int drawerNamesColor() {
        return 0xff303030;
    }

    @Override
    public int onlineStatusColor() {
        return actionbarColor();
    }

    @Override
    public int unreadEyeColor() {
        return 0xff85BBF1;
    }

    @Override
    public int light() {
        return 0xff137FAC;
    }

    @Override
    public boolean setToLight() {
        return false;
    }

    @Override
    public int themePicture() {
        return R.drawable.real_theme;
    }

    @Override
    public int actionPicture() {
        return R.drawable.real_3;
    }

    @Override
    public int actionSmallPicture() {
        return R.drawable.real_2;
    }

    @Override
    public int tileImage() {
        return R.drawable.dark_tile;
    }


    @Override
    public boolean hideDrawerProfile() {
        return true;
    }

    @Override
    public String drawerProfileTitle() {
        return "Real Madrid";
    }

    @Override
    public String drawerProfileSubtitle() {
        return "Madrid, Spain";
    }
}
