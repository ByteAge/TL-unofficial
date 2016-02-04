package chitchat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.DialogsActivity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import chitchat.fragments.ChatActivityFantasy;
import chitchat.fragments.DialogActivityH;
import chitchat.messenger.GhostHelper;
import chitchat.skin.SkinMan;

/**
 *
 * Created by RaminBT on 16/01/2016.
 */
public class ChitSettings {

    public static final String WallpaperFileName="LaunchActivity.png";
    public static Drawable cachedWallpaper;
    public static final int defaultBackGround = 1000001;
    public static final String showTabKey="showTabs", showInviterKey="showInv", confirmStickerKey="cskey"
            , overRideLedColorKey="orled", channelJoinShownKey="cjshown", fantasyActivityKey="showFantasy"
            ,showMutualKey = "showMut", defTabKey="defaultTab", lastSelectedTabKey="lst", dialogBackKey="dbgimg", dialogColorKey="dcolor";



    private static Context context;
    public static boolean showTabs, showInviterInGroup, showMutualDot, GhostModeEnabled, showFantasy,
            hideTypingState, confirmBeforeSendingSticker, overRideLedColor=false, channelJoinShown=false;
    public static int defaultTab, lastSelectedTab, selectedWallpaper, selectedColor;
    private static SharedPreferences preferences;



    public static void init(Context context){
        ChitSettings.context=context;
        preferences=context.getSharedPreferences("mainconfig", Context.MODE_PRIVATE);

        GhostHelper.init(preferences);
        SkinMan.config(preferences);
        NightModeUtil.config(context, preferences);

        GhostModeEnabled=GhostHelper.isGhostEnabled();
        hideTypingState =GhostHelper.isHideTyping();

        confirmBeforeSendingSticker=getTBoolean(confirmStickerKey);
        showTabs=getTBoolean(showTabKey);
        showInviterInGroup=getTBoolean(showInviterKey);
        showMutualDot=getTBoolean(showMutualKey);
        defaultTab=preferences.getInt(defTabKey, DialogActivityH.globalDefaultSelectedTab);
        lastSelectedTab=preferences.getInt(lastSelectedTabKey, -1);

        showFantasy=getTBoolean(fantasyActivityKey);

        channelJoinShown=getBoolean(channelJoinShownKey);

        selectedWallpaper=preferences.getInt(dialogBackKey, defaultBackGround);
        selectedColor=preferences.getInt(dialogColorKey, 0);
        overRideLedColor=getBoolean(overRideLedColorKey);

        loadWallpaper();
    }


    public static void nightModeChanged(){
        //SkinMan.nightModeChanged(preferences);
    }


    public static void setJoinShowed(boolean shown){
        preferences.edit().putBoolean(channelJoinShownKey, channelJoinShown).apply();
        channelJoinShown=shown;
    }

    public static void reloadWallpaper() {
        cachedWallpaper = null;
        loadWallpaper();
    }

    public static void clearWallpaper(){
        preferences.edit().putInt(dialogBackKey, 1000001).apply();
        selectedWallpaper=1000001;
        reloadWallpaper();
    }
    public static void updateFragments(){
        List<WeakReference<DialogsActivity>>
                acs=DialogsActivity.getActivities();
        for(WeakReference<DialogsActivity> a:acs){
            DialogsActivity d=a.get();
            if(d==null)
                return;
            Utils.setBackground(d.getFragmentView(), cachedWallpaper);
        }
    }

    public static boolean hasWallpaper(){
        return cachedWallpaper!=null && selectedWallpaper!=1000001;
    }
    public static void loadWallpaper() {
        if (cachedWallpaper != null) {
            return;
        }

        Utilities.searchQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                synchronized (ApplicationLoader.sync) {
                    try {
                        if (selectedColor == 0) {
                            if (selectedWallpaper != 1000001) {
                                File toFile = new File(((ApplicationLoader) context).getFilesDirFixed(), WallpaperFileName);
                                if (toFile.exists()) {
                                    cachedWallpaper = Drawable.createFromPath(toFile.getAbsolutePath());
                                } else {
                                    // cachedWallpaper = context.getResources().getDrawable(R.drawable.background_hd);
                                }
                            }
                        }
                    } catch (Throwable throwable) {
                        //ignore
                    }
                    if (cachedWallpaper == null) {
                        if (selectedColor == 0) {
                            selectedColor = Color.WHITE;
                        }
                        cachedWallpaper = new ColorDrawable(selectedColor);
                    }

                    updateFragments();
                }
            }
        });

    }


    public static boolean changeSkin(int id){
        return SkinMan.changeSkin(preferences, id);
    }
    public static boolean changeShowTabs(){
        if(changeItem(showTabKey, !showTabs))
            showTabs=!showTabs;
        return showTabs;
    }
    public static boolean changeShowInviter(){
        if(changeItem(showInviterKey, !showInviterInGroup))
            showInviterInGroup=!showInviterInGroup;
        return showInviterInGroup;
    }
    public static boolean changeShowMutualDot(){
        if(changeItem(showMutualKey, !showMutualDot))
            showMutualDot=!showMutualDot;
        return showMutualDot;
    }
    public static boolean changeFantasy(){
        if(changeItem(fantasyActivityKey, !showFantasy))
            showFantasy=!showFantasy;
        return showFantasy;
    }

    public static ChatActivity instance(Bundle args){
        return showFantasy ? new ChatActivityFantasy(args) : new ChatActivity(args);
    }

    /**
     *
     * @return night enabled
     */
    public static boolean changeNightMode(boolean enabled){
        return NightModeUtil.change(preferences, enabled);
    }
    public static void changeNightMode(int hour, int minute, boolean from){
        NightModeUtil.changeHourMinute(preferences, hour, minute, from);
    }

    public static boolean changeOverrideLed(){
        if(changeItem(overRideLedColorKey, !overRideLedColor))
            overRideLedColor=!overRideLedColor;
        return overRideLedColor;
    }

    public static int ledColor(int defaultColor){
        return overRideLedColor ? SkinMan.currentSkin.actionbarColor() : defaultColor;
    }

    public static ColorUtils.ColorPresenter currentColorPresenter(){
        return showFantasy ? ColorUtils.fantasyColorPresenter : ColorUtils.defaultColorPresenter;
    }

    public static boolean changeConfirmStickerSending(){
        if(changeItem(confirmStickerKey, !confirmBeforeSendingSticker))
            confirmBeforeSendingSticker=!confirmBeforeSendingSticker;
        return confirmBeforeSendingSticker;
    }
    public static boolean changeGhost(){
        return GhostHelper.changeGhost();
    }
    public static boolean changeTypingState(){
        return GhostHelper.changeTypingState();
    }

    public static void setLastSelectedTab(int tab){
        preferences.edit().putInt(lastSelectedTabKey, tab).apply();
        lastSelectedTab=tab;
    }

    public static void setDefaultTab(int defaultTab) {
        preferences.edit().putInt(defTabKey, defaultTab).apply();
        ChitSettings.defaultTab = defaultTab;
    }
    public static void setSelecteBG(int bg, int color) {
        preferences.edit().putInt(dialogBackKey, bg)
                .putInt(dialogColorKey, color)
                .commit();
        selectedWallpaper=bg;
        selectedColor=color;
    }

    public static boolean changeItem(String key, boolean value){
        return preferences.edit().putBoolean(key, value).commit();
    }
    public static boolean getBoolean(String key){
        return preferences.getBoolean(key, false);
    }
    public static boolean getTBoolean(String key){
        return preferences.getBoolean(key, true);
    }
}
