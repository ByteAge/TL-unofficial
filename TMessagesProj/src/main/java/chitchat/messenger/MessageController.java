package chitchat.messenger;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

/**
 *
 *
 * Created by RaminBT on 15/01/2016.
 */
public class MessageController {
    public ArrayList<TLRPC.Dialog> channels = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> contacts = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> bots = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> unreads = new ArrayList<>();
    public ArrayList<TLRPC.Dialog> toporlar = new ArrayList<>();



    public int getUnreadCount(){
        int unread=0;
        for(TLRPC.Dialog d:unreads)
            unread+=d.unread_count;
        return unread;
    }
    public void modifyUnread(TLRPC.Dialog dialog){
        if(dialog.unread_count<1){
            unreads.remove(dialog);
        }else if(!unreads.contains(dialog)){
            unreads.add(dialog);
        }
    }
    public void init(){
        bots.clear();
        channels.clear();
        contacts.clear();
        unreads.clear();
        toporlar.clear();
    }
    public void remove2(TLRPC.Dialog d){
        toporlar.remove(d);
        unreads.remove(d);
    }
    public void remove(TLRPC.Dialog d){
        toporlar.remove(d);
        unreads.remove(d);
    }
    public void removeFromAll(TLRPC.Dialog d){
        toporlar.remove(d);
        channels.remove(d);
        contacts.remove(d);
        bots.remove(d);
        unreads.remove(d);
    }


    public void processUnread(TLRPC.Dialog d){

        if(d.unread_count>0)
            unreads.add(d);
    }
    public void addGroup(TLRPC.Dialog d){
        toporlar.add(d);
    }
    public void processChat(TLRPC.Dialog d, TLRPC.Chat chat){
        if(chat!=null){
            if(!chat.megagroup && ChatObject.isChannel(chat)){
                channels.add(d);
            }else{
                toporlar.add(d);
            }
        }
    }
    public void processEtc(TLRPC.Dialog d, org.telegram.messenger.MessagesController c){
        TLRPC.User user=c.getUser((int)d.id);
        if(user!=null && user.bot){
            bots.add(d);
        }else if(user!=null){
            contacts.add(d);
        }
    }





}
