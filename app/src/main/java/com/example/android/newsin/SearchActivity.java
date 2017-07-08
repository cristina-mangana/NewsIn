package com.example.android.newsin;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<New>>, SwipeRefreshLayout.OnRefreshListener {

    /**
     * URL for news data from the The Guardian API
     */
    private static final String SEARCH_NEWS_REQUEST_URL =
            "http://content.guardianapis.com/search?show-fields=thumbnail%2CbodyText&api-key=4c4e06be-a758-41ec-8ce2-863320d9a0a6";

    /**
     * User's query
     */
    String query;

    /**
     * Adapter for the list of news
     */
    private NewAdapter mAdapter;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyTextView;

    /**
     * SwipeRefreshLayout to refresh the list of news
     */
    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        handleIntent(getIntent());

        //Toolbar settings
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        // No title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Add back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Progress bar color
        // https://stackoverflow.com/questions/26962136/indeterminate-circle-progress-bar-on-android-is-white-despite-coloraccent-color
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.loading_spinner);
        if (progressBar.getIndeterminateDrawable() != null) {
            progressBar.getIndeterminateDrawable()
                    .setColorFilter(ContextCompat.getColor(this, R.color.colorAccent),
                            android.graphics.PorterDuff.Mode.SRC_IN);
        }

        // Find a reference to the {@link ListView} in the layout
        ListView newsListView = (ListView) findViewById(R.id.list);

        // Find a reference to the TextView in the layout
        mEmptyTextView = (TextView) findViewById(R.id.empty_text);
        // Set an empty view
        newsListView.setEmptyView(mEmptyTextView);

        // Create a new {@link NewAdapter} that takes an empty list of news as input
        final List<New> mNewsList = new ArrayList<>();
        mAdapter = new NewAdapter(this, mNewsList);

        // Set the adapter on the {@link ListView} so the list can be populated in the UI
        newsListView.setAdapter(mAdapter);

        // Set a click listener to open the original new in the user's browser when the list item
        // is clicked on
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // If newUrl exists, open it, else open the default The Guardian webPage
                Uri webPage;
                if (!TextUtils.isEmpty(mNewsList.get(position).getNewUrl())) {
                    webPage = Uri.parse(mNewsList.get(position).getNewUrl());
                } else {
                    webPage = Uri.parse("https://www.theguardian.com/");
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        // Refresh Layout on pulling down
        // https://stackoverflow.com/questions/4583484/how-to-implement-android-pull-to-refresh
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        // Get a reference to the LoaderManager, in order to interact with loaders.
        LoaderManager loaderManager = getLoaderManager();

        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        loaderManager.initLoader(1, null, this);
    }

    // Helper method to check for Internet connection
    public boolean checkInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // Get the intent from the search action in the toolbar
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    // Helper method to handle the search intent
    // https://developer.android.com/guide/topics/search/search-dialog.html#SearchableConfiguration
    private void handleIntent(Intent intent) {
        // If query comes from the toolbar search widget, refresh activity
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            mAdapter.clear();
            // Hide empty state TextView
            mEmptyTextView.setVisibility(View.GONE);
            // Show the loading indicator
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.loading_spinner);
            progressBar.setVisibility(View.VISIBLE);
            // Restart Loader to set new data
            getLoaderManager().restartLoader(1, null, this);
        } else {
            // If query comes from MainActivity
            query = getIntent().getExtras().getString("query");
        }
    }

    // Handle back button on toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Inflate toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_toolbar_menu, menu);
        // Retrieve the SearchView and plug it into SearchManager
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    // Handle clicks on toolbar menu buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<New>> onCreateLoader(int i, Bundle bundle) {
        String queryForUrl = query;
        // Replace spaces with %20 character in query, to be used as URL to fetch data
        if (query.contains(" ")) {
            queryForUrl = query.replace(" ", "%20");
        }

        // Create a new loader for the given URL appending the user query
        Uri baseUri = Uri.parse(SEARCH_NEWS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("q", queryForUrl);
        return new NewLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<New>> loader, List<New> news) {
        // Hide the loading indicator
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.loading_spinner);
        progressBar.setVisibility(View.GONE);
        // Hide empty state text
        mEmptyTextView.setText(getString(R.string.no_results));
        mEmptyTextView.setVisibility(View.GONE);
        // Clear the adapter of previous data
        mAdapter.clear();

        // Check for network connectivity
        boolean isConnected = checkInternetConnection();
        if (!isConnected) {
            // Show error message
            mEmptyTextView.setText(getString(R.string.no_internet));
        }

        // If there is a valid list of {@link New}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            mAdapter.addAll(news);
        } else {
            // Set empty state text
            mEmptyTextView.setVisibility(View.VISIBLE);
        }

        // Hide the refresh icon
        if (isRefreshing) {
            swipeRefreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<New>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    // Listen to refreshes made by the user
    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        isRefreshing = true;
        // Restart Loader to set new data
        getLoaderManager().restartLoader(1, null, this);
    }
}
