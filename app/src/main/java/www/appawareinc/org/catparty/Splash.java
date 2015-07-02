package www.appawareinc.org.catparty;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AsyncPlayer;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

/*this class is the intro screen that leads into the "TwoRooms" activity once it ends*/
public class Splash extends Activity {

    private static ImageView catIcon;
    private Thread splashTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        runTaskInBackground("crashlytics");

        setContentView(R.layout.activity_splash);

        if(myServiceIsRunning(JinglePlayer.class)) {
            //do nothing;
        } else {
            Intent jinglePlayer = new Intent(this, JinglePlayer.class);
            startService(jinglePlayer);
        }

        final View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        splashTimer = new Thread() {
            final Object timer = new Object();
            public void run() {
                try {
                    synchronized (timer) {
                        timer.wait(3000); //TODO: 3000 for production
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent openMain = new Intent("www.appawareinc.org.catparty.TWOROOMS");
                            startActivity(openMain);
                            int uiFlagVisible = View.SYSTEM_UI_FLAG_VISIBLE;
                            decorView.setSystemUiVisibility(uiFlagVisible);
                        }
                    });
                    finish();
                }
            }
        };
        splashTimer.start();

        catIcon = (ImageView) findViewById(R.id.splash_image);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_fade);
        animation.setAnimationListener(new MyAnimationListener());
        catIcon.startAnimation(animation);

        runTaskInBackground("unwanted");
    }

    private boolean myServiceIsRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void runTaskInBackground(String task){
        Intent serviceIntent = new Intent(this, MultiIntentService.class);
        serviceIntent.putExtra("controller", task);
        startService(serviceIntent);
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
