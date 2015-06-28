package www.appawareinc.org.catparty;

import android.content.Context;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/* this class saves and returns three files: VIP videos, search offset, and seen videos */

public class Storage {

    public Context context;

    public Storage(Context context) {
        this.context = context;
    }

    public void saveVideos(HashSet<String> id) {

        Intent serviceIntent = new Intent(context, MultiIntentService.class);
        serviceIntent.putExtra("controller", "saveIDs");
        serviceIntent.putExtra("IDs", id.toArray(new String[id.size()]));
        context.startService(serviceIntent);
    }

    public HashSet<String> accessVideos() {
        HashSet<String> seenVideos = new HashSet<>();
        try {
            String inputLine = "";
            FileInputStream fIn = context.openFileInput("videos_seen.txt");
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader inBuff = new BufferedReader(isr);

            while ((inputLine = inBuff.readLine()) != null) {
                seenVideos.add(inputLine);
            }
            inBuff.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return seenVideos;
    }

    public void increaseOffset() {
        try {
            int previous = accessOffset() + 100; //add the number of gifs we ask Giphy to return for each query
            String savedNumber = String.valueOf(previous);
            FileOutputStream fOut = context.openFileOutput("starting_number.txt",
                    Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            try {
                osw.write(savedNumber);
                osw.flush();
                osw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int accessOffset() {
        int offset;
        try {
            String temp = "";
            String inputLine = "";
            FileInputStream fIn = context.openFileInput("starting_number.txt");
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader inBuff = new BufferedReader(isr);

            while ((inputLine = inBuff.readLine()) != null) {
                temp = temp + inputLine;
            }
            inBuff.close();
            offset = Integer.parseInt(temp);

        } catch (Exception e) {
            e.printStackTrace();
            offset = 0;
        }
        return offset;
    }

    public void saveVIP(List<String> gifs) {

        Intent serviceIntent = new Intent(context, MultiIntentService.class);
        serviceIntent.putExtra("controller", "saveVIP");
        serviceIntent.putExtra("info", gifs.toArray(new String[gifs.size()]));
        context.startService(serviceIntent);
    }

    public List<String> accessVIPs() {
        List<String> savedVIPs = new ArrayList<>();
        try {
            String inputLine = "";
            FileInputStream fIn = context.openFileInput("vip_videos.txt");
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader inBuff = new BufferedReader(isr);

            while ((inputLine = inBuff.readLine()) != null) {
                savedVIPs.add(inputLine);
            }
            inBuff.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return savedVIPs;
    }

    public void eraseVIPs() {
        try {
            FileOutputStream fOut = context.openFileOutput("vip_videos.txt",
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
    }
}