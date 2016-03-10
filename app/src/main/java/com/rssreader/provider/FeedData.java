package com.rssreader.provider;

import android.net.Uri;

public class FeedData {

    public static final String CONTENT = "content://";
    public static final String AUTHORITY = "com.example.rssreader.provider.FeedDataContentProvider";
    public static final String CONTENT_AUTHORITY = CONTENT + AUTHORITY;

    static final String TYPE_PRIMARY_KEY = "INTEGER PRIMARY KEY AUTOINCREMENT";
    static final String TYPE_EXTERNAL_ID = "INTEGER(7)";
    static final String TYPE_TEXT = "TEXT";
    static final String TYPE_TEXT_UNIQUE = "TEXT UNIQUE";
    static final String TYPE_DATE_TIME = "DATETIME";
    static final String TYPE_INT = "INT";
    static final String TYPE_BOOLEAN = "INTEGER(1)";
    static final String TYPE_BLOB = "BLOB";

    public static class FeedNews {
        public static final String TABLE_NAME = "feednews";

        public static final String ID = "_id";
        public static final String NAME = "name";
        public static final String IS_FAVORITE = "isfavorite";
        public static final String URL = "url";
        public static final String IMAGE_URL = "image_url";

        public static final String[][] COLUMNS = {{ID, TYPE_PRIMARY_KEY}, {NAME, TYPE_TEXT},
            {IS_FAVORITE, TYPE_BOOLEAN}, {URL, TYPE_TEXT_UNIQUE}, {IMAGE_URL, TYPE_TEXT}};

        public static final Uri CONTENT_URI = Uri.parse(CONTENT_AUTHORITY + "/feednews");

        public static final Uri CONTENT_URI(String feedId) {
            return Uri.parse(CONTENT_AUTHORITY + "/feednews/" + feedId);
        }
    }

    public static class FeedEntries {
        public static final String TABLE_NAME = "feedentries";

        public static final String ID = "_id";
        public static final String ID_REF = "id_ref";
        public static final String NAME = "name";
        public static final String URL = "url";
        public static final String DESCRIPTION = "description";
        public static final String IMAGE_URL = "image_url";
        public static final String PUB_DATE = "date";

        public static final String[][] COLUMNS = {{ID, TYPE_PRIMARY_KEY}, {ID_REF, TYPE_EXTERNAL_ID},
            {NAME, TYPE_TEXT}, {DESCRIPTION, TYPE_TEXT}, {URL, TYPE_TEXT_UNIQUE}, {IMAGE_URL, TYPE_BLOB},
            {PUB_DATE, TYPE_TEXT}, {FOREIGN_KEY_CONSTRAINTS(), ""}};

        public static final String FOREIGN_KEY_CONSTRAINTS() {
            return new StringBuilder().append("FOREIGN KEY").append('(').append(ID_REF).append(')')
                    .append("REFERENCES ").append(FeedNews.TABLE_NAME).append('(').append(FeedNews.ID)
                    .append(')').append(" ON DELETE CASCADE").toString();
        }

        public static final Uri CONTENT_URI = Uri.parse(CONTENT_AUTHORITY + "/feedentries");

        public static final Uri CONTENT_URI(String entryId) {
            return Uri.parse(CONTENT_AUTHORITY + "/feedentries/" + entryId);
        }
    }
}

