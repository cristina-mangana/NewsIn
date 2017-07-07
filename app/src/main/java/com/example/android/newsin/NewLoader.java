package com.example.android.newsin;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Created by Cristina on 07/07/2017.
 * This class loads a list of news using an AsyncTask to perform the network request to the given
 * URL.
 */

public class NewLoader extends AsyncTaskLoader<List<New>> {

    private String mUrl;

    public NewLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<New> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the HTTP request for new data and process the response.
        List<New> news = QueryUtils.fetchNewsData(mUrl);
        return news;
    }
}
