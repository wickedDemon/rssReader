package com.rssreader.utils;

import android.database.MatrixCursor;

import com.rssreader.model.FeedlyResult;

import java.util.List;

public class Transformation {

    public static MatrixCursor convertToCursor(List<FeedlyResult> feedlyResults, String[] columns) {
        MatrixCursor cursor = new MatrixCursor(columns);
        int i = 0;
        for (FeedlyResult feedlyResult : feedlyResults) {
            String[] temp = new String[5];
            i = i + 1;
            temp[0] = Integer.toString(i);

            String feedUrl = feedlyResult.getFeedId();
            if (feedUrl != null) {
                int index = feedUrl.indexOf("feed/");
                if (index != -1) {
                    feedUrl = feedUrl.substring(5);
                }
            }
            temp[1] = feedUrl;
            temp[2] = feedlyResult.getIconUrl();
            temp[3] = feedlyResult.getSubscribers();
            temp[4] = feedlyResult.getTitle();
            cursor.addRow(temp);
        }
        return cursor;
    }
}
