package chitchat.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.tisad.chitchat2.BuildConfig;
import com.tisad.chitchat2.R;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.StickerEmojiCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import chitchat.ChitSettings;
import chitchat.Log;
import chitchat.NightModeUtil;
import chitchat.Utils;

/**
 *
 * Created by RaminBT on 16/01/2016.
 */
public class StickerSetActivity extends BaseFragment {

    public static File stickerSavePath(String key, boolean appendPng){
        File telegramPath=new File(Environment.getExternalStorageDirectory()+File.separator+"Pictures", "Stickers");
        telegramPath.mkdirs();
        return new File(telegramPath, appendPng ? key+".png" : key);
    }
    public static void deleteAllSavedStickers(Context context){
        //Context context= ApplicationLoader.applicationContext;
        AlertDialog.Builder b=new AlertDialog.Builder(context);
        b.setTitle(R.string.AppName);
        b.setMessage(R.string.DeleteStickerAlert);
        b.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                File st = new File(Environment.getExternalStorageDirectory() + File.separator + "Pictures", "Stickers");
                if (st.isDirectory() && st.exists()) {
                    File[] toDelete = st.listFiles();
                    for (File file : toDelete) {
                        if (file.isFile()) {
                            if (file.getName().endsWith(".png"))
                                file.delete();
                        }
                    }
                }
            }
        });
        b.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        b.show();



    }

    /**
     *
     *
     *
     * @param hide hide sticker or unHide
     * @return true if stickers are hidden after action
     */
    public static boolean hideStickers(boolean hide){
        File noMedia=stickerSavePath(".nomedia", false);

        if(!hide)
            noMedia.delete();
        else
        try {
            noMedia.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return noMedia.exists();
    }
    public static boolean isStickersHidden(){
        return stickerSavePath(".nomedia", false).exists();
    }

    TLRPC.TL_messages_stickerSet stickerSet;
    public ArrayList<TLRPC.Document> stickers;
    public ListAdapter listAdapter;
    public FrameLayout stickerView;
    ImageReceiver imageReceiver;

    private File cacheDir;
    private File currentStickerFile;
    private File destFile;
    private Bitmap currentImage;

    public StickerSetActivity(TLRPC.TL_messages_stickerSet stickerSet) {
        this.stickerSet = stickerSet;
        stickers=stickerSet.documents;
        imageReceiver=new ImageReceiver();

        imageReceiver.setAspectFit(true);
        imageReceiver.setInvalidateAll(true);

        cacheDir=AndroidUtilities.getCacheDir();
        if(BuildConfig.DEBUG)
        Log.d("cache dir " + cacheDir);
    }


    @Override
    public View createView(Context context) {
        //actionBar.setAllowOverlayTitle(true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(stickerSet.set.title);
        actionBar.createMenu().addItem(5,R.drawable.ic_action_save_f);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if(id==5){
                    saveCurrentSticker();
                    return;
                }
                finishFragment();
            }
        });

        FrameLayout layout=new FrameLayout(context);
        fragmentView=layout;

        stickerView=new FrameLayoutDrawer(context){
            @Override
            protected void onDraw(Canvas canvas) {
                //canvas.save();
                int size = (int) (Math.min(stickerView.getWidth(), stickerView.getHeight()) / 1.8f);
                //canvas.translate(stickerView.getWidth() / 2, Math.max(size / 2 + AndroidUtilities.statusBarHeight, (stickerView.getHeight() ) / 2));
                currentImage = imageReceiver.getBitmap();
                if (currentImage != null) {
                    imageReceiver.setImageCoords(
                            0//-size// / 2
                            ,0// -size// / 2
                            , getWidth()//*2
                            , getHeight()//*2

                    );
                    imageReceiver.draw(canvas);
                }
                //canvas.restore();
                stickerView.invalidate();
            }
        };
        //stickerView.setFocusable(false);
        imageReceiver.setAspectFit(true);
        imageReceiver.setInvalidateAll(true);
        imageReceiver.setParentView(stickerView);


        layout.addView(stickerView, LayoutHelper.createFrame(-1, -1, Gravity.CENTER, 0, 60, 0, 102));
        addListView(layout, context);

        Utils.setBackground(fragmentView, ChitSettings.cachedWallpaper);
        presentEmoji(0);
        NightModeUtil.dark(fragmentView);
        return fragmentView;
    }
    public void presentEmoji(int index){
        TLRPC.Document sticker=stickers.get(index);
        imageReceiver.setImage(sticker, null, sticker.thumb.location, null, "webp", true);
        if(BuildConfig.DEBUG){

            Log.d("stickers key " + imageReceiver.currentKey);
            Log.d("stickers thumb key " + imageReceiver.currentThumbKey);
            Log.d("file path " + new File(cacheDir + File.separator + imageReceiver.getKey() + "." + imageReceiver.getExt()).getAbsolutePath());
        }
        currentStickerFile=new File(cacheDir, imageReceiver.getKey() + "." + imageReceiver.getExt());
        destFile=makeDestFile(imageReceiver.getKey());
        if(BuildConfig.DEBUG)
        Log.d("file exists " + currentStickerFile.exists());
        stickerView.invalidate();
    }

    private Toast toast;
    private void saveCurrentSticker(){
        if(currentStickerFile == null || !currentStickerFile.exists() || currentStickerFile.isDirectory())
            return;
        File localDest=destFile;
        Bitmap local=currentImage;

        CharSequence charS;
        if(localDest.exists() || localDest.isDirectory())
            charS=LocaleController.getString("FileExists", R.string.FileExists);
        else{
            FileOutputStream fos=null;
            try {
                if(local==null)
                    throw new Exception();
                fos=new FileOutputStream(localDest, false);
                local.compress(Bitmap.CompressFormat.PNG, 100,fos);
                //AndroidUtilities.copyFile(currentStickerFile, destFile);
                charS= LocaleController.getString("SaveSticker", R.string.StickerSuccessfullySaved)+"\n"+localDest.getAbsolutePath();
            }catch (Exception e){
                e.printStackTrace();
                charS= LocaleController.getString("CantSave", R.string.CantSaveSticker);
            }finally {
                if(fos!=null)
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }

        if(toast!=null)
            toast.cancel();
        toast=Toast.makeText(getParentActivity(), charS, Toast.LENGTH_LONG);
        toast.show();
    }
    private File makeDestFile(String key){
        return stickerSavePath(key, true);
    }


    public void addListView(FrameLayout layout, Context context){
        RecyclerListView listView = new RecyclerListView(context);
        listView.setClipToPadding(false);
        listView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), 0);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        listView.setLayoutManager(layoutManager);
        listView.setDisallowInterceptTouchEvents(true);
        if (Build.VERSION.SDK_INT >= 9) {
            listView.setOverScrollMode(RecyclerListView.OVER_SCROLL_NEVER);
        }
        listView.setAdapter(listAdapter = new ListAdapter(context));
        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                presentEmoji(position);
            }
        });
        layout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 102, Gravity.LEFT | Gravity.BOTTOM));;
    }




    class ListAdapter extends RecyclerView.Adapter{
        Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            StickerEmojiCell cell = new StickerEmojiCell(context) {
                public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(82), MeasureSpec.EXACTLY));
                }
            };
            return new RecyclerView.ViewHolder(cell){};
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            StickerEmojiCell cell= (StickerEmojiCell) holder.itemView;
            cell.setSticker(stickers.get(position), false);
        }

        @Override
        public int getItemCount() {
            return stickers.size();
        }
    }



    public static class FrameLayoutDrawer extends FrameLayout {
        public FrameLayoutDrawer(Context context) {
            super(context);
            setWillNotDraw(false);
        }
    }
}
