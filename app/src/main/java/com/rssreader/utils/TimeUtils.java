package com.rssreader.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    private static final String TAG = TimeUtils.class.getSimpleName();

    private static final DateFormat[] PUBDATE_DATE_FORMATS = {new SimpleDateFormat("d' 'MMM' 'yy' 'HH:mm:ss", Locale.US),
            new SimpleDateFormat("d' 'MMM' 'yy' 'HH:mm:ss' 'Z", Locale.US), new SimpleDateFormat("d' 'MMM' 'yy' 'HH:mm:ss' 'z", Locale.US)};

    private static final DateFormat[] UPDATE_DATE_FORMATS = {new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ssZ", Locale.US), new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSz", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd", Locale.US)};

    private Date parseUpdateDate(String dateStr, boolean tryAllFormat) {
        for (DateFormat format : UPDATE_DATE_FORMATS) {
            try {
                Date result = format.parse(dateStr);
                return (result.getTime() > mNow ? new Date(mNow) : result);
            } catch (ParseException e) {
                Log.e(TAG, "Wrong update format");
            }
        }

        if (tryAllFormat)
            return parsePubdateDate(dateStr, false);
        else
            return null;
    }

    private Date parsePubdateDate(String dateStr, boolean tryAllFormat) {
        for (DateFormat format : PUBDATE_DATE_FORMATS) {
            try {
                Date result = format.parse(dateStr);
                return (result.getTime() > mNow ? new Date(mNow) : result);
            } catch (ParseException e) {
                Log.e(TAG, "Wrong pubdate format");
            }
        }

        if (tryAllFormat)
            return parseUpdateDate(dateStr, false);
        else
            return null;
    }
}
