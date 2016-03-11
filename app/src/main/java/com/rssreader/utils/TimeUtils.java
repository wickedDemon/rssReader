package com.rssreader.utils;

import android.util.Log;

import org.joda.time.Duration;
import org.joda.time.DurationFieldType;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatterBuilder;

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

    private static final String[][] TIMEZONES_REPLACE = {
            {"MEST", "+0200"},
            {"EST", "-0500"},
            {"PST", "-0800"},
            {"ICT", "+0700"}};

    public static Date parseUpdateDate(String dateStr, boolean tryAllFormat) {
        if (dateStr == null) {
            Log.e(TAG, "Wrong string date format");
            return null;
        }

        long mNow = new Date().getTime();
        for (DateFormat format : UPDATE_DATE_FORMATS) {
            try {
                Date result = format.parse(improveDateString(dateStr));
                return (result.getTime() > mNow ? new Date(mNow) : result);
            } catch (ParseException e) {
                Log.e(TAG, "Wrong update format");
            }
        }
        return tryAllFormat ? parsePubdateDate(dateStr, false) : null;
    }

    public static Date parsePubdateDate(String dateStr, boolean tryAllFormat) {
        if (dateStr == null) {
            Log.e(TAG, "Wrong string date format");
            return null;
        }

        long mNow = new Date().getTime();
        for (DateFormat format : PUBDATE_DATE_FORMATS) {
            try {
                Date result = format.parse(improveDateString(dateStr));
                return (result.getTime() > mNow ? new Date(mNow) : result);
            } catch (ParseException e) {
                Log.e(TAG, "Wrong pubdate format");
            }
        }
        return tryAllFormat ? parseUpdateDate(dateStr, false) : null;
    }

    private static String improveDateString(String date) {
        int coma = date.indexOf(", ");
        if (coma != -1) {
            date = date.substring(coma + 2);
        }
        date = date.replaceAll("([0-9])T([0-9])", "$1 $2").replaceAll("Z$", "").replaceAll(" ", " ").trim();
        for (String[] timezoneReplace : TIMEZONES_REPLACE) {
            date = date.replace(timezoneReplace[0], timezoneReplace[1]);
        }
        return date;
    }

    private static String getPeriodTime(Period period) {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder()
                .appendDays().appendSuffix(" day ", " days ")
                .appendHours().appendSuffix(" hour ", " hours ")
                .appendMinutes().appendSuffix(" min ", " mins ")
                .appendSeconds().appendSuffix(" less than sec ");
        return builder.toFormatter().print(period).trim();
    }

    private static DurationFieldType[] getTimeUnitsType(long duration) {
        Duration durationWrap = new Duration(duration);
        if (durationWrap.getStandardDays() > 0) {
            return new DurationFieldType[] {
                    DurationFieldType.days(),
                    DurationFieldType.hours()};
        } else if (durationWrap.getStandardHours() > 0) {
            return new DurationFieldType[] {
                    DurationFieldType.hours(),
                    DurationFieldType.minutes()};
        } else if (durationWrap.getStandardMinutes()  > 0) {
            return new DurationFieldType[] {
                    DurationFieldType.minutes()};
        } else {
            return new DurationFieldType[] {
                    DurationFieldType.seconds()};
        }
    }

    public static String getHumanReadableTimeDuration(long duration) {
        PeriodType type = PeriodType.forFields(getTimeUnitsType(duration));
        return getPeriodTime(new Period(duration, type).normalizedStandard());
    }
}
