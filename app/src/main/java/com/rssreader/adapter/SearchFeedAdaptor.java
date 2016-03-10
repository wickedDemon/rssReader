package com.rssreader.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rssreader.R;
import com.rssreader.utils.ImageUtils;

public class SearchFeedAdaptor extends SimpleCursorAdapter {

    private static final String TAG = SearchFeedAdaptor.class.getSimpleName();
    public static String[] columns = new String[]{"_id", "FEED_URL", "FEED_ICON", "FEED_SUBSCRIBERS", "FEED_TITLE"};

    private Context context;

    public SearchFeedAdaptor(Context context) {
        super(context, R.layout.search_result_item, null, columns, null, -1);
        this.context = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view.findViewById(R.id.feedIcon);
        TextView textView = (TextView) view.findViewById(R.id.feedTitle);
        TextView subscribersView = (TextView) view.findViewById(R.id.subscriberCount);
        ImageUtils.loadImageToVeiw(context, imageView, cursor.getString(2), R.drawable.ic_no_image, 15, true);
        textView.setText(cursor.getString(4));
        subscribersView.setText(cursor.getString(3) + " followers");
    }
}
