package com.rssreader.service;

import java.io.IOException;

import org.xml.sax.SAXException;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.rssreader.MainApplication;
import com.rssreader.parser.RSSAtomParser;
import com.rssreader.provider.FeedData;
import com.rssreader.utils.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class RefreshService extends IntentService {

    private static final String TAG = RefreshService.class.getSimpleName();
    private static final Context context = MainApplication.getContext();

    public static final String ACTION_REFRESH_ALL = "com.example.rssreader.REFRESH_ALL";
    public static final String ACTION_REFRESH_ONLY_BY_ID = "com.example.rssreader.REFRESH_ONLY_BY_ID";

    private static final String name = "downloaderService";

    public RefreshService() {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!Utils.isNetworkConnected(context)) {
            Log.d(TAG, "No internet connection!");
            return;
        }

        if (intent == null)
            return;

        int feedId;
        String feedUrl;
        String action = intent.getAction();

        if (ACTION_REFRESH_ALL.equals(action)) {
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(FeedData.FeedNews.CONTENT_URI, new String[]{FeedData.FeedNews.ID, FeedData.FeedNews.URL}, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    feedId = cursor.getInt(0);
                    feedUrl = cursor.getString(1);
                    refreshEntries(feedId, feedUrl);
                } while (cursor.moveToNext());
            }
        } else if (ACTION_REFRESH_ONLY_BY_ID.equals(action)) {
            feedId = intent.getIntExtra("feedId", 0);
            String where = FeedData.FeedNews.ID + "=" + feedId;

            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(FeedData.FeedNews.CONTENT_URI, new String[]{FeedData.FeedNews.ID, FeedData.FeedNews.URL}, where, null, null);

            if (cursor.moveToFirst()) {
                feedId = cursor.getInt(0);
                feedUrl = cursor.getString(1);
                refreshEntries(feedId, feedUrl);
            }
        }
    }

    private void refreshEntries(int feedId, String feedUrl) {
        OkHttpClient client = new OkHttpClient();

        try {
            Request request = new Request.Builder()
                    .url(feedUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                RSSAtomParser.RSSParser.setFeedId(feedId);

                RSSAtomParser.parseRSSFile(response.body().string());
            }
        } catch (IOException e) {
            Log.e(TAG, "IO error has occurred " + e.getMessage());
        } catch (SAXException e) {
            Log.e(TAG, "Parsing error has occurred " + e.getMessage());
        }
    }
}
