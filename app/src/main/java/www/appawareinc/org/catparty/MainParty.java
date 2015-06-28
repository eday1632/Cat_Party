package www.appawareinc.org.catparty;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Random;

import analytics.AnalyticsTool;
import circleprogressbar.CircleProgressBar;

/* this class represents the first "room" of the party. it will pop up automatically after the intro
* screen ends. */
public class MainParty extends Fragment {

    public static ViewHolderAdapter mainPartyAdapter;
    private OnFragmentInteractionListener mListener;
    private boolean confirmSave = true;
    private static View rootView;
    private static Context context;
    private boolean firstTime = true;
    private static NetworkReceiver receiver;
    private static int returnPosition = 0;
    public static boolean isActive = false;

    /*Method automatically generated when class was created in Android Studio. Functions as a constructor*/
    public static MainParty newInstance(Context mContext) {
        MainParty fragment = new MainParty();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        context = mContext;
        return fragment;
    }

    /*specifies the type of layout manager the recycler view will use*/
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
    }

    /*Method automatically created by Android Studio. Preserves the state of the app, so that
    * when you return to the app after leaving it, you return to the state you left it in*/
    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    /*Method automatically created by Android Studio. Typically onCreate is where most of the
    * action happens, i.e. layouts, initializations, etc., but in fragments that happens in
    * onCreateView for some reason*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*Method automatically created by Android Studio. Does TONS of stuff:
    * - inflates the fragment view, which will defer UI layout to the "gif_item" view created in the adapter
    * - initializes the recycler view
    * - initializes the recycler view's layout manager, adapter, and swipeTouchListener*/
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main_party, container, false);

        Activity activity = getActivity();

        mainPartyAdapter = new ViewHolderAdapter(context, activity);

        final SnappyRecyclerView recyclerView = (SnappyRecyclerView) rootView.findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(getLayoutManager());
        recyclerView.setAdapter(mainPartyAdapter);

        SwipeableTouchListener swipeTouchListener =
                new SwipeableTouchListener(recyclerView,
                        new SwipeableTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipe(int position) {
                                return true;
                            }

                            @Override
                            public void onShareBySwipeUp(SnappyRecyclerView recyclerView, int shareThis) {
                                logAnalyticsEvent("MainParty", "Share");
                                GifItem item = mainPartyAdapter.returnItem(shareThis);
                                sendGif(item.getGuestAudition(), recyclerView);
                            }

                            @Override
                            public void onSaveBySwipeDown(SnappyRecyclerView mRecyclerView, int saveThis) {
                                logAnalyticsEvent("MainParty", "Save");

                                GifItem item = mainPartyAdapter.returnItem(saveThis);
                                mainPartyAdapter.removeItem(saveThis);
                                confirmSaveOnce();
                                Log.d("unwanted", item.getGuestID());

                                if(VIPParty.vipAdapter != null) {
                                    VIPParty.vipAdapter.addSavedGif(item);
                                    VIPParty.setSeekBarMax();
                                } else {
                                    saveVIPs(item);
                                }

                                if(saveThis == 0){
                                    ViewHolderAdapter.SimpleViewHolder svh =
                                            (ViewHolderAdapter.SimpleViewHolder) recyclerView.findViewHolderForPosition(1);
                                    int topMargin = (TwoRooms.screenHeightDp - Integer.parseInt(item.getGuestHeight())) / 2;
                                    int leftMargin = Math.round((TwoRooms.screenWidthDp - Integer.parseInt(item.getGuestWidth())) / 2 * TwoRooms.densityMultiple);
                                    if(svh != null) {
                                        svh.showWebView();
                                        RecyclerView.LayoutParams marginParams = (RecyclerView.LayoutParams) svh.getContainer().getLayoutParams();
                                        marginParams.setMargins(leftMargin, topMargin, 0, 0);
                                        svh.getContainer().setLayoutParams(marginParams);
                                    }
                                }
                            }
                        });
        recyclerView.addOnItemTouchListener(swipeTouchListener);

        return rootView;
    }

    public static void saveVIPs(GifItem item){
        Storage storage = new Storage(context);
        List<String> savedGifs = storage.accessVIPs();
        savedGifs.add(item.getGuestAudition());
        savedGifs.add(item.getGuestHeight());
        savedGifs.add(item.getGuestWidth());
        savedGifs.add(item.getGuestID());
        storage.saveVIP(savedGifs);
    }

    public static void hideProgressSpinner(){
        rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    public static void showProgressSpinner(){
        rootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    public static void hideRecyclerView(){
        rootView.findViewById(R.id.main_recycler_view).setVisibility(View.GONE);
    }

    public static void showRecyclerView(){
        rootView.findViewById(R.id.main_recycler_view).setVisibility(View.VISIBLE);
    }

    private void confirmSaveOnce(){
        if(confirmSave) {
            Toast.makeText(context, R.string.saved, Toast.LENGTH_LONG).show();
            confirmSave = false;
        }
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

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /*Method generated by Android Studio. Called when the fragment is created and attached to the
    * TwoRooms activity*/
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

    private void runTaskInBackground(String task){
        Intent serviceIntent = new Intent(context, MultiIntentService.class);
        serviceIntent.putExtra("controller", task);
        context.startService(serviceIntent);
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(context).registerReceiver(privateReceiver,
                new IntentFilter("buildURL"));

        if (isOnline() && firstTime) {
            firstTime = false;
            rootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            runTaskInBackground("buildURL");
        } else if (!isOnline()) {
            rootView.findViewById(R.id.main_recycler_view).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
        }

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver(context);
        context.registerReceiver(receiver, filter);

        isActive = true;
        SharedPreferences prefs = context.getSharedPreferences("instructions", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return; } //comment out for testing
        //else

            TextView swipeUp = (TextView) rootView.findViewById(R.id.swipeUp);
            TextView swipeDown = (TextView) rootView.findViewById(R.id.swipeDown);
            Animation fadeInOut = AnimationUtils.loadAnimation(context, R.anim.fade_in_out);
            fadeInOut.setAnimationListener(new MyAnimationListener(swipeUp, swipeDown));
            swipeUp.startAnimation(fadeInOut);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dontshowagain", true);
            editor.apply();
    }

    public static void playGifsWhenVisible(){
        SnappyRecyclerView visibleRecyclerView = (SnappyRecyclerView) rootView.findViewById(R.id.main_recycler_view);
        visibleRecyclerView.requestLayout();
        visibleRecyclerView.scrollToPosition(returnPosition);
    }

    public static void dontPlayGifsWhenOffscreen(){
        SnappyRecyclerView visibleRecyclerView =
                (SnappyRecyclerView) rootView.findViewById(R.id.main_recycler_view);
        LinearLayoutManager llm = (LinearLayoutManager) visibleRecyclerView.getLayoutManager();
        ViewHolderAdapter.SimpleViewHolder svh =
                (ViewHolderAdapter.SimpleViewHolder)
                        visibleRecyclerView.findViewHolderForPosition(llm.findFirstVisibleItemPosition());
        if(svh != null) {
            returnPosition = svh.getPosition();
            visibleRecyclerView.removeAllViewsInLayout();
        }
    }


    /*Method automatically created by Android Studio. Relates to the onButtonPressed method above, but
    * it's unclear how*/
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*Method generated by Android Studio. Not sure what to do with it, and we can't delete it*/
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /*sendGif takes the url of a gif and returns a share intent that activates the share function
    * in Android. A pop-down menu appears and shows the apps the user can use to share the gif*/
    public void sendGif(String url, SnappyRecyclerView recyclerView) {
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
        String link = "market://details?id=rebuild.catpartyprotected"; //TODO: link to play store when available
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/*");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Cat Party");
        intent.putExtra(Intent.EXTRA_TEXT, "\n \n" + url + shamelessPlug);
        context.startActivity(Intent.createChooser(intent, "Send"));
    }

    /*detects if there is wifi, but does not do so dynamically. It only checks when onResume is called
    * but we really need it to listen for when wifi drops or comes on*/
    public static boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private static class MyAnimationListener implements Animation.AnimationListener{
        private TextView upInstructions;
        private TextView downInstructions;

        public MyAnimationListener(TextView tv1, TextView tv2){
            upInstructions = tv1;
            downInstructions = tv2;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            upInstructions.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            upInstructions.clearAnimation();
            upInstructions.setVisibility(View.GONE);
            Animation fadeInOut = AnimationUtils.loadAnimation(context, R.anim.fade_in_out);
            fadeInOut.setAnimationListener(new MyOtherAnimationListener(downInstructions));
            downInstructions.startAnimation(fadeInOut);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private static class MyOtherAnimationListener implements Animation.AnimationListener{
        private TextView downInstructions;

        public MyOtherAnimationListener(TextView textView){
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

    private BroadcastReceiver privateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new VideoLoaderTask(context, getActivity()).execute(intent.getStringExtra("URL"));
        }
    };

    @Override
    public void onStop() {
        super.onStop();

        dontPlayGifsWhenOffscreen();

        try {
            if(privateReceiver != null){
                context.unregisterReceiver(privateReceiver);
                privateReceiver = null;
            }
            if(receiver != null) {
                context.unregisterReceiver(receiver);
                receiver = null;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
