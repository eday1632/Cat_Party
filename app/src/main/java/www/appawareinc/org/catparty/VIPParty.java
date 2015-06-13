package www.appawareinc.org.catparty;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import analytics.AnalyticsTool;

/* this is the second party room - the VIP room. it is similar to MainParty*/
public class VIPParty extends Fragment {

    private OnFragmentInteractionListener mListener;
    public static SnappyRecyclerView recyclerView;
    public static TextView vipCounter;
    public static String background;
    public static SeekBar seekBar;
    private static View rootView;
    private boolean confirmDelete = true;
    private static List<GifItem> gifItems;
    private static Context context;
    private static int returnPosition = 0;
    public static ViewHolderAdapter vipAdapter;
    private static ImageView catPaw;
    private static TextView swipeDown;

    public static VIPParty newInstance(Context mContext) {
        VIPParty fragment = new VIPParty();
        Bundle args = new Bundle();
        context = mContext;
        Storage storage = new Storage(context);
        fragment.setArguments(args);
        gifItems = gifListRebuilder(storage.accessVIPs());
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

        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
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
                                sendGif(item.getGuestAudition());
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

    public static void playGifsWhenVisible(){
        Storage storage = new Storage(context);
        gifItems = gifListRebuilder(storage.accessVIPs());
        recyclerView.requestLayout();
        recyclerView.scrollToPosition(returnPosition);
        vipAdapter = new ViewHolderAdapter(context, gifItems);
        recyclerView.setAdapter(vipAdapter);
        setSeekBarMax();
    }

    public static void dontPlayGifsWhenOffscreen(){
        LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
        ViewHolderAdapter.SimpleViewHolder svh =
                (ViewHolderAdapter.SimpleViewHolder) recyclerView.findViewHolderForPosition(llm.findFirstVisibleItemPosition());
        if(svh != null) {
            returnPosition = svh.getPosition();
            recyclerView.removeAllViewsInLayout();
        }
    }

    private void confirmDeleteOnce(){
        if(confirmDelete)Toast.makeText(context, R.string.bounced, Toast.LENGTH_LONG).show();
        confirmDelete = false;
    }

    private void logAnalyticsEvent(String category, String action){
        Tracker t = ((AnalyticsTool) getActivity().getApplication()).getTracker(
                AnalyticsTool.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setValue(1)
                .build());
    }

    public static void setSeekBarMax(){
        if(vipAdapter != null) seekBar.setMax(vipAdapter.getItemCount() - 1);
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

    public static void showInitialInstruction(){
        SharedPreferences prefs = context.getSharedPreferences("vip_instructions", 0);
        SharedPreferences.Editor editor = prefs.edit();

        Animation animationDown;
        Animation animationFadeInOut;
        catPaw = (ImageView) rootView.findViewById(R.id.demoPaw);
        swipeDown = (TextView) rootView.findViewById(R.id.swipeDown);

        animationDown = AnimationUtils.loadAnimation(context, R.anim.paw_swipes_down);
        animationFadeInOut = AnimationUtils.loadAnimation(context, R.anim.fade_in_out);

        animationFadeInOut.setAnimationListener(new MyAnimationListener());
        catPaw.startAnimation(animationDown);
        swipeDown.startAnimation(animationFadeInOut);
        editor.putBoolean("dontshowagain", false);
        editor.apply();
    }

    private static class MyAnimationListener implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {
            catPaw.setVisibility(View.INVISIBLE);
            swipeDown.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            catPaw.clearAnimation();
            catPaw.setVisibility(View.GONE);
            swipeDown.clearAnimation();
            swipeDown.setVisibility(View.GONE);
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void sendGif(String url) {
        LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
        ViewHolderAdapter.SimpleViewHolder svh =
                (ViewHolderAdapter.SimpleViewHolder) recyclerView.findViewHolderForPosition(llm.findFirstVisibleItemPosition());
        if(svh != null) {
            returnPosition = svh.getPosition();
        }
        String shamelessPlug = "";
        Random random = new Random();
        int chosen = random.nextInt(10);
        switch (chosen) {
            case 0:
                shamelessPlug = "\n - Let's Cat Party!";
                break;
            case 1:
                shamelessPlug = "\n - Wanna Cat Party?";
                break;
            case 2:
                shamelessPlug = "\n - Cheetahs need us!";
                break;
            case 3:
                shamelessPlug = "\n - Make love AND jaguar";
                break;
            case 4:
                shamelessPlug = "\n - Smitten for kittens?";
                break;
            case 5:
                shamelessPlug = "\n - Doesn't cost a lot to help the ocelot!";
                break;
            case 6:
                shamelessPlug = "\n - Unjinx the lynx!";
                break;
            case 7:
                shamelessPlug = "\n - Pass the hat for the bobcat!";
                break;
            case 8:
                shamelessPlug = "\n - Start tryin' for the lion!";
                break;
            case 9:
                shamelessPlug = "\n - Be shepard to the leopard";
                break;
            case 10:
                shamelessPlug = "\n - Beggars can't be cougars";
                break;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/*");
        intent.putExtra(Intent.EXTRA_SUBJECT,"Cat Party");
        intent.putExtra(Intent.EXTRA_TEXT, "\n \n" + url + shamelessPlug);
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
