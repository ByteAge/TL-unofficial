package chitchat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.tisad.chitchat2.BuildConfig;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.ui.ActionBar.ActionBar;

import chitchat.skin.SkinMan;

/**
 * Created by RaminBT on 26/01/2016.
 */
public class ThemePicUtil {

    FrameLayout frameLayout;
    View themV;

    int vHeight, extraHeight;

    public ThemePicUtil(FrameLayout frameLayout, ActionBar ab, View extraView, Context context) {
        this.frameLayout = frameLayout;


        int pic= SkinMan.currentSkin.actionPicture();
        if(pic==0 || (ContextCompat.getDrawable(context, pic))==null)
            return;
        themV=new View(context){
            @Override
            protected void onDraw(Canvas canvas) {
                if(BuildConfig.DEBUG)
                Log.d("realH: " + getHeight());
                super.onDraw(canvas);
            }
        };
        this.extraHeight=AndroidUtilities.dp(88);
        vHeight=this.extraHeight+ ActionBar.getCurrentActionBarHeight();

        Bitmap decoded=BitmapFactory.decodeResource(context.getResources(), pic);
        Bitmap img=Bitmap.createScaledBitmap(decoded, decoded.getWidth(), vHeight, false);
        Utils.setBackground(themV, new BitmapDrawable(context.getResources(), img));
        FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(-1, vHeight, Gravity.TOP);
        frameLayout.addView(themV, 1, params);

        ab.setBackgroundColor(Color.TRANSPARENT);
        extraView.setBackgroundColor(Color.TRANSPARENT);
        Utils.setBackground(ab, null);
        Utils.setBackground(extraView, null);
    }

    public void progress(ActionBar ab, View extraView, int color){
        if(themV!=null)
            return;
        ab.setBackgroundColor(color);
        extraView.setBackgroundColor(color);
    }


    public void translate(int height){
        if(themV==null)
            return;

        extraHeight=height;
        ViewProxy.setTranslationY(themV, currentTranslation());
    }
    public int currentTranslation(){
        return - (vHeight - (extraHeight+ActionBar.getCurrentActionBarHeight()));//- AndroidUtilities.dp(29.5f);
    }
}
