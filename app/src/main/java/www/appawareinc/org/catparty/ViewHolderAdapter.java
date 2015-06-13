package www.appawareinc.org.catparty;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import circleprogressbar.CircleProgressBar;

public class ViewHolderAdapter extends RecyclerView.Adapter<ViewHolderAdapter.SimpleViewHolder> {

    private Context context;
    private List<GifItem> gifs;
    private Activity activity;
    private int lastPosition = -1;
    private boolean mainPartyAdapter = false;
    private boolean vipAdapter = false;

    /*SimpleViewHolder is the whole item that contains the gif, the colorful background, and anything
    * else that goes in that frame. We define the links between the XML and the Java file and
    * set up a few other conditions for the view. */
    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        private WebView gifView;
        private FrameLayout container;
        private CircleProgressBar progressBar;
        private ImageView stillView;
        public boolean loaded = false;

        public SimpleViewHolder(final View itemView) {
            super(itemView);
            gifView = (WebView) itemView.findViewById(R.id.videoView);
            container = (FrameLayout) itemView.findViewById(R.id.container);
            progressBar = (CircleProgressBar) itemView.findViewById(R.id.progressBar);
            stillView = (ImageView) itemView.findViewById(R.id.stillView);

            itemView.setBackgroundColor(Color.TRANSPARENT);
            itemView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            gifView.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageFinished(WebView view, String url) {
                    loaded = true;
                    int screenMiddle = Math.round(TwoRooms.screenWidthDp * TwoRooms.densityMultiple / 2);
                    if (container.getLeft() < screenMiddle && container.getRight() > screenMiddle) {
                        showWebView();
                    }
                }
            });
        }

        public void hideAllViews(){
            progressBar.setVisibility(View.GONE);
            stillView.setVisibility(View.INVISIBLE);
            gifView.setVisibility(View.INVISIBLE);
        }

        /*hides the webView so we can't see the gif, even though it may be loaded*/
        public void hideWebView() {
            progressBar.setVisibility(View.VISIBLE);
            stillView.setVisibility(View.VISIBLE);
            gifView.setVisibility(View.INVISIBLE);
        }

        /*shows the webView so we can see the gif*/
        public void showWebView() {
            if(vipAdapter)VIPParty.setVIPCounter(getPosition()+1);
            if(loaded) {
                progressBar.setVisibility(View.GONE);
                stillView.setVisibility(View.INVISIBLE);
                gifView.setVisibility(View.VISIBLE);
            } else {
                hideWebView();
            }
        }

        public void resetLoadStatus(){
            loaded = false;
        }

        public FrameLayout getContainer(){
            return container;
        }
    }

    /*constructor for this adapter. it needs context for certain methods and gifs so it doesn't
    * try to bind null elements into the viewholders*/
    public ViewHolderAdapter(Context context, Activity activity) { //MainParty adapter
        this.context = context;
        gifs = new ArrayList<>();
        this.activity = activity;
        mainPartyAdapter = true;
    }

    public ViewHolderAdapter(Context context, List<GifItem> list) { //VIP adapter
        this.context = context;
        gifs = list;
        if(getItemCount() == 0) VIPParty.setVIPCounter(0);
        else VIPParty.setVIPCounter(1);
        vipAdapter = true;
    }

    /*Create new views (invoked by the layout manager)*/
    @Override
    public ViewHolderAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gif_item, parent, false);
        return new SimpleViewHolder(view);
    }

    /*Replace the contents of a view (invoked by the layout manager)*/
    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {
        /*sets the width of the gifView because they are inconsistently sized*/
        final GifItem item = gifs.get(position);

        ViewGroup.LayoutParams params = holder.stillView.getLayoutParams();
        ViewGroup.LayoutParams pbParams = holder.progressBar.getLayoutParams();

        //layout parameters need to be in pixels, not density independent pixels (dp)
        params.width = Math.round(Integer.parseInt(String.valueOf(item.getGuestWidth())) * TwoRooms.densityMultiple);
        params.height = Math.round(Integer.parseInt(String.valueOf(item.getGuestHeight())) * TwoRooms.densityMultiple);
        pbParams.height = params.height;

        holder.progressBar.setLayoutParams(pbParams);
        holder.stillView.setLayoutParams(params);
        holder.gifView.setLayoutParams(params);

        if(position < 4) setAnimation(holder.container, position);

        int topMargin = (TwoRooms.screenHeightDp - Integer.parseInt(item.getGuestHeight())) / 2;
        int sideMargin = Math.round((TwoRooms.screenWidthDp - Integer.parseInt(item.getGuestWidth())) / 2 * TwoRooms.densityMultiple);
        RecyclerView.LayoutParams marginParams = (RecyclerView.LayoutParams) holder.container.getLayoutParams();
        if(position == 0) {
            marginParams.setMargins(sideMargin, topMargin, 0, 0); //left side and top
            holder.hideWebView();
        } else if (position == getItemCount()-1) {
            marginParams.setMargins(0, topMargin, sideMargin, 0); //right side and top
        } else {
            marginParams.setMargins(0, topMargin, 0, 0);
        }
        holder.container.setLayoutParams(marginParams);

        holder.resetLoadStatus();
        try{
            holder.gifView.loadUrl(item.getGuestAudition());
            //holder.gifView.loadUrl("http://www.google.com");
        } catch (Exception e){
            e.printStackTrace();
            holder.hideWebView();
        }

        /*gets new videos so we don't run out*/
        if(mainPartyAdapter) getMoreVideosIfNeeded(position);
    }

    private void getMoreVideosIfNeeded(int position){
        if (position == getItemCount() - 3) {
            BuildURL buildURL = new BuildURL(context);
            new VideoLoaderTask(context, activity).execute(buildURL.getURL());
        }
    }

    /* If the bound view wasn't previously displayed on screen, it's animated*/
    private void setAnimation(View viewToAnimate, int position) {
        if(position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    /*returns the number of GifItems in the adapter*/
    @Override
    public int getItemCount() {
        if(gifs == null) {
            return 0;
        } else {
            return gifs.size();
        }
    }

    public void addSavedGif(GifItem item) {
        gifs.add(item);
        saveVIPs();
        this.notifyDataSetChanged();
    }

    /*Allows another class to pass in GifItems. Used primarily (only?) by VideoTaskLoader*/
    public void setGifs(List<GifItem> gifItems) {
        for (GifItem item : gifItems) {
            gifs.add(item);
            notifyItemInserted(gifs.size() - 1);
        }
    }

    /*allows an external class to remove a gifItem from the adapter*/
    public void removeItem(int position) {
        gifs.remove(position);
        if(position == 0) notifyDataSetChanged();
        else notifyItemRemoved(position);
    }

    public void saveVIPs(){
        Storage storage = new Storage(context);
        List<String> vipGifs = new ArrayList<>();
        for (GifItem item : gifs){
            vipGifs.add(item.getGuestAudition());
            vipGifs.add(item.getGuestHeight());
            vipGifs.add(item.getGuestWidth());
            vipGifs.add(item.getGuestID());
        }
        storage.saveVIP(vipGifs);
    }

    public void removeVIPItem(int position) {
        gifs.remove(position);
        if(position == 0) notifyDataSetChanged();
        else notifyItemRemoved(position);
        saveVIPs();
    }

    /*allows an outside class to add a gifItem to the adapter*/
    public GifItem returnItem(int position) {
        try {
            return gifs.get(position);
        } catch (IndexOutOfBoundsException e){
            e.printStackTrace();
            return null;
        }
    }

    public void clearAllVIPs(){
        gifs.clear();
        notifyDataSetChanged();
    }
}