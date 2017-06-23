package com.example.administrator.wangyi_music.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/6/13.
 */

public class RecentStore {
    private static final int MAX_ITEMS_IN_DB = 100;

    private static RecentStore sInstance = null;

    private MusicDB mMusicDatabase = null;

    public RecentStore(Context context) {
        mMusicDatabase = MusicDB.getsInstance(context);
    }

    public static synchronized  final RecentStore getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RecentStore(context.getApplicationContext());
        }
        return sInstance;
    }


    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + RecentStoreColumns.NAME + " ("
                + RecentStoreColumns.ID + " LONG NOT NULL," + RecentStoreColumns.TIMEPLAYED
                + " LONG NOT NULL);");
    }

    public synchronized void addSongId(final long songId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            Cursor mostRecentItem = null;
            try {
                mostRecentItem = queryRecentIds("1");
                if (mostRecentItem != null && mostRecentItem.moveToFirst())
                    if (songId == mostRecentItem.getLong(0)) return;
            } finally {
                if (mostRecentItem != null) {
                    mostRecentItem.close();
                    mostRecentItem = null;
                }
            }

            final ContentValues values = new ContentValues(2);
            values.put(RecentStoreColumns.ID, songId);
            values.put(RecentStoreColumns.TIMEPLAYED, System.currentTimeMillis());
            database.insert(RecentStoreColumns.NAME, null, values);

            Cursor oldest = null;
            try {
                oldest = database.query(RecentStoreColumns.NAME,
                        new String[]{RecentStoreColumns.TIMEPLAYED}, null, null, null, null,
                        RecentStoreColumns.TIMEPLAYED + " ASC");
                if (oldest != null && oldest.getCount() > MAX_ITEMS_IN_DB) {
                    oldest.moveToPosition(oldest.getCount() - MAX_ITEMS_IN_DB);
                    long timeRecord = oldest.getLong(0);
                    database.delete(RecentStoreColumns.NAME, RecentStoreColumns.TIMEPLAYED + "<?", new String[]{String.valueOf(timeRecord)});

                }
            } finally {
                if (oldest != null) {
                    oldest.close();
                    oldest = null;
                }
            }
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    public synchronized void removeItem(final long songId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            database.delete(RecentStoreColumns.NAME, RecentStoreColumns.ID + "=?", new String[]{String.valueOf(songId)});
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized void deleteAll() {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            database.delete(RecentStoreColumns.NAME, null, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized Cursor queryRecentIds(final String limit) {
        final SQLiteDatabase database = mMusicDatabase.getReadableDatabase();
        return database.query(RecentStoreColumns.NAME, new String[]{RecentStoreColumns.ID}, null, null, null, null,
                RecentStoreColumns.TIMEPLAYED + " DESC", limit);
    }

    public synchronized long[] getRecentId() {
        Cursor cursor = queryRecentIds(null);
        if (cursor == null) {
            long[] c = new long[0];
            return c;
        }
        ArrayList<Long> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                list.add(cursor.getLong(cursor.getColumnIndex(RecentStoreColumns.ID)));
            }
        }
        cursor.close();
        long[] l = new long[list.size()];
        for (int i=0;i<list.size();i++){
            l[i]=list.get(i);
        }
        return l;
    }


    public interface RecentStoreColumns {
        /* Table name */
        String NAME = "recenthistory";

        /* Album IDs column */
        String ID = "songid";

        /* Time played column */
        String TIMEPLAYED = "timeplayed";
    }
}
