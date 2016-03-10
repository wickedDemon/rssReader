package com.rssreader.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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

import com.rssreader.adapter.FeedEntryAdapter;
import com.rssreader.R;
import com.rssreader.listener.ItemClickListener;
import com.rssreader.provider.FeedData;
import com.rssreader.view.ListDivider;

public class FeedEntriesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String FEED_ID_PARAMETER = "feedId";
    private static final String QUERY_LIMIT = "limit";
    private static final int LOADER_ID = 0;

    private long currentId;
    private FeedEntryAdapter adapter;

    public static FeedEntriesFragment createInstance(long id) {
        FeedEntriesFragment fragment = new FeedEntriesFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(FEED_ID_PARAMETER, id);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            currentId = arguments.getLong(FEED_ID_PARAMETER);
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

        adapter = new FeedEntryAdapter(getActivity());
        adapter.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, long id) {
                String where = FeedData.FeedNews.ID + "=" + id;
                ContentResolver cr = getActivity().getContentResolver();
                Cursor cursor = cr.query(FeedData.FeedEntries.CONTENT_URI, new String[]{FeedData.FeedEntries.URL}, where, null, null);

                if (cursor.moveToFirst()) {
                    String url = cursor.getString(0);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(View view, int position, long id) {}
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {
        return new CursorLoader(getActivity(),
                FeedData.FeedEntries.CONTENT_URI(Long.toString(currentId)).buildUpon()
                        .appendQueryParameter(QUERY_LIMIT , "0, 10")
                        .build(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
        //TODO: Add empty view if there is no content
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursor) {
        adapter.swapCursor(null);
    }
}
