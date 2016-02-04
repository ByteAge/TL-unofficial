package chitchat.messenger;

import android.content.SharedPreferences;

import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

/**
 * handles ghost mode
 * Created by RaminBT on 16/01/2016.
 */
public class GhostHelper {

    private static final String ghostKey="ruh", hideTypingKey="yazyar";

    private static SharedPreferences preferences;
    private static boolean
            ghostEnabled=false,
            hideTyping=false;

    public static void init(SharedPreferences prefs){
        preferences=prefs;
        ghostEnabled=preferences.getBoolean(ghostKey, false);
        hideTyping=preferences.getBoolean(hideTypingKey, false);
    }

    public static boolean filter(TLObject object){
        return // ghost
                (ghostEnabled && ((object instanceof TLRPC.TL_messages_readHistory&&!(((TLRPC.TL_messages_readHistory) object).peer instanceof TLRPC.TL_inputPeerChannel)) || object instanceof TLRPC.TL_channels_readHistory))
               ;//// typing
               // || (hideTyping && object instanceof TLRPC.TL_geochats_setTyping);
    }


    public static boolean isGhostEnabled() {
        return ghostEnabled;
    }

    public static boolean isHideTyping() {
        return hideTyping;
    }



    public static boolean changeGhost(){
        if(changeItem(ghostKey, !ghostEnabled)){
            ghostEnabled=!ghostEnabled;
            MessagesController.getInstance().resetTimerProc();
        }
        return ghostEnabled;
    }
    public static boolean changeTypingState(){
        if(changeItem(hideTypingKey, !hideTyping))
            hideTyping=preferences.getBoolean(hideTypingKey, !hideTyping);
        return hideTyping;
    }
    public static boolean changeItem(String key, boolean value){
        return preferences.edit().putBoolean(key, value).commit();
    }
}
