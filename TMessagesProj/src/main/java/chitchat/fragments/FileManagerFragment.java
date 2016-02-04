package chitchat.fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tisad.chitchat2.R;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.SharedDocumentCell;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import chitchat.Log;
import chitchat.Utils;
import chitchat.skin.SkinMan;

/**
 * Created by RaminBT on 23/01/2016.
 */
public class FileManagerFragment extends BaseFragment implements AdapterView.OnItemClickListener, View.OnClickListener, AdapterView.OnItemLongClickListener {

    private Context context;
    private File currentDir;

    private TextView emptyView;
    private ListView listView;
    private ListAdapter listAdapter;

    //ramin vars
    public View loading;
    public TextView usedSpace, totalFiles;
    public DefaultPresenter currentPresenter;

    //selections
    private ArrayList<ListItem> items = new ArrayList<>();
    private HashMap<String, ListItem> selectedFiles = new HashMap<>();


    //region life

    @Override
    public void onResume() {
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    public void onFragmentDestroy() {
        unregisterReceiver();
        super.onFragmentDestroy();
    }
    //endregion

    //region receiver
    private boolean receiverRegistered=false;
    private void registerReceiver(){
        if (!receiverRegistered) {
            receiverRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            filter.addAction(Intent.ACTION_MEDIA_CHECKING);
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_NOFS);
            filter.addAction(Intent.ACTION_MEDIA_REMOVED);
            filter.addAction(Intent.ACTION_MEDIA_SHARED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addDataScheme("file");
            ApplicationLoader.applicationContext.registerReceiver(receiver, filter);
        }
    }
    private void unregisterReceiver(){
        try {
            if (receiverRegistered) {
                ApplicationLoader.applicationContext.unregisterReceiver(receiver);
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        if (currentDir != null) {
                            //listFiles(currentDir);
                        }
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            };
            if (Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) {
                //listView.postDelayed(r, 1000);
            } else {
                r.run();
            }
        }
    };
    //endregion


    @Override
    public View createView(Context context) {
        this.context=context;
        registerReceiver();

        currentPresenter=new VideoPresenter();
        updateUserSelectedPath();

        selectedFiles.clear();
        items.clear();

        fragmentView = getParentActivity().getLayoutInflater().inflate(R.layout.files, null, false);
        listAdapter = new ListAdapter(context);
        emptyView = (TextView) fragmentView.findViewById(R.id.searchEmptyView);
        emptyView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        listView = (ListView) fragmentView.findViewById(R.id.listView);
        listView.setEmptyView(emptyView);
        listView.setAdapter(listAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                scrolling = scrollState != SCROLL_STATE_IDLE;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        findIds(fragmentView);

        actionBar.setAllowOverlayTitle(true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                finishFragment();
            }
        });

        return fragmentView;
    }

    public void everyThingLoaded(int files, long bytes){
        AndroidUtilities.clearDrawableAnimation(listView);
        scrolling = true;
        loading.setVisibility(View.GONE);
        if(files==0){
            Log.d("no files");
            totalFiles.setText("0");
            usedSpace.setText("0");
            emptyView.setVisibility(View.VISIBLE);
            return;
        }
        emptyView.setVisibility(View.GONE);
        usedSpace.setText(Utils.humanReadableByteCount(bytes, false));
        totalFiles.setText("" + files);

        listAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long id) {
        if (selectionStarted || i < 0 || i >= items.size()) {
            return false;
        }
        ListItem item = items.get(i);
        File file = item.file;
        if (file != null && !file.isDirectory()) {
            if (!file.canRead()) {
                showErrorBox(LocaleController.getString("AccessError", R.string.AccessError));
                return false;
            }
            if (file.length() == 0) {
                return false;
            }
            selectedFiles.put(file.toString(), item);
            countUp();
            scrolling = false;
            if (view instanceof SharedDocumentCell) {
                ((SharedDocumentCell) view).setChecked(true, true);
            }
            setSelectionStarted();
        }

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
        ListItem item = items.get(i);
        File file = item.file;

        if (!file.canRead()) {
            showErrorBox(LocaleController.getString("AccessError", R.string.AccessError));
            file = new File("/mnt/sdcard");
        }
        if (file.length() == 0) {
            return;
        }

        if(selectionStarted){
            if (selectedFiles.containsKey(file.toString())) {
                selectedFiles.remove(file.toString());
            } else {
                selectedFiles.put(file.toString(), item);
            }
            if (selectedFiles.isEmpty()) {
                actionBar.hideActionMode();
            } else {
                countUp();
            }
            scrolling = false;
            if (view instanceof SharedDocumentCell) {
                ((SharedDocumentCell) view).setChecked(selectedFiles.containsKey(item.file.toString()), true);
            }
        }else {
            Utils.openFile(context, file);
        }


    }


