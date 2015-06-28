package www.appawareinc.org.catparty;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
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
        private ViewGroup.LayoutParams viewParams;
        private ViewGroup.LayoutParams progressBarParams;
        private Animation fadeIn;
        public int width = 0;

        public SimpleViewHolder(final View itemView) {
            super(itemView);
            gifView = (WebView) itemView.findViewById(R.id.videoView);
            container = (FrameLayout) itemView.findViewById(R.id.container);
            progressBar = (CircleProgressBar) itemView.findViewById(R.id.progressBar);
            stillView = (ImageView) itemView.findViewById(R.id.stillView);

            itemView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            viewParams = stillView.getLayoutParams();
            progressBarParams = progressBar.getLayoutParams();

            fadeIn = AnimationUtils.loadAnimation(context, R.anim.quick_fade_in);

            gifView.setWebViewClient(new WebViewClient() {
                @Override
                public void onLoadResource(WebView view, String url) {
                    if(url.contains("favicon.ico")) {
                        ;
                    } else {
                        super.onLoadResource(view, url);
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    if(url.contains("blank.html")) return;
                    loaded = true;
                    int screenMiddle = Math.round(TwoRooms.screenWidthDp * TwoRooms.densityMultiple / 2);
                    if (container.getLeft() < screenMiddle && container.getRight() > screenMiddle) {
                        showWebView();
                    }
                }
            });
        }

        public int getLeft(){
            return itemView.getLeft();
        }

        public int getRight(){
            return itemView.getRight();
        }

        public void hideAllViews(){
            progressBar.setVisibility(View.GONE);
            stillView.setVisibility(View.INVISIBLE);
            gifView.setVisibility(View.INVISIBLE);
        }

        /*hides the webView so we can't see the gif, even though it may be loaded*/
        public void hideWebView() {
            progressBar.setVisibility(View.VISIBLE);
            fadeIn.setAnimationListener(new BlankInListener(stillView));
            stillView.startAnimation(fadeIn);
        }

        /*shows the webView so we can see the gif*/
        public void showWebView() {
            if(vipAdapter)VIPParty.setVIPCounter(getPosition()+1);
            if(loaded) {
                progressBar.setVisibility(View.GONE);
                fadeIn.setAnimationListener(new WebInListener(gifView));
                gifView.startAnimation(fadeIn);
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

    private static class WebInListener implements Animation.AnimationListener{
        private WebView gifView;

        public WebInListener(WebView webView){
            gifView = webView;
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            gifView.clearAnimation();
            gifView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private static class BlankInListener implements Animation.AnimationListener{
        private ImageView imageView;

        public BlankInListener(ImageView image){
            imageView = image;
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            imageView.clearAnimation();
            imageView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

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
        int width = Integer.parseInt(item.getGuestWidth());
        int height = Integer.parseInt(item.getGuestHeight());
        holder.width = width;

        //layout parameters need to be in pixels, not density independent pixels (dp)
        holder.viewParams.width = Math.round(width * TwoRooms.densityMultiple);
        holder.viewParams.height = Math.round(height * TwoRooms.densityMultiple);
        holder.progressBarParams.height = holder.viewParams.height;

        holder.progressBar.setLayoutParams(holder.progressBarParams);
        holder.stillView.setLayoutParams(holder.viewParams);
        holder.gifView.setLayoutParams(holder.viewParams);

        if(position < 4) setAnimation(holder.container, position);

        int topMargin = Math.round((TwoRooms.screenHeightDp - height) * TwoRooms.densityMultiple / 3);
        int sideMargin = Math.round((TwoRooms.screenWidthDp - width) / 2 * TwoRooms.densityMultiple);
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
//            holder.gifView.loadUrl("http://www.google.com");
        } catch (Exception e){
            e.printStackTrace();
            holder.hideWebView();
        }

        /*gets new videos so we don't run out*/
        if(mainPartyAdapter) getMoreVideosIfNeeded(position);
    }

    private void getMoreVideosIfNeeded(int position){
        if (position == getItemCount() - 3) {
            runTaskInBackground("buildURL");
        }
    }

    private void runTaskInBackground(String task){
        Intent serviceIntent = new Intent(context, MultiIntentService.class);
        serviceIntent.putExtra("controller", task);
        context.startService(serviceIntent);
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