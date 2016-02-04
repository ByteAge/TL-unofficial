package chitchat.skin;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.Components.AvatarDrawable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import chitchat.NightModeUtil;

/**
 * Created by RaminBT on 22/01/2016.
 */
public class SkinMan {

    public static final int defaultSkinId=0;
    public static Skin currentSkin;

    public static void config(SharedPreferences preferences){
        updateSkin(preferences);
    }

    /**
     *
     * @param preferences
     * @param skinId the id of skin
     * @return true if success
     */
    public static boolean changeSkin(SharedPreferences preferences, int skinId){
        if(preferences.edit().putInt("SkinId", skinId).commit()){
            updateSkin(preferences);
            return true;
        }
        return false;
    }

    public static int barSelector(Context context){
        String name=currentSkin.getClass().getSimpleName().replace("Skin", "").toLowerCase(Locale.US);
        return context.getResources().getIdentifier("bar_selector_"+name, "drawable", context.getPackageName());
    }


   /* public static void nightModeChanged(SharedPreferences preferences){
        /*boolean enabled= NightModeUtil.nightEnabled;
        if(enabled){
            currentSkin=new NightSkin();

            AvatarDrawable.changeIndexColor(5, currentSkin.actionbarColor());
            AvatarDrawable.changeIndexBarSelector(5, barSelector(ApplicationLoader.applicationContext));
        }else {
            updateSkin(preferences);
        }
    }*/


    private static void updateSkin(SharedPreferences preferences){
        currentSkin=null;
        int id=preferences.getInt("SkinId", defaultSkinId);
        if(id!=0)
            for(Skin skin:getAllSkins()){
                if(id==skin.id()){
                    currentSkin=skin;
                    break;
                }
            }
        if(currentSkin==null)
            currentSkin=new ChitChatSkin();

        AvatarDrawable.changeIndexColor(5, currentSkin.actionbarColor());
        AvatarDrawable.changeIndexBarSelector(5, barSelector(ApplicationLoader.applicationContext));
    }
    public static List<Skin> getAllSkins(){
        ArrayList<Skin> skins=new ArrayList<>();
        skins.add(new ChitChatSkin());
        skins.add(new PinkSkin());
        skins.add(new DeepPurpleSkin());
        skins.add(new PurpleSkin());
        skins.add(new OrangeSkin());
        //skins.add(new RealSkin());
        skins.add(new IndigoSkin());
        skins.add(new BrownSkin());
        //9
        skins.add(new DeepOrangeSkin());
        skins.add(new BlueGraySkin());
        skins.add(new CyanSkin());
        skins.add(new TealSkin());
        skins.add(new GraySkin());
        skins.add(new NightSkin());
        //20
        skins.add(new EsteghlalSkin());
        skins.add(new PerspolisSkin());
        skins.add(new BarcaSkin());
        skins.add(new RealMadridSkin());

        return skins;
    }

}