    //region folder section
    public void changePresenter(DefaultPresenter presenter){
        currentPresenter=presenter;
        updateUserSelectedPath();
        listAdapter.clear();
        listAdapter.notifyDataSetChanged();
        loading.setVisibility(View.VISIBLE);
        totalFiles.setText(R.string.Loading);
        usedSpace.setText(R.string.Loading);

        actionBar.setTitle(currentPresenter.title());
        listView.setAdapter(listAdapter = new ListAdapter(context));
    }
    private void updateUserSelectedPath(){
        String sep=File.separator;
        currentDir=new File(
                Environment.getExternalStorageDirectory().getPath()+sep+
                        "Telegram" + sep +
                        currentPresenter.folder()
        );
    }
    private View fileSection, sVide, sDocs, sAudio, sPhoto, sApk, listItems, sectioner;
    private TextView currentFolderSect;
    private void findIds(View parent){
        loading=parent.findViewById(R.id.loading);
        // listView= (RecyclerView) layout.findViewById(R.id.listView);
        usedSpace= (TextView) parent.findViewById(R.id.TextViewUsedSpace);
        totalFiles= (TextView) parent.findViewById(R.id.TextViewLength);
        emptyView= (TextView) parent.findViewById(R.id.EmptyView);

        fileSection=parent.findViewById(R.id.view);
        listItems=parent.findViewById(R.id.listItems);
        sectioner=parent.findViewById(R.id.sectioner);
        currentFolderSect= (TextView) parent.findViewById(R.id.TextViewCurrentSection);

        listItems.setBackgroundColor(SkinMan.currentSkin.drawerNamesColor());

        sVide=parent.findViewById(R.id.Videos);
        sDocs=parent.findViewById(R.id.Documents);
        sAudio=parent.findViewById(R.id.Audios);
        sPhoto=parent.findViewById(R.id.Photos);
        sApk=parent.findViewById(R.id.Apk);

        fileSection.setOnClickListener(this);
        sVide.setOnClickListener(this);
        sDocs.setOnClickListener(this);
        sAudio.setOnClickListener(this);
        sPhoto.setOnClickListener(this);
        sApk.setOnClickListener(this);

        sectioner.setBackgroundColor(SkinMan.currentSkin.actionbarColor());
    }

    @Override
    public void onClick(View v) {
        if(v==fileSection){
            boolean listVis=listItems.getVisibility()==View.VISIBLE;
            listItems.setVisibility(listVis ? View.GONE : View.VISIBLE);
            return;
        }
        listItems.setVisibility(View.GONE);

        String txt=((TextView) v).getText().toString();
        currentFolderSect.setText(txt);

        DefaultPresenter local = null;
        if(v==sVide)
            local=new VideoPresenter();
        else if(v==sDocs)
            local=new DocumentPresenter();
        else if(v==sAudio)
            local=new MusicPresenter();
        else if(v==sPhoto)
            local=new ImagePresenter();
        else if(v==sApk)
            local=new ApkPresenter();

        if(local==null || local.folder().equals(currentPresenter.folder()))
            return;
        changePresenter(local);
    }
    //endregion


    //region selections
    private boolean selectionStarted=false;
    private void countUp(){
        int size=selectedFiles.size();
    }
    private void setSelectionStarted(){
        selectionStarted=true;
    }
    //endregion

    // region enddd
    private void showErrorBox(String error) {
        if (getParentActivity() == null) {
            return;
        }
        new AlertDialog.Builder(getParentActivity()).setTitle(LocaleController.getString("AppName", R.string.AppName)).setMessage(error).setPositiveButton(LocaleController.getString("OK", R.string.OK), null).show();
    }
    //endregion

    //region FilePresenters
    interface FolderPresenter{
        String title();
        String folder();
        void bind(ImageView imageView, File file, int position);
    }
    abstract class DefaultPresenter implements FolderPresenter{
        public List<File> listFiles(File parentDir){
            return new ArrayList<>(Arrays.asList(parentDir.listFiles()));
        }
    }
    class VideoPresenter extends DefaultPresenter{
        @Override
        public String title() {
            return LocaleController.getString("Videos", R.string.LocalVideoCache);
        }

