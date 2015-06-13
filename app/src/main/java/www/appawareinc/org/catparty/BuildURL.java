package www.appawareinc.org.catparty;

import android.content.Context;
import android.net.Uri;

/* This class builds the URL that we query to return videos. */

public class BuildURL {

    public Context context;

    /*constructor needs to receive context because the storage class we instantiate needs it*/
    public BuildURL(Context context) {
        this.context = context;
    }

    /*builds the url we use to access gifs. we search for cat and return 20 videos. we also use an
    * "offset" value so that we do not receive the same 20 videos each time we query for more*/
    public String getURL() {
        Storage storage = new Storage(context);
        Uri.Builder builder;
        builder = new Uri.Builder();
        builder.scheme("http")
                .authority("api.giphy.com")
                .appendPath("v1")
                .appendPath("gifs")
                .appendPath("search")
                .appendQueryParameter("api_key", "l41lICEpoxH594Kly")
                .appendQueryParameter("q", "cat")
                .appendQueryParameter("limit", "20")
                .appendQueryParameter("offset", String.valueOf(storage.accessOffset()));

        return builder.build().toString();
    }
}