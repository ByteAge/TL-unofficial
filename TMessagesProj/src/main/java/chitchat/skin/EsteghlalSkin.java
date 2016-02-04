package chitchat.skin;

import com.tisad.chitchat2.R;

/**
 * Created by RaminBT on 26/01/2016.
 */
public class EsteghlalSkin extends Skin {
    @Override
    public int id() {
        return 20;
    }

    @Override
    public int actionbarColor() {
        return 0xff006FB4;
    }

    @Override
    public int drawerNamesColor() {
        return 0xff014D7C;
    }

    @Override
    public int onlineStatusColor() {
        return actionbarColor();
    }

    @Override
    public int unreadEyeColor() {
        return 0xffFFC107;
    }

    @Override
    public int light() {
        return 0xff000000;
    }

    @Override
    public int themePicture() {
        return R.drawable.esteghlal_theme;
    }

    @Override
    public int actionPicture() {
        return R.drawable.esteghlal_2;
    }

    @Override
    public int actionSmallPicture() {
        return R.drawable.esteghlal_3;
    }

    @Override
    public int tileImage() {
        return R.drawable.blue_tile;
    }

    @Override
    public boolean hideDrawerProfile() {
        return true;
    }

    @Override
    public String drawerProfileTitle() {
        return "استقلال تهران";
    }

    @Override
    public String drawerProfileSubtitle() {
        return "ایران, تهران";
    }


    @Override
    public boolean setToLight() {
        return false;
    }
}
