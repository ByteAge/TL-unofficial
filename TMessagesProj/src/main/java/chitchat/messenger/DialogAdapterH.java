package chitchat.messenger;

import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

/**
 * Created by RaminBT on 15/01/2016.
 */
public class DialogAdapterH {
    public static final int
            allDialogs=0,
            serversOnly=1,
            groupsOnly=2,

            channels=3,
            bots=4,
            contacts=5,
            unreads=6,
            toporlar=7
    ;

    public static ArrayList<TLRPC.Dialog> getDialogsArray(int type){
        if (type == allDialogs) {
            return MessagesController.getInstance().dialogs;
        } else if (type == serversOnly) {
            return MessagesController.getInstance().dialogsServerOnly;
        } else if (type == groupsOnly) {
            return MessagesController.getInstance().dialogsGroupsOnly;
        } else if (type == channels) {
            return MessagesController.getInstance().controller.channels;
        } else if (type == bots) {
            return MessagesController.getInstance().controller.bots;
        } else if (type == contacts) {
            return MessagesController.getInstance().controller.contacts;
        } else if (type == unreads) {
            return MessagesController.getInstance().controller.unreads;
        } else if (type == toporlar) {
            return MessagesController.getInstance().controller.toporlar;
        }



        return null;
    }

    public static boolean canHaveMore(int type){
        return type == allDialogs;
    }

}
