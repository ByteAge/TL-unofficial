package chitchat.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tisad.chitchat2.R;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

import chitchat.NightModeUtil;
import chitchat.skin.SkinMan;

/**
 * finds items by id
 * Created by RaminBT on 14/01/2016.
 */
public class IdFinderActivity extends BaseFragment {

    public View done;
    public EditText edtId;
    public TextView tvHint, tvNotify;

    private void setBack(View v, String tag){
        ImageView bot= (ImageView) v.findViewWithTag(tag);
        if(bot!=null)
            bot.setColorFilter(SkinMan.currentSkin.drawerNamesColor());
    }

    @Override
    public View createView(Context context) {

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("IdFinder", R.string.IdFinder));

        done=actionBar.createMenu().addItemWithWidth(4, R.drawable.ic_done, AndroidUtilities.dp(56f));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if(id==-1){
                    finishFragment();
                    return;
                }
                if (id == 4) {

                    String text=edtId.getText().toString();
                    MessagesController.openByUserName(text, IdFinderActivity.this, 0);
                }
            }
        });


        fragmentView=new LinearLayout(context);
        LinearLayout f= (LinearLayout) fragmentView;
        View inflated=LayoutInflater.from(context).inflate(R.layout.id_finder, null);
        edtId= (EditText) inflated.findViewById(R.id.EditTextId);
        tvHint= (TextView) inflated.findViewById(R.id.TextViewHint);
        tvNotify= (TextView) inflated.findViewById(R.id.TextViewStatus);
        f.addView(inflated, -1, -1);

        setBack(inflated, "bot");
        setBack(inflated, "topar");
        setBack(inflated, "channel");
        setBack(inflated, "contact");

        edtId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkUserName(s.toString(), false);
            }
        });

        if(LocaleController.isRTL){
            tvNotify.setGravity(Gravity.RIGHT);
            tvHint.setGravity(Gravity.RIGHT);
        }



        edtId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE && done!=null){
                    done.performClick();
                    return true;
                }
                return false;
            }
        });




        NightModeUtil.dark(fragmentView);
        return fragmentView;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (!ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getBoolean("view_animations", true))
        {
            edtId.requestFocus();
            AndroidUtilities.showKeyboard(edtId);
        }
    }

    @Override
    protected void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if(isOpen){
            edtId.requestFocus();
            AndroidUtilities.showKeyboard(edtId);
        }
    }




    private boolean lastNameAvailable = false;
    private int checkReqId = 0;
    private String lastCheckName = null, currentName=null;
    private Runnable checkRunnable = null;
    private boolean checkUserName(final String name, boolean alert) {
        /*if (name != null && name.length() > 0) {
            tvNotify.setVisibility(View.VISIBLE);
        } else {
            tvNotify.setVisibility(View.GONE);
        }*/
        if (alert && name.length() == 0) {
            return true;
        }
        currentName=name;
        if (checkRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(checkRunnable);
            checkRunnable = null;
            lastCheckName = null;
            if (checkReqId != 0) {
                ConnectionsManager.getInstance().cancelRequest(checkReqId, true);
            }
        }
        lastNameAvailable = false;
        if (name != null) {
            if (name.startsWith("_") || name.endsWith("_")) {
                tvNotify.setText(LocaleController.getString("UsernameInvalid", R.string.UsernameInvalid));
                tvNotify.setTextColor(0xffcf3030);
                return false;
            }
            for (int a = 0; a < name.length(); a++) {
                char ch = name.charAt(a);
                if (a == 0 && ch >= '0' && ch <= '9') {
                    if (alert) {
                        showErrorAlert(LocaleController.getString("UsernameInvalidStartNumber", R.string.UsernameInvalidStartNumber));
                    } else {
                        tvNotify.setText(LocaleController.getString("UsernameInvalidStartNumber", R.string.UsernameInvalidStartNumber));
                        tvNotify.setTextColor(0xffcf3030);
                    }
                    return false;
                }
                if (!(ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_')) {
                    if (alert) {
                        showErrorAlert(LocaleController.getString("UsernameInvalid", R.string.UsernameInvalid));
                    } else {
                        tvNotify.setText(LocaleController.getString("UsernameInvalid", R.string.UsernameInvalid));
                        tvNotify.setTextColor(0xffcf3030);
                    }
                    return false;
                }
            }
        }
        if (name == null || name.length() < 5) {
            if (alert) {
                showErrorAlert(LocaleController.getString("UsernameInvalidShort", R.string.UsernameInvalidShort));
            } else {
                tvNotify.setText(LocaleController.getString("UsernameInvalidShort", R.string.UsernameInvalidShort));
                tvNotify.setTextColor(0xffcf3030);
            }
            return false;
        }
        if (name.length() > 32) {
            if (alert) {
                showErrorAlert(LocaleController.getString("UsernameInvalidLong", R.string.UsernameInvalidLong));
            } else {
                tvNotify.setText(LocaleController.getString("UsernameInvalidLong", R.string.UsernameInvalidLong));
                tvNotify.setTextColor(0xffcf3030);
            }
            return false;
        }

        if (!alert) {
            String currentName = UserConfig.getCurrentUser().username;
            if (currentName == null) {
                currentName = "";
            }
            if (name.equals(currentName)) {
                tvNotify.setText(LocaleController.formatString("UsernameAvailable", R.string.UsernameAvailable, name));
                tvNotify.setTextColor(0xff26972c);
                return true;
            }

            tvNotify.setText(LocaleController.getString("UsernameChecking", R.string.UsernameChecking));
            tvNotify.setTextColor(0xff6d6d72);
            lastCheckName = name;
            checkRunnable = new Runnable() {
                @Override
                public void run() {
                    TLRPC.TL_account_checkUsername req = new TLRPC.TL_account_checkUsername();
                    req.username = name;
                    checkReqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                        @Override
                        public void run(final TLObject response, final TLRPC.TL_error error) {
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    checkReqId = 0;
                                    if (lastCheckName != null && lastCheckName.equals(name)) {
                                        if (error == null && response instanceof TLRPC.TL_boolTrue) {
                                            tvNotify.setText(LocaleController.getString("NoUsernameFound", R.string.NoUsernameFound));
                                            tvNotify.setTextColor(0xffcf3030);

                                            lastNameAvailable = true;
                                        } else {
                                            tvNotify.setText(LocaleController.formatString("UsernameAvailable", R.string.UsernameAvailable, name));
                                            tvNotify.setTextColor(0xff26972c);

                                            lastNameAvailable = false;
                                        }
                                    }
                                }
                            });
                        }
                    }, ConnectionsManager.RequestFlagFailOnServerErrors);
                }
            };
            AndroidUtilities.runOnUIThread(checkRunnable, 300);
        }
        return true;
    }
    private void showErrorAlert(String error) {
        if (getParentActivity() == null) {
            return;
        }
        switch (error) {
            case "USERNAME_INVALID":
                tvNotify.setText(LocaleController.getString("UsernameInvalid", R.string.UsernameInvalid));
                break;
            case "USERNAME_OCCUPIED":
                tvNotify.setText(LocaleController.formatString("UsernameAvailable", R.string.UsernameAvailable, currentName));
                //tvNotify.setText(LocaleController.getString("UsernameInUse", R.string.UsernameAvailable));
                break;
            case "USERNAMES_UNAVAILABLE":
                tvNotify.setText(LocaleController.getString("FeatureUnavailable", R.string.FeatureUnavailable));
                break;
            default:
                tvNotify.setText(LocaleController.getString("ErrorOccurred", R.string.ErrorOccurred));
                break;
        }
    }
}
