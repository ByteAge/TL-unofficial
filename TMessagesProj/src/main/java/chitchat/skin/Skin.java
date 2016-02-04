package chitchat.skin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.TextView;

import chitchat.Log;
import chitchat.Utils;

public abstract class Skin {

    abstract public int id();
    abstract public int actionbarColor();
    abstract public int drawerNamesColor();
    abstract public int onlineStatusColor();
    abstract public int unreadEyeColor();
    abstract public int light();


    public int themePicture(){return 0;}
    public int actionPicture(){return 0;}
    public int actionSmallPicture(){return 0;}
    public int tileImage(){return 0;}

    public boolean hideDrawerProfile(){
        return false;
    }
    public String drawerProfileTitle(){return null;}
    public String drawerProfileSubtitle(){return null;}


    public void setTileOrColor(View v, Context a, int color){
        int res=tileImage();
        if(res==0){
            v.setBackgroundColor(color);
            return;
        }
        BitmapFactory.Options opts=new BitmapFactory.Options();
        opts.outHeight=v.getLayoutParams().height;
        Bitmap img= BitmapFactory.decodeResource(a.getResources(), res, opts);
        if(img==null){
            v.setBackgroundColor(color);
            return;
        }
        BitmapDrawable d=new BitmapDrawable(a.getResources(), img);
        d.setTileModeX(Shader.TileMode.REPEAT);
        d.setTileModeY(Shader.TileMode.REPEAT);
        Utils.setBackground(v, d);
    }


    public boolean setToLight(){return true;}
    public void setTextColorBigAction(TextView a, int defColor){
        int color=setToLight() ? light() : onlineStatusColor();
        if(color==0)
            color=defColor;
        a.setTextColor(color);

        if(!setToLight())
            a.setBackgroundColor(0xffffffff);
    }


}
