package chitchat.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tisad.chitchat2.R;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.w3c.dom.Text;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import chitchat.Log;
import chitchat.Utils;
import chitchat.skin.SkinMan;

/**
 *  manages downloaded files
 * Created by RaminBT on 21/01/2016.
 */
public class FilesFragment extends BaseFragment implements View.OnClickListener, RecyclerView.ComputeListener{

    public RecyclerView listView;
    public LinearLayoutManager layoutManager;
    public FileAdapter adapter;

    public View loading;
    public TextView usedSpace, totalFiles, emptyView;

    public File userSelectedPath;
    public int imageSize;
    private Context context;
    private DefaultPresenter currentPresenter;

    public FilesFragment() {
        imageSize= AndroidUtilities.dp(50f);
    }

    @Override
    public View createView(Context context) {
        this.context=context;
        currentPresenter=new VideoPresenter();
        updateUserSelectedPath();
        ViewGroup layout= (ViewGroup) LayoutInflater.from(context).inflate(R.layout.files, null);
        fragmentView=layout;

        loading=layout.findViewById(R.id.loading);
       // listView= (RecyclerView) layout.findViewById(R.id.listView);
        usedSpace= (TextView) layout.findViewById(R.id.TextViewUsedSpace);
        totalFiles= (TextView) layout.findViewById(R.id.TextViewLength);
        emptyView= (TextView) layout.findViewById(R.id.EmptyView);

        findIds(layout);

        layoutManager=new LinearLayoutManager(context);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter = new FileAdapter(context));
        listView.setComputeListener(this);

        actionBar.setTitle(currentPresenter.title());
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

    public void changePresenter(DefaultPresenter presenter){
        currentPresenter=presenter;
        updateUserSelectedPath();
        adapter.clear();
        if(!listView.isComputingLayout())
            adapter.notifyDataSetChanged();
        loading.setVisibility(View.VISIBLE);
        totalFiles.setText(R.string.Loading);
        usedSpace.setText(R.string.Loading);

        actionBar.setTitle(currentPresenter.title());
        listView.setAdapter(adapter = new FileAdapter(context));
    }
    private void updateUserSelectedPath(){
        String sep=File.separator;
        userSelectedPath=new File(
                Environment.getExternalStorageDirectory().getPath()+sep+
                        "Telegram" + sep +
                        currentPresenter.folder()
        );
    }

