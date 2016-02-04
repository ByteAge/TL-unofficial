package chitchat.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.tisad.chitchat2.R;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.ChannelCreateActivity;
import org.telegram.ui.ChannelIntroActivity;
import org.telegram.ui.ContactsActivity;
import org.telegram.ui.GroupCreateActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.SettingsActivity;

import chitchat.fragments.ChitSettingsActivity;
import chitchat.fragments.FileManagerFragment;
import chitchat.fragments.FilesFragment;
import chitchat.fragments.IdFinderActivity;
import chitchat.fragments.StickersActivity;
import chitchat.fragments.ThemesFragment;

/**
 * Created by RaminBT on 14/01/2016.
 */
public class DrawerLayoutAdapter {

    public static int getCount(){
        return UserConfig.isClientActivated() ? 14 : 0;
    }

    public static int getType(int i){
        if (i == 0) {
            // profile
            return 0;
        } else if (i == 1) {
            // empty
            return 1;
        } else if (i == 5) {
            // divider
            return 2;
        }
        return 3;
    }
    public static View getView(int i, View view, ViewGroup viewGroup, Context mContext) {
        int type=getType(i);
        if (type == 0) {
            if (view == null) {
                view = new DrawerProfileCell(mContext);
            }
            ((DrawerProfileCell) view).setUser(MessagesController.getInstance().getUser(UserConfig.getClientUserId()));
        } else if (type == 1) {
            if (view == null) {
                view = new EmptyCell(mContext, AndroidUtilities.dp(8));
            }
        } else if (type == 2) {
            if (view == null) {
                view = new DividerCell(mContext);
            }
        } else if (type == 3) {
            if (view == null) {
                view = new DrawerActionCell(mContext);
            }
            DrawerActionCell actionCell = (DrawerActionCell) view;
            if (i == 2) {
                actionCell.setTextAndIcon(LocaleController.getString("NewGroup", R.string.NewGroup), R.drawable.menu_newgroup);
            } else if (i == 3) {
                actionCell.setTextAndIcon(LocaleController.getString("NewSecretChat", R.string.NewSecretChat), R.drawable.menu_secret);
            } else if (i == 4) {
                actionCell.setTextAndIcon(LocaleController.getString("NewChannel", R.string.NewChannel), R.drawable.menu_broadcast);
            } else if (i == 6) {
                actionCell.setTextAndIcon(LocaleController.getString("StickerBigSize", R.string.StickerBigSize), R.drawable.sticker);
            } else if (i == 7) {
                actionCell.setTextAndIcon(LocaleController.getString("IdFinder", R.string.IdFinder), R.drawable.search_g);
            } else if (i == 8) {
                actionCell.setTextAndIcon(LocaleController.getString("Contacts", R.string.Contacts), R.drawable.menu_contacts);
            } else if (i == 9) {
                actionCell.setTextAndIcon(LocaleController.getString("InviteFriends", R.string.InviteFriends), R.drawable.menu_invite);
            } else if(i==10){
                actionCell.setTextAndIcon(LocaleController.getString("Themes", R.string.Themes), R.drawable.ic_style_grey600_24dp);
            }else if (i == 12) {
                actionCell.setTextAndIcon(LocaleController.getString("Settings", R.string.Settings), R.drawable.menu_settings);
            } else if (i == 11) {
                actionCell.setTextAndIcon(LocaleController.getString("ChitSettings", R.string.ChitSettings), R.drawable.menu_settings);
            } else if (i == 13) {
                actionCell.setTextAndIcon(LocaleController.getString("TelegramFaq", R.string.TelegramFaq), R.drawable.menu_help);
            }else if(i==14){
                actionCell.setTextAndIcon("File Manager", R.drawable.ic_ab_attach);
            }
        }

        return view;
    }

    public static void itemClicked(int position, LaunchActivity fragment){

        if (position == 2) {
            if (!MessagesController.isFeatureEnabled("chat_create", fragment.actionBarLayout.fragmentsStack.get(fragment.actionBarLayout.fragmentsStack.size() - 1))) {
                return;
            }
            fragment.presentFragment(new GroupCreateActivity());
            fragment.drawerLayoutContainer.closeDrawer(false);
        } else if (position == 3) {
            Bundle args = new Bundle();
            args.putBoolean("onlyUsers", true);
            args.putBoolean("destroyAfterSelect", true);
            args.putBoolean("createSecretChat", true);
            fragment.presentFragment(new ContactsActivity(args));
            fragment.drawerLayoutContainer.closeDrawer(false);
        } else if (position == 4) {
            if (!MessagesController.isFeatureEnabled("broadcast_create", fragment.actionBarLayout.fragmentsStack.get(fragment.actionBarLayout.fragmentsStack.size() - 1))) {
                return;
            }
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
            if (preferences.getBoolean("channel_intro", false)) {
                Bundle args = new Bundle();
                args.putInt("step", 0);
                fragment.presentFragment(new ChannelCreateActivity(args));
            } else {
                fragment.presentFragment(new ChannelIntroActivity());
                preferences.edit().putBoolean("channel_intro", true).commit();
            }
            fragment.drawerLayoutContainer.closeDrawer(false);
        } else if( position == 6){
            fragment.presentFragment(new StickersActivity());
            fragment.drawerLayoutContainer.closeDrawer(false);
        } else if( position == 7){
            fragment.presentFragment(new IdFinderActivity());
            fragment.drawerLayoutContainer.closeDrawer(false);
        } else if (position == 8) {
            fragment.presentFragment(new ContactsActivity(null));
            fragment.drawerLayoutContainer.closeDrawer(false);
        } else if (position == 9) {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, ApplicationLoader.applicationContext.getString(R.string.ChitChatInvite));
                fragment.startActivityForResult(Intent.createChooser(intent, LocaleController.getString("InviteFriends", R.string.InviteFriends)), 500);
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
            fragment.drawerLayoutContainer.closeDrawer(false);
        } else if (position == 12) {
            fragment. presentFragment(new SettingsActivity());
            fragment.drawerLayoutContainer.closeDrawer(false);
        } else if (position==11) {
            fragment.presentFragment(new ChitSettingsActivity());
            fragment.drawerLayoutContainer.closeDrawer(false);
        }else if (position == 13) {
            try {
                Intent pickIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(LocaleController.getString("TelegramFaqUrl", R.string.TelegramFaqUrl)));
                fragment.startActivityForResult(pickIntent, 500);
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
            fragment.drawerLayoutContainer.closeDrawer(false);
        }else if(position==14){
            fragment.presentFragment(new FilesFragment());
            fragment.drawerLayoutContainer.closeDrawer(false);
        }else if(position==10){
            fragment.presentFragment(new ThemesFragment());
            fragment.drawerLayoutContainer.closeDrawer(false);
        }

    }
}
