package chitchat.fragments;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tisad.chitchat2.BuildConfig;
import com.tisad.chitchat2.R;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Animation.ObjectAnimator10;
import org.telegram.messenger.Animation.TypeEvaluator;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.ContactsActivity;
import org.telegram.ui.DialogsActivity;

import chitchat.ChitSettings;
import chitchat.Log;
import chitchat.skin.SkinMan;

import static chitchat.messenger.DialogAdapterH.allDialogs;
import static chitchat.messenger.DialogAdapterH.bots;
import static chitchat.messenger.DialogAdapterH.channels;
import static chitchat.messenger.DialogAdapterH.contacts;
import static chitchat.messenger.DialogAdapterH.toporlar;
import static chitchat.messenger.DialogAdapterH.unreads;

/**
 * shows tabs
 * switches bitween tab and floating button
 * Created by RaminBT on 15/01/2016.
 */
public class DialogActivityH implements View.OnClickListener {

    public static final int globalDefaultSelectedTab=allDialogs;

    public static final SparseIntArray tabNames, adapterIndex, adapterNames;
    static {
        tabNames=new SparseIntArray();
        tabNames.put(0, R.string.Unreads);
        tabNames.put(1, R.string.All);
        tabNames.put(2, R.string.Contacts);
        tabNames.put(3, R.string.Channels);
        tabNames.put(4, R.string.Groups);
        tabNames.put(5, R.string.Bot);

        adapterIndex=new SparseIntArray();
        adapterIndex.put(0, unreads);
        adapterIndex.put(1, allDialogs);
        adapterIndex.put(2, contacts);
        adapterIndex.put(3, channels);
        adapterIndex.put(4, toporlar);
        adapterIndex.put(5, bots);

        adapterNames=new SparseIntArray();
        adapterNames.put(unreads, R.string.Unreads);
        adapterNames.put(allDialogs, R.string.All);
        adapterNames.put(contacts, R.string.Contacts);
        adapterNames.put(channels, R.string.Channels);
        adapterNames.put(toporlar, R.string.Groups);
        adapterNames.put(bots, R.string.Bot);
    }


    public static String getTabName(int index){
        return LocaleController.getString("", adapterNames.get(index));
    }
    public static String getDefaultTabName(){
        return getTabName(ChitSettings.defaultTab);
    }





    public ImageView floatingButton;
    public DialogsActivity activity;
    public Context context;
    public View view;
    private View pressedView=null;
    private int desiredTab=allDialogs;
    private ImageView unreadIv;
    private int redColor=SkinMan.currentSkin.unreadEyeColor();
    private int lastUnreadCount=0;

    private RecyclerListView listView;
    private LinearLayoutManager manager;

