package com.rssreader.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rssreader.adapter.BaseFeedAdapter;
import com.rssreader.R;
import com.rssreader.listener.ItemClickListener;
import com.rssreader.provider.FeedData;
import com.rssreader.utils.Utils;
import com.rssreader.view.ListDivider;

public class FeedListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = FeedListFragment.class.getSimpleName();
    private static final String TAB_FAVORITE_MODE = "isFavorite";
    private static final int LOADER_ID = 0;

    private boolean favoriteMode;
    private BaseFeedAdapter adapter;

    public static FeedListFragment createInstance(boolean favoriteMode) {
        FeedListFragment fragment = new FeedListFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(TAB_FAVORITE_MODE, favoriteMode);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            favoriteMode = arguments.getBoolean(TAB_FAVORITE_MODE);
        }

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recyclerview, container, false);
        setupRecyclerView(recyclerView);
        return recyclerView;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        final RecyclerView.ItemDecoration itemDecoration = new ListDivider(getActivity());
        recyclerView.addItemDecoration(itemDecoration);

        adapter = new BaseFeedAdapter(getActivity(), favoriteMode);
        adapter.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, long id) {
                Fragment fragment = FeedEntriesFragment.createInstance(id);
                Utils.showFragment(getActivity(), fragment, R.id.container, TAG);
            }

            @Override
            public void onLongClick(View view, int position, long id) {}
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String whereClause = favoriteMode ? FeedData.FeedNews.IS_FAVORITE + "=" + 1 : null;
        return new CursorLoader(getActivity(), FeedData.FeedNews.CONTENT_URI, null, whereClause, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }
}
