package www.appawareinc.org.catparty;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import analytics.AnalyticsTool;

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

        mainPartyAdapter = new ViewHolderAdapter(context);

        final SnappyRecyclerView recyclerView = (SnappyRecyclerView) rootView.findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
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
                                sendGif(item, recyclerView);
                            }

                            @Override
                            public void onSaveBySwipeDown(SnappyRecyclerView mRecyclerView, int saveThis) {
                                logAnalyticsEvent("MainParty", "Save");

                                GifItem item = mainPartyAdapter.returnItem(saveThis);
                                mainPartyAdapter.removeItem(saveThis);
                                confirmSaveOnce();

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

        String[] gifToSave = new String [4];
        gifToSave[0] = item.getGuestAudition();
        gifToSave[1] = item.getGuestHeight();
        gifToSave[2] = item.getGuestWidth();
        gifToSave[3] = item.getGuestID();

        System.out.println(gifToSave[3]);

        Intent serviceIntent = new Intent(context, MultiIntentService.class);
        serviceIntent.putExtra("controller", "saveAllVIPs");
        serviceIntent.putExtra("gif", gifToSave);
        context.startService(serviceIntent);
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

    private static void runTaskInBackground(String task){
        Intent serviceIntent = new Intent(context, MultiIntentService.class);
        serviceIntent.putExtra("controller", task);
        context.startService(serviceIntent);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isOnline() && firstTime) {
            firstTime = false;
            rootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            runTaskInBackground("getGifs");
        } else if (!isOnline()) {
            rootView.findViewById(R.id.main_recycler_view).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
        }

        if(receiver == null) {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            receiver = new NetworkReceiver(context);
            getActivity().registerReceiver(receiver, filter);
        }

        isActive = true;

        runTaskInBackground("MainPartyNUX");
    }

    public static void animateNUX(){
        TextView swipeUp = (TextView) rootView.findViewById(R.id.swipeUp);
        TextView swipeDown = (TextView) rootView.findViewById(R.id.swipeDown);
        Animation fadeInOut = AnimationUtils.loadAnimation(context, R.anim.fade_in_out);
        fadeInOut.setAnimationListener(new MyAnimationListener(swipeUp, swipeDown));
        swipeUp.startAnimation(fadeInOut);
    }

    public static void playGifsWhenVisible() {
        SnappyRecyclerView visibleRecyclerView = (SnappyRecyclerView) rootView.findViewById(R.id.main_recycler_view);
        visibleRecyclerView.requestLayout();
        visibleRecyclerView.scrollToPosition(returnPosition);
        ViewHolderAdapter.SimpleViewHolder svh =
                (ViewHolderAdapter.SimpleViewHolder)
                        visibleRecyclerView.findViewHolderForPosition(returnPosition);
        if(svh != null) svh.showWebView();
    }

    public static void dontPlayGifsWhenOffscreen(){
        SnappyRecyclerView visibleRecyclerView =
                (SnappyRecyclerView) rootView.findViewById(R.id.main_recycler_view);
        LinearLayoutManager llm = (LinearLayoutManager) visibleRecyclerView.getLayoutManager();
        ViewHolderAdapter.SimpleViewHolder svh =
                (ViewHolderAdapter.SimpleViewHolder)
                        visibleRecyclerView.findViewHolderForPosition(llm.findFirstVisibleItemPosition());
        if(svh != null) {
            returnPosition = svh.getPosition() + 1;
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
        void onFragmentInteraction(Uri uri);
    }

    /*sendGif takes the url of a gif and returns a share intent that activates the share function
    * in Android. A pop-down menu appears and shows the apps the user can use to share the gif*/
    public void sendGif(GifItem item, SnappyRecyclerView recyclerView) {
        LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
        ViewHolderAdapter.SimpleViewHolder svh =
                (ViewHolderAdapter.SimpleViewHolder) recyclerView.findViewHolderForPosition(llm.findFirstVisibleItemPosition());
        if(svh != null) {
            returnPosition = svh.getPosition() + 1;
        }
//        String shamelessPlug = "";
//        Random random = new Random();
//        int chosen = random.nextInt(10);
//        switch (chosen) {
//            case 0:
//                shamelessPlug = "\n - Let's Cat Party!";
//                break;
//            case 1:
//                shamelessPlug = "\n - Wanna Cat Party?";
//                break;
//            case 2:
//                shamelessPlug = "\n - Cheetahs need us!";
//                break;
//            case 3:
//                shamelessPlug = "\n - Make love AND jaguar";
//                break;
//            case 4:
//                shamelessPlug = "\n - Smitten for kittens?";
//                break;
//            case 5:
//                shamelessPlug = "\n - Doesn't cost a lot to help the ocelot!";
//                break;
//            case 6:
//                shamelessPlug = "\n - Unjinx the lynx!";
//                break;
//            case 7:
//                shamelessPlug = "\n - Pass the hat for the bobcat!";
//                break;
//            case 8:
//                shamelessPlug = "\n - Start tryin' for the lion!";
//                break;
//            case 9:
//                shamelessPlug = "\n - Be shepard to the leopard";
//                break;
//            case 10:
//                shamelessPlug = "\n - Beggars can't be cougars";
//                break;
//        }

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

    @Override
    public void onPause() {
        try {
            getActivity().unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        dontPlayGifsWhenOffscreen();
        super.onStop();
    }
}
