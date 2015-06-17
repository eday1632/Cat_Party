package www.appawareinc.org.catparty;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import analytics.AnalyticsTool;

/* this class gets us the gifs from a server. it runs in the background and is activated once when app starts
* and again each time the user has viewed nearly all the videos in the list returned by the last
* search. */

public class VideoLoaderTask extends AsyncTask<String, Integer, ArrayList<GifItem>> {

    private Context context;
    private Activity activity;
    private HashSet<String> seenVideos;

    public VideoLoaderTask(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Storage storage = new Storage(context);
        seenVideos = storage.accessVideos();
    }

    @Override
    protected ArrayList<GifItem> doInBackground(String... url) {
        try {
            return getGifs(url[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<GifItem> gifs) {
        super.onPostExecute(gifs);

        if(gifs != null) {
            Storage storage = new Storage(context);
            storage.increaseOffset();
            storage.saveVideos(seenVideos);
            MainParty.mainPartyAdapter.setGifs(gifs);
            MainParty.hideProgressSpinner();
            logAnalyticsEvent(gifs.size());
        } else {
            Toast.makeText(context, R.string.trouble_receiving_gifs, Toast.LENGTH_SHORT).show();
        }
    }

    private void logAnalyticsEvent(int size){
        Tracker t = ((AnalyticsTool) activity.getApplication()).getTracker(
                AnalyticsTool.TrackerName.APP_TRACKER);
            t.send(new HitBuilders.EventBuilder()
                    .setCategory("VideoLoaderTask")
                    .setAction("Retrieved gifs")
                    .setValue(size)
                    .build());
    }

    String run(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private ArrayList<GifItem> getGifs(String output) {
        ArrayList<GifItem> URLs = new ArrayList<>();
        JSONArray returnedVideos;

        try {
            String rawResponse = run(output);
            if(rawResponse == null){
                return URLs = null; //exit the process here if we didn't get anything back from Giphy
            }
            JSONParser parser = new JSONParser();
            JSONObject response = null;
            try {
                response = (JSONObject) parser.parse(rawResponse);
            } catch (ParseException pe) {
                pe.printStackTrace();
                return URLs = null; //also exit if we can't parse what was returned
            }

            returnedVideos = (JSONArray) response.get("data");

            for (int i = 0; i < returnedVideos.size() - 1; i++) {
                JSONObject video = (JSONObject) returnedVideos.get(i);
                String id = String.valueOf(video.get("id"));

                if (seenVideos.contains(id)) {
                    returnedVideos.remove(video);
                }
            }

            returnedVideos = (JSONArray) response.get("data");

            for (int i = 0; i < returnedVideos.size(); i++) {
                JSONObject chosenVideo = (JSONObject) returnedVideos.get(i);

                /*original sized videos*/
                JSONObject Oimages = (JSONObject) chosenVideo.get("images");
                JSONObject OHeight = (JSONObject) Oimages.get("original");

                String Ovideo = String.valueOf(OHeight.get("url"));
                String Oid = String.valueOf(chosenVideo.get("id"));
                String Oheight = String.valueOf(OHeight.get("height"));
                String Owidth = String.valueOf(OHeight.get("width"));

                /*fixed width*/
                JSONObject Wimages = (JSONObject) chosenVideo.get("images");
                JSONObject WHeight = (JSONObject) Wimages.get("fixed_width");

                String Wvideo = String.valueOf(WHeight.get("url"));
                String Wid = String.valueOf(chosenVideo.get("id"));
                String Wheight = String.valueOf(WHeight.get("height"));
                String Wwidth = String.valueOf(WHeight.get("width"));

                /*fixed height*/
                JSONObject Himages = (JSONObject) chosenVideo.get("images");
                JSONObject HHeight = (JSONObject) Himages.get("fixed_height");

                String Hvideo = String.valueOf(HHeight.get("url"));
                String Hid = String.valueOf(chosenVideo.get("id"));
                String Hheight = String.valueOf(HHeight.get("height"));
                String Hwidth = String.valueOf(HHeight.get("width"));

                GifItem item;

                double heightO = Integer.parseInt(Oheight);
                double widthO = Integer.parseInt(Owidth);
                double ratioO = heightO / widthO;
                double areaO = heightO * widthO;
                double heightW = Integer.parseInt(Wheight);
                double heightH = Integer.parseInt(Hheight);
                double widthH = Integer.parseInt(Hwidth);
                double areaH = heightH * widthH;
                int acceptableWidth = TwoRooms.screenWidthDp + Math.round(TwoRooms.screenWidthDp * 0.15f);
                int acceptableHeight = TwoRooms.screenHeightDp + Math.round(TwoRooms.screenHeightDp * 0.15f);

                /*algorithm for choosing the optimal gif: original, fixed height, or fixed width*/
                if(ratioO < 1.0){
                    if(widthO < acceptableWidth && heightO > heightH){
                        item = new GifItem(Ovideo, Oheight, Owidth, Oid);
                    } else if (widthH < acceptableWidth) {
                        item = new GifItem(Hvideo, Hheight, Hwidth, Hid);
                    } else {
                        item = new GifItem(Wvideo, Wheight, Wwidth, Wid);
                    }
                } else {
                    if(widthO < acceptableWidth && heightO < acceptableHeight && areaO > areaH){
                        item = new GifItem(Ovideo, Oheight, Owidth, Oid);
                    } else if(heightW < acceptableWidth) {
                        item = new GifItem(Wvideo, Wheight, Wwidth, Wid);
                    } else {
                        item = new GifItem(Hvideo, Hheight, Hwidth, Hid);
                    }
                }

                /*makes sure regardless of the gif chosen, it fits within the screen when displayed*/
                if(Integer.parseInt(item.getGuestWidth()) + 8 > TwoRooms.screenWidthDp)
                    item.setGuestWidth(String.valueOf(TwoRooms.screenWidthDp - 8));

                URLs.add(item);
                seenVideos.add(Oid);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return URLs = null;
        }
        return URLs;
    }
}
