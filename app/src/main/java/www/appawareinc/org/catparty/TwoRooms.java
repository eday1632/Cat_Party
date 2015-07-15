package www.appawareinc.org.catparty;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.util.ArrayList;
import java.util.List;

/*this class handles switching between the two party rooms. each party is a "fragment"; this is the
* "activity" */
public class TwoRooms extends ActionBarActivity implements MainParty.OnFragmentInteractionListener,
        VIPParty.OnFragmentInteractionListener, NoVIPAccess.OnFragmentInteractionListener {

    private Context context;
    private static StickyViewPager mViewPager;
    private static String background;
    private boolean notFirst = false;
    private ResponseReceiver responseReceiver;
    private static boolean notYetExecuted = true;
    private static boolean mainPartyWasLastVisible = true;
    public static float densityMultiple;
    public static int screenWidthDp;
    public static int screenHeightDp;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_two_rooms);
        context = this;
        activity = this;
        AppRater.evaluateIfRatingCriteriaMet(this);
        screenDimensions();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setLogo(R.drawable.logo_noedge);

        final RippleBackground rippleBackground = (RippleBackground) findViewById(R.id.content);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        List<String> list = new ArrayList<>();
        list.add("  Tonight's Guests      ");
        list.add("  VIP Section             ");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_text, list);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_text);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mViewPager.setCurrentItem(position, false);
                notYetExecuted = true;

                if (position == 0 && VIPParty.isActive) {
                    VIPParty.dontPlayGifsWhenOffscreen();
                }

                if (notFirst) {
                    rippleBackground.startRippleAnimation();
                    Thread rippleTimer = new Thread() {
                        public void run() {
                            try {
                                sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } finally {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        rippleBackground.stopRippleAnimation();
                                    }
                                });
                            }
                        }
                    };
                    rippleTimer.start();
                }
                notFirst = true;

                SharedPreferences prefs = getSharedPreferences("vip_access", 0);
                SharedPreferences instructions = getSharedPreferences("vip_instructions", 0);

                if (prefs.getInt("granted", 0) == 1 && position == 1) {
                    NoVIPAccess.catsShunYou();
                    Toast.makeText(getBaseContext(), R.string.guest_list, Toast.LENGTH_SHORT).show();
                } else if (instructions.getBoolean("dontshowagain", true) && position == 1) {
                    VIPParty.showInitialInstruction();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (StickyViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position == 0) {
                    MainParty.playGifsWhenVisible();
                } else if (position == 1 && VIPParty.isActive){
                    VIPParty.playGifsWhenVisible();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private class ResponseReceiver extends BroadcastReceiver {
        private ResponseReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            new VideoLoaderTask(context, activity).execute(extras.getString("URL"));
        }
    }

    private void runTaskInBackground(String task){
        Intent serviceIntent = new Intent(this, MultiIntentService.class);
        serviceIntent.putExtra("controller", task);
        startService(serviceIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (NoVIPAccess.mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!NoVIPAccess.mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {

        }
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static void newBackgroundChosen(String selection){
        background = selection;
    }

    public static void setBackgroundImage(int img, Resources resources){
        Drawable drawable = new BitmapDrawable(resources,
            decodeSampledBitmapFromResource(resources, img, screenWidthDp, screenHeightDp));

        mViewPager.setBackground(drawable);
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

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    private int getWashedResourceId(String image){
        try {
            return getResources().getIdentifier(image + "_washed", "drawable", getPackageName());
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int getResourceId(String image) {
        try {
            return getResources().getIdentifier(image, "drawable", getPackageName());
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void screenDimensions() {
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point dimensions = new Point();
        display.getSize(dimensions);
        float pxWidth = dimensions.x;
        float pxHeight = dimensions.y;

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float density = metrics.densityDpi;
        densityMultiple = density / 160.0f;
        screenWidthDp = Math.round(pxWidth/metrics.density);
        screenHeightDp = Math.round(pxHeight/metrics.density);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(getApplicationContext());

        IntentFilter mStatusIntentFilter = new IntentFilter("URL");
        mStatusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        responseReceiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, mStatusIntentFilter);

        if(mainPartyWasLastVisible && MainParty.isActive){
            MainParty.playGifsWhenVisible();
        } else if (!mainPartyWasLastVisible && VIPParty.isActive){
            VIPParty.playGifsWhenVisible();
        }

        SharedPreferences preferences = getSharedPreferences(getString(R.string.pref_background), Context.MODE_PRIVATE);
        background = preferences.getString(getString(R.string.pref_background), "black_fur");

        if (isOnline()) {
            setBackgroundImage(getResourceId(background), getResources());
        } else {
            setBackgroundImage(getWashedResourceId(background), getResources());
        }
    }

    @Override
    protected void onPause() {
        AppEventsLogger.deactivateApp(getApplicationContext());

        if (responseReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(responseReceiver);
            responseReceiver = null;
        }
        super.onPause();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_two_rooms, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent preferences = new Intent("www.appawareinc.org.catparty.CHOOSEBACKGROUND");
            startActivity(preferences);
            return true;
        } else if (id == R.id.about_cat_party) {
            Intent aboutCP = new Intent("www.appawareinc.org.catparty.ABOUTCATPARTY");
            startActivity(aboutCP);
            return true;
        } else if (id == R.id.clear_vips) {
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.erase_vips);
            dialog.getWindow().setLayout(Math.round(280 * densityMultiple), Math.round(179 * densityMultiple)); //width, height

            Button yesDelete = (Button) dialog.findViewById(R.id.yes_delete);
            yesDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Storage storage = new Storage(getBaseContext());
                    storage.eraseVIPs();
                    if(VIPParty.vipAdapter != null) {
                        VIPParty.vipAdapter.clearAllVIPs();
                        VIPParty.setSeekBarMax();
                        VIPParty.setVIPCounter(0);

                    }
                    dialog.dismiss();
                    Toast.makeText(getBaseContext(), R.string.vips_cleared, Toast.LENGTH_SHORT).show();
                }
            });

            Button dontDelete = (Button) dialog.findViewById(R.id.dont_delete);
            dontDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();

            return true;
        } else {
            return true;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);

            if(position == 0 && notYetExecuted){
                mainPartyWasLastVisible = true;
                if(VIPParty.isActive) VIPParty.dontPlayGifsWhenOffscreen();
                if(MainParty.isActive) MainParty.playGifsWhenVisible();
            } else if (position == 1 && notYetExecuted) {
                mainPartyWasLastVisible = false;
                MainParty.dontPlayGifsWhenOffscreen();
                if(VIPParty.isActive) VIPParty.playGifsWhenVisible();
            }
            notYetExecuted = false;
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 0) {
                return MainParty.newInstance(context);
            } else {
                SharedPreferences prefs = context.getSharedPreferences("vip_access", 0);
                if (prefs.getInt("granted", 1) == 2) { // == 1 to reset, == 2 otherwise
                    return VIPParty.newInstance(context);
                } else {
                    return NoVIPAccess.newInstance(context);
                }
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        runTaskInBackground("clearData");
    }
}
