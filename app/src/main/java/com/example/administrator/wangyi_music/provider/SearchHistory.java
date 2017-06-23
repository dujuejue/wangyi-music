package com.example.administrator.wangyi_music.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/6/19.
 */

public class SearchHistory {

    private static final int MAX_ITEMS_IN_DB = 25;

    private static SearchHistory sInstance = null;

    private MusicDB mMusicDatabase = null;

    public SearchHistory(Context context) {
        mMusicDatabase = MusicDB.getsInstance(context.getApplicationContext());
    }

    public static synchronized final SearchHistory getsInstance(Context context) {
        if (sInstance == null) sInstance = new SearchHistory(context.getApplicationContext());
        return sInstance;
    }

    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS " + SearchHistoryColumns.NAME + "("
                + SearchHistoryColumns.SEARCHSTRING + " TEXT NOT NULL,"
                + SearchHistoryColumns.TIMESEARCHED + " LONG NOT NULL);");
    }


    public void addSearchString(String searchString) {
        if (searchString == null) return;


        String trimedString = searchString.trim();

        if (trimedString.isEmpty()) return;

        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();

        database.beginTransaction();

        try {
            database.delete(SearchHistoryColumns.NAME,
                    SearchHistoryColumns.SEARCHSTRING + "=? COLLATE NOCASE",
                    new String[]{trimedString});

            ContentValues values = new ContentValues(2);

            values.put(SearchHistoryColumns.SEARCHSTRING, trimedString);
            values.put(SearchHistoryColumns.TIMESEARCHED, System.currentTimeMillis());

            database.insert(SearchHistoryColumns.NAME, null, values);

            Cursor oldest = null;

            try {
                oldest = database.query(SearchHistoryColumns.NAME,
                        new String[]{SearchHistoryColumns.TIMESEARCHED}, null, null, null, null,
                        SearchHistoryColumns.TIMESEARCHED + "ASC");

                if (oldest != null && oldest.getCount() > MAX_ITEMS_IN_DB) {
                    oldest.moveToPosition(oldest.getCount() - MAX_ITEMS_IN_DB);
                    long timeOfRecordToKeep = oldest.getInt(0);

                    database.delete(SearchHistoryColumns.NAME,
                            SearchHistoryColumns.TIMESEARCHED + "<?",
                            new String[]{String.valueOf(timeOfRecordToKeep)});
                }
            } finally {
                if (oldest != null) {
                    oldest.close();
                    oldest = null;
                }
                database.setTransactionSuccessful();
            }
        } finally {
            database.endTransaction();
        }
    }

    public void deleteRecentSearches(String name) {
        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(SearchHistoryColumns.NAME, SearchHistoryColumns.SEARCHSTRING + "=?", new String[]{name});
    }

    public Cursor queryRecentSearches(final String limit) {
        SQLiteDatabase database = mMusicDatabase.getReadableDatabase();
        return database.query(SearchHistoryColumns.NAME, new String[]{SearchHistoryColumns.SEARCHSTRING},
                null, null, null, null, SearchHistoryColumns.TIMESEARCHED + "DESC", limit);
    }

    public ArrayList<String> getRecentSearches() {
        Cursor search = queryRecentSearches(String.valueOf(MAX_ITEMS_IN_DB));

        ArrayList<String> result = new ArrayList<>(MAX_ITEMS_IN_DB);

        try {
            if (search != null && search.moveToFirst()) {
                int index = search.getColumnIndex(SearchHistoryColumns.SEARCHSTRING);

                do {
                    result.add(search.getString(index));
                } while (search.moveToNext());
            }
        } finally {
            if (search!=null){
                search.close();
                search=null;
            }
        }

        return result;
    }

    public interface SearchHistoryColumns {
        /* Table name */
        String NAME = "searchhistory";

        /* What was searched */
        String SEARCHSTRING = "searchstring";

        /* Time of search */
        String TIMESEARCHED = "timesearched";
    }
}