    public DialogActivityH(DialogsActivity activity) {
        this.activity = activity;
    }
    public void createView(FrameLayout layout, Context context, boolean onlySelect, RecyclerListView listView, LinearLayoutManager manager){
        this.listView=listView;
        this.manager=manager;

        redColor=SkinMan.currentSkin.unreadEyeColor();

        boolean showTab=ChitSettings.showTabs;
        if(!showTab || onlySelect){
            floatingButton = new ImageView(context);
            floatingButton.setVisibility(onlySelect ? View.GONE : View.VISIBLE);
            floatingButton.setScaleType(ImageView.ScaleType.CENTER);
            floatingButton.setBackgroundResource(R.drawable.floating_states);
            floatingButton.setImageResource(R.drawable.floating_pencil);
            if (Build.VERSION.SDK_INT >= 21) {
                StateListAnimator animator = new StateListAnimator();
                animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
                animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
                floatingButton.setStateListAnimator(animator);
                floatingButton.setOutlineProvider(new ViewOutlineProvider() {
                    @SuppressLint("NewApi")
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                    }
                });
            }
            //:ramin
            layout.addView(floatingButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 14));
            floatingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putBoolean("destroyAfterSelect", true);
                    activity.presentFragment(new ContactsActivity(args));
                }
            });
            view=floatingButton;
            return;
        }

        if(ChitSettings.defaultTab!=-1)
            desiredTab=ChitSettings.defaultTab;

        LinearLayout inflated= (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_footer, null);
        view=inflated;
        inflated.setVisibility(onlySelect ? View.GONE : View.VISIBLE);

        layout.addView(inflated, LayoutHelper.createFrame(-1, -2, Gravity.BOTTOM));


        //inflated.setBackgroundColor(.actionbarColor());

        int deviceWidth=AndroidUtilities.displaySize.x;
        int childCount=inflated.getChildCount();
        int childWidth= ActionBar.getCurrentActionBarHeight();
        boolean changeWidth=(childCount*childWidth) > deviceWidth;

        if(changeWidth)
            childWidth=deviceWidth/childCount;

        inflated.getLayoutParams().height=childWidth;
        SkinMan.currentSkin.setTileOrColor(inflated, context, SkinMan.currentSkin.actionbarColor());

        int barId=SkinMan.barSelector(context);
        for(int i=0; i<inflated.getChildCount(); i++){
            View child= inflated.getChildAt(i);
            child.setOnClickListener(this);
            //if(changeWidth){
                child.getLayoutParams().width=childWidth;
                child.getLayoutParams().height=childWidth;
            //}

            child.setBackgroundResource(barId);

            if(i==5){
                unreadIv= (ImageView) child;
            }

            if(desiredTab==tagIndex((String) child.getTag())){
                child.setSelected(true);
                pressedView=child;
                changeAdapter(desiredTab);
            }
        }

        if(BuildConfig.DEBUG){
            Log.d("unread count " + MessagesController.getInstance().controller.getUnreadCount());
            Log.d("unread length " + MessagesController.getInstance().controller.unreads.size());
        }
        if(MessagesController.getInstance().controller.getUnreadCount()>0)
            animteTint(false);

        //layout.addView(menu, LayoutHelper.createFrame(-2, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 8, 0, 0, 8));
    }

    public void dismiss(){
        view.setVisibility(View.GONE);
    }
    public void show(){
        view.setVisibility(View.VISIBLE);
        activity.floatingHidden = true;
        ViewProxy.setTranslationY(view, AndroidUtilities.dp(100));
        hide(false);
    }
    public void onGlobalLayout(){
        if(view==null)
            return;
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewProxy.setTranslationY(view, activity.floatingHidden ? AndroidUtilities.dp(100) : 0);
                view.setClickable(!activity.floatingHidden);
                if (view != null) {
                    if (Build.VERSION.SDK_INT < 16) {
                        view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            }
        });
    }

    private long lastChangeAdapter=-1;
    public void hide(boolean hide){
        if (activity.floatingHidden == hide || (System.currentTimeMillis() - lastChangeAdapter) < 100) {
            return;
        }

        if(hide){
            int count=listView.getAdapter().getItemCount();
            if(manager.findFirstCompletelyVisibleItemPosition() == 0
                    && manager.findLastCompletelyVisibleItemPosition() >= count)
                return;
        }

        activity.floatingHidden = hide;
        ObjectAnimatorProxy animator = ObjectAnimatorProxy.ofFloatProxy(view, "translationY", activity.floatingHidden ? AndroidUtilities.dp(100) : 0).setDuration(300);
        animator.setInterpolator(activity.floatingInterpolator);
        view.setClickable(!hide);
        animator.start();
    }

    @Override
    public void onClick(View v) {
        String tag= (String) v.getTag();
        if(tag.equals("start")){
            start();
            return;
        }
        int type=tagIndex(tag);
        if(type==desiredTab)
            return;
        if(type!=-1){
            if(pressedView!=null)
                pressedView.setSelected(false);

            changeAdapter(type);
            pressedView=v;
            pressedView.setSelected(true);
            desiredTab=type;
            ChitSettings.setLastSelectedTab(type);
        }
    }


    private int tagIndex(String tag){
        return "bot".equals(tag) ? bots :
                "contact".equals(tag) ? contacts :
                        "topar".equals(tag) ? toporlar :
                                "unread".equals(tag) ? unreads :
                                        "channel".equals(tag) ? channels :
                                                "telegram".equals(tag) ? allDialogs :
                                                        -1;
    }
    private void start(){
        Bundle args = new Bundle();
        args.putBoolean("destroyAfterSelect", true);
        activity.presentFragment(new ContactsActivity(args));
    }

    /*@Override
    public void eventOccured(int id) {
        int type=id==R.drawable.menu_broadcast ? channels : id==R.drawable.menu_contacts ? contacts :
                id==R.drawable.menu_newgroup ? toporlar : bots;
        Log.d("selected menu " + type);
        //changeAdapter(generateAdapter(type));
        changeAdapter(type);
    }
    public DialogsAdapter generateAdapter(int type){
        return new DialogsAdapter(context, type);
    }
    public void changeAdapter(DialogsAdapter adapter){
        activity.listView.setAdapter(adapter);
    }*/

    public void changeAdapter(int type){
        if(unreads==type)
            if(MessagesController.getInstance().controller.unreads.size() > 0)
                if(MessagesController.getInstance().controller.getUnreadCount()==0)
                    MessagesController.getInstance().controller.unreads.clear();
        lastChangeAdapter=System.currentTimeMillis();
        activity.dialogsAdapter.dialogsType=type;
        activity.dialogsAdapter.notifyDataSetChanged();
        //activity.listView.setAdapter(adapter);
    }
    public void createMenu(ActionBarMenu menu){}

    public void actionItemClicked(int id){}





    ObjectAnimator10 animator;
    public void notificationReceived(int id){
        if(unreadIv!=null && id== NotificationCenter.updateInterfaces){
            int unread=MessagesController.getInstance().controller.getUnreadCount();
            if(BuildConfig.DEBUG)
            Log.d("unreas " + unread);
            if(BuildConfig.DEBUG)
            Log.d("last Unreas " + lastUnreadCount);
           // if(unread<=lastUnreadCount)
           //     return;
            if(unread<=0){
                unreadIv.setColorFilter(Color.WHITE);
            }
            else {
                hide(false);
                animteTint(true);
            }

            lastUnreadCount=unread;
        }
    }
    private void animteTint(boolean animate){
        if(animate){
            if(animator!=null)
                animator.cancel();
            int w=Color.WHITE;
            animator= ObjectAnimator10.ofObject(unreadIv, "colorFilter", new TypeEvaluator<Object>(){

                @Override
                public Object evaluate(float fraction, Object startValue, Object endValue) {
                    int startInt = (Integer) startValue;
                    int startA = (startInt >> 24);
                    int startR = (startInt >> 16) & 0xff;
                    int startG = (startInt >> 8) & 0xff;
                    int startB = startInt & 0xff;

                    int endInt = (Integer) endValue;
                    int endA = (endInt >> 24);
                    int endR = (endInt >> 16) & 0xff;
                    int endG = (endInt >> 8) & 0xff;
                    int endB = endInt & 0xff;

                    return (int)((startA + (int)(fraction * (endA - startA))) << 24) |
                            (int)((startR + (int)(fraction * (endR - startR))) << 16) |
                            (int)((startG + (int)(fraction * (endG - startG))) << 8) |
                            (int)((startB + (int)(fraction * (endB - startB))));
                }
            }, w, redColor, w, redColor, w, redColor);
           // animator= com.nineoldandroids.animation.ObjectAnimator
            //        .ofObject(unreadIv, "colorFilter", new ArgbEvaluator(), w,redColor,w,redColor,w,redColor);
            animator.setDuration(2000);
            animator.start();
        }else{
            unreadIv.setColorFilter(redColor);
        }

    }

    public void destroy(){
        if(animator!=null)
            animator.cancel();
    }

}
