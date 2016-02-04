/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package chitchat.fragments;

import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tisad.chitchat2.R;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.AvatarUpdater;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ResourceLoader;
import org.telegram.ui.PhotoViewer;

import java.util.ArrayList;
import java.util.Locale;

import chitchat.NightModeUtil;
import chitchat.ThemePicUtil;
import chitchat.Utils;
import chitchat.messenger.GhostHelper;
import chitchat.skin.SkinMan;

import static chitchat.ChitSettings.changeConfirmStickerSending;
import static chitchat.ChitSettings.changeFantasy;
import static chitchat.ChitSettings.changeGhost;
import static chitchat.ChitSettings.changeOverrideLed;
import static chitchat.ChitSettings.changeShowTabs;
import static chitchat.ChitSettings.changeTypingState;
import static chitchat.ChitSettings.clearWallpaper;
import static chitchat.ChitSettings.confirmBeforeSendingSticker;
import static chitchat.ChitSettings.hasWallpaper;
import static chitchat.ChitSettings.overRideLedColor;
import static chitchat.ChitSettings.setDefaultTab;
import static chitchat.ChitSettings.showFantasy;
import static chitchat.ChitSettings.showTabs;

public class ChitSettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, PhotoViewer.PhotoViewerProvider {

    private ListView listView;
    private ListAdapter listAdapter;
    private BackupImageView avatarImage;
    private TextView nameTextView;
    private TextView onlineTextView;
    private ImageView writeButton;
    private AnimatorSetProxy writeButtonAnimation;
    private AvatarUpdater avatarUpdater = new AvatarUpdater();
    private View extraHeightView;
    private View shadowView;

    private int extraHeight;

    private ThemePicUtil controller;

