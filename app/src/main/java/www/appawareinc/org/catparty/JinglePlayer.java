package www.appawareinc.org.catparty;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import java.io.IOException;

public class JinglePlayer extends Service implements MediaPlayer.OnPreparedListener {
    MediaPlayer mMediaPlayer = new MediaPlayer();

    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            mMediaPlayer.setDataSource(getBaseContext(),
                    Uri.parse("android.resource://www.appawareinc.org.catparty/" + R.raw.cat_party_jingle));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mMediaPlayer.release();
                stopSelf();
            }
        });
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.prepareAsync(); // prepare async to not block main thread

        return 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer != null) mMediaPlayer.release();
    }
}