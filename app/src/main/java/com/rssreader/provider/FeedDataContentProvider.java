package com.rssreader.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class FeedDataContentProvider extends ContentProvider{

    public static final String QUERY_PARAMETER_LIMIT = "limit";

    public static final int URI_LIST_FEED = 0;
    public static final int URI_LIST_FEED_ID = 1;
    public static final int URL_FEED_ENTRIES = 2;
    public static final int URL_FEED_ENTRIES_ID = 3;

    public static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(FeedData.AUTHORITY, "feednews", URI_LIST_FEED);
        URI_MATCHER.addURI(FeedData.AUTHORITY, "feednews/#", URI_LIST_FEED_ID);
        URI_MATCHER.addURI(FeedData.AUTHORITY, "feedentries", URL_FEED_ENTRIES);
        URI_MATCHER.addURI(FeedData.AUTHORITY, "feedentries/#", URL_FEED_ENTRIES_ID);
    }

    private DatabaseHelper mDatabaseHelper;

    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        int matchResult = URI_MATCHER.match(uri);
        int oldId;
        String where;

        switch (matchResult) {
        case URI_LIST_FEED:
            oldId = database.delete(FeedData.FeedNews.TABLE_NAME, whereClause, whereArgs);
            break;
        case URI_LIST_FEED_ID:
            long feedId = Long.parseLong(uri.getLastPathSegment());
            where = FeedData.FeedNews.ID + "=" + feedId;
            if (whereClause != null && !whereClause.isEmpty())
                where += " AND " + whereClause;
            oldId = database.delete(FeedData.FeedNews.TABLE_NAME, where, whereArgs);
            break;
        case URL_FEED_ENTRIES:
            oldId = database.delete(FeedData.FeedEntries.TABLE_NAME, whereClause, whereArgs);
            break;
        case URL_FEED_ENTRIES_ID:
            long entryId = Long.parseLong(uri.getLastPathSegment());
            where = FeedData.FeedEntries.ID_REF + "=" + entryId;//!!!
            if (whereClause != null && !whereClause.isEmpty())
                where += " AND " + whereClause;
            oldId = database.delete(FeedData.FeedEntries.TABLE_NAME, where, whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unsupported uri: " + uri);
        }

        if (oldId > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return oldId;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        int matchResult = URI_MATCHER.match(uri);
        long newId;

        switch (matchResult) {
        case URI_LIST_FEED:
            newId = database.insert(FeedData.FeedNews.TABLE_NAME, null, values);
            break;
        case URL_FEED_ENTRIES:
            newId = database.insert(FeedData.FeedEntries.TABLE_NAME, null, values);
            break;
        default:
            throw new IllegalArgumentException("Unsupported uri: " + uri);
        }

        if (newId > -1) {
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, newId);
        }

        return null;
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String limit = uri.getQueryParameter(QUERY_PARAMETER_LIMIT);
        int matchResult = URI_MATCHER.match(uri);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String pointId;

        switch (matchResult) {
        case URI_LIST_FEED:
            queryBuilder.setTables(FeedData.FeedNews.TABLE_NAME);
            break;
        case URI_LIST_FEED_ID:
            queryBuilder.setTables(FeedData.FeedNews.TABLE_NAME);
            pointId = uri.getLastPathSegment();
            queryBuilder.appendWhere(new StringBuilder(FeedData.FeedNews.ID).append('=').append(pointId));
            break;
        case URL_FEED_ENTRIES:
            queryBuilder.setTables(FeedData.FeedEntries.TABLE_NAME);
            break;
        case URL_FEED_ENTRIES_ID:
            queryBuilder.setTables(FeedData.FeedEntries.TABLE_NAME);
            pointId = uri.getLastPathSegment();
            queryBuilder.appendWhere(new StringBuilder(FeedData.FeedEntries.ID_REF).append('=').append(pointId));
            break;
        default:
            throw new IllegalArgumentException("Unsupported uri: " + uri);
        }

        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();

        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder, limit);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        int matchResult = URI_MATCHER.match(uri);
        int newId;
        long feedId;

        StringBuilder where = new StringBuilder();

        switch (matchResult) {
        case URI_LIST_FEED:
            newId = database.update(FeedData.FeedNews.TABLE_NAME, values, selection, selectionArgs);
            break;
        case URI_LIST_FEED_ID:
            feedId = Long.parseLong(uri.getLastPathSegment());
            where.append(FeedData.FeedNews.ID).append('=').append(feedId);
            if (selection != null && !selection.isEmpty())
                where.append(" AND ").append(selection);
            newId = database.update(FeedData.FeedNews.TABLE_NAME, values, where.toString(), selectionArgs);
            break;
        case URL_FEED_ENTRIES:
            newId = database.update(FeedData.FeedEntries.TABLE_NAME, values, selection, selectionArgs);
            break;
        case URL_FEED_ENTRIES_ID:
            feedId = Long.parseLong(uri.getLastPathSegment());
            where.append(FeedData.FeedEntries.ID).append('=').append(feedId);
            if (selection != null && !selection.isEmpty())
                where.append(" AND ").append(selection);
            newId = database.update(FeedData.FeedEntries.TABLE_NAME, values, where.toString(), selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unsupported uri: " + uri);
        }

        if (newId > 0) {
            this.getContext().getContentResolver().notifyChange(uri, null);
        }
        return newId;
    }

}
