package chitchat.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tisad.chitchat2.R;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import java.util.List;

import chitchat.ChitSettings;
import chitchat.NightModeUtil;
import chitchat.Utils;
import chitchat.skin.Skin;
import chitchat.skin.SkinMan;

/**
 * Created by RaminBT on 22/01/2016.
 */
public class ThemesFragment extends BaseFragment implements View.OnClickListener {

    private  FrameLayout selected;

    @Override
    public View createView(Context context) {

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                finishFragment();
            }
        });
        actionBar.setTitle(LocaleController.getString("Themes", R.string.Themes));

        ScrollView sv=new ScrollView(context);
        LinearLayout a=new LinearLayout(context);
        sv.addView(a, -1, -2);
        a.setOrientation(LinearLayout.VERTICAL);

        // padding
        a.addView(new View(context), -1, 15);

        List<Skin> skins= SkinMan.getAllSkins();
        int height= AndroidUtilities.dp(72);
        for(Skin skin:skins){
            String name=skin.getClass().getSimpleName().replace("Skin", "");
            int id=skin.id();

            // skinView
            FrameLayout skinV=new FrameLayout(context);
            int pic=skin.themePicture();
            if(pic==0)
                skinV.setBackgroundColor(skin.actionbarColor());
            else Utils.setBackground(skinV, ContextCompat.getDrawable(context, pic));
            a.addView(skinV, -1, height+(pic==0 ? 0 : height / 4));
            skinV.setTag(id);

            //draw name
            TextView t=new TextView(context);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setTextSize(20f);
            t.setTextColor(Color.WHITE);
            t.setText(name);
            if(pic==0)
            skinV.addView(t, LayoutHelper.createFrame(-2,-2, Gravity.RIGHT|Gravity.BOTTOM, 0,0,20,5));

            if(id==SkinMan.currentSkin.id()){
                selected=skinV;
                // this is default skin
                View sel=new View(context);
                sel.setBackgroundColor(Color.GREEN);
                skinV.addView(sel, LayoutHelper.createFrame(40,-1, Gravity.LEFT, 24,0,0,0));
            }

            skinV.setOnClickListener(this);
            // paddingView
            a.addView(new View(context), -1, 15);
        }

        fragmentView=sv;
        NightModeUtil.dark(fragmentView);
        return fragmentView;
    }

    @Override
    public void onClick(View v) {
        FrameLayout layout= (FrameLayout) v;
        int id= (int) layout.getTag();
        if(id==SkinMan.currentSkin.id())
            return;

        ChitSettings.changeSkin(id);
        if(Build.VERSION.SDK_INT >= 11)
            getParentActivity().recreate();
        else {
            getParentActivity().finish();
            Intent d=new Intent(getParentActivity(), LaunchActivity.class);
            getParentActivity().startActivity(d);
        }
    }
}