        @Override
        public String folder() {
            return "Telegram Video";
        }

        @Override
        public void bind(ImageView imageView, File file, int position) {
            //thumb(file, imageView, false);
        }
    }
    class ImagePresenter extends DefaultPresenter{
        @Override
        public String title() {
            return LocaleController.getString("Photos", R.string.LocalPhotoCache);
        }

        @Override
        public String folder() {
            return "Telegram Images";
        }

        @Override
        public void bind(ImageView imageView, File file, int position) {
            //thumb(file, imageView, true);
        }
    }
    class DocumentPresenter extends DefaultPresenter{
        @Override
        public String title() {
            return LocaleController.getString("LocalDocumentCache", R.string.LocalDocumentCache);
        }

        @Override
        public String folder() {
            return "Telegram Documents";
        }

        @Override
        public void bind(ImageView imageView, File file, int position) {
        }
    }
    class MusicPresenter extends DefaultPresenter{
        @Override
        public String title() {
            return LocaleController.getString("Music", R.string.LocalMusicCache);
        }

        @Override
        public String folder() {
            return "Telegram Audio";
        }

        @Override
        public void bind(ImageView imageView, File file, int position) {

        }
    }
    class ApkPresenter extends DefaultPresenter{
        @Override
        public List<File> listFiles(File parentDir) {
            List<File> files= super.listFiles(parentDir);
            List<File> apks=new ArrayList<>();
            for(File file:files)
                if(file.isFile() && file.getName().toLowerCase().endsWith(".apk"))
                    apks.add(file);
            return apks;
        }

        @Override
        public String title() {
            return "APK";
        }

        @Override
        public String folder() {
            return "Telegram Documents";
        }

        @Override
        public void bind(ImageView imageView, File file, int position) {

        }
    }
    //endregion


    // region adapter
    private class ListItem {
        int icon;
        String title;
        String subtitle = "";
        String ext = "";
        String thumb;
        File file;
    }
    private boolean scrolling;

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;
        private Comparator<File> comparator;
        private SparseBooleanArray sections;
        private SparseArray<String> fileSects;

        public ListAdapter(Context context) {
            mContext = context;
            refreshItems();
        }

        public void clear(){
            items.clear();
            selectedFiles.clear();
            if(sections!=null)
                sections.clear();
            if(fileSects!=null)
                fileSects.clear();
        }

        public void nameComparator(final boolean asc){
            //TODO
            comparator=new Comparator<File>() {
                @Override
                public int compare(File f, File a) {
                    File fr=asc ? f : a;
                    File to=asc ? a : f;
                    return fr.getName().compareTo(to.getName());
                }
            };
        }
        public void sizeComparator(final boolean asc){
            comparator=new Comparator<File>() {
                @Override
                public int compare(File f, File a) {
                    File fr=asc ? f : a;
                    File to=asc ? a : f;

                    return Long.valueOf(fr.length()).compareTo(to.length());//(int)Math.min(fr.length(),(long) Integer.MAX_VALUE) - (int)Math.min(to.length(), (long) Integer.MAX_VALUE);
                }
            };
        }
        public void defaultComparator(){
            comparator=new Comparator<File>() {
                @Override
                public int compare(File f, File a) {
                    return Long.valueOf(a.lastModified()).compareTo(f.lastModified());
                }
            };
        }

