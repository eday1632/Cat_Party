package www.appawareinc.org.catparty;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import analytics.AnalyticsTool;

public class ChooseBackground extends Activity {
    private static TextView tap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_background);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new BackgroundAdapter(getResources().getStringArray(R.array.image),
                getResources().getStringArray(R.array.backgroundImage)));

        SharedPreferences prefs = getSharedPreferences("tap_select", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return; } //comment out for testing
        //else
            tap = (TextView) findViewById(R.id.tap_instruction);
            Animation animationFadeInOut = AnimationUtils.loadAnimation(this, R.anim.fade_in_out);
            animationFadeInOut.setAnimationListener(new MyAnimationListener());
            tap.startAnimation(animationFadeInOut);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dontshowagain", true);
            editor.apply();
    }

    protected class BackgroundAdapter extends RecyclerView.Adapter<BackgroundAdapter.SimpleViewHolder> {
        private String[] titles;
        private String[] IDs;

        protected class SimpleViewHolder extends RecyclerView.ViewHolder {
            private ImageButton image;
            private TextView description;
            private int myPosition;

            public SimpleViewHolder(final View itemView) {
                super(itemView);
                image = (ImageButton) itemView.findViewById(R.id.image);
                description = (TextView) itemView.findViewById(R.id.description);

                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TwoRooms.newBackgroundChosen(IDs[myPosition]);
                        SharedPreferences prefs = getSharedPreferences(getString(R.string.pref_background), MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(getString(R.string.pref_background), IDs[myPosition]);
                        editor.commit();
                        logAnalyticsEvent(IDs[myPosition]);
                        finish();
                    }
                });
            }
        }

        public BackgroundAdapter(String[] prefs, String[] imgs) {
            titles = prefs;
            IDs = imgs;
        }

        @Override
        public BackgroundAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.background_item, parent, false);
            return new SimpleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BackgroundAdapter.SimpleViewHolder holder, int position) {
            String backgroundDescription = titles[position];
            String image = IDs[position];

            holder.myPosition = position;
            holder.description.setText(backgroundDescription);
            int width = Math.round(200.0f * TwoRooms.densityMultiple);
            int height = Math.round(300.0f * TwoRooms.densityMultiple);

            holder.image.setImageBitmap(
                    decodeSampledBitmapFromResource(getResources(), getResourceId(image), width, height));
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }
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


    private void logAnalyticsEvent(String backgroundImage){
        Tracker t = ((AnalyticsTool) getApplication()).getTracker(
                AnalyticsTool.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory("Background")
                .setAction(backgroundImage)
                .setValue(1)
                .build());
    }

    private int getResourceId(String image){
        try {
            return getResources().getIdentifier(image, "drawable", getPackageName());
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static class MyAnimationListener implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {
            tap.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            tap.clearAnimation();
            tap.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }


}
