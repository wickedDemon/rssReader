package com.rssreader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.rssreader.adapter.SearchFeedAdaptor;
import com.rssreader.fragment.FeedListFragment;
import com.rssreader.model.FeedlyResponse;
import com.rssreader.provider.FeedData;
import com.rssreader.service.FeedlyService;
import com.rssreader.service.RefreshService;
import com.rssreader.utils.Transformation;
import com.rssreader.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final String FEED_ID_PARAMETER = "feedId";

    public static final int FRAGMENT_ALL_NEWS = 0;
    public static final int FRAGMENT_LAST_NEWS = 1;
    public static final int FRAGMENT_STARRED_NEWS = 2;

    private CursorAdapter mSearchAdapter;
    private SearchView searchView;

    private long feedId;

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(mToolbar);
        mSearchAdapter = new SearchFeedAdaptor(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();

        if (getSupportFragmentManager().findFragmentById(R.id.container) == null) {
            showTabFragment(FRAGMENT_ALL_NEWS);
        }

        Intent intent = new Intent(this, RefreshService.class);
        intent.setAction(RefreshService.ACTION_REFRESH_ALL);
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        menu.findItem(R.id.action_search).collapseActionView();
        setupSearchView(searchView);
        searchView.setQuery("", false);
        searchView.setIconified(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.action_filter_all:
                fragment = FeedListFragment.createInstance(getString(R.string.action_filter_all), false);
                Utils.showFragment(this, fragment, R.id.container);
                break;
            case R.id.action_filter_last:
                break;
            case R.id.action_filter_starred:
                fragment = FeedListFragment.createInstance(getString(R.string.action_filter_starred), true);
                Utils.showFragment(this, fragment, R.id.container);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSearchView(final SearchView searchView) {
        searchView.setOnQueryTextListener(this);
        searchView.setOnSuggestionListener(this);
        searchView.setImeOptions(searchView.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        searchView.setSuggestionsAdapter(mSearchAdapter);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if (!queryTextFocused) {
                    invalidateOptionsMenu();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putLong(FEED_ID_PARAMETER, feedId);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        feedId = savedInstanceState.getLong(FEED_ID_PARAMETER);
    }

    public void showTabFragment(int index) {
        Fragment fragment = null;
        switch (index) {
            case FRAGMENT_ALL_NEWS:
                fragment = FeedListFragment.createInstance(getString(R.string.action_filter_all), false);
                break;
            case FRAGMENT_LAST_NEWS:
            case FRAGMENT_STARRED_NEWS:
                fragment = FeedListFragment.createInstance(getString(R.string.action_filter_starred), true);
                break;
        }
        Utils.showFragment(this, fragment, R.id.container);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        loadFeedData(query);
        return true;
    }

    @Override
    public boolean onSuggestionSelect(int position) {
        return true;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
        invalidateOptionsMenu();

        ContentValues values = new ContentValues();
        values.put(FeedData.FeedNews.NAME, cursor.getString(4));
        values.put(FeedData.FeedNews.URL, cursor.getString(1));
        values.put(FeedData.FeedNews.IMAGE_URL, cursor.getString(2));

        ContentResolver cr = getContentResolver();
        cr.insert(FeedData.FeedNews.CONTENT_URI, values);

        Intent intent = new Intent(this, RefreshService.class);
        intent.setAction(RefreshService.ACTION_REFRESH_ALL);
        startService(intent);
        return true;
    }

    private void loadFeedData(String query) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(FeedlyService.FEEDLY_ENDPOINT)
                .build();
        FeedlyService searchService = restAdapter.create(FeedlyService.class);
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("query", query);

        searchService.searchFeeds(queryMap, new Callback<FeedlyResponse>() {
            @Override
            public void success(FeedlyResponse feedlyResponse, Response response) {
                MatrixCursor matrixCursor = Transformation.convertToCursor(feedlyResponse.getResults(), SearchFeedAdaptor.columns);
                mSearchAdapter.changeCursor(matrixCursor);
            }

            @Override
            public void failure(RetrofitError error) {}
        });
    }
}
