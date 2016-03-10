package com.rssreader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
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

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final String TAB_INDEX_PARAMETER = "tabIndex";
    private static final String FEED_ID_PARAMETER = "feedId";

    public static final String[] TAB_NAMES = {"News", "Favorites"};
    public static final int FRAGMENT_ALL_NEWS = 0;
    public static final int FRAGMENT_FAVORITE = 1;

    private CursorAdapter mSearchAdapter;
    private SearchView searchView;
    private TabLayout tabLayout;

    private int selectedTabIndex;
    private long feedId;

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(mToolbar);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        for (String tabName : TAB_NAMES) {
            tabLayout.addTab(tabLayout.newTab().setText(tabName));
        }
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setOnTabSelectedListener(this);
        mSearchAdapter = new SearchFeedAdaptor(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            showTabFragment();
        }

        Intent intent = new Intent(this, RefreshService.class);
        intent.setAction(RefreshService.ACTION_REFRESH_ALL);
        startService(intent);
    }

    private void updateToolBar() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment topFragment = fm.findFragmentById(R.id.container);
        boolean showToolBar = topFragment instanceof FeedListFragment;
        if (tabLayout != null) {
            tabLayout.getTabAt(selectedTabIndex).select();
            tabLayout.setVisibility(showToolBar ? View.VISIBLE : View.GONE);
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        menu.findItem(R.id.menu_search).collapseActionView();
        setupSearchView(searchView);
        searchView.setQuery("", false);
        searchView.setIconified(true);
        return super.onCreateOptionsMenu(menu);
    }

    private void setupSearchView(final SearchView searchView) {
        searchView.setOnQueryTextListener(this);
        searchView.setOnSuggestionListener(this);
        searchView.setImeOptions(searchView.getImeOptions() | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        searchView.setSuggestionsAdapter(mSearchAdapter);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if(!queryTextFocused) {
                    invalidateOptionsMenu();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putLong(FEED_ID_PARAMETER, feedId);
        savedInstanceState.putInt(TAB_INDEX_PARAMETER, selectedTabIndex);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        feedId = savedInstanceState.getLong(FEED_ID_PARAMETER);
        selectedTabIndex = savedInstanceState.getInt(TAB_INDEX_PARAMETER);
        updateToolBar();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        updateToolBar();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateToolBar();
    }

    public void showTabFragment() {
        Fragment fragment = null;
        switch (selectedTabIndex) {
            case FRAGMENT_FAVORITE:
                fragment = FeedListFragment.createInstance(true);
                break;
            case FRAGMENT_ALL_NEWS:
                fragment = FeedListFragment.createInstance(false);
                break;
        }
        Utils.showFragment(this, fragment, R.id.container);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        selectedTabIndex = tab.getPosition();
        showTabFragment();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(TabLayout.Tab tab) {}

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
        String feedName = cursor.getString(4);
        invalidateOptionsMenu();

        ContentValues values = new ContentValues();
        values.put(FeedData.FeedNews.NAME, feedName);
        values.put(FeedData.FeedNews.URL, cursor.getString(1));

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
        HashMap<String, String> queryMap = new HashMap<>();
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
