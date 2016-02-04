package chitchat;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import com.tisad.chitchat2.R;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Cells.BaseCell;

/**
 * Created by RaminBT on 14/01/2016.
 */
public class OnlineDrawableHelper {
    public static final GradientDrawable onlineDrawable;

    static {
        onlineDrawable=new GradientDrawable();
        onlineDrawable.setColor(Color.GREEN);
        onlineDrawable.setCornerRadius(AndroidUtilities.dp(16.0F));
        onlineDrawable.setStroke(AndroidUtilities.dp(2.0F), Color.parseColor("#0c5776"));
    }


    public static boolean isOnline(TLRPC.User user){
        return LocaleController.getString("Online", R.string.Online).equals(LocaleController.formatUserStatus(user));
    }


    public static void draw(BaseCell cell, Canvas canvas, int avatarTop, int avatarLeft){
        cell.setDrawableBounds(onlineDrawable, avatarLeft + AndroidUtilities.dp(4f), avatarTop + AndroidUtilities.dp(38f), AndroidUtilities.dp(16f), AndroidUtilities.dp(16f));
        onlineDrawable.draw(canvas);
    }

}
