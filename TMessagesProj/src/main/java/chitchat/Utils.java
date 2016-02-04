package chitchat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import com.tisad.chitchat2.BuildConfig;
import com.tisad.chitchat2.R;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.ChatActivityEnterView;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import chitchat.fragments.StickerSetActivity;

/**
 * Created by RaminBT on 13/01/2016.
 */
public class Utils {

    private static final String[] okImageExts =  new String[] {"jpg", "png", "gif","jpeg"};

    public static boolean isImageFile(File file)
    {
        for (String extension : okImageExts)
        {
            if (file.getName().toLowerCase().endsWith(extension))
            {
                return true;
            }
        }
        return false;
    }
    public static boolean isApk(File file){
        return file.getName().toLowerCase(Locale.US).endsWith(".apk");
    }

    public static Bitmap apkIcon(Context context, File path){
        //TODO
        return null;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static Bitmap decodeSampledBitmap(File res,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res.getPath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(res.getPath(), options);
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static void setBackground(View v, Drawable d){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            v.setBackground(d);
        else
            v.setBackgroundDrawable(d);

    }

    public static void contactMail(Context context){

        sendMail(context, "tisadlogs@gmail.com", getVersionName(),"");

    }
    public static void sendMail(Context context, String email, String subject, String body){
        Intent result = new Intent(android.content.Intent.ACTION_SEND);
        result.setType("plain/text");
        result.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
        result.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        result.putExtra(android.content.Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(result, context.getString(R.string.SendEmail)));
    }

    public static String getVersionName(){
        return "ChitChat";
    }


    public static void sendSticker(final ChatActivity activity, final TLRPC.Document document, final long peer, final MessageObject replyingMessageObject, final boolean asAdmin){
        if(ChitSettings.confirmBeforeSendingSticker){
            AlertDialog.Builder b=new AlertDialog.Builder(activity.getParentActivity());

            Context c=activity.getParentActivity();
            final ImageReceiver receiver=new ImageReceiver();
            StickerSetActivity.FrameLayoutDrawer view=new StickerSetActivity.FrameLayoutDrawer(c){

                @Override
                protected void onDraw(Canvas canvas) {
                    //canvas.save();
                    int size = (int) (Math.min(getWidth(), getHeight()) / 1.8f);
                    //canvas.translate(stickerView.getWidth() / 2, Math.max(size / 2 + AndroidUtilities.statusBarHeight, (stickerView.getHeight() ) / 2));
                    Bitmap currentImage = receiver.getBitmap();
                    if (currentImage != null) {
                        receiver.setImageCoords(
                                0//-size// / 2
                                ,0// -size// / 2
                                , getWidth()//*2
                                , getHeight()//*2

                        );
                        receiver.draw(canvas);
                    }
                    //canvas.restore();
                    invalidate();
                    /*canvas.save();
                    int size = (int) (Math.min(getWidth(), getHeight()) / 1.8f);
                    canvas.translate(getWidth() / 2, Math.max(size / 2 + AndroidUtilities.statusBarHeight, (getHeight() ) / 2));
                    Bitmap bitmap = receiver.getBitmap();
                    if (bitmap != null) {
                        receiver.setImageCoords(-size / 2, -size / 2, size, size);
                        receiver.draw(canvas);
                    }
                    canvas.restore();
                    invalidate();*/
                }

                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec),
                            View.MeasureSpec.getSize(heightMeasureSpec)
                    );
                }
            };
            view.setFocusable(false);


            receiver.setAspectFit(true);
            receiver.setInvalidateAll(true);
            receiver.setParentView(view);
            receiver.setImage(document, null, document.thumb.location, null, "webp", true);
            b.setTitle(R.string.AppName);
            b.setView(view);
            b.setPositiveButton(LocaleController.getString("Send", R.string.Send), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SendMessagesHelper.getInstance().sendSticker(document, peer, replyingMessageObject, asAdmin);
                    activity.showReplyPanel(false, null, null, null, false, true);
                    activity.chatActivityEnterView.setFieldText("");
                }
            });
            b.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            b.show();
            return;
        }
        SendMessagesHelper.getInstance().sendSticker(document, peer, replyingMessageObject, asAdmin);
        activity.showReplyPanel(false, null, null, null, false, true);
        activity.chatActivityEnterView.setFieldText("");
    }
    public static void sendSticker(final ChatActivityEnterView enterView, final TLRPC.Document document, final long peer, final MessageObject replyingMessageObject, final boolean asAdmin){
        if(ChitSettings.confirmBeforeSendingSticker){
            AlertDialog.Builder b=new AlertDialog.Builder(enterView.getContext());

            final Context c=enterView.getContext();
            final ImageReceiver receiver=new ImageReceiver();
            StickerSetActivity.FrameLayoutDrawer view=new StickerSetActivity.FrameLayoutDrawer(c){

                @Override
                protected void onDraw(Canvas canvas) {
                    //canvas.save();
                    int size = (int) (Math.min(getWidth(), getHeight()) / 1.8f);
                    //canvas.translate(stickerView.getWidth() / 2, Math.max(size / 2 + AndroidUtilities.statusBarHeight, (stickerView.getHeight() ) / 2));
                    Bitmap currentImage = receiver.getBitmap();
                    if (currentImage != null) {
                        receiver.setImageCoords(
                                0//-size// / 2
                                ,0// -size// / 2
                                , getWidth()//*2
                                , getHeight()//*2

                        );
                        receiver.draw(canvas);
                    }
                    //canvas.restore();
                    invalidate();
                   /* canvas.save();
                    int size = (int) (Math.min(getWidth(), getHeight()) / 1.8f);
                    canvas.translate(getWidth() / 2, Math.max(size / 2 + AndroidUtilities.statusBarHeight, (getHeight() ) / 2));
                    Bitmap bitmap = receiver.getBitmap();
                    if (bitmap != null) {
                        receiver.setImageCoords(-size / 2, -size / 2, size, size);
                        receiver.draw(canvas);
                    }
                    canvas.restore();
                    invalidate();*/
                }

                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec),
                            View.MeasureSpec.getSize(heightMeasureSpec)
                            );
                }
            };
            view.setFocusable(false);


            receiver.setAspectFit(true);
            receiver.setInvalidateAll(true);
            receiver.setParentView(view);
            receiver.setImage(document, null, document.thumb.location, null, "webp", true);
            b.setTitle(R.string.AppName);
            b.setView(view);
            b.setPositiveButton(LocaleController.getString("Send", R.string.Send), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SendMessagesHelper.getInstance().sendSticker(document, peer, replyingMessageObject, asAdmin);
                    if (enterView.delegate != null) {
                        enterView.delegate.onMessageSend(null);
                    }
                    dialog.dismiss();
                }
            });
            b.setNeutralButton(R.string.SendAsImage, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    File file=new File(AndroidUtilities.getCacheDir(), receiver.getKey() + "." + receiver.getExt());
                    if(BuildConfig.DEBUG)
                        Log.d("sending " + file.getAbsolutePath() + " as image");
                    boolean success=false;
                    int errorRes=0;
                    if(file.exists()){
                        if(BuildConfig.DEBUG)
                            Log.d("sticker exists");
                        success=true;
                        if(file.canRead()){
                            if(BuildConfig.DEBUG)
                                Log.d("sticker will be sent");
                            sendWithCaption(c, file, null, peer, replyingMessageObject, asAdmin, true);
                        }else {
                            if(BuildConfig.DEBUG)
                                Log.d("sticker can not be read");
                            success=false;
                        }

                    }else {
                        if(BuildConfig.DEBUG)
                            Log.d("download sticker first");
                        errorRes=R.string.PleaseDownload;
                    }

                    if(!success && errorRes!=0){
                        Toast.makeText(c, errorRes, Toast.LENGTH_LONG).show();
                    }else if(success){
                        if(BuildConfig.DEBUG)
                            Log.d("sticker sent");
                        dialog.dismiss();
                        if (enterView.delegate != null) {
                            enterView.delegate.onMessageSend(null);
                        }
                    }
                }
            });
            b.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            b.show();
        }else {
            SendMessagesHelper.getInstance().sendSticker(document, peer, replyingMessageObject, asAdmin);
            if (enterView.delegate != null) {
                enterView.delegate.onMessageSend(null);
            }
        }
    }


    public static void sendWithCaption(Context context, final File file, Uri imageUri, final long dialog_id, final MessageObject reply_to_msg, final boolean asAdmin, final boolean asImage){
        AlertDialog.Builder a=new AlertDialog.Builder(context);
        a.setTitle(R.string.PhotoCaption);

        View inflated= LayoutInflater.from(context).inflate(R.layout.sticker_caption, null);
        final EditText edt= (EditText) inflated.findViewById(R.id.PhotoCaption);

        a.setNeutralButton(R.string.Send, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SendMessagesHelper.prepareSendingPhoto(file.getAbsolutePath(), null, dialog_id, reply_to_msg,
                        edt.getText().toString(), asAdmin, asImage);
                dialog.dismiss();
            }
        });
        a.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        a.setView(inflated);
        a.show();


    }

    public static String tisadChannel(){
        return "tisaddev";
    }

    public static String fileDate(long when){

        String cached;
        Calendar c=Calendar.getInstance();
        c.setTimeInMillis(when);

        cached=convertMonth(c.get(Calendar.MONTH), false) + " " + c.get(Calendar.DAY_OF_MONTH);

        int msgYear=c.get(Calendar.YEAR);
        if(msgYear!=new Date().getYear())
            cached=msgYear+" "+cached;
        return cached;
    }
    /**
     * Converts a month by number to full text
     *
     * @param month    number of the month 1..12
     * @param useShort boolean that gives "Jun" instead of "June" if true
     * @return returns "January" if "1" is given
     */
    public static String convertMonth(int month, boolean useShort) {
        String monthStr;
        switch (month) {
            default:
                monthStr = "January";
                break;
            case Calendar.FEBRUARY:
                monthStr = "February";
                break;
            case Calendar.MARCH:
                monthStr = "March";
                break;
            case Calendar.APRIL:
                monthStr = "April";
                break;
            case Calendar.MAY:
                monthStr = "May";
                break;
            case Calendar.JUNE:
                monthStr = "June";
                break;
            case Calendar.JULY:
                monthStr = "July";
                break;
            case Calendar.AUGUST:
                monthStr = "August";
                break;
            case Calendar.SEPTEMBER:
                monthStr = "September";
                break;
            case Calendar.OCTOBER:
                monthStr = "October";
                break;
            case Calendar.NOVEMBER:
                monthStr = "November";
                break;
            case Calendar.DECEMBER:
                monthStr = "December";
                break;
        }
        if (useShort) monthStr = monthStr.substring(0, 3);
        return monthStr;
    }


    public static void openFile(Context context, File file){
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = myMime.getMimeTypeFromExtension(fileExt(file.getPath()).substring(1));
        newIntent.setDataAndType(Uri.fromFile(file),mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(newIntent);
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(context, "No handler for this type of file.", Toast.LENGTH_LONG).show();
            //if(BuildConfig.DEBUG)
            //    throw e;
        }
    }
    public static String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf("."));
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

}