    private void addActions(){
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                if(id==-1)
                    finishFragment();
            }
        });
    }
    @Override
    public View createView(Context context) {

        addActions();
        actionBar.setBackgroundColor(AvatarDrawable.getProfileBackColorForId(5));
        actionBar.setItemsBackground(AvatarDrawable.getButtonColorForId(5));
        actionBar.setAddToContainer(false);
        extraHeight = 88;
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context) {
            @Override
            protected boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
                if (child == listView) {
                    boolean result = super.drawChild(canvas, child, drawingTime);
                    if (parentLayout != null) {
                        int actionBarHeight = 0;
                        int childCount = getChildCount();
                        for (int a = 0; a < childCount; a++) {
                            View view = getChildAt(a);
                            if (view == child) {
                                continue;
                            }
                            if (view instanceof ActionBar && view.getVisibility() == VISIBLE) {
                                if (((ActionBar) view).getCastShadows()) {
                                    actionBarHeight = view.getMeasuredHeight();
                                }
                                break;
                            }
                        }
                        parentLayout.drawHeaderShadow(canvas, actionBarHeight);
                    }
                    return result;
                } else {
                    return super.drawChild(canvas, child, drawingTime);
                }
            }
        };
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new ListView(context);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);
        AndroidUtilities.setListViewEdgeEffectColor(listView, AvatarDrawable.getProfileBackColorForId(5));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                // :ramin clicked item
                itemSelected(view, i);
            }
        });

        frameLayout.addView(actionBar);

        extraHeightView = new View(context);
        ViewProxy.setPivotY(extraHeightView, 0);
        extraHeightView.setBackgroundColor(AvatarDrawable.getProfileBackColorForId(5));
        frameLayout.addView(extraHeightView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 88));

        shadowView = new View(context);
        shadowView.setBackgroundResource(R.drawable.header_shadow);
        frameLayout.addView(shadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 3));

        avatarImage = new BackupImageView(context);
        avatarImage.setRoundRadius(AndroidUtilities.dp(21));
        ViewProxy.setPivotX(avatarImage, 0);
        ViewProxy.setPivotY(avatarImage, 0);
        frameLayout.addView(avatarImage, LayoutHelper.createFrame(42, 42, Gravity.TOP | Gravity.LEFT, 64, 0, 0, 0));
        avatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                if (user.photo != null && user.photo.photo_big != null) {
                    PhotoViewer.getInstance().setParentActivity(getParentActivity());
                    PhotoViewer.getInstance().openPhoto(user.photo.photo_big, ChitSettingsActivity.this);
                }
            }
        });

        nameTextView = new TextView(context);
        //nameTextView.setTextColor(0xffffffff);
        SkinMan.currentSkin.setTextColorBigAction(nameTextView, 0xffffffff);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        ViewProxy.setPivotX(nameTextView, 0);
        ViewProxy.setPivotY(nameTextView, 0);
        frameLayout.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 48, 0));

        onlineTextView = new TextView(context);
       /* onlineTextView.setTextColor(
                //AvatarDrawable.getProfileTextColorForId(5)
                SkinMan.currentSkin.light()

        );*/
        SkinMan.currentSkin.setTextColorBigAction(onlineTextView, AvatarDrawable.getProfileTextColorForId(5));
        onlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        onlineTextView.setLines(1);
        onlineTextView.setMaxLines(1);
        onlineTextView.setSingleLine(true);
        onlineTextView.setEllipsize(TextUtils.TruncateAt.END);
        onlineTextView.setGravity(Gravity.LEFT);
        frameLayout.addView(onlineTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 48, 0));

        writeButton = new ImageView(context);
        writeButton.setBackgroundResource(R.drawable.floating_user_states);
        writeButton.setImageResource(R.drawable.floating_camera);
        writeButton.setScaleType(ImageView.ScaleType.CENTER);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(writeButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(writeButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            writeButton.setStateListAnimator(animator);
            writeButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        //frameLayout.addView(writeButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.TOP, 0, 0, 16, 0));
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getParentActivity() == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());

                CharSequence[] items;

                TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                if (user == null) {
                    user = UserConfig.getCurrentUser();
                }
                if (user == null) {
                    return;
                }
                boolean fullMenu = false;
                if (user.photo != null && user.photo.photo_big != null && !(user.photo instanceof TLRPC.TL_userProfilePhotoEmpty)) {
                    items = new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley), LocaleController.getString("DeletePhoto", R.string.DeletePhoto)};
                    fullMenu = true;
                } else {
                    items = new CharSequence[]{LocaleController.getString("FromCamera", R.string.FromCamera), LocaleController.getString("FromGalley", R.string.FromGalley)};
                }

                final boolean full = fullMenu;
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            avatarUpdater.openCamera();
                        } else if (i == 1) {
                            avatarUpdater.openGallery();
                        } else if (i == 2) {
                            MessagesController.getInstance().deleteUserPhoto(null);
                        }
                    }
                });
                showDialog(builder.create());
            }
        });
        writeButton.setVisibility(View.GONE);

        controller=new ThemePicUtil(frameLayout, actionBar, extraHeightView, context);

        needLayout();

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount == 0) {
                    return;
                }
                int height = 0;
                View child = view.getChildAt(0);
                if (child != null) {
                    if (firstVisibleItem == 0) {
                        height = AndroidUtilities.dp(88) + (child.getTop() < 0 ? child.getTop() : 0);
                    }
                    if (extraHeight != height) {
                        extraHeight = height;
                        needLayout();
                    }
                }
            }
        });



        NightModeUtil.dark(fragmentView);
        return fragmentView;
    }


    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        avatarUpdater.parentFragment = this;
        avatarUpdater.delegate = new AvatarUpdater.AvatarUpdaterDelegate() {
            @Override
            public void didUploadedPhoto(TLRPC.InputFile file, TLRPC.PhotoSize small, TLRPC.PhotoSize big) {
                TLRPC.TL_photos_uploadProfilePhoto req = new TLRPC.TL_photos_uploadProfilePhoto();
                req.caption = "";
                req.crop = new TLRPC.TL_inputPhotoCropAuto();
                req.file = file;
                req.geo_point = new TLRPC.TL_inputGeoPointEmpty();
                ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                    @Override
                    public void run(TLObject response, TLRPC.TL_error error) {
                        if (error == null) {
                            TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                            if (user == null) {
                                user = UserConfig.getCurrentUser();
                                if (user == null) {
                                    return;
                                }
                                MessagesController.getInstance().putUser(user, false);
                            } else {
                                UserConfig.setCurrentUser(user);
                            }
                            TLRPC.TL_photos_photo photo = (TLRPC.TL_photos_photo) response;
                            ArrayList<TLRPC.PhotoSize> sizes = photo.photo.sizes;
                            TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(sizes, 100);
                            TLRPC.PhotoSize bigSize = FileLoader.getClosestPhotoSizeWithSize(sizes, 1000);
                            user.photo = new TLRPC.TL_userProfilePhoto();
                            user.photo.photo_id = photo.photo.id;
                            if (smallSize != null) {
                                user.photo.photo_small = smallSize.location;
                            }
                            if (bigSize != null) {
                                user.photo.photo_big = bigSize.location;
                            } else if (smallSize != null) {
                                user.photo.photo_small = smallSize.location;
                            }
                            MessagesStorage.getInstance().clearUserPhotos(user.id);
                            ArrayList<TLRPC.User> users = new ArrayList<>();
                            users.add(user);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
                                    UserConfig.saveConfig(true);
                                }
                            });
                        }
                    }
                });
            }
        };
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        MessagesController.getInstance().loadFullUser(UserConfig.getCurrentUser(), classGuid);

        resolveRows();
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (avatarImage != null) {
            avatarImage.setImageDrawable(null);
        }
        MessagesController.getInstance().cancelLoadFullUser(UserConfig.getClientUserId());
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        avatarUpdater.clear();
    }



    /*public int showTab=2,
    showDot=3,
    defaultTab=4,
    showInviter=5,
    ghostMode=8,
    typingState=9;
    @Override
    public void onClick(View v) {
        int index=((ViewGroup)fragmentView).indexOfChild(v);

        if(index==showTab){
            TextCheckCell cell= (TextCheckCell) v;
            cell.setSelected(!cell.isChecked());
        }

    }
    public TextCheckCell getCheckCell(int index){
        return getCheckCell(((ViewGroup) fragmentView).getChildAt(index));
    }
    public TextCheckCell getCheckCell(View v){
        return (TextCheckCell) v;
    }*/





    //region extras
    private void needLayout() {
        FrameLayout.LayoutParams layoutParams;
        int newTop = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();
        if (listView != null) {
            layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            if (layoutParams.topMargin != newTop) {
                layoutParams.topMargin = newTop;
                listView.setLayoutParams(layoutParams);
                ViewProxy.setTranslationY(extraHeightView, newTop);
            }
        }

        controller.translate(extraHeight);

        if (avatarImage != null) {
            float diff = extraHeight / (float) AndroidUtilities.dp(88);
            ViewProxy.setScaleY(extraHeightView, diff);
            ViewProxy.setTranslationY(shadowView, newTop + extraHeight);

            if (Build.VERSION.SDK_INT < 11) {
                layoutParams = (FrameLayout.LayoutParams) writeButton.getLayoutParams();
                if(layoutParams!=null){
                    layoutParams.topMargin = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + extraHeight - AndroidUtilities.dp(29.5f);
                    writeButton.setLayoutParams(layoutParams);
                }
            } else {
                ViewProxy.setTranslationY(writeButton, (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + extraHeight - AndroidUtilities.dp(29.5f));
            }

            final boolean setVisible = diff > 0.2f;
            boolean currentVisible = writeButton.getTag() == null;
            if (setVisible != currentVisible) {
                if (setVisible) {
                    writeButton.setTag(null);
                    writeButton.setVisibility(View.VISIBLE);
                } else {
                    writeButton.setTag(0);
                }
                if (writeButtonAnimation != null) {
                    AnimatorSetProxy old = writeButtonAnimation;
                    writeButtonAnimation = null;
                    old.cancel();
                }
                writeButtonAnimation = new AnimatorSetProxy();
                if (setVisible) {
                    writeButtonAnimation.setInterpolator(new DecelerateInterpolator());
                    writeButtonAnimation.playTogether(
                            ObjectAnimatorProxy.ofFloat(writeButton, "scaleX", 1.0f),
                            ObjectAnimatorProxy.ofFloat(writeButton, "scaleY", 1.0f),
                            ObjectAnimatorProxy.ofFloat(writeButton, "alpha", 1.0f)
                    );
                } else {
                    writeButtonAnimation.setInterpolator(new AccelerateInterpolator());
                    writeButtonAnimation.playTogether(
                            ObjectAnimatorProxy.ofFloat(writeButton, "scaleX", 0.2f),
                            ObjectAnimatorProxy.ofFloat(writeButton, "scaleY", 0.2f),
                            ObjectAnimatorProxy.ofFloat(writeButton, "alpha", 0.0f)
                    );
                }
                writeButtonAnimation.setDuration(150);
                writeButtonAnimation.addListener(new AnimatorListenerAdapterProxy() {
                    @Override
                    public void onAnimationEnd(Object animation) {
                        if (writeButtonAnimation != null && writeButtonAnimation.equals(animation)) {
                            writeButton.clearAnimation();
                            writeButton.setVisibility(setVisible ? View.VISIBLE : View.GONE);
                            writeButtonAnimation = null;
                        }
                    }
                });
                writeButtonAnimation.start();
            }

            ViewProxy.setScaleX(avatarImage, (42 + 18 * diff) / 42.0f);
            ViewProxy.setScaleY(avatarImage, (42 + 18 * diff) / 42.0f);
            float avatarY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f * (1.0f + diff) - 21 * AndroidUtilities.density + 27 * AndroidUtilities.density * diff;
            ViewProxy.setTranslationX(avatarImage, -AndroidUtilities.dp(47) * diff);
            ViewProxy.setTranslationY(avatarImage, (float) Math.ceil(avatarY));
            ViewProxy.setTranslationX(nameTextView, -21 * AndroidUtilities.density * diff);
            ViewProxy.setTranslationY(nameTextView, (float) Math.floor(avatarY) - (float) Math.ceil(AndroidUtilities.density) + (float) Math.floor(7 * AndroidUtilities.density * diff));
            ViewProxy.setTranslationX(onlineTextView, -21 * AndroidUtilities.density * diff);
            ViewProxy.setTranslationY(onlineTextView, (float) Math.floor(avatarY) + AndroidUtilities.dp(22) + (float )Math.floor(11 * AndroidUtilities.density) * diff);
            ViewProxy.setScaleX(nameTextView, 1.0f + 0.12f * diff);
            ViewProxy.setScaleY(nameTextView, 1.0f + 0.12f * diff);
        }
    }
    @Override
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        if (fileLocation == null) {
            return null;
        }
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        if (user != null && user.photo != null && user.photo.photo_big != null) {
            TLRPC.FileLocation photoBig = user.photo.photo_big;
            if (photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                int coords[] = new int[2];
                avatarImage.getLocationInWindow(coords);
                PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                object.viewX = coords[0];
                object.viewY = coords[1] - AndroidUtilities.statusBarHeight;
                object.parentView = avatarImage;
                object.imageReceiver = avatarImage.getImageReceiver();
                object.user_id = UserConfig.getClientUserId();
                object.thumb = object.imageReceiver.getBitmap();
                object.size = -1;
                object.radius = avatarImage.getImageReceiver().getRoundRadius();
                object.scale = ViewProxy.getScaleX(avatarImage);
                return object;
            }
        }
        return null;
    }

    @Override
    public Bitmap getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        return null;
    }

    @Override
    public void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
    }

    @Override
    public void willHidePhotoViewer() {
        avatarImage.getImageReceiver().setVisible(true, true);
    }

    @Override
    public boolean isPhotoChecked(int index) {
        return false;
    }

    @Override
    public void setPhotoChecked(int index) {
    }

    @Override
    public boolean cancelButtonPressed() {
        return true;
    }

    @Override
    public void sendButtonPressed(int index) {
    }

    @Override
    public int getSelectedCount() {
        return 0;
    }
    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        avatarUpdater.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void saveSelfArgs(Bundle args) {
        if (avatarUpdater != null && avatarUpdater.currentPicturePath != null) {
            args.putString("path", avatarUpdater.currentPicturePath);
        }
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
        if (avatarUpdater != null) {
            avatarUpdater.currentPicturePath = args.getString("path");
        }
    }
    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                updateUserData();
            }
        }
    }
    @Override
    protected void onDialogDismiss(Dialog dialog) {
        MediaController.getInstance().checkAutodownloadSettings();
    }

    @Override
    public void updatePhotoAtIndex(int index) {

    }
    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        updateUserData();
        fixLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }
    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    needLayout();
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
    }

    private void updateUserData() {
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        TLRPC.FileLocation photo = null;
        TLRPC.FileLocation photoBig = null;
        if (user.photo != null) {
            photo = user.photo.photo_small;
            photoBig = user.photo.photo_big;
        }
        AvatarDrawable avatarDrawable = new AvatarDrawable(user, true);
        avatarDrawable.setColor(0xff5c98cd);
        if (avatarImage != null) {
            avatarImage.setImage(photo, "50_50", avatarDrawable);
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);

            nameTextView.setText(UserObject.getUserName(user));
            onlineTextView.setText(LocaleController.getString("Online", R.string.Online));

            avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);
        }
    }
    //endregion








    private Dialog createdDialog;
    public void showSelectDefaultTab(View view){
        final TextDetailSettingsCell cell= (TextDetailSettingsCell) view;
        BottomSheet.Builder b=new BottomSheet.Builder(getParentActivity());

        LinearLayout layout=new LinearLayout(getParentActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        for(int i=0; i< 6; i++){
            TextView tv=new TextView(getParentActivity());
            tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            tv.setText(DialogActivityH.tabNames.get(i));
            tv.setTag(i);
            tv.setBackgroundResource(R.drawable.list_selector);
            tv.setPadding(AndroidUtilities.dp(17),0,AndroidUtilities.dp(17),0);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(createdDialog!=null)
                        createdDialog.dismiss();
                    int tab=(Integer) v.getTag();
                    cell.setValueTextView(DialogActivityH.tabNames.get(tab));
                    // convert to adapter index
                    tab=DialogActivityH.adapterIndex.get(tab);
                    setDefaultTab(tab);
                    createdDialog=null;
                }
            });

            layout.addView(tv, LayoutHelper.createLinear(-1, 48));
        }

        b.setCustomView(layout);
        createdDialog=b.create();
        showDialog(createdDialog);

    }
    public void itemSelected(View view, int position){
        if(view instanceof TextCheckCell){
            TextCheckCell cell= (TextCheckCell) view;
            boolean check = false;
            if(position==showTab)
                check=changeShowTabs();
           // else if(position==showInviter)
            //    check=changeShowInviter();
           // else if(position==showDot)
           //     check=changeShowMutualDot();
            else if(position==ghostMode)
                check=changeGhost();
            else if(position==typingState)
                check=changeTypingState();
            else if(position==confirmStickerRow)
                check=changeConfirmStickerSending();
            else if(position==hideStickersRow)
                check=StickerSetActivity.hideStickers(!StickerSetActivity.isStickersHidden());
            else if(position==useThemeLedColorRow)
                check=changeOverrideLed();
            else if(position==chitchatModeRow){
                check=!changeFantasy();
                ResourceLoader.fantasyChanged(showFantasy);
            }

            cell.setChecked(check);
        }

        if(position==defaultTabRow){
            showSelectDefaultTab(view);
        }else if(position==backgroundRow){
            if(hasWallpaper()){
                AlertDialog.Builder b=new AlertDialog.Builder(listAdapter.context);
                b.setTitle(R.string.AppName);
                b.setPositiveButton(R.string.SelectWallpaper, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        presentFragment(new LaunchFragmentWallpaper());
                    }
                });
                b.setNegativeButton(R.string.DeleteWallpaper, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        clearWallpaper();
                    }
                });
                b.show();
            }
           else
                presentFragment(new LaunchFragmentWallpaper());
        }else if(position==nightModeRow){
            presentFragment(new NightModeActivity());
        }
        else if(position==deleteStickers)
            StickerSetActivity.deleteAllSavedStickers(getParentActivity());
        else if(position==contactUs)
            Utils.contactMail(getParentActivity());


    }


    public void resolveRows(){
        rowCount=1;
        headerView=rowCount++;
        showTab=rowCount++;
        defaultTabRow=rowCount++;
        bakgroundShadow=rowCount++;
        backgroundRow=rowCount++;
        nightModeRow=rowCount++;
        chitchatModeRow=rowCount++;
        useThemeLedColorRow=rowCount++;
        confirmStickerRow=rowCount++;
        deleteStickers=rowCount++;
        hideStickersRow=rowCount++;
        ghostHeaderShadow=rowCount++;
        ghostHeader=rowCount++;
        ghostMode=rowCount++;
        typingState=rowCount++;
        contactUs=rowCount++;
        aboutRow=rowCount++;
    }
    int rowCount=17;
    int headerView=2, showTab=3, //showDot=4, showInviter=5,
            defaultTabRow=4, chitchatModeRow,
            bakgroundShadow=5, backgroundRow=6, nightModeRow, useThemeLedColorRow=7, confirmStickerRow=8, deleteStickers=9, hideStickersRow=10,
    ghostHeaderShadow=11, ghostHeader=12
            , ghostMode=13, typingState=14, contactUs=15
            // about rows
            , aboutRow=16
            ;
    class ListAdapter extends BaseFragmentAdapter{

        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return rowCount;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return
                    // settings
                    (position >= showTab && position <= defaultTabRow)
                            // stickerRow - dialogWallpaper
                            || (position>=backgroundRow && position <= hideStickersRow )
                            //ghosts
                            || (position>=ghostMode && position<=contactUs);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int t=getItemViewType(i);

            if(t==5){
                if (view == null)
                    view = new EmptyCell(context);
                ((EmptyCell) view).setHeight(AndroidUtilities.dp(88));
            }
            else if(t==1){
                // shadow
                if(view==null)
                    view=new ShadowSectionCell(context);
            }else if(t==2){
                // header
                if(view==null)
                    view= new HeaderCell(context);
                HeaderCell cell= (HeaderCell) view;
                if(i==ghostHeader)
                    cell.setText(LocaleController.getString("Ghost", R.string.Ghost));
                else if(i==headerView)
                    cell.setText(LocaleController.getString("Settings", R.string.SETTINGS));
            }else if(t==4){
                // boolean
                if(view==null)
                    view=new TextCheckCell(context);
                TextCheckCell cell= (TextCheckCell) view;
                if(i==showTab)
                    cell.setTextAndCheck(LocaleController.getString("ShowTab", R.string.ShowTab), showTabs, true);
               /* else if(i==showDot)
                    cell.setTextAndCheck(LocaleController.getString("ShowDot", R.string.ShowDot), showMutualDot, true);
                else if(i==showInviter)
                    cell.setTextAndCheck(LocaleController.getString("ShowInviter", R.string.ShowInviter), showInviterInGroup, true);*/
                else if(i==ghostMode)
                    cell.setTextAndCheck(LocaleController.getString("GhostModeEnabled", R.string.GhostModeEnabled), GhostHelper.isGhostEnabled(), true);
                else if(i==typingState)
                    cell.setTextAndCheck(LocaleController.getString("HideTypingState", R.string.HideTypingState), GhostHelper.isHideTyping(), true);
                else if(i==confirmStickerRow)
                    cell.setTextAndCheck(LocaleController.getString("Confirm", R.string.ConfirmSticker), confirmBeforeSendingSticker, true);
                else if(i==hideStickersRow)
                    cell.setTextAndCheck(LocaleController.getString("HideStickers", R.string.HideStickers), StickerSetActivity.isStickersHidden(), false);
                else if(i==useThemeLedColorRow)
                    cell.setTextAndCheck(LocaleController.getString("", R.string.UseThemeLed), overRideLedColor, true);
                else if(i==chitchatModeRow)
                    cell.setTextAndCheck(LocaleController.getString("Chit",R.string.ChatInTelegramStyle), !showFantasy, true);


            }else if(t==3){
                if(view==null)
                    view=new TextDetailSettingsCell(context);
                TextDetailSettingsCell cell= (TextDetailSettingsCell) view;
                String text = null, value = null;
                if(i==defaultTabRow){
                    text=LocaleController.getString("DefaultTab", R.string.DefaultTab);
                    value=DialogActivityH.getDefaultTabName();
                }
                cell.setTextAndValue(text, value, false);

            }else if(t==6){
                if(view==null)
                    view=new TextSettingsCell(context);
                TextSettingsCell cell= (TextSettingsCell) view;
                if(i==backgroundRow)
                    cell.setText(LocaleController.getString("DialogBackground", R.string.DialogWallpaper), true);
                if(i==deleteStickers)
                    cell.setText(LocaleController.getString("DS", R.string.DeleteSavedStickers), true);
                if(i==contactUs)
                    cell.setText(LocaleController.getString("ContactUs", R.string.ContactUs), true);
                if(i==nightModeRow)
                    cell.setText(LocaleController.getString("NightMode", R.string.NightMode), true);
            }else if(t==7){
                if(view==null)
                    view=new TextInfoCell(context);
                TextInfoCell cell= (TextInfoCell) view;
                try {
                    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                    cell.setText(String.format(Locale.US, "ChitCHat for Android v%s (%d)", pInfo.versionName, pInfo.versionCode));
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            }


            return view;
        }

        @Override
        public int getViewTypeCount() {
            return 8;
        }
        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public int getItemViewType(int position) {
            // text Check
            int a=4;

            if(position==0)
                // empty
                a=5;
            else if(position==ghostHeaderShadow || position==1 || position==bakgroundShadow)
                // shadow
                a=1;
            else if(position==headerView || position==ghostHeader)
                // header
                a=2;
            else if(position==defaultTabRow)
                // multiline item
                a=3;
            else if(position==backgroundRow || position==nightModeRow || position==deleteStickers || position==contactUs)
                // singleLine item
                a=6;
            else if(position==aboutRow)
                a=7;

            return a;
        }
    }

}