    //region folder sectioners
    private View fileSection, sVide, sDocs, sAudio, sPhoto, sApk, listItems, sectioner;
    private TextView currentFolderSect;
    private void findIds(View parent){
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


    boolean toNotify=false;
    @Override
    public void end() {
        Log.d("end of compute");
        // listViewCompute end
        if(toNotify && !listView.isComputingLayout()){
            Log.d("notifying after computing");
            toNotify=false;
            adapter.notifyDataSetChanged();
        }
    }

    private void everythingLoaded(int files, long bytes){
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

        Log.d("notifying");
        Runnable a=new Runnable() {
            @Override
            public void run() {
                Log.d("notified");
                if(!listView.isComputingLayout()){
                    toNotify=false;
                    adapter.notifyDataSetChanged();
                }
                else {
                    toNotify=true;
                    try {
                        adapter.notifyDataSetChanged();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    Log.d("listView is computing");
                }
                Log.d("size " + adapter.getItemCount());
            }
        };
        listView.postDelayed(a, 500);
    }

    class FileAdapter extends RecyclerView.Adapter<Holder> implements View.OnClickListener {

        Context context;
        private SparseBooleanArray sections;
        private SparseArray<String> fileSects;
        private List<File> files;
        private Comparator<File> comparator;

        public FileAdapter(Context context) {
            this.context = context;
            refreshItems();
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

        public void clear(){
            if(files!=null)
                files.clear();
            if(sections!=null)
                sections.clear();
            if(fileSects!=null)
                fileSects.clear();
        }
        public void refreshItems(){
            if(comparator==null)
                defaultComparator();
            new AsyncTask<Void, Void, Long>(){
                @Override
                protected Long doInBackground(Void... params) {
                    Log.d("getting files");
                    files=currentPresenter.listFiles(userSelectedPath);

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

                    sections=new SparseBooleanArray();
                    fileSects=new SparseArray<>();
                    String sect=null;
                    long size=0;
                    for(File file:files){
                        size+=file.length();
                        String temp=Utils.fileDate(file.lastModified());
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
                protected void onPostExecute(Long fileLEngth) {
                    Log.d("loaded");
                    if(listView.getAdapter()==FileAdapter.this)
                        everythingLoaded(files.size(), fileLEngth);
                }
            }.execute();
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view=LayoutInflater.from(context).inflate(R.layout.file_item, parent, false);
            Holder holder= new Holder(view);
            if(viewType==1)
                holder.section.setVisibility(View.VISIBLE);
            holder.clickable.setOnClickListener(this);
            holder.clickable.setTag(holder);
            return holder;
        }

        @Override
        public int getItemViewType(int position) {
            return sections.get(position) ? 1 : 2;
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            File item=files.get(position);
            holder.file=item;
            holder.name.setText(item.getName());
            holder.size.setText(Utils.humanReadableByteCount(item.length(), false));
            if(getItemViewType(position)==1)
                holder.section.setText(fileSects.get(position));


            currentPresenter.bind(holder, item, position);
        }

        @Override
        public int getItemCount() {
            return files==null ? 0 : files.size();
        }

        @Override
        public void onClick(View v) {
            Holder holder= (Holder) v.getTag();
            Utils.openFile(context, holder.file);
        }
    }


    public void thumb(final File file, Holder holder, final boolean image){
        final WeakReference<Holder> weakIv=new WeakReference<>(holder);
        final int position=holder.getAdapterPosition();
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                return image ? Utils.decodeSampledBitmap(file, imageSize, imageSize) : videoThumb(file);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                Holder iv=weakIv.get();
                if(iv==null || bitmap==null || position!=iv.getAdapterPosition())
                    return;
                iv.imageView.setImageBitmap(bitmap);
            }
        }.execute();
    }
    public Bitmap videoThumb(File path){
        return ThumbnailUtils.createVideoThumbnail(path.getPath(), 1);
    }




    static class Holder extends RecyclerView.ViewHolder{

        public TextView name, size, section;
        public ImageView imageView;
        public File file;
        public View clickable;

        public Holder(View itemView) {
            super(itemView);

            name= (TextView) itemView.findViewById(R.id.name);
            size= (TextView) itemView.findViewById(R.id.size);
            section= (TextView) itemView.findViewById(R.id.TextViewSection);
            imageView= (ImageView) itemView.findViewById(R.id.imageView);
            clickable=itemView.findViewById(R.id.Clickable);
        }
    }
    interface FolderPresenter{
        String title();
        String folder();
        void bind(Holder holder, File file, int position);
    }
    abstract class DefaultPresenter implements FolderPresenter{
        public List<File> listFiles(File parentDir){
            return new ArrayList<>(Arrays.asList(parentDir.listFiles()));
        }
    }
    class VideoPresenter extends DefaultPresenter{
        Drawable d;

        public VideoPresenter() {
            d=ContextCompat.getDrawable(context, R.drawable.attach_video);
        }

        @Override
        public String title() {
            return LocaleController.getString("Videos", R.string.LocalVideoCache);
        }

        @Override
        public String folder() {
            return "Telegram Video";
        }

        @Override
        public void bind(Holder holder, File file, int position) {
            holder.imageView.setImageDrawable(d);
            thumb(file, holder, false);
        }
    }
    class ImagePresenter extends DefaultPresenter{
        Drawable d;

        public ImagePresenter() {
            d=ContextCompat.getDrawable(context, R.drawable.attach_gallery);
        }
        @Override
        public String title() {
            return LocaleController.getString("Photos", R.string.LocalPhotoCache);
        }

        @Override
        public String folder() {
            return "Telegram Images";
        }

        @Override
        public void bind(Holder holder, File file, int position) {
            holder.imageView.setImageDrawable(d);
            thumb(file, holder, true);
        }
    }
    class DocumentPresenter extends DefaultPresenter{
        Drawable d;

        public DocumentPresenter() {
            d=ContextCompat.getDrawable(context, R.drawable.attach_file);
        }
        @Override
        public String title() {
            return LocaleController.getString("LocalDocumentCache", R.string.LocalDocumentCache);
        }

        @Override
        public String folder() {
            return "Telegram Documents";
        }

        @Override
        public void bind(Holder holder, File file, int position) {
            holder.imageView.setImageDrawable(d);
        }
    }
    class MusicPresenter extends DefaultPresenter{
        private Drawable musicDrawable;
        public MusicPresenter() {
            musicDrawable=ContextCompat.getDrawable(context, R.drawable.attach_audio);
        }

        @Override
        public String title() {
            return LocaleController.getString("Music", R.string.LocalMusicCache);
        }

        @Override
        public String folder() {
            return "Telegram Audio";
        }

        @Override
        public void bind(Holder holder, File file, int position) {
            holder.imageView.setImageDrawable(musicDrawable);
        }
    }
    class ApkPresenter extends DefaultPresenter{

        Drawable d;
        public ApkPresenter() {
            d=ContextCompat.getDrawable(context, R.drawable.attach_send1);
        }

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
        public void bind(Holder holder, File file, int position) {
            holder.imageView.setImageDrawable(d);
        }
    }
}
