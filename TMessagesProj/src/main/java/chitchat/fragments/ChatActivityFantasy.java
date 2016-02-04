package chitchat.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Browser;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tisad.chitchat2.R;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.messenger.AnimationCompat.AnimatorSetProxy;
import org.telegram.messenger.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.messenger.AnimationCompat.ViewProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.query.MessagesSearchQuery;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.Adapters.MentionsAdapter;
import org.telegram.ui.Adapters.StickersAdapter;
import org.telegram.ui.Cells.BotHelpCell;
import org.telegram.ui.Cells.ChatMediaCell;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.ChatAttachView;
import org.telegram.ui.Components.FrameLayoutFixed;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberTextView;
import org.telegram.ui.Components.PlayerView;
import org.telegram.ui.Components.RadioButton;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ResourceLoader;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.TimerDrawable;
import org.telegram.ui.Components.WebFrameLayout;
import org.telegram.ui.ContactAddActivity;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.SecretPhotoViewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import chitchat.Utils;

/**
 * Created by RaminBT on 28/01/2016.
 */
public class ChatActivityFantasy extends ChatActivity{

    public static final int grey=0xff797979
                , lightBlue=0xff68BBC5;


    public ChatActivityFantasy(Bundle args) {
        super(args);
    }

    boolean t=true;

