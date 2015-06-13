package www.appawareinc.org.catparty;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

/*this class is the intro screen that leads into the "TwoRooms" activity once it ends*/
public class Splash extends Activity {

    private Object timer = new Object();
    private static ImageView catIcon;
    private Thread splashTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        splashTimer = new Thread() {
            public void run() {
                try {
                    synchronized (timer) {
                        timer.wait(3000); //TODO: 3000 for production
                        Intent openMain = new Intent("www.appawareinc.org.catparty.TWOROOMS");
                        startActivity(openMain);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    finish();
                }
            }
        };
        splashTimer.start();

        catIcon = (ImageView) findViewById(R.id.splash_image);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_fade);
        animation.setAnimationListener(new MyAnimationListener());
        catIcon.startAnimation(animation);

        SharedPreferences prefs = getSharedPreferences("unwanted_gifs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        if (prefs.getBoolean("dontloadagain", true)) {
            Resources resources = getResources();
            try {
                //Load the file from the raw folder - don't forget to OMIT the extension
                Storage storage = new Storage(this);
                storage.saveVideos(LoadFile("unwanted_gifs", resources));

            } catch (IOException e) {
                e.printStackTrace();
            }
            editor.putBoolean("dontloadagain", false);
            editor.apply();
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

    private static class MyAnimationListener implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            catIcon.clearAnimation();
            catIcon.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        splashTimer.interrupt();
    }
}