        public void refreshItems(){
            if(comparator==null)
                defaultComparator();
            final File dir=currentDir;
            if (!dir.canRead()) {
                if (dir.getAbsolutePath().startsWith(Environment.getExternalStorageDirectory().toString())
                        || dir.getAbsolutePath().startsWith("/sdcard")
                        || dir.getAbsolutePath().startsWith("/mnt/sdcard")) {
                    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                            && !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                        currentDir = dir;
                        items.clear();
                        String state = Environment.getExternalStorageState();
                        if (Environment.MEDIA_SHARED.equals(state)) {
                            emptyView.setText(LocaleController.getString("UsbActive", R.string.UsbActive));
                        } else {
                            emptyView.setText(LocaleController.getString("NotMounted", R.string.NotMounted));
                        }
                        AndroidUtilities.clearDrawableAnimation(listView);
                        scrolling = true;
                        listAdapter.notifyDataSetChanged();
                        return;
                    }
                }
                showErrorBox(LocaleController.getString("AccessError", R.string.AccessError));
                return;
            }
            new AsyncTask<Void, Object, Long>(){
                @Override
                protected Long doInBackground(Void... params) {
                    ArrayList<File> files;
                    try {
                        files = new ArrayList<File>(Arrays.asList(dir.listFiles()));
                    } catch(Exception e) {
                        publishProgress(e);
                        return -1l;
                    }

                    if(files==null){
                        publishProgress(new Exception(LocaleController.getString("UnknownError", R.string.UnknownError)));
                        return -1l;
                    }

                    items.clear();

                    List<File> filesToRemove=new ArrayList<>();
                    Log.d("got files");
                    int noMediaIndex=-1;
                    for(File file:files)
                        if(file.isDirectory())
                            filesToRemove.add(file);
                        else if(noMediaIndex==-1 && file.getName().toLowerCase().equalsIgnoreCase(".nomedia")){
                            noMediaIndex=files.indexOf(file);
                            filesToRemove.add(file);
                        }
                    //if(-1!=noMediaIndex)
                    //    files.remove(noMediaIndex);
                    files.removeAll(filesToRemove);
                    Log.d("soring files");
                    Collections.sort(files, comparator);
                    Log.d("sorted files");


                    for (File file : files) {
                        if (file.getName().startsWith(".")) {
                            continue;
                        }
                        ListItem item = new ListItem();
                        item.title = file.getName();
                        item.file = file;
                        /*if (file.isDirectory()) {
                            item.icon = R.drawable.ic_directory;
                            item.subtitle = LocaleController.getString("Folder", R.string.Folder);
                        } else {*/
                        String fname = file.getName();
                        String[] sp = fname.split("\\.");
                        item.ext = sp.length > 1 ? sp[sp.length - 1] : "?";
                        item.subtitle = AndroidUtilities.formatFileSize(file.length());
                        fname = fname.toLowerCase();
                        if (fname.endsWith(".jpg") || fname.endsWith(".png") || fname.endsWith(".gif") || fname.endsWith(".jpeg")) {
                            item.thumb = file.getAbsolutePath();
                        }
                        //}
                        items.add(item);
                    }

                    sections=new SparseBooleanArray();
                    fileSects=new SparseArray<>();
                    String sect=null;
                    long size=0;
                    for(File file:files){
                        size+=file.length();
                        String temp= Utils.fileDate(file.lastModified());
                        if(!temp.equals(sect)){
                            int index=files.indexOf(file);
                            sections.put(index, true);
                            fileSects.put(index, temp);
                        }
                        sect=temp;
                    }
                    Log.d("files sectioned");
                    return size;
                }

                @Override
                protected void onProgressUpdate(Object... values) {
                    if(values.length>0 && values[0] instanceof Exception){
                        Exception e= (Exception) values[0];
                        showErrorBox(e.getLocalizedMessage());
                    }
                }

                @Override
                protected void onPostExecute(Long aLong) {
                    if(aLong==null || aLong < 0)
                        return;

                    if(listView.getAdapter()!=ListAdapter.this)
                        return;


                    everyThingLoaded(items.size(), aLong);
                }
            }.execute();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public int getItemViewType(int pos) {
            return sections.get(pos) ? 1 : 2;//items.get(pos).subtitle.length() > 0 ? 0 : 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView sect;
            if (convertView == null) {
                ViewGroup par= (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.file_doc_item, parent, false);
                convertView = par;//par.getChildAt(1);//new SharedDocumentCell(mContext);
                par.getChildAt(0).setVisibility(View.VISIBLE);
            }
            sect= (TextView) ((ViewGroup)convertView).getChildAt(0);
            SharedDocumentCell textDetailCell = (SharedDocumentCell) ((ViewGroup)convertView).getChildAt(1);;
            ListItem item = items.get(position);
            if (item.icon != 0) {
                textDetailCell.setTextAndValueAndTypeAndThumb(item.title, item.subtitle, null, null, item.icon);
            } else {
                String type = item.ext.toUpperCase().substring(0, Math.min(item.ext.length(), 4));
                textDetailCell.setTextAndValueAndTypeAndThumb(item.title, item.subtitle, type, item.thumb, 0);
            }
            if (item.file != null && actionBar.isActionModeShowed()) {
                textDetailCell.setChecked(selectedFiles.containsKey(item.file.toString()), !scrolling);
            } else {
                textDetailCell.setChecked(false, !scrolling);
            }
            if(getItemViewType(position)==1)
                sect.setText(fileSects.get(position));
            return convertView;
        }
    }
    //endregion
}