    @Override
    public View createView(final Context context) {
        if (chatMessageCellsCache.isEmpty()) {
            for (int a = 0; a < 8; a++) {
                chatMessageCellsCache.add(new ChatMessageCell(context));
            }
        }
        if (chatMediaCellsCache.isEmpty()) {
            for (int a = 0; a < 4; a++) {
                chatMediaCellsCache.add(new ChatMediaCell(context));
            }
        }
        for (int a = 1; a >= 0; a--) {
            selectedMessagesIds[a].clear();
            selectedMessagesCanCopyIds[a].clear();
        }
        cantDeleteMessagesCount = 0;

        lastPrintString = null;
        lastStatus = null;
        hasOwnBackground = true;
        chatAttachView = null;
        chatAttachViewSheet = null;

        ResourceLoader.loadRecources(context);

        actionBar.setItemsBackground(R.drawable.bar_selector_fantasy);
        Utils.setBackground(actionBar, null);
        actionBar.setBackgroundColor(Color.WHITE);

        actionBar.setBackButtonDrawable(getBackDrawable(false));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(final int id) {
                if (id == -1) {
                    if (actionBar.isActionModeShowed()) {
                        for (int a = 1; a >= 0; a--) {
                            selectedMessagesIds[a].clear();
                            selectedMessagesCanCopyIds[a].clear();
                        }
                        cantDeleteMessagesCount = 0;
                        actionBar.hideActionMode();
                        updateVisibleRows();
                    } else {
                        finishFragment();
                    }
                } else if (id == copy) {
                    String str = "";
                    for (int a = 1; a >= 0; a--) {
                        ArrayList<Integer> ids = new ArrayList<>(selectedMessagesCanCopyIds[a].keySet());
                        if (currentEncryptedChat == null) {
                            Collections.sort(ids);
                        } else {
                            Collections.sort(ids, Collections.reverseOrder());
                        }
                        for (int b = 0; b < ids.size(); b++) {
                            Integer messageId = ids.get(b);
                            MessageObject messageObject = selectedMessagesCanCopyIds[a].get(messageId);
                            if (str.length() != 0) {
                                str += "\n";
                            }
                            if (messageObject.messageOwner.message != null) {
                                str += messageObject.messageOwner.message;
                            } else {
                                str += messageObject.messageText;
                            }
                        }
                    }
                    if (str.length() != 0) {
                        try {
                            if (Build.VERSION.SDK_INT < 11) {
                                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboard.setText(str);
                            } else {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplicationLoader.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("label", str);
                                clipboard.setPrimaryClip(clip);
                            }
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                    }
                    for (int a = 1; a >= 0; a--) {
                        selectedMessagesIds[a].clear();
                        selectedMessagesCanCopyIds[a].clear();
                    }
                    cantDeleteMessagesCount = 0;
                    actionBar.hideActionMode();
                    updateVisibleRows();
                } else if (id == delete) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setMessage(LocaleController.formatString("AreYouSureDeleteMessages", R.string.AreYouSureDeleteMessages, LocaleController.formatPluralString("messages", selectedMessagesIds[0].size() + selectedMessagesIds[1].size())));
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            for (int a = 1; a >= 0; a--) {
                                ArrayList<Integer> ids = new ArrayList<>(selectedMessagesIds[a].keySet());
                                ArrayList<Long> random_ids = null;
                                int channelId = 0;
                                if (!ids.isEmpty()) {
                                    MessageObject msg = selectedMessagesIds[a].get(ids.get(0));
                                    if (channelId == 0 && msg.messageOwner.to_id.channel_id != 0) {
                                        channelId = msg.messageOwner.to_id.channel_id;
                                    }
                                }
                                if (currentEncryptedChat != null) {
                                    random_ids = new ArrayList<>();
                                    for (HashMap.Entry<Integer, MessageObject> entry : selectedMessagesIds[a].entrySet()) {
                                        MessageObject msg = entry.getValue();
                                        if (msg.messageOwner.random_id != 0 && msg.type != 10) {
                                            random_ids.add(msg.messageOwner.random_id);
                                        }
                                    }
                                }
                                MessagesController.getInstance().deleteMessages(ids, random_ids, currentEncryptedChat, channelId);
                            }
                            actionBar.hideActionMode();
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                } else if (id == forward) {
                    Bundle args = new Bundle();
                    args.putBoolean("onlySelect", true);
                    args.putInt("dialogsType", 1);
                    DialogsActivity fragment = new DialogsActivity(args);
                    fragment.setDelegate(ChatActivityFantasy.this);
                    presentFragment(fragment);
                } else if(id == share_qoute){
                    ArrayList<MessageObject> sels=forwards();
                    //:ramin
                    shareMessage(context, sels, true);
                    actionBar.hideActionMode();
                    updateVisibleRows();
                } else if (id == chat_enc_timer) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    showDialog(AndroidUtilities.buildTTLAlert(getParentActivity(), currentEncryptedChat).create());
                } else if (id == clear_history || id == delete_chat) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    final boolean isChat = (int) dialog_id < 0 && (int) (dialog_id >> 32) != 1;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    if (id == clear_history) {
                        builder.setMessage(LocaleController.getString("AreYouSureClearHistory", R.string.AreYouSureClearHistory));
                    } else {
                        if (isChat) {
                            builder.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", R.string.AreYouSureDeleteAndExit));
                        } else {
                            builder.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", R.string.AreYouSureDeleteThisChat));
                        }
                    }
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (id != clear_history) {
                                if (isChat) {
                                    if (ChatObject.isNotInChat(currentChat)) {
                                        MessagesController.getInstance().deleteDialog(dialog_id, 0);
                                    } else {
                                        MessagesController.getInstance().deleteUserFromChat((int) -dialog_id, MessagesController.getInstance().getUser(UserConfig.getClientUserId()), null);
                                    }
                                } else {
                                    MessagesController.getInstance().deleteDialog(dialog_id, 0);
                                }
                                finishFragment();
                            } else {
                                MessagesController.getInstance().deleteDialog(dialog_id, 1);
                            }
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                } else if (id == share_contact) {
                    if (currentUser == null || getParentActivity() == null) {
                        return;
                    }
                    if (currentUser.phone != null && currentUser.phone.length() != 0) {
                        Bundle args = new Bundle();
                        args.putInt("user_id", currentUser.id);
                        args.putBoolean("addContact", true);
                        presentFragment(new ContactAddActivity(args));
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setMessage(LocaleController.getString("AreYouSureShareMyContactInfo", R.string.AreYouSureShareMyContactInfo));
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SendMessagesHelper.getInstance().sendMessage(UserConfig.getCurrentUser(), dialog_id, replyingMessageObject, chatActivityEnterView == null || chatActivityEnterView.asAdmin());
                                moveScrollToLastMessage();
                                showReplyPanel(false, null, null, null, false, true);
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                    }
                } else if (id == mute) {
                    toggleMute(false);
                } else if (id == reply) {
                    MessageObject messageObject = null;
                    for (int a = 1; a >= 0; a--) {
                        if (messageObject == null && selectedMessagesIds[a].size() == 1) {
                            ArrayList<Integer> ids = new ArrayList<>(selectedMessagesIds[a].keySet());
                            messageObject = messagesDict[a].get(ids.get(0));
                        }
                        selectedMessagesIds[a].clear();
                        selectedMessagesCanCopyIds[a].clear();
                    }
                    if (messageObject != null && messageObject.messageOwner.id > 0) {
                        showReplyPanel(true, messageObject, null, null, false, true);
                    }
                    cantDeleteMessagesCount = 0;
                    actionBar.hideActionMode();
                    updateVisibleRows();
                } else if (id == chat_menu_attach) {
                    if (getParentActivity() == null) {
                        return;
                    }

                    if (chatAttachView == null) {
                        BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
                        chatAttachView = new ChatAttachView(getParentActivity());
                        chatAttachView.setDelegate(new ChatAttachView.ChatAttachViewDelegate() {
                            @Override
                            public void didPressedButton(int button) {
                                if (button == 7) {
                                    chatAttachViewSheet.dismiss();
                                    HashMap<Integer, MediaController.PhotoEntry> selectedPhotos = chatAttachView.getSelectedPhotos();
                                    if (!selectedPhotos.isEmpty()) {
                                        ArrayList<String> photos = new ArrayList<>();
                                        ArrayList<String> captions = new ArrayList<>();
                                        for (HashMap.Entry<Integer, MediaController.PhotoEntry> entry : selectedPhotos.entrySet()) {
                                            MediaController.PhotoEntry photoEntry = entry.getValue();
                                            if (photoEntry.imagePath != null) {
                                                photos.add(photoEntry.imagePath);
                                                captions.add(photoEntry.caption != null ? photoEntry.caption.toString() : null);
                                            } else if (photoEntry.path != null) {
                                                photos.add(photoEntry.path);
                                                captions.add(photoEntry.caption != null ? photoEntry.caption.toString() : null);
                                            }
                                            photoEntry.imagePath = null;
                                            photoEntry.thumbPath = null;
                                            photoEntry.caption = null;
                                        }
                                        SendMessagesHelper.prepareSendingPhotos(photos, null, dialog_id, replyingMessageObject, captions, chatActivityEnterView == null || chatActivityEnterView.asAdmin());
                                        showReplyPanel(false, null, null, null, false, true);
                                    }
                                    return;
                                } else {
                                    if (chatAttachViewSheet != null) {
                                        chatAttachViewSheet.dismissWithButtonClick(button);
                                    }
                                }
                                processSelectedAttach(button);
                            }
                        });
                        builder.setDelegate(new BottomSheet.BottomSheetDelegate() {

                            @Override
                            public void onRevealAnimationStart(boolean open) {
                                if (chatAttachView != null) {
                                    chatAttachView.onRevealAnimationStart(open);
                                }
                            }

                            @Override
                            public void onRevealAnimationProgress(boolean open, float radius, int x, int y) {
                                if (chatAttachView != null) {
                                    chatAttachView.onRevealAnimationProgress(open, radius, x, y);
                                }
                            }

                            @Override
                            public void onRevealAnimationEnd(boolean open) {
                                if (chatAttachView != null) {
                                    chatAttachView.onRevealAnimationEnd(open);
                                }
                            }

                            @Override
                            public void onOpenAnimationEnd() {
                                if (chatAttachView != null) {
                                    chatAttachView.onRevealAnimationEnd(true);
                                }
                            }

                            @Override
                            public View getRevealView() {
                                return menuItem;
                            }
                        });
                        builder.setApplyTopPaddings(false);
                        builder.setUseRevealAnimation();
                        builder.setCustomView(chatAttachView);
                        chatAttachViewSheet = builder.create();
                    }
                    if (Build.VERSION.SDK_INT == 21 || Build.VERSION.SDK_INT == 22) {
                        chatActivityEnterView.closeKeyboard();
                    }

                    chatAttachView.init(ChatActivityFantasy.this);
                    showDialog(chatAttachViewSheet);
                } else if (id == bot_help) {
                    SendMessagesHelper.getInstance().sendMessage("/help", dialog_id, null, null, false, chatActivityEnterView == null || chatActivityEnterView.asAdmin(), null, null);
                } else if (id == bot_settings) {
                    SendMessagesHelper.getInstance().sendMessage("/settings", dialog_id, null, null, false, chatActivityEnterView == null || chatActivityEnterView.asAdmin(), null, null);
                } else if (id == search) {
                    openSearchWithText(null);
                } else if (id == search_up) {
                    MessagesSearchQuery.searchMessagesInChat(null, dialog_id, mergeDialogId, classGuid, 1);
                } else if (id == search_down) {
                    MessagesSearchQuery.searchMessagesInChat(null, dialog_id, mergeDialogId, classGuid, 2);
                } else if (id == open_channel_profile) {
                    Bundle args = new Bundle();
                    args.putInt("chat_id", currentChat.id);
                    ProfileActivity fragment = new ProfileActivity(args);
                    fragment.setChatInfo(info);
                    fragment.setPlayProfileAnimation(true);
                    presentFragment(fragment);
                }
            }
        });

        avatarContainer = new FrameLayoutFixed(context);
        avatarContainer.setBackgroundResource(
                //R.drawable.bar_selector_f
                R.drawable.bar_selector_fantasy
        );
        avatarContainer.setPadding(AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8), 0);
        actionBar.addView(avatarContainer, 0, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, 56, 0, 40, 0));
        avatarContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioButton == null || radioButton.getVisibility() != View.VISIBLE) {
                    if (currentUser != null) {
                        Bundle args = new Bundle();
                        args.putInt("user_id", currentUser.id);
                        if (currentEncryptedChat != null) {
                            args.putLong("dialog_id", dialog_id);
                        }
                        ProfileActivity fragment = new ProfileActivity(args);
                        fragment.setPlayProfileAnimation(true);
                        presentFragment(fragment);
                    } else if (currentChat != null) {
                        Bundle args = new Bundle();
                        args.putInt("chat_id", currentChat.id);
                        ProfileActivity fragment = new ProfileActivity(args);
                        fragment.setChatInfo(info);
                        fragment.setPlayProfileAnimation(true);
                        presentFragment(fragment);
                    }
                } else {
                    switchImportantMode(null);
                }
            }
        });

        if (currentChat != null) {
            if (!ChatObject.isChannel(currentChat)) {
                int count = currentChat.participants_count;
                if (info != null) {
                    count = info.participants.participants.size();
                }
                if (count == 0 || currentChat.deactivated || currentChat.left || currentChat instanceof TLRPC.TL_chatForbidden || info != null && info.participants instanceof TLRPC.TL_chatParticipantsForbidden) {
                    avatarContainer.setEnabled(false);
                }
            }
        }

        avatarImageView = new BackupImageView(context);
        avatarImageView.setRoundRadius(AndroidUtilities.dp(21));
        avatarContainer.addView(avatarImageView, LayoutHelper.createFrame(42, 42, Gravity.TOP | Gravity.LEFT, 0, 3, 0, 0));

        if (currentEncryptedChat != null) {
            timeItem = new ImageView(context);
            timeItem.setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(10), AndroidUtilities.dp(5), AndroidUtilities.dp(5));
            timeItem.setScaleType(ImageView.ScaleType.CENTER);
            timeItem.setImageDrawable(timerDrawable = new TimerDrawable(context));
            avatarContainer.addView(timeItem, LayoutHelper.createFrame(34, 34, Gravity.TOP | Gravity.LEFT, 16, 18, 0, 0));
            timeItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    showDialog(AndroidUtilities.buildTTLAlert(getParentActivity(), currentEncryptedChat).create());
                }
            });
        }

        nameTextView = new TextView(context);
        nameTextView.setTextColor(0xff000000//0xffffffff
        );
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setCompoundDrawablePadding(AndroidUtilities.dp(4));
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        avatarContainer.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 54, 0, 0, 22));

        onlineTextView = new TextView(context);
        onlineTextView.setTextColor(0xff797979//0xffd7e8f7
        );
        onlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        onlineTextView.setLines(1);
        onlineTextView.setMaxLines(1);
        onlineTextView.setSingleLine(true);
        onlineTextView.setEllipsize(TextUtils.TruncateAt.END);
        onlineTextView.setGravity(Gravity.LEFT);


        if (ChatObject.isChannel(currentChat) && !currentChat.megagroup && !(currentChat instanceof TLRPC.TL_channelForbidden)) {
            radioButton = new RadioButton(context);
            radioButton.setChecked(channelMessagesImportant == 1, false);
            radioButton.setVisibility(View.GONE);
            avatarContainer.addView(radioButton, LayoutHelper.createFrame(24, 24, Gravity.LEFT | Gravity.BOTTOM, 50, 0, 0, 0));
            avatarContainer.addView(onlineTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 54, 0, 0, 4));
        } else {
            avatarContainer.addView(onlineTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 54, 0, 0, 4));
        }

        ActionBarMenu menu = actionBar.createMenu();

        if (currentEncryptedChat == null && !isBroadcast) {
            searchItem = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true, false).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            //:ramin

                @Override
                public void onSearchCollapse() {
                    avatarContainer.setVisibility(View.VISIBLE);
                    if (chatActivityEnterView.hasText()) {
                        if (headerItem != null) {
                            headerItem.setVisibility(View.GONE);
                        }
                        if (attachItem != null) {
                            attachItem.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (headerItem != null) {
                            headerItem.setVisibility(View.VISIBLE);
                        }
                        if (attachItem != null) {
                            attachItem.setVisibility(View.GONE);
                        }
                    }
                    searchItem.setVisibility(View.GONE);
                    //chatActivityEnterView.setVisibility(View.VISIBLE);
                    searchUpItem.clearAnimation();
                    searchDownItem.clearAnimation();
                    searchUpItem.setVisibility(View.GONE);
                    searchDownItem.setVisibility(View.GONE);
                    highlightMessageId = Integer.MAX_VALUE;
                    updateVisibleRows();
                    scrollToLastMessage(false);
                }

                @Override
                public void onSearchExpand() {
                    if (!openSearchKeyboard) {
                        return;
                    }
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            searchItem.getSearchField().requestFocus();
                            AndroidUtilities.showKeyboard(searchItem.getSearchField());
                        }
                    }, 300); //TODO find a better way to open keyboard
                }

                @Override
                public void onSearchPressed(EditText editText) {
                    updateSearchButtons(0);
                    MessagesSearchQuery.searchMessagesInChat(editText.getText().toString(), dialog_id, mergeDialogId, classGuid, 0);
                }
            });
            searchItem.getSearchField().setHint(LocaleController.getString("Search", R.string.Search));
            searchItem.setVisibility(View.GONE);
            //:ramin
            searchItem.iconView.setColorFilter(lightBlue);
            searchItem.getSearchField().setHintTextColor(grey);
            searchItem.getSearchField().setTextColor(Color.BLACK);

            searchUpItem = menu.addItem(search_up, R.drawable.search_up);
            searchUpItem.setVisibility(View.GONE);
            searchDownItem = menu.addItem(search_down, R.drawable.search_down);
            searchDownItem.setVisibility(View.GONE);

            searchUpItem.iconView.setColorFilter(lightBlue);
            searchDownItem.iconView.setColorFilter(lightBlue);
        }

        headerItem = menu.addItem(0, R.drawable.ic_ab_other);

        //:ramin
        headerItem.iconView.setColorFilter(lightBlue);




        if (channelMessagesImportant != 0 && !currentChat.megagroup) {
            headerItem.addSubItem(open_channel_profile, LocaleController.getString("OpenChannelProfile", R.string.OpenChannelProfile), 0);
        }
        if (searchItem != null) {
            headerItem.addSubItem(search, LocaleController.getString("Search", R.string.Search), 0);
        }
        if (currentUser != null) {
            addContactItem = headerItem.addSubItem(share_contact, "", 0);
        }
        if (currentEncryptedChat != null) {
            timeItem2 = headerItem.addSubItem(chat_enc_timer, LocaleController.getString("SetTimer", R.string.SetTimer), 0);
        }
        if (channelMessagesImportant == 0) {
            headerItem.addSubItem(clear_history, LocaleController.getString("ClearHistory", R.string.ClearHistory), 0);
            if (currentChat != null && !isBroadcast) {
                headerItem.addSubItem(delete_chat, LocaleController.getString("DeleteAndExit", R.string.DeleteAndExit), 0);
            } else {
                headerItem.addSubItem(delete_chat, LocaleController.getString("DeleteChatUser", R.string.DeleteChatUser), 0);
            }
        }
        muteItem = headerItem.addSubItem(mute, null, 0);
        if (currentUser != null && currentEncryptedChat == null && currentUser.bot) {
            headerItem.addSubItem(bot_settings, LocaleController.getString("BotSettings", R.string.BotSettings), 0);
            headerItem.addSubItem(bot_help, LocaleController.getString("BotHelp", R.string.BotHelp), 0);
            updateBotButtons();
        }

        updateTitle();
        updateSubtitle();
        updateTitleIcons();

        attachItem = menu.addItem(chat_menu_attach, R.drawable.ic_ab_other).setOverrideMenuClick(true).setAllowCloseAnimation(false);
        attachItem.setVisibility(View.GONE);
        menuItem = menu.addItem(chat_menu_attach, R.drawable.ic_ab_attach).setAllowCloseAnimation(false);
        menuItem.setBackgroundDrawable(null);

        menuItem.iconView.setColorFilter(lightBlue);

        actionModeViews.clear();

        final ActionBarMenu actionMode = actionBar.createActionMode();

        selectedMessagesCountTextView = new NumberTextView(actionMode.getContext());
        selectedMessagesCountTextView.setTextSize(18);
        selectedMessagesCountTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        selectedMessagesCountTextView.setTextColor(0xff737373);
        actionMode.addView(selectedMessagesCountTextView, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f, 65, 0, 0, 0));
        selectedMessagesCountTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        if (currentEncryptedChat == null) {
            if (!isBroadcast) {
                actionModeViews.add(actionMode.addItem(reply, R.drawable.ic_ab_reply, R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54)));
            }
            actionModeViews.add(actionMode.addItem(copy, R.drawable.ic_ab_fwd_copy, R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54)));
            actionModeViews.add(actionMode.addItem(share_qoute, R.drawable.ic_share_qoute, R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54)));
            actionModeViews.add(actionMode.addItem(forward, R.drawable.ic_ab_fwd_forward, R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54)));
            actionModeViews.add(actionMode.addItem(delete, R.drawable.ic_ab_fwd_delete, R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54)));
        } else {
            actionModeViews.add(actionMode.addItem(copy, R.drawable.ic_ab_fwd_copy, R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54)));
            actionModeViews.add(actionMode.addItem(delete, R.drawable.ic_ab_fwd_delete, R.drawable.bar_selector_mode, null, AndroidUtilities.dp(54)));
        }
        actionMode.getItem(copy).setVisibility(selectedMessagesCanCopyIds[0].size() + selectedMessagesCanCopyIds[1].size() != 0 ? View.VISIBLE : View.GONE);
        actionMode.getItem(delete).setVisibility(cantDeleteMessagesCount == 0 ? View.VISIBLE : View.GONE);
        checkActionBarMenu();

        fragmentView = new SizeNotifierFrameLayout(context) {

            int inputFieldHeight = 0;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);

                setMeasuredDimension(widthSize, heightSize);
                heightSize -= getPaddingTop();

                int keyboardSize = getKeyboardHeight();

                if (keyboardSize <= AndroidUtilities.dp(20)) {
                    heightSize -= chatActivityEnterView.getEmojiPadding();
                }

                int childCount = getChildCount();

                measureChildWithMargins(chatActivityEnterView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                inputFieldHeight = chatActivityEnterView.getMeasuredHeight();

                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child == null || child.getVisibility() == GONE || child == chatActivityEnterView) {
                        continue;
                    }
                    try {
                        if (child == chatListView || child == progressView) {
                            int contentWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                            int contentHeightSpec = MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10), heightSize - inputFieldHeight + AndroidUtilities.dp(2)), MeasureSpec.EXACTLY);
                            child.measure(contentWidthSpec, contentHeightSpec);
                        } else if (child == emptyViewContainer) {
                            int contentWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                            int contentHeightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
                            child.measure(contentWidthSpec, contentHeightSpec);
                        } else if (chatActivityEnterView.isPopupView(child)) {
                            child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, MeasureSpec.EXACTLY));
                        } else {
                            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                        }
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                final int count = getChildCount();

                int paddingBottom = getKeyboardHeight() <= AndroidUtilities.dp(20) ? chatActivityEnterView.getEmojiPadding() : 0;
                setBottomClip(paddingBottom);

                for (int i = 0; i < count; i++) {
                    final View child = getChildAt(i);
                    if (child.getVisibility() == GONE) {
                        continue;
                    }
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                    final int width = child.getMeasuredWidth();
                    final int height = child.getMeasuredHeight();

                    int childLeft;
                    int childTop;

                    int gravity = lp.gravity;
                    if (gravity == -1) {
                        gravity = Gravity.TOP | Gravity.LEFT;
                    }

                    final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                    final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                    switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                        case Gravity.CENTER_HORIZONTAL:
                            childLeft = (r - l - width) / 2 + lp.leftMargin - lp.rightMargin;
                            break;
                        case Gravity.RIGHT:
                            childLeft = r - width - lp.rightMargin;
                            break;
                        case Gravity.LEFT:
                        default:
                            childLeft = lp.leftMargin;
                    }

                    switch (verticalGravity) {
                        case Gravity.TOP:
                            childTop = lp.topMargin + getPaddingTop();
                            break;
                        case Gravity.CENTER_VERTICAL:
                            childTop = ((b - paddingBottom) - t - height) / 2 + lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            childTop = ((b - paddingBottom) - t) - height - lp.bottomMargin;
                            break;
                        default:
                            childTop = lp.topMargin;
                    }

                    if (child == mentionListView) {
                        childTop -= chatActivityEnterView.getMeasuredHeight() - AndroidUtilities.dp(2);
                    } else if (child == pagedownButton) {
                        childTop -= chatActivityEnterView.getMeasuredHeight();
                    } else if (child == emptyViewContainer) {
                        childTop -= inputFieldHeight / 2;
                    } else if (chatActivityEnterView.isPopupView(child)) {
                        childTop = chatActivityEnterView.getBottom();
                    } else if (child == gifHintTextView) {
                        childTop -= inputFieldHeight;
                    }
                    child.layout(childLeft, childTop, childLeft + width, childTop + height);
                }

                notifyHeightChanged();
            }
        };

        SizeNotifierFrameLayout contentView = (SizeNotifierFrameLayout) fragmentView;

        // :ramin chat background
        contentView.setBackgroundImage(null);
        contentView.setBackgroundColor(Color.WHITE);
        //contentView.setBackgroundResource(R.drawable.gradiant_bg);

        emptyViewContainer = new FrameLayout(context);
        emptyViewContainer.setVisibility(View.INVISIBLE);
        contentView.addView(emptyViewContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        emptyViewContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        if (currentEncryptedChat == null) {
            TextView emptyView = new TextView(context);
            if (currentUser != null && currentUser.id != 777000 && currentUser.id != 429000 && (currentUser.id / 1000 == 333 || currentUser.id % 1000 == 0)) {
                emptyView.setText(LocaleController.getString("GotAQuestion", R.string.GotAQuestion));
            } else {
                emptyView.setText(LocaleController.getString("NoMessages", R.string.NoMessages));
            }
            emptyView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setTextColor(0xffffffff);
            emptyView.setBackgroundResource(ApplicationLoader.isCustomTheme() ? R.drawable.system_black : R.drawable.system_blue);
            emptyView.setPadding(AndroidUtilities.dp(7), AndroidUtilities.dp(1), AndroidUtilities.dp(7), AndroidUtilities.dp(1));
            emptyViewContainer.addView(emptyView, new FrameLayout.LayoutParams(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        } else {
            LinearLayout secretChatPlaceholder = new LinearLayout(context);
            secretChatPlaceholder.setBackgroundResource(ApplicationLoader.isCustomTheme() ? R.drawable.system_black : R.drawable.system_blue);
            secretChatPlaceholder.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(12), AndroidUtilities.dp(16), AndroidUtilities.dp(12));
            secretChatPlaceholder.setOrientation(LinearLayout.VERTICAL);
            emptyViewContainer.addView(secretChatPlaceholder, new FrameLayout.LayoutParams(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

            secretViewStatusTextView = new TextView(context);
            secretViewStatusTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            secretViewStatusTextView.setTextColor(0xffffffff);
            secretViewStatusTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            secretViewStatusTextView.setMaxWidth(AndroidUtilities.dp(210));
            if (currentEncryptedChat.admin_id == UserConfig.getClientUserId()) {
                secretViewStatusTextView.setText(LocaleController.formatString("EncryptedPlaceholderTitleOutgoing", R.string.EncryptedPlaceholderTitleOutgoing, UserObject.getFirstName(currentUser)));
            } else {
                secretViewStatusTextView.setText(LocaleController.formatString("EncryptedPlaceholderTitleIncoming", R.string.EncryptedPlaceholderTitleIncoming, UserObject.getFirstName(currentUser)));
            }
            secretChatPlaceholder.addView(secretViewStatusTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.TOP));

            TextView textView = new TextView(context);
            textView.setText(LocaleController.getString("EncryptedDescriptionTitle", R.string.EncryptedDescriptionTitle));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            textView.setTextColor(0xffffffff);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setMaxWidth(AndroidUtilities.dp(260));
            secretChatPlaceholder.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 0, 8, 0, 0));

            for (int a = 0; a < 4; a++) {
                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                secretChatPlaceholder.addView(linearLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 0, 8, 0, 0));

                ImageView imageView = new ImageView(context);
                imageView.setImageResource(R.drawable.ic_lock_white);

                textView = new TextView(context);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                textView.setTextColor(0xffffffff);
                textView.setGravity(Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT));
                textView.setMaxWidth(AndroidUtilities.dp(260));

                switch (a) {
                    case 0:
                        textView.setText(LocaleController.getString("EncryptedDescription1", R.string.EncryptedDescription1));
                        break;
                    case 1:
                        textView.setText(LocaleController.getString("EncryptedDescription2", R.string.EncryptedDescription2));
                        break;
                    case 2:
                        textView.setText(LocaleController.getString("EncryptedDescription3", R.string.EncryptedDescription3));
                        break;
                    case 3:
                        textView.setText(LocaleController.getString("EncryptedDescription4", R.string.EncryptedDescription4));
                        break;
                }

                if (LocaleController.isRTL) {
                    linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
                    linearLayout.addView(imageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 8, 3, 0, 0));
                } else {
                    linearLayout.addView(imageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 4, 8, 0));
                    linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
                }
            }
        }

        if (chatActivityEnterView != null) {
            chatActivityEnterView.onDestroy();
        }

        chatListView = new RecyclerListView(context) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                super.onLayout(changed, l, t, r, b);
                if (chatAdapter.isBot/* || ChatObject.isChannel(currentChat) && currentChat.megagroup*/) {
                    int childCount = getChildCount();
                    for (int a = 0; a < childCount; a++) {
                        View child = getChildAt(a);
                        if (child instanceof BotHelpCell/* || child instanceof ChatMigrateCell*/) {
                            int height = b - t;
                            int top = height / 2 - child.getMeasuredHeight() / 2;
                            if (child.getTop() > top) {
                                child.layout(0, top, r - l, top + child.getMeasuredHeight());
                            }
                            break;
                        }
                    }
                }
            }
        };
        chatListView.setVerticalScrollBarEnabled(true);
        chatListView.setAdapter(chatAdapter = new ChatActivityAdapter(context));
        chatListView.setClipToPadding(false);
        chatListView.setPadding(0, AndroidUtilities.dp(4), 0, AndroidUtilities.dp(3));
        chatListView.setItemAnimator(null);
        chatListView.setLayoutAnimation(null);
        chatLayoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        chatLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatLayoutManager.setStackFromEnd(true);
        chatListView.setLayoutManager(chatLayoutManager);
        contentView.addView(chatListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        chatListView.setOnItemLongClickListener(onItemLongClickListener);
        chatListView.setOnItemClickListener(onItemClickListener);
        chatListView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && highlightMessageId != Integer.MAX_VALUE) {
                    highlightMessageId = Integer.MAX_VALUE;
                    updateVisibleRows();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                checkScrollForLoad();
                int firstVisibleItem = chatLayoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = firstVisibleItem == RecyclerView.NO_POSITION ? 0 : Math.abs(chatLayoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                if (visibleItemCount > 0) {
                    int totalItemCount = chatAdapter.getItemCount();
                    if (firstVisibleItem + visibleItemCount == totalItemCount && forwardEndReached[0]) {
                        showPagedownButton(false, true);
                    }
                }
                updateMessagesVisisblePart();
            }
        });
        chatListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (openSecretPhotoRunnable != null || SecretPhotoViewer.getInstance().isVisible()) {
                    if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_POINTER_UP) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                chatListView.setOnItemClickListener(onItemClickListener);
                            }
                        }, 150);
                        if (openSecretPhotoRunnable != null) {
                            AndroidUtilities.cancelRunOnUIThread(openSecretPhotoRunnable);
                            openSecretPhotoRunnable = null;
                            try {
                                Toast.makeText(v.getContext(), LocaleController.getString("PhotoTip", R.string.PhotoTip), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                FileLog.e("tmessages", e);
                            }
                        } else if (SecretPhotoViewer.getInstance().isVisible()) {
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    chatListView.setOnItemLongClickListener(onItemLongClickListener);
                                    chatListView.setLongClickable(true);
                                }
                            });
                            SecretPhotoViewer.getInstance().closePhoto();
                        }
                    } else if (event.getAction() != MotionEvent.ACTION_DOWN) {
                        if (SecretPhotoViewer.getInstance().isVisible()) {
                            return true;
                        } else if (openSecretPhotoRunnable != null) {
                            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                                if (Math.hypot(startX - event.getX(), startY - event.getY()) > AndroidUtilities.dp(5)) {
                                    AndroidUtilities.cancelRunOnUIThread(openSecretPhotoRunnable);
                                    openSecretPhotoRunnable = null;
                                }
                            } else {
                                AndroidUtilities.cancelRunOnUIThread(openSecretPhotoRunnable);
                                openSecretPhotoRunnable = null;
                            }
                            chatListView.setOnItemClickListener(onItemClickListener);
                            chatListView.setOnItemLongClickListener(onItemLongClickListener);
                            chatListView.setLongClickable(true);
                        }
                    }
                }
                return false;
            }
        });
        chatListView.setOnInterceptTouchListener(new RecyclerListView.OnInterceptTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent event) {
                if (actionBar.isActionModeShowed()) {
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    int count = chatListView.getChildCount();
                    for (int a = 0; a < count; a++) {
                        View view = chatListView.getChildAt(a);
                        int top = view.getTop();
                        int bottom = view.getBottom();
                        if (top > y || bottom < y) {
                            continue;
                        }
                        if (!(view instanceof ChatMediaCell)) {
                            break;
                        }
                        final ChatMediaCell cell = (ChatMediaCell) view;
                        final MessageObject messageObject = cell.getMessageObject();
                        if (messageObject == null || messageObject.isSending() || !messageObject.isSecretPhoto() || !cell.getPhotoImage().isInsideImage(x, y - top)) {
                            break;
                        }
                        File file = FileLoader.getPathToMessage(messageObject.messageOwner);
                        if (!file.exists()) {
                            break;
                        }
                        startX = x;
                        startY = y;
                        chatListView.setOnItemClickListener(null);
                        openSecretPhotoRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if (openSecretPhotoRunnable == null) {
                                    return;
                                }
                                chatListView.requestDisallowInterceptTouchEvent(true);
                                chatListView.setOnItemLongClickListener(null);
                                chatListView.setLongClickable(false);
                                openSecretPhotoRunnable = null;
                                if (sendSecretMessageRead(messageObject)) {
                                    cell.invalidate();
                                }
                                SecretPhotoViewer.getInstance().setParentActivity(getParentActivity());
                                SecretPhotoViewer.getInstance().openPhoto(messageObject);
                            }
                        };
                        AndroidUtilities.runOnUIThread(openSecretPhotoRunnable, 100);
                        return true;
                    }
                }
                return false;
            }
        });

        progressView = new FrameLayout(context);
        progressView.setVisibility(View.INVISIBLE);
        contentView.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));

        View view = new View(context);
        view.setBackgroundResource(ApplicationLoader.isCustomTheme() ? R.drawable.system_loader2 : R.drawable.system_loader1);
        progressView.addView(view, LayoutHelper.createFrame(36, 36, Gravity.CENTER));

        ProgressBar progressBar = new ProgressBar(context);
        try {
            progressBar.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.loading_animation));
        } catch (Exception e) {
            //don't promt
        }
        progressBar.setIndeterminate(true);
        AndroidUtilities.setProgressBarAnimationDuration(progressBar, 1500);
        progressView.addView(progressBar, LayoutHelper.createFrame(32, 32, Gravity.CENTER));

        reportSpamView = new LinearLayout(context);
        reportSpamView.setVisibility(View.GONE);
        reportSpamView.setBackgroundResource(R.drawable.blockpanel);
        contentView.addView(reportSpamView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 50, Gravity.TOP | Gravity.LEFT));

        addToContactsButton = new TextView(context);
        addToContactsButton.setTextColor(0xff4a82b5);
        addToContactsButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        addToContactsButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        addToContactsButton.setSingleLine(true);
        addToContactsButton.setMaxLines(1);
        addToContactsButton.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
        addToContactsButton.setGravity(Gravity.CENTER);
        addToContactsButton.setText(LocaleController.getString("AddContactChat", R.string.AddContactChat));
        reportSpamView.addView(addToContactsButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 0.5f, Gravity.LEFT | Gravity.TOP, 0, 0, 0, AndroidUtilities.dp(1)));
        addToContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt("user_id", currentUser.id);
                args.putBoolean("addContact", true);
                presentFragment(new ContactAddActivity(args));
            }
        });

        reportSpamContainer = new FrameLayout(context);
        reportSpamView.addView(reportSpamContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 0.5f, Gravity.LEFT | Gravity.TOP));

        reportSpamButton = new TextView(context);
        reportSpamButton.setTextColor(0xffcf5957);
        reportSpamButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        reportSpamButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        reportSpamButton.setSingleLine(true);
        reportSpamButton.setMaxLines(1);
        reportSpamButton.setText(LocaleController.getString("ReportSpam", R.string.ReportSpam));
        reportSpamButton.setGravity(Gravity.CENTER);
        reportSpamButton.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(50), 0);
        reportSpamContainer.addView(reportSpamButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
        reportSpamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reportSpamUser == null || getParentActivity() == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                if (currentChat != null) {
                    builder.setMessage(LocaleController.getString("ReportSpamAlertGroup", R.string.ReportSpamAlertGroup));
                } else {
                    builder.setMessage(LocaleController.getString("ReportSpamAlert", R.string.ReportSpamAlert));
                }
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (reportSpamUser == null) {
                            return;
                        }
                        TLRPC.TL_messages_reportSpam req = new TLRPC.TL_messages_reportSpam();
                        req.peer = new TLRPC.TL_inputPeerUser();
                        req.peer.user_id = reportSpamUser.id;
                        req.peer.access_hash = reportSpamUser.access_hash;
                        MessagesController.getInstance().blockUser(reportSpamUser.id);
                        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                            @Override
                            public void run(TLObject response, TLRPC.TL_error error) {
                                if (error == null) {
                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                            preferences.edit().putBoolean("spam_" + dialog_id, true).commit();
                                            updateSpamView();
                                        }
                                    });
                                }
                            }
                        }, ConnectionsManager.RequestFlagFailOnServerErrors);
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            }
        });

        ImageView closeReportSpam = new ImageView(context);
        closeReportSpam.setImageResource(R.drawable.delete_reply);
        closeReportSpam.setScaleType(ImageView.ScaleType.CENTER);
        reportSpamContainer.addView(closeReportSpam, LayoutHelper.createFrame(48, 48, Gravity.RIGHT | Gravity.TOP));
        closeReportSpam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                preferences.edit().putBoolean("spam_" + dialog_id, true).commit();
                updateSpamView();
            }
        });

        if (currentEncryptedChat == null && !isBroadcast) {
            mentionListView = new RecyclerListView(context);
            mentionLayoutManager = new LinearLayoutManager(context) {
                @Override
                public boolean supportsPredictiveItemAnimations() {
                    return false;
                }
            };
            mentionLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mentionListView.setLayoutManager(mentionLayoutManager);

            mentionListView.setBackgroundResource(R.drawable.compose_panel);
            mentionListView.setVisibility(View.GONE);
            mentionListView.setPadding(0, AndroidUtilities.dp(2), 0, 0);
            mentionListView.setClipToPadding(true);
            mentionListView.setDisallowInterceptTouchEvents(true);
            if (Build.VERSION.SDK_INT > 8) {
                mentionListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
            }
            contentView.addView(mentionListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 110, Gravity.LEFT | Gravity.BOTTOM));

            mentionListView.setAdapter(mentionsAdapter = new MentionsAdapter(context, false, new MentionsAdapter.MentionsAdapterDelegate() {
                @Override
                public void needChangePanelVisibility(boolean show) {
                    if (show) {
                        int orientation = mentionsAdapter.getOrientation();

                        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) mentionListView.getLayoutParams();
                        int height;
                        if (orientation == LinearLayoutManager.HORIZONTAL) {
                            mentionListView.setPadding(0, AndroidUtilities.dp(2), AndroidUtilities.dp(5), 0);
                            mentionListView.setClipToPadding(false);
                            height = 90;
                        } else {
                            mentionListView.setPadding(0, AndroidUtilities.dp(2), 0, 0);
                            mentionListView.setClipToPadding(true);
                            if (mentionsAdapter.isBotContext()) {
                                height = 36 * 3 + 18;
                            } else {
                                height = 36 * Math.min(3, mentionsAdapter.getItemCount()) + (mentionsAdapter.getItemCount() > 3 ? 18 : 0);
                            }
                        }
                        layoutParams3.height = AndroidUtilities.dp(2 + height);
                        layoutParams3.topMargin = -AndroidUtilities.dp(height);
                        mentionListView.setLayoutParams(layoutParams3);

                        mentionLayoutManager.setOrientation(orientation);

                        if (mentionListAnimation != null) {
                            mentionListAnimation.cancel();
                            mentionListAnimation = null;
                        }

                        if (mentionListView.getVisibility() == View.VISIBLE) {
                            ViewProxy.setAlpha(mentionListView, 1.0f);
                            return;
                        } else {
                            mentionLayoutManager.scrollToPositionWithOffset(0, 10000);
                        }
                        if (allowStickersPanel && (!mentionsAdapter.isBotContext() || (allowContextBotPanel || allowContextBotPanelSecond))) {
                            mentionListView.setVisibility(View.VISIBLE);
                            mentionListView.setTag(null);
                            mentionListAnimation = new AnimatorSetProxy();
                            mentionListAnimation.playTogether(
                                    ObjectAnimatorProxy.ofFloat(mentionListView, "alpha", 0.0f, 1.0f)
                            );
                            mentionListAnimation.addListener(new AnimatorListenerAdapterProxy() {
                                @Override
                                public void onAnimationEnd(Object animation) {
                                    if (mentionListAnimation != null && mentionListAnimation.equals(animation)) {
                                        mentionListView.clearAnimation();
                                        mentionListAnimation = null;
                                    }
                                }
                            });
                            mentionListAnimation.setDuration(200);
                            mentionListAnimation.start();
                        } else {
                            ViewProxy.setAlpha(mentionListView, 1.0f);
                            mentionListView.clearAnimation();
                            mentionListView.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        if (mentionListAnimation != null) {
                            mentionListAnimation.cancel();
                            mentionListAnimation = null;
                        }

                        if (mentionListView.getVisibility() == View.GONE) {
                            return;
                        }
                        if (allowStickersPanel) {
                            mentionListAnimation = new AnimatorSetProxy();
                            mentionListAnimation.playTogether(
                                    ObjectAnimatorProxy.ofFloat(mentionListView, "alpha", 0.0f)
                            );
                            mentionListAnimation.addListener(new AnimatorListenerAdapterProxy() {
                                @Override
                                public void onAnimationEnd(Object animation) {
                                    if (mentionListAnimation != null && mentionListAnimation.equals(animation)) {
                                        mentionListView.clearAnimation();
                                        mentionListView.setVisibility(View.GONE);
                                        mentionListView.setTag(null);
                                        mentionListAnimation = null;
                                    }
                                }
                            });
                            mentionListAnimation.setDuration(200);
                            mentionListAnimation.start();
                        } else {
                            mentionListView.setTag(null);
                            mentionListView.clearAnimation();
                            mentionListView.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onContextSearch(boolean searching) {
                    if (chatActivityEnterView != null) {
                        chatActivityEnterView.setCaption(mentionsAdapter.getBotCaption());
                        chatActivityEnterView.showContextProgress(searching);
                    }
                }

                @Override
                public void onContextClick(TLRPC.BotInlineResult result) {
                    if (getParentActivity() == null || result.content_url == null) {
                        return;
                    }
                    if (result.type.equals("video") || result.type.equals("web_player_video")) {
                        BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
                        builder.setCustomView(new WebFrameLayout(getParentActivity(), builder.create(), result.title != null ? result.title : "", result.content_url, result.content_url, result.w, result.h));
                        builder.setUseFullWidth(true);
                        showDialog(builder.create());
                    } else {
                        try {
                            Uri uri = Uri.parse(result.content_url);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            intent.putExtra(Browser.EXTRA_APPLICATION_ID, getParentActivity().getPackageName());
                            getParentActivity().startActivity(intent);
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                    }
                }
            }));
            mentionsAdapter.setBotInfo(botInfo);
            mentionsAdapter.setChatInfo(info);
            mentionsAdapter.setNeedUsernames(currentChat != null);
            mentionsAdapter.setNeedBotContext(currentEncryptedChat == null);
            mentionsAdapter.setBotsCount(currentChat != null ? botsCount : 1);
            mentionListView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Object object = mentionsAdapter.getItem(position);
                    int start = mentionsAdapter.getResultStartPosition();
                    int len = mentionsAdapter.getResultLength();
                    if (object instanceof TLRPC.User) {
                        TLRPC.User user = (TLRPC.User) object;
                        if (user != null) {
                            chatActivityEnterView.replaceWithText(start, len, "@" + user.username + " ");
                        }
                    } else if (object instanceof String) {
                        if (mentionsAdapter.isBotCommands()) {
                            SendMessagesHelper.getInstance().sendMessage((String) object, dialog_id, null, null, false, chatActivityEnterView == null || chatActivityEnterView.asAdmin(), null, null);
                            chatActivityEnterView.setFieldText("");
                        } else {
                            chatActivityEnterView.replaceWithText(start, len, object + " ");
                        }
                    } else if (object instanceof TLRPC.BotInlineResult) {
                        String text = chatActivityEnterView.getFieldText();
                        if (text == null) {
                            return;
                        }
                        TLRPC.BotInlineResult result = (TLRPC.BotInlineResult) object;
                        HashMap<String, String> params = new HashMap<>();
                        params.put("id", result.id);
                        params.put("query_id", "" + result.query_id);
                        params.put("bot", "" + mentionsAdapter.getContextBotId());
                        mentionsAdapter.addRecentBot();
                        SendMessagesHelper.prepareSendingBotContextResult(result, params, dialog_id, replyingMessageObject, chatActivityEnterView == null || chatActivityEnterView.asAdmin());
                        chatActivityEnterView.setFieldText("");
                        showReplyPanel(false, null, null, null, false, true);
                    }
                }
            });

            mentionListView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener() {
                @Override
                public boolean onItemClick(View view, int position) {
                    if (!mentionsAdapter.isLongClickEnabled()) {
                        return false;
                    }
                    Object object = mentionsAdapter.getItem(position);
                    if (object instanceof String) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("ClearSearch", R.string.ClearSearch));
                        builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton).toUpperCase(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mentionsAdapter.clearRecentHashtags();
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                        return true;
                    }
                    return false;
                }
            });

            mentionListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    int firstVisibleItem = mentionLayoutManager.findFirstVisibleItemPosition();
                    int visibleItemCount = firstVisibleItem == RecyclerView.NO_POSITION ? 0 : firstVisibleItem;
                    if (visibleItemCount > 0 && firstVisibleItem > mentionsAdapter.getItemCount() - 5) {
                        mentionsAdapter.searchForContextBotForNextOffset();
                    }
                }
            });
        }

        pagedownButton = new ImageView(context);
        pagedownButton.setVisibility(View.INVISIBLE);
        pagedownButton.setImageResource(R.drawable.pagedown);
        contentView.addView(pagedownButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 6, 4));
        pagedownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (returnToMessageId > 0) {
                    scrollToMessageId(returnToMessageId, 0, true, 0);
                } else {
                    scrollToLastMessage(true);
                }
            }
        });

        chatActivityEnterView = new ChatActivityEnterView(getParentActivity(), contentView, this, true);
        // :ramin
        //chatActivityEnterView.setBackgroundResource(0);
        //chatActivityEnterView.setBackgroundColor(ColorUtils.getColor(R.color.bg_light));

        chatActivityEnterView.setDialogId(dialog_id);
        chatActivityEnterView.addToAttachLayout(menuItem);
        chatActivityEnterView.setId(id_chat_compose_panel);
        chatActivityEnterView.setBotsCount(botsCount, hasBotsCommands);
        contentView.addView(chatActivityEnterView, contentView.getChildCount() - 1, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM));
        chatActivityEnterView.setDelegate(new ChatActivityEnterView.ChatActivityEnterViewDelegate() {
            @Override
            public void onMessageSend(String message) {
                moveScrollToLastMessage();
                showReplyPanel(false, null, null, null, false, true);
                if (mentionsAdapter != null) {
                    mentionsAdapter.addHashtagsFromMessage(message);
                }
            }

            @Override
            public void onTextChanged(final CharSequence text, boolean bigChange) {
                if (stickersAdapter != null) {
                    stickersAdapter.loadStikersForEmoji(text);
                }
                if (mentionsAdapter != null) {
                    mentionsAdapter.searchUsernameOrHashtag(text.toString(), chatActivityEnterView.getCursorPosition(), messages);
                }
                if (waitingForCharaterEnterRunnable != null) {
                    AndroidUtilities.cancelRunOnUIThread(waitingForCharaterEnterRunnable);
                    waitingForCharaterEnterRunnable = null;
                }
                if (chatActivityEnterView.isMessageWebPageSearchEnabled()) {
                    if (bigChange) {
                        searchLinks(text, true);
                    } else {
                        waitingForCharaterEnterRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if (this == waitingForCharaterEnterRunnable) {
                                    searchLinks(text, false);
                                    waitingForCharaterEnterRunnable = null;
                                }
                            }
                        };
                        AndroidUtilities.runOnUIThread(waitingForCharaterEnterRunnable, AndroidUtilities.WEB_URL == null ? 3000 : 1000);
                    }
                }
            }

            @Override
            public void needSendTyping() {
                MessagesController.getInstance().sendTyping(dialog_id, 0, classGuid);
            }

            @Override
            public void onAttachButtonHidden() {
                if (actionBar.isSearchFieldVisible()) {
                    return;
                }
                if (attachItem != null) {
                    attachItem.setVisibility(View.VISIBLE);
                }
                if (headerItem != null) {
                    headerItem.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAttachButtonShow() {
                if (actionBar.isSearchFieldVisible()) {
                    return;
                }
                if (attachItem != null) {
                    attachItem.setVisibility(View.GONE);
                }
                if (headerItem != null) {
                    headerItem.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onWindowSizeChanged(int size) {
                if (size < AndroidUtilities.dp(72) + ActionBar.getCurrentActionBarHeight()) {
                    allowStickersPanel = false;
                    if (stickersPanel.getVisibility() == View.VISIBLE) {
                        stickersPanel.clearAnimation();
                        stickersPanel.setVisibility(View.INVISIBLE);
                    }
                    if (mentionListView != null && mentionListView.getVisibility() == View.VISIBLE) {
                        mentionListView.clearAnimation();
                        mentionListView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    allowStickersPanel = true;
                    if (stickersPanel.getVisibility() == View.INVISIBLE) {
                        stickersPanel.clearAnimation();
                        stickersPanel.setVisibility(View.VISIBLE);
                    }
                    if (mentionListView != null && mentionListView.getVisibility() == View.INVISIBLE && (!mentionsAdapter.isBotContext() || (allowContextBotPanel || allowContextBotPanelSecond))) {
                        mentionListView.clearAnimation();
                        mentionListView.setVisibility(View.VISIBLE);
                        mentionListView.setTag(null);
                    }
                }
                allowContextBotPanel = !chatActivityEnterView.isPopupShowing();
                checkContextBotPanel();
                updateMessagesVisisblePart();
            }

            @Override
            public void onStickersTab(boolean opened) {
                if (emojiButtonRed != null) {
                    emojiButtonRed.setVisibility(View.GONE);
                }
                allowContextBotPanelSecond = !opened;
                checkContextBotPanel();
            }
        });

        FrameLayout replyLayout = new FrameLayout(context);
        replyLayout.setClickable(true);
        chatActivityEnterView.addTopView(replyLayout, 48);

        View lineView = new View(context);
        lineView.setBackgroundColor(0xffe8e8e8);
        replyLayout.addView(lineView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1, Gravity.BOTTOM | Gravity.LEFT));

        replyIconImageView = new ImageView(context);
        replyIconImageView.setScaleType(ImageView.ScaleType.CENTER);
        replyLayout.addView(replyIconImageView, LayoutHelper.createFrame(52, 46, Gravity.TOP | Gravity.LEFT));

        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.delete_reply);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        replyLayout.addView(imageView, LayoutHelper.createFrame(52, 46, Gravity.RIGHT | Gravity.TOP, 0, 0.5f, 0, 0));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (forwardingMessages != null) {
                    forwardingMessages.clear();
                }
                showReplyPanel(false, null, null, foundWebPage, true, true);
            }
        });

        replyNameTextView = new TextView(context);
        replyNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        replyNameTextView.setTextColor(0xff377aae);
        replyNameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        replyNameTextView.setSingleLine(true);
        replyNameTextView.setEllipsize(TextUtils.TruncateAt.END);
        replyNameTextView.setMaxLines(1);
        replyLayout.addView(replyNameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 52, 4, 52, 0));

        replyObjectTextView = new TextView(context);
        replyObjectTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        replyObjectTextView.setTextColor(0xff999999);
        replyObjectTextView.setSingleLine(true);
        replyObjectTextView.setEllipsize(TextUtils.TruncateAt.END);
        replyObjectTextView.setMaxLines(1);
        replyLayout.addView(replyObjectTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 52, 22, 52, 0));

        replyImageView = new BackupImageView(context);
        replyLayout.addView(replyImageView, LayoutHelper.createFrame(34, 34, Gravity.TOP | Gravity.LEFT, 52, 6, 0, 0));

        stickersPanel = new FrameLayout(context);
        stickersPanel.setVisibility(View.GONE);
        contentView.addView(stickersPanel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 81.5f, Gravity.LEFT | Gravity.BOTTOM, 0, 0, 0, 38));

        stickersListView = new RecyclerListView(context);
        stickersListView.setDisallowInterceptTouchEvents(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        stickersListView.setLayoutManager(layoutManager);
        stickersListView.setClipToPadding(false);
        if (Build.VERSION.SDK_INT >= 9) {
            stickersListView.setOverScrollMode(RecyclerListView.OVER_SCROLL_NEVER);
        }
        stickersPanel.addView(stickersListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 78));
        if (currentEncryptedChat == null || AndroidUtilities.getPeerLayerVersion(currentEncryptedChat.layer) >= 23) {
            chatActivityEnterView.setAllowStickersAndGifs(true, currentEncryptedChat == null);
            if (stickersAdapter != null) {
                stickersAdapter.onDestroy();
            }
            stickersListView.setPadding(AndroidUtilities.dp(18), 0, AndroidUtilities.dp(18), 0);
            stickersListView.setAdapter(stickersAdapter = new StickersAdapter(context, new StickersAdapter.StickersAdapterDelegate() {
                @Override
                public void needChangePanelVisibility(final boolean show) {
                    if (show && stickersPanel.getVisibility() == View.VISIBLE || !show && stickersPanel.getVisibility() == View.GONE) {
                        return;
                    }
                    if (show) {
                        stickersListView.scrollToPosition(0);
                        stickersPanel.clearAnimation();
                        stickersPanel.setVisibility(allowStickersPanel ? View.VISIBLE : View.INVISIBLE);
                    }
                    if (runningAnimation != null) {
                        runningAnimation.cancel();
                        runningAnimation = null;
                    }
                    if (stickersPanel.getVisibility() != View.INVISIBLE) {
                        runningAnimation = new AnimatorSetProxy();
                        runningAnimation.playTogether(
                                ObjectAnimatorProxy.ofFloat(stickersPanel, "alpha", show ? 0.0f : 1.0f, show ? 1.0f : 0.0f)
                        );
                        runningAnimation.setDuration(150);
                        runningAnimation.addListener(new AnimatorListenerAdapterProxy() {
                            @Override
                            public void onAnimationEnd(Object animation) {
                                if (runningAnimation != null && runningAnimation.equals(animation)) {
                                    if (!show) {
                                        stickersAdapter.clearStickers();
                                        stickersPanel.clearAnimation();
                                        stickersPanel.setVisibility(View.GONE);
                                    }
                                    runningAnimation = null;
                                }
                            }
                        });
                        runningAnimation.start();
                    } else if (!show) {
                        stickersPanel.setVisibility(View.GONE);
                    }
                }
            }));
            stickersListView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    TLRPC.Document document = stickersAdapter.getItem(position);
                    if (document instanceof TLRPC.TL_document) {
                        //:ramin
                        Utils.sendSticker(ChatActivityFantasy.this, document, dialog_id, replyingMessageObject, chatActivityEnterView == null || chatActivityEnterView.asAdmin());
                        //SendMessagesHelper.getInstance().sendSticker(document, dialog_id, replyingMessageObject, chatActivityEnterView == null || chatActivityEnterView.asAdmin());
                        //showReplyPanel(false, null, null, null, false, true);
                    }else
                        chatActivityEnterView.setFieldText("");
                }
            });
        }

        imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.stickers_back_arrow);
        stickersPanel.addView(imageView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 53, 0, 0, 0));

        bottomOverlay = new FrameLayout(context);
        bottomOverlay.setBackgroundColor(0xffffffff);
        bottomOverlay.setVisibility(View.INVISIBLE);
        bottomOverlay.setFocusable(true);
        bottomOverlay.setFocusableInTouchMode(true);
        bottomOverlay.setClickable(true);
        contentView.addView(bottomOverlay, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM));

        bottomOverlayText = new TextView(context);
        bottomOverlayText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        bottomOverlayText.setTextColor(0xff7f7f7f);
        bottomOverlay.addView(bottomOverlayText, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        bottomOverlayChat = new FrameLayout(context);
        bottomOverlayChat.setBackgroundColor(0xfffbfcfd);
        bottomOverlayChat.setVisibility(View.INVISIBLE);
        contentView.addView(bottomOverlayChat, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM));
        bottomOverlayChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getParentActivity() == null) {
                    return;
                }
                AlertDialog.Builder builder = null;
                if (currentUser != null && userBlocked) {
                    if (currentUser.bot) {
                        String botUserLast = botUser;
                        botUser = null;
                        MessagesController.getInstance().unblockUser(currentUser.id);
                        if (botUserLast != null && botUserLast.length() != 0) {
                            MessagesController.getInstance().sendBotStart(currentUser, botUserLast);
                        } else {
                            SendMessagesHelper.getInstance().sendMessage("/start", dialog_id, null, null, false, chatActivityEnterView == null || chatActivityEnterView.asAdmin(), null, null);
                        }
                    } else {
                        builder = new AlertDialog.Builder(getParentActivity());
                        builder.setMessage(LocaleController.getString("AreYouSureUnblockContact", R.string.AreYouSureUnblockContact));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MessagesController.getInstance().unblockUser(currentUser.id);
                            }
                        });
                    }
                } else if (currentUser != null && currentUser.bot && botUser != null) {
                    if (botUser.length() != 0) {
                        MessagesController.getInstance().sendBotStart(currentUser, botUser);
                    } else {
                        SendMessagesHelper.getInstance().sendMessage("/start", dialog_id, null, null, false, chatActivityEnterView == null || chatActivityEnterView.asAdmin(), null, null);
                    }
                    botUser = null;
                    updateBottomOverlay();
                } else {
                    if (ChatObject.isChannel(currentChat) && !currentChat.megagroup && !(currentChat instanceof TLRPC.TL_channelForbidden)) {
                        if (ChatObject.isNotInChat(currentChat)) {
                            MessagesController.getInstance().addUserToChat(currentChat.id, UserConfig.getCurrentUser(), null, 0, null, null);
                        } else {
                            toggleMute(true);
                        }
                    } else {
                        builder = new AlertDialog.Builder(getParentActivity());
                        builder.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", R.string.AreYouSureDeleteThisChat));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MessagesController.getInstance().deleteDialog(dialog_id, 0);
                                finishFragment();
                            }
                        });
                    }
                }
                if (builder != null) {
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                }
            }
        });

        bottomOverlayChatText = new TextView(context);
        bottomOverlayChatText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        bottomOverlayChatText.setTextColor(0xff3e6fa1);
        bottomOverlayChat.addView(bottomOverlayChatText, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        chatAdapter.updateRows();
        if (loading && messages.isEmpty()) {
            progressView.setVisibility(chatAdapter.botInfoRow == -1 ? View.VISIBLE : View.INVISIBLE);
            chatListView.setEmptyView(null);
        } else {
            progressView.setVisibility(View.INVISIBLE);
            chatListView.setEmptyView(emptyViewContainer);
        }

        chatActivityEnterView.setButtons(userBlocked ? null : botButtons);

        if (!AndroidUtilities.isTablet() || AndroidUtilities.isSmallTablet()) {
            contentView.addView(playerView = new PlayerView(context, this), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
        }

        updateContactStatus();
        updateBottomOverlay();
        updateSecretStatus();
        updateSpamView();


        //:ramin
        //headerItem.iconView.setColorFilter(ColorUtils.getColor(R.color.rdivider));
        //actionBar.setBackgroundColor(ColorUtils.getColor(R.color.bg_dark));

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        //if(t)
          //  throw new RuntimeException("test exception 108");
    }

    @Override
    public BackDrawable getBackDrawable(boolean close) {
        return new BackDrawable(close).changeColor(lightBlue);
    }
}
