package chitchat;

import static org.telegram.ui.Components.ResourceLoader.*;
import static chitchat.ChatBaseColors.*;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tisad.chitchat2.R;

import org.telegram.ui.Components.ResourceLoader;

/**
 *
 * Created by RaminBT on 13/01/2016.
 */
public class ColorUtils {

    public static ColorPresenter defaultColorPresenter,
                fantasyColorPresenter;

    public static Context context;
    public static void init(Context context){
        ColorUtils.context=context;

        defaultColorPresenter=new ColorPresenter();
        fantasyColorPresenter=new FantasyColorPresenter();
    }



    public static class ColorPresenter{
        public int
                // forwards color
                 forwardInColor=0xff006fc8 ,forwardOutColor=0xff4A923C,
        // reply colors
        replySticker=0xffffffff, replyOut=0xff61A349, replyIn=0xff377aae,
                replyLineSticker=0xffffffff, replyLineIn=0xff6c9fd2, replyLineOut=0xff8dc97a,
                replyTextSticker=0xffffffff, replyTextIn=0xff999999, replyTextOut=0xff70b15c,
        // time colora1aab3
        timeIn=0xffa1aab3, timeInSelected=0xff89b4c1, timeOut=0xff70b15c,
        //media back
        docBack=0xffebf0f5, docBackSelected=0xffcbeaf6, docBackOut=0xffdaf5c3, docBackOutSelected=0xffc5eca7,
        infoBack=0xffa1aab3,infoBackSelected=0xff89b4c1, infoOut=0xff70b15c,
                audioTimeOut=0xff70b15c, audioTimeIn=0xffa1aab3, audioTimeInSelected=0xff89b4c1;

        public Drawable checkD= checkDrawable,
                halfCheckD=halfCheckDrawable,
                        msgIn=backgroundDrawableIn, msgInSelected=backgroundDrawableInSelected, msgOut=backgroundDrawableOut,
        msgOutSelected=backgroundDrawableOutSelected, clock=clockDrawable, broadcast=broadcastDrawable, viewsOut=viewsOutCountDrawable
                , viewsIn=viewsCountDrawable,
        mediaInSelected=backgroundMediaDrawableInSelected, mediaOutSelected=backgroundMediaDrawableOutSelected,
        mediaIn=backgroundMediaDrawableIn, mediaOut=backgroundMediaDrawableOut;

        public Drawable[] docMenuDrawable=ResourceLoader.docMenuDrawable;


        public void modifyUnreadBg(FrameLayout frameLayout, TextView tv){
            frameLayout.setBackgroundResource(R.drawable.newmsg_divider);
            tv.setTextColor(0xff4a7297);
        }

    }

    public static class FantasyColorPresenter extends ColorPresenter{
        public FantasyColorPresenter() {
            forwardInColor=forwardInColorF;
            forwardOutColor=forwardOutColorF;

            replyOut=replyOutF;
            replyIn=replyInF;
            replyLineIn=replyLineInF;

            replyLineOut=replyLineOutF;
            replyTextIn=replyTextInF;
            replyTextOut=timeOutF;
            timeIn=timeInF;
            timeInSelected=timeInSelectedF;
            timeOut=timeOutF;

            checkD=checkDrawableF;
            halfCheckD=halfCheckDrawableF;
            msgIn=backgroundDrawableInF;
            msgInSelected=backgroundDrawableInSelectedF;
            msgOut=backgroundDrawableOutF;
            msgOutSelected=backgroundDrawableOutSelectedF;
            clock=clockDrawableF;
            broadcast=broadcastDrawableF;
            viewsIn=viewsCountDrawableF;
            viewsOut=viewsOutCountDrawableF;

            infoOut=0xff569DA3;
            docBack=0xffFFE681;
            docBackSelected=0xffFFE26A;
            docBackOut=0xffB5EDF3;
            infoBack=timeIn;
            infoBackSelected=timeInSelected;

            audioTimeOut=timeOut;
            audioTimeIn=timeInF;
            audioTimeInSelected=timeInSelectedF;

            docMenuDrawable=docMenuDrawableF;


            mediaInSelected=backgroundMediaDrawableInSelectedF;
            mediaOutSelected=backgroundMediaDrawableOutSelectedF;
            mediaIn=backgroundMediaDrawableInF;
            mediaOut=backgroundMediaDrawableOutF;

        }

        @Override
        public void modifyUnreadBg(FrameLayout frameLayout, TextView tv){
            frameLayout.setBackgroundColor(0xffD2EFF2);
            tv.setTextColor(0xff4a7297);
        }
    }


}
