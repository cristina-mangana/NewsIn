package com.example.android.newsin;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<New>>, SwipeRefreshLayout.OnRefreshListener {

    public static final String LOG_TAG = MainActivity.class.getName();

    /** URL to fetch data from the The Guardian API */
    private String requestUrl = NEWS_REQUEST_URL;

    /** URL for last news data */
    private static final String NEWS_REQUEST_URL =
            "http://content.guardianapis.com/search?q=NOT%20%22Corrections%20and%20clarifications%22&show-fields=thumbnail%2CbodyText&order-by=newest&api-key=4c4e06be-a758-41ec-8ce2-863320d9a0a6";

    /** URL for international news data */
    private static final String INTERNATIONAL_REQUEST_URL =
            "http://content.guardianapis.com/search?section=world&show-fields=thumbnail%2CbodyText&api-key=4c4e06be-a758-41ec-8ce2-863320d9a0a6";

    /** URL for culture news data */
    private static final String CULTURE_REQUEST_URL =
            "http://content.guardianapis.com/search?section=culture&show-fields=thumbnail%2CbodyText&api-key=4c4e06be-a758-41ec-8ce2-863320d9a0a6";

    /** URL for science news data */
    private static final String SCIENCE_REQUEST_URL =
            "http://content.guardianapis.com/search?section=science&show-fields=thumbnail%2CbodyText&api-key=4c4e06be-a758-41ec-8ce2-863320d9a0a6";

    /** URL for sports news data */
    private static final String SPORTS_REQUEST_URL =
            "http://content.guardianapis.com/search?section=sport&show-fields=thumbnail%2CbodyText&api-key=4c4e06be-a758-41ec-8ce2-863320d9a0a6";
    /** Adapter for the list of news */
    private NewAdapter mAdapter;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyTextView;

    /** SwipeRefreshLayout to refresh the list of news */
    private SwipeRefreshLayout swipeRefreshLayout;

    /** Drawer toggle */
    private ActionBarDrawerToggle mDrawerToggle;

    /** Head TextView with section title */
    private TextView headTextView;

    /** Boolean to know whether or not the layout is refreshing */
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        // Initialize head Textview
        headTextView = (TextView) findViewById(R.id.head);

        // Restore state from saved instance
        if (savedInstanceState != null) {
            // Apply the text to the TextView
            headTextView.setText(savedInstanceState.getCharSequence("headText"));
            // Apply current Url
            requestUrl = savedInstanceState.getString("currentUrl");
        }

        //Hide the android keyboard (it opens by default due to the EditText)
        //From: https://stackoverflow.com/questions/9732761/how-to-avoid-automatically-appear-android-keyboard-when-activity-start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //Toolbar settings
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        // No title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //Set text typeface
        //Find TextView
        TextView toolbarTextView = (TextView) myToolbar.findViewById(R.id.toolbar_text);
        //Load typeface
        String fontPathRobotoBold = "fonts/Roboto-Bold.ttf";
        Typeface typeFaceRobotoBold = Typeface.createFromAsset(getAssets(), fontPathRobotoBold);
        //Set typeface
        toolbarTextView.setTypeface(typeFaceRobotoBold);

        //Initialize navigation drawer options
        final String[] mDrawerTitles = getResources().getStringArray(R.array.drawer_options);
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the drawer list view
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, mDrawerTitles));
        // Set first item checked by default
        mDrawerList.setItemChecked(0, true);
        // Open and close navigation drawer with toolbar icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                myToolbar,             /* Toolbar */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        // Open/Close drawer on hamburger icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set the drawer list's click listener
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Set head text
                headTextView.setText(mDrawerTitles[position]);
                switch (position) {
                    case 0:
                        // Refresh the data
                        requestUrl = NEWS_REQUEST_URL;
                        break;
                    case 1:
                        // Refresh the data
                        requestUrl = INTERNATIONAL_REQUEST_URL;
                        break;
                    case 2:
                        // Refresh the data
                        requestUrl = CULTURE_REQUEST_URL;
                        break;
                    case 3:
                        // Refresh the data
                        requestUrl = SCIENCE_REQUEST_URL;
                        break;
                    case 4:
                        // Refresh the data
                        requestUrl = SPORTS_REQUEST_URL;
                        break;
                }
                // Check for network connectivity
                boolean isConnected = checkInternetConnection();
                if (isConnected) {
                    // Restart Loader to set new data
                    restartLoader();
                } else {
                    // Clear current data
                    mAdapter.clear();
                    // Show error message
                    mEmptyTextView.setText(getString(R.string.no_internet));
                }
                // Highlight the selected item
                mDrawerList.setItemChecked(position, true);
                // Close the drawer
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

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

        // Check for network connectivity
        boolean isConnected = checkInternetConnection();

        if (isConnected) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(0, null, this);
        } else {
            // Hide the loading indicator
            progressBar.setVisibility(View.GONE);
            // Show error message
            mEmptyTextView.setText(getString(R.string.no_internet));
        }

        // Listen to search in the EditText
        // https://stackoverflow.com/questions/2004344/how-do-i-handle-imeoptions-done-button-click
        final EditText searchEditText = (EditText) findViewById(R.id.search_edit_text);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String query = searchEditText.getText().toString();
                    if (!TextUtils.isEmpty(query)) {
                        Intent openActivitySearch = new Intent(getApplicationContext(),
                                SearchActivity.class);
                        openActivitySearch.putExtra("query", query);
                        startActivity(openActivitySearch);
                    } else {
                        // Show an error message as a toast
                        Toast.makeText(getApplicationContext(), getString(R.string.query_error),
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    // Sync the toggle state after onRestoreInstanceState has occurred.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    // Sync the toggle onConfigurationChange
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    // Fires when a configuration change occurs and activity needs to save state
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // Save custom values into the bundle
        savedInstanceState.putCharSequence("headText", headTextView.getText());
        savedInstanceState.putString("currentUrl", requestUrl);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    // Helper method to check for Internet connection
    public boolean checkInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<New>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL of last news
        Uri baseUri = Uri.parse(requestUrl);
        return new NewLoader(this, baseUri.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<New>> loader, List<New> news) {
        // Hide the loading indicator
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.loading_spinner);
        progressBar.setVisibility(View.GONE);
        // Hide empty state text
        TextView emptyTextView = (TextView) findViewById(R.id.empty_text);
        emptyTextView.setVisibility(View.GONE);
        // Clear the adapter of previous data
        mAdapter.clear();

        // If there is a valid list of {@link New}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            mAdapter.addAll(news);
        } else {
            // Set empty state text
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(getString(R.string.no_results));
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
    public void onRefresh(){
        swipeRefreshLayout.setRefreshing(true);
        isRefreshing = true;
        // Check for network connectivity
        boolean isConnected = checkInternetConnection();

        if (isConnected) {
            // Restart Loader to set new data
            restartLoader();
        } else {
            // Hide the refreshing indicator
            swipeRefreshLayout.setRefreshing(false);
            isRefreshing = false;
            // Clear current data
            mAdapter.clear();
            // Show error message
            mEmptyTextView.setText(getString(R.string.no_internet));
        }
    }

    // Restart the Loader to set new data
    public void restartLoader(){
        getLoaderManager().restartLoader(0, null, this);
    }
}
