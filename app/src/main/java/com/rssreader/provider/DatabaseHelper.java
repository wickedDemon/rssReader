package com.rssreader.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "feedapp";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(createTable(FeedData.FeedNews.TABLE_NAME, FeedData.FeedNews.COLUMNS));
        database.execSQL(createTable(FeedData.FeedEntries.TABLE_NAME, FeedData.FeedEntries.COLUMNS));
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int arg1, int arg2) {
        deleteTable(FeedData.FeedNews.TABLE_NAME);
        deleteTable(FeedData.FeedEntries.TABLE_NAME);
        onCreate(database);
    }

    private String createTable(String tableName, String[][] collumns){
        StringBuilder stringBuilder = new StringBuilder("CREATE TABLE ");
        stringBuilder.append(tableName);
        stringBuilder.append(" (");
        for (int i = 0; i < collumns.length; i++) {
            if (i > 0)
                stringBuilder.append(",");
            stringBuilder.append(collumns[i][0]).append(" ").append(collumns[i][1]);
        }

        return stringBuilder.append(");").toString();
    }

    private String deleteTable(String tableName){
        return new StringBuilder("DROP TABLE IF EXISTS" + tableName).toString();
    }

}
