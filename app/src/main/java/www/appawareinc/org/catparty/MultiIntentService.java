package www.appawareinc.org.catparty;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;

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

        } else if (controller.contentEquals("getGifs")) { //multiple per session
            BuildURL url = new BuildURL(this);
            Intent localIntent = new Intent("URL").putExtra("URL", url.getURL());
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        } else if (controller.contentEquals("clearData")) { //once per session
            clearApplicationData(this);

        } else if (controller.contentEquals("rateApp")) { //for rating app; once per session
            AppRater.evaluateIfRatingCriteriaMet(getApplicationContext());

        } else if (controller.contentEquals("saveAllVIPs")) { //for saving VIPs; multiple per session
            Storage storage = new Storage(this);
            List<String> savedGifs = storage.accessVIPs();
            for(String item : intent.getStringArrayExtra("gif"))
                savedGifs.add(item);

            storage.saveVIP(savedGifs);

        } else if (controller.contentEquals("saveVIP")) { //for saving VIPs; multiple per session

            try {
                FileOutputStream fOut = openFileOutput("vip_videos.txt",
                        Context.MODE_PRIVATE); //mode append?
                OutputStreamWriter osw = new OutputStreamWriter(fOut);
                try {
                    for (String piece : intent.getStringArrayExtra("info")) {
                        osw.write(piece + "\n");
                        System.out.println(piece);
                    }
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

        } else if (controller.contentEquals("initializeVIP")){ //once per session
            SharedPreferences prefs = getSharedPreferences("vip_access", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("granted", 1);
            editor.commit();

        } else if (controller.contentEquals("MainPartyNUX")){ //multiple per session
            SharedPreferences prefs = getSharedPreferences("instructions", 0);
            if (prefs.getBoolean("dontshowagain", false)) { return; } //comment out for testing
            //else
            MainParty.animateNUX();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dontshowagain", true);
            editor.commit();

        } else if (controller.contentEquals("backgroundChange")){//occasionally called
            SharedPreferences prefs = getSharedPreferences(getString(R.string.pref_background), MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.pref_background), extras.getString("image"));
            editor.apply();

        } else if (controller.contentEquals("eraseVIPs")){ //occasionally called
            try {
                FileOutputStream fOut = openFileOutput("vip_videos.txt",
                        Context.MODE_PRIVATE);
                OutputStreamWriter osw = new OutputStreamWriter(fOut);
                try {
                    osw.write("");
                    osw.flush();
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else if (controller.contentEquals("vipInstructions")) {// once ever
            SharedPreferences prefs = getSharedPreferences("vip_instructions", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dontshowagain", false);
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
                editor.commit();
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

    public static void clearApplicationData(Context context) {
        File cache = context.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                File f = new File(appDir, s);
                if(f.getAbsolutePath().contains("files") ||
                        f.getAbsolutePath().contains("shared_prefs")){
                    //do nothing
                } else {
                    deleteDir(f);
                }
            }
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
