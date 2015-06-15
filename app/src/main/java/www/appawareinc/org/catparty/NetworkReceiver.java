package www.appawareinc.org.catparty;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.Toast;


public class NetworkReceiver extends BroadcastReceiver {

    private boolean theWifiIsReturning = false;
    private Activity activity;
    private Context context;

    public NetworkReceiver(Activity activity, Context context){
        this.activity = activity;
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.pref_background), Context.MODE_PRIVATE);
        String background = preferences.getString(context.getString(R.string.pref_background), "black_fur");

        ConnectivityManager conn =  (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();
        if(networkInfo != null) {
            if(theWifiIsReturning) {
                BuildURL buildURL = new BuildURL(context);
                new VideoLoaderTask(context, activity).execute(buildURL.getURL());
                MainParty.progressBar.setVisibility(View.VISIBLE);
                TwoRooms.setBackgroundImage(getResourceID(background), context.getResources());
            }
            MainParty.recyclerView.setVisibility(View.VISIBLE);
            if(VIPParty.isActive) {
                VIPParty.showViews();
            } else {
                NoVIPAccess.showViews();
            }

        } else {
            MainParty.recyclerView.setVisibility(View.INVISIBLE);
            MainParty.progressBar.setVisibility(View.GONE);
            if(VIPParty.isActive) {
                VIPParty.hideViews();
            } else {
                NoVIPAccess.hideViews();
            }
            theWifiIsReturning = true;

            TwoRooms.setBackgroundImage(getWashedResourceID(background), context.getResources());

            Toast.makeText(context, R.string.no_internet, Toast.LENGTH_LONG).show();
        }
    }

    private int getWashedResourceID(String image){
        try {
            return context.getResources().getIdentifier(image + "_washed", "drawable", context.getPackageName());
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int getResourceID(String image){
        try {
            return context.getResources().getIdentifier(image, "drawable", context.getPackageName());
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
