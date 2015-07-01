package www.appawareinc.org.catparty;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

        if (controller.contentEquals("crashlytics")) { //for crashlytics; once per session
            Fabric.with(this, new Crashlytics());

        } else if (controller.contentEquals("rateApp")) { //for rating app; once per session
            AppRater.evaluateIfRatingCriteriaMet(getApplicationContext());

        } else if (controller.contentEquals("saveVIP")) { //for saving one VIP; multiple per session

            try {
                FileOutputStream fOut = openFileOutput("vip_videos.txt",
                        Context.MODE_PRIVATE); //mode append?
                OutputStreamWriter osw = new OutputStreamWriter(fOut);
                try {
                    for (String piece : intent.getStringArrayExtra("info"))
                        osw.write(piece + "\n");
                    osw.flush();
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else if (controller.contentEquals("saveIDs")) { //for saving all IDs; multiple per session

            try {
                FileOutputStream fOut = openFileOutput("videos_seen.txt",
                        Context.MODE_PRIVATE);
                OutputStreamWriter osw = new OutputStreamWriter(fOut);
                try {
                    for (String item : intent.getStringArrayExtra("IDs")) {
                        osw.write(item + "\n");
                    }
                    osw.flush();
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else if (controller.contentEquals("increaseOffset")){ //for increasing query offset; multiple per session
            Storage storage = new Storage(this);
            storage.increaseOffset();

        } else if (controller.contentEquals("MainPartyNUX")){ //for increasing query offset; multiple per session
            SharedPreferences prefs = getSharedPreferences("instructions", 0);
            if (prefs.getBoolean("dontshowagain", false)) { return; } //comment out for testing
            //else
            MainParty.animateNUX();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dontshowagain", true);
            editor.apply();

        } else if (controller.contentEquals("unwanted")) { //loads unwanted video list; once per session
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
}
