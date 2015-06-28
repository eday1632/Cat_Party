package www.appawareinc.org.catparty;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import inappbilling.BuildKey;
import io.fabric.sdk.android.Fabric;

public class MultiIntentService extends IntentService {

    public MultiIntentService() {
        super("MultiIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        String controller = extras.getString("controller", "");

        if (controller.contentEquals("crashlytics")) {
            Fabric.with(this, new Crashlytics());

        } else if (controller.contentEquals("rateApp")) {
            AppRater.evaluateIfRatingCriteriaMet(getApplicationContext());

        } else if (controller.contentEquals("increaseOffset")){
            Storage storage = new Storage(this);
            storage.increaseOffset();

        } else if (controller.contentEquals("buildURL")){
            BuildURL url = new BuildURL(this);
            Intent localIntent =
                    new Intent("buildURL")
                            .putExtra("URL", url.getURL());
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

//        } else if (controller.contentEquals("saveIDs")){
//            Storage storage = new Storage(this);
//
//            Log.d("xkcd", "increased query offset off the main thread!");

        } else if (controller.contentEquals("unwanted")) {
            SharedPreferences prefs = getSharedPreferences("unwanted_gifs", 0);
            SharedPreferences.Editor editor = prefs.edit();
            if (prefs.getBoolean("dontloadagain", true)) {
                try {
                    Resources resources = getResources();
                    Storage storage = new Storage(this);
                    storage.saveVideos(LoadFile("unwanted_gifs", resources));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                editor.putBoolean("dontloadagain", false);
                editor.apply();
            }
        }
    }

    private HashSet<String> LoadFile(String fileName, Resources resources) throws IOException {
        //Create a InputStream to read the file into
        InputStream iS;

        //get the resource id from the file name
        int rID = resources.getIdentifier("www.appawareinc.org.catparty:raw/"+fileName, null, null);
        //get the file as a stream
        iS = resources.openRawResource(rID);

        InputStreamReader inputStreamReader = new InputStreamReader(iS);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String inputLine = "";
        HashSet<String> unwantedGifs = new HashSet<>();

        while((inputLine = bufferedReader.readLine()) != null){
            unwantedGifs.add(inputLine);
        }
        bufferedReader.close();

        return unwantedGifs;
    }

    public final class Constants {
        // Defines a custom Intent action
        public static final String BROADCAST_ACTION =
                "www.appawareinc.org.catparty.ACTION";
        // Defines the key for the status "extra" in an Intent
        public static final String EXTENDED_DATA_STATUS =
                "www.appawareinc.org.catparty.STATUS";
    }
}
