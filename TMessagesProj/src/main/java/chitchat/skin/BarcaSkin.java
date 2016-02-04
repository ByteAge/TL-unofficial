package chitchat.skin;

import com.tisad.chitchat2.R;

/**
 * Created by RaminBT on 26/01/2016.
 */
public class BarcaSkin extends Skin {
    @Override
    public int id() {
        return 22;
    }

    @Override
    public int actionbarColor() {
        return 0xff2C82CD;
    }

    @Override
    public int drawerNamesColor() {
        return 0xff1B5C94;
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
        return 0xffFFffff;
    }


    @Override
    public int themePicture() {
        return R.drawable.barca_theme;
    }

    @Override
    public int actionPicture() {
        return R.drawable.barca_2;
    }

    @Override
    public int tileImage() {
        return R.drawable.barca_tile;
    }

    @Override
    public int actionSmallPicture() {
        return R.drawable.barca_4;
    }


    @Override
    public boolean hideDrawerProfile() {
        return true;
    }

    @Override
    public String drawerProfileTitle() {
        return "Barcelona";
    }

    @Override
    public String drawerProfileSubtitle() {
        return "Barcelona, Catalonia, Spain";
    }
}
