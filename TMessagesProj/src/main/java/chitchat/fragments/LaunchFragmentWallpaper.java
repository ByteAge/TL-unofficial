package chitchat.fragments;

import android.content.Context;
import android.view.View;

import com.tisad.chitchat2.R;

import org.telegram.messenger.LocaleController;
import org.telegram.ui.WallpapersActivity;

import chitchat.ChitSettings;

/**
 * changes default background of dialogs activity
 * Created by RaminBT on 16/01/2016.
 */
public class LaunchFragmentWallpaper extends WallpapersActivity {


    @Override
    public void saveToPreferences() {
        ChitSettings.setSelecteBG(selectedBackground, selectedColor);
        ChitSettings.reloadWallpaper();
    }

    @Override
    public View createView(Context context) {
        super.createView(context);
        actionBar.setTitle(LocaleController.getString("DialogWallpaper", R.string.DialogWallpaper));
        return fragmentView;
    }

    @Override
    public int getSelectedBackground() {
        return ChitSettings.selectedWallpaper;
    }

    @Override
    public int getSelectedColor() {
        return ChitSettings.selectedColor;
    }

    @Override
    public String getTargetFileName() {
        return ChitSettings.WallpaperFileName;
    }
}
