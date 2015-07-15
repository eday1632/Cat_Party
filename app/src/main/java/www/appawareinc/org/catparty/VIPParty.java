package www.appawareinc.org.catparty;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import analytics.AnalyticsTool;

/* this is the second party room - the VIP room. it is similar to MainParty*/
public class VIPParty extends Fragment {

    private OnFragmentInteractionListener mListener;
    private static SnappyRecyclerView recyclerView;
    private static TextView vipCounter;
    private static View rootView;
    private boolean confirmDelete = true;
    private static Context context;
    private static int returnPosition = 0;
    public static ViewHolderAdapter vipAdapter;
    public static boolean isActive = false;

    public static VIPParty newInstance(Context mContext) {
        VIPParty fragment = new VIPParty();
        Bundle args = new Bundle();
        context = mContext;
        fragment.setArguments(args);
        return fragment;
    }

    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_vipparty, container, false);

        vipCounter = (TextView) rootView.findViewById(R.id.vip_counter);
        vipCounter.bringToFront();

        recyclerView = (SnappyRecyclerView) rootView.findViewById(R.id.vip_recycler_view);
        recyclerView.setLayoutManager(getLayoutManager());

        SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int tempProgress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setVIPCounter(progress + 1);
                tempProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                recyclerView.scrollToPosition(tempProgress);
            }
        });

        SwipeableTouchListener swipeTouchListener =
                new SwipeableTouchListener(recyclerView,
                        new SwipeableTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipe(int position) {
                                return true;
                            }

                            @Override
                            public void onShareBySwipeUp(SnappyRecyclerView recyclerView, int shareThis) {
                                logAnalyticsEvent("VIPParty", "Share");
                                GifItem item = vipAdapter.returnItem(shareThis);
                                sendGif(item);
                            }

                            @Override /* !!!! THIS REMOVES, NOT SAVES, THE GIF !!!! */
                            public void onSaveBySwipeDown(SnappyRecyclerView recyclerView, int dismissThis) {
                                logAnalyticsEvent("VIPParty", "Delete");
                                try {
                                    vipAdapter.removeVIPItem(dismissThis);
                                } catch (IndexOutOfBoundsException e){
                                    e.printStackTrace();
                                }
                                if(dismissThis == 0){
                                    GifItem item = vipAdapter.returnItem(dismissThis);
                                    if(item != null) {
                                        ViewHolderAdapter.SimpleViewHolder svh =
                                                (ViewHolderAdapter.SimpleViewHolder) recyclerView.findViewHolderForPosition(1);
                                        int topMargin = (TwoRooms.screenHeightDp - Integer.parseInt(item.getGuestHeight())) / 2;
                                        int leftMargin = Math.round((TwoRooms.screenWidthDp - Integer.parseInt(item.getGuestWidth())) / 2 * TwoRooms.densityMultiple);
                                        RecyclerView.LayoutParams marginParams = (RecyclerView.LayoutParams) svh.getContainer().getLayoutParams();
                                        marginParams.setMargins(leftMargin, topMargin, 0, 0);
                                        svh.getContainer().setLayoutParams(marginParams);
                                        svh.showWebView();
                                    }
                                } else if (dismissThis == vipAdapter.getItemCount()) {
                                    GifItem item = vipAdapter.returnItem(dismissThis-1);
                                    ViewHolderAdapter.SimpleViewHolder svh =
                                            (ViewHolderAdapter.SimpleViewHolder) recyclerView.findViewHolderForPosition(dismissThis-1);
                                    if(svh != null) {
                                        int topMargin = (TwoRooms.screenHeightDp - Integer.parseInt(item.getGuestHeight())) / 2;
                                        int rightMargin = Math.round((TwoRooms.screenWidthDp - Integer.parseInt(item.getGuestWidth())) / 2 * TwoRooms.densityMultiple);
                                        RecyclerView.LayoutParams marginParams = (RecyclerView.LayoutParams) svh.getContainer().getLayoutParams();
                                        marginParams.setMargins(0, topMargin, rightMargin, 0);
                                        svh.getContainer().setLayoutParams(marginParams);
                                        svh.showWebView();
                                    }
                                }
                                confirmDeleteOnce();
                                setSeekBarMax();
                            }
                        });
        recyclerView.addOnItemTouchListener(swipeTouchListener);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;
    }

    public static void hideViews() {
        rootView.findViewById(R.id.vip_recycler_view).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.seekBar).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.vip_counter).setVisibility(View.INVISIBLE);
    }

    public static void showViews(){
        rootView.findViewById(R.id.vip_recycler_view).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.vip_counter).setVisibility(View.VISIBLE);
        if(vipAdapter != null && vipAdapter.getItemCount() > 9) rootView.findViewById(R.id.seekBar).setVisibility(View.VISIBLE);
    }


    public static void playGifsWhenVisible(){
        Storage storage = new Storage(context);
        vipAdapter = new ViewHolderAdapter(context, gifListRebuilder(storage.accessVIPs()));

        SnappyRecyclerView recyclerView = (SnappyRecyclerView) rootView.findViewById(R.id.vip_recycler_view);
        recyclerView.requestLayout();
        recyclerView.scrollToPosition(returnPosition);
        recyclerView.setAdapter(vipAdapter);
        ViewHolderAdapter.SimpleViewHolder svh =
                (ViewHolderAdapter.SimpleViewHolder)
                        recyclerView.findViewHolderForPosition(returnPosition);
        if(svh != null) svh.showWebView();
        setSeekBarMax();
    }

    public static void dontPlayGifsWhenOffscreen(){
        SnappyRecyclerView recyclerView = (SnappyRecyclerView) rootView.findViewById(R.id.vip_recycler_view);
        LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
        ViewHolderAdapter.SimpleViewHolder svh =
                (ViewHolderAdapter.SimpleViewHolder) recyclerView.findViewHolderForPosition(llm.findFirstVisibleItemPosition());
        if(svh != null) {
            returnPosition = svh.getPosition() + 1;
            recyclerView.removeAllViewsInLayout();
            vipAdapter = null;
        }
    }

    private void confirmDeleteOnce(){
        if(confirmDelete) Toast.makeText(context, R.string.bounced, Toast.LENGTH_LONG).show();
        confirmDelete = false;
    }

    private void logAnalyticsEvent(String category, String action){
        try {
            Tracker t = ((AnalyticsTool) getActivity().getApplication()).getTracker(
                    AnalyticsTool.TrackerName.APP_TRACKER);
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .setValue(1)
                    .build());
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public static void setSeekBarMax(){
        if(vipAdapter != null) {
            SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
            seekBar.setMax(vipAdapter.getItemCount() - 1);
            if(vipAdapter.getItemCount() > 9 && MainParty.isOnline()) {
                rootView.findViewById(R.id.seekBar).setVisibility(View.VISIBLE);
            } else {
                rootView.findViewById(R.id.seekBar).setVisibility(View.GONE);
            }
        }
    }

    public static void setVIPCounter(int position){
        if(vipAdapter != null && vipCounter != null){
            if(vipAdapter.getItemCount() == 0){
                vipCounter.setText(0 + " of " + 0);
            } else {
                vipCounter.setText(position + " of " + vipAdapter.getItemCount());
            }
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private static void runTaskInBackground(String task){
        Intent serviceIntent = new Intent(context, MultiIntentService.class);
        serviceIntent.putExtra("controller", task);
        context.startService(serviceIntent);
    }

    public static void showInitialInstruction(){
        TextView swipeDown = (TextView) rootView.findViewById(R.id.swipeDown);
        Animation animationFadeInOut = AnimationUtils.loadAnimation(context, R.anim.fade_in_out);
        animationFadeInOut.setAnimationListener(new MyAnimationListener(swipeDown));
        swipeDown.startAnimation(animationFadeInOut);

        runTaskInBackground("vipInstructions");
    }

    private static class MyAnimationListener implements Animation.AnimationListener {
        private TextView downInstructions;

        public MyAnimationListener(TextView textView){
            downInstructions = textView;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            downInstructions.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            downInstructions.clearAnimation();
            downInstructions.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        dontPlayGifsWhenOffscreen();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void sendGif(GifItem item) {
        LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
        ViewHolderAdapter.SimpleViewHolder svh =
                (ViewHolderAdapter.SimpleViewHolder) recyclerView.findViewHolderForPosition(llm.findFirstVisibleItemPosition());
        if(svh != null) {
            returnPosition = svh.getPosition() + 1;
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(item.getGuestAudition()));
        request.setTitle("Cat Party");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, item.getGuestID()+".gif");
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);

        Toast.makeText(context, R.string.downloading, Toast.LENGTH_SHORT).show();

        String link = "http://play.google.com/store/apps/details?id=www.appawareinc.org.catparty";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/*");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Cat Party");
        intent.putExtra(Intent.EXTRA_TEXT, "\n" + link);

        context.startActivity(Intent.createChooser(intent, "Send"));
    }

    public static List<GifItem> gifListRebuilder(List<String> list) {

        List<GifItem> gifs = new ArrayList<>();
        while (!list.isEmpty()) {
            GifItem item = new GifItem();
            for (int i = 0; i < 4; i++) {
                switch (i) {
                    case 0:
                        item.setGuestAudition(list.get(0));
                        list.remove(0);
                        break;
                    case 1:
                        item.setGuestHeight(list.get(0));
                        list.remove(0);
                        break;
                    case 2:
                        item.setGuestWidth(list.get(0));
                        list.remove(0);
                        break;
                    case 3:
                        item.setGuestID(list.get(0));
                        list.remove(0);
                        break;
                }
            }
            gifs.add(item);
        }
        return gifs;
    }
}
