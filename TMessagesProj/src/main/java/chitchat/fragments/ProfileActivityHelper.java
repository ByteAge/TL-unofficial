package chitchat.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tisad.chitchat2.BuildConfig;
import com.tisad.chitchat2.R;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;

import chitchat.ChitSettings;
import chitchat.Log;

/**
 *
 * adds inviter button on group pages
 *  directs to user who invited this user to group
 * Created by RaminBT on 16/01/2016.
 */
public class ProfileActivityHelper {

    public static void createInviterMenu(BaseFragment fragment, ActionBarMenuItem item, TLRPC.Chat chat, TLRPC.ChatFull chatFull, ArrayList<TLRPC.ChannelParticipant> participants){
        if(!ChitSettings.showInviterInGroup || chat.left || chat.creator || chat.kicked || chat.restricted){
            Log.d("cant create menu");
            return;
        }

        int myId= UserConfig.getClientUserId();

        if(participants!=null && participants.size()>0){
            if(BuildConfig.DEBUG)
            Log.d("size: " + participants.size());
            for(TLRPC.ChannelParticipant participant:participants){
                if(participant instanceof TLRPC.TL_channelParticipantSelf || participant.user_id==myId){
                    if(BuildConfig.DEBUG){

                        Log.d("foound us " + myId);
                        Log.d("inviter " + participant.inviter_id);
                    }
                    if(participant.inviter_id > 0 && participant.inviter_id!=myId){
                        setOnClickListener(fragment,
                                item.addSubItem(44, LocaleController.getString("inv", R.string.Inviter), 0), participant.inviter_id);
                    }
                    return;
                }
            }
            if(BuildConfig.DEBUG)
            Log.d("cant find us");
        }else if(BuildConfig.DEBUG) Log.d("participant empy " + participants);

        if(chatFull!=null && chatFull.participants!=null && chatFull.participants.participants.size() > 0){
            if(BuildConfig.DEBUG)
            Log.d("size: " + chatFull.participants.participants.size());
            for(TLRPC.ChatParticipant participant : chatFull.participants.participants){
                if(participant.user_id==myId){
                    if(BuildConfig.DEBUG){

                        Log.d("foound us " + myId);
                        Log.d("inviter " + participant.inviter_id);
                    }
                    if(participant.inviter_id > 0 && participant.inviter_id!=myId){
                        setOnClickListener(fragment,
                                item.addSubItem(44, LocaleController.getString("inv", R.string.Inviter), 0), participant.inviter_id);
                    }
                }
            }
            if(BuildConfig.DEBUG)
            Log.d("cant find us2");
        }else Log.d("info empty " + chatFull);


    }

    public static void setOnClickListener(final BaseFragment fragment, TextView tv, final int id){
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt("user_id", id);
                fragment.presentFragment(new ProfileActivity(args));
            }
        });
    }

}
