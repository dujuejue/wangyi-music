package com.example.administrator.wangyi_music.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.administrator.wangyi_music.info.PlayList;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/6/10.
 */

public class PlaylistInfo {
    private static PlaylistInfo sInstance = null;

    private MusicDB mMusicDB = null;

    public PlaylistInfo(Context context) {
        mMusicDB = MusicDB.getsInstance(context.getApplicationContext());
    }

    public static final synchronized PlaylistInfo getsInstance(Context context) {
        if (sInstance == null) sInstance = new PlaylistInfo(context);
        return sInstance;
    }

    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS" + PlaylistInfoColumns.NAME + "("
                + PlaylistInfoColumns.PLAYLIST_ID + " LONG NOT NULL,"
                + PlaylistInfoColumns.NAME + " CHAR NOT NULL,"
                + PlaylistInfoColumns.SONG_COUNT + " INT NOT NULL,"
                + PlaylistInfoColumns.ALBUM_ART + " CHAR,"
                + PlaylistInfoColumns.AUTHOR + " CHAR);");
    }

    public synchronized void addPlayList(long id, String name, int song_count, String albumArt, String author) {
        SQLiteDatabase database = mMusicDB.getWritableDatabase();

        database.beginTransaction();

        try {
            ContentValues values = new ContentValues(5);
            values.put(PlaylistInfoColumns.PLAYLIST_ID, id);
            values.put(PlaylistInfoColumns.PLAYLIST_NAME, name);
            values.put(PlaylistInfoColumns.SONG_COUNT, song_count);
            values.put(PlaylistInfoColumns.ALBUM_ART, albumArt);
            values.put(PlaylistInfoColumns.AUTHOR, author);

            database.insert(PlaylistInfoColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized void addPlayList(ArrayList<PlayList> playLists) {
        SQLiteDatabase database = mMusicDB.getWritableDatabase();

        database.beginTransaction();

        try {
            for (PlayList item : playLists) {
                ContentValues values = new ContentValues(5);
                values.put(PlaylistInfoColumns.PLAYLIST_ID, item.id);
                values.put(PlaylistInfoColumns.PLAYLIST_NAME, item.name);
                values.put(PlaylistInfoColumns.SONG_COUNT, item.songCount);
                values.put(PlaylistInfoColumns.ALBUM_ART, item.albumArt);
                values.put(PlaylistInfoColumns.AUTHOR, item.author);
                database.insert(PlaylistInfoColumns.NAME, null, values);
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized boolean hasPlaylist(long playlistId) {
        SQLiteDatabase database = mMusicDB.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = database.query(PlaylistInfoColumns.NAME, null, PlaylistInfoColumns.PLAYLIST_ID + "=?",
                    new String[]{String.valueOf(playlistId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                return true;
            }

            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public synchronized void updatePlayList(long playlistId, int oldcount) {
        ArrayList<PlayList> playLists = getPlayList();
        int count = 0;
        for (PlayList item : playLists) {
            if (item.id == playlistId) count = item.songCount;
        }
        count += oldcount;
        update(playlistId, count);
    }

    public synchronized void update(long playlistId, int count) {
        SQLiteDatabase database = mMusicDB.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(2);
            values.put(PlaylistInfoColumns.PLAYLIST_ID, playlistId);
            values.put(PlaylistInfoColumns.SONG_COUNT, count);
            database.update(PlaylistInfoColumns.NAME, values, PlaylistInfoColumns.PLAYLIST_ID + "=?", new String[]{String.valueOf(playlistId)});
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }


    public synchronized void update(long playlistId, int count, String album) {
        SQLiteDatabase database = mMusicDB.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(3);
            values.put(PlaylistInfoColumns.PLAYLIST_ID, playlistId);
            values.put(PlaylistInfoColumns.SONG_COUNT, count);
            values.put(PlaylistInfoColumns.ALBUM_ART, album);
            database.update(PlaylistInfoColumns.NAME, values, PlaylistInfoColumns.PLAYLIST_ID + "=?", new String[]{String.valueOf(playlistId)});
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    //删除本地文件时更新播放列表歌曲数量信息
    public void updatePlaylistMusicCount(long[] PlaylistId) {
        SQLiteDatabase database = mMusicDB.getWritableDatabase();
        StringBuilder builder = new StringBuilder();
        builder.append(PlaylistInfoColumns.PLAYLIST_ID + "IN (");
        for (int i = 0; i < PlaylistId.length; i++) {
            builder.append(PlaylistId[i]);
            if (i < PlaylistId.length - 1) builder.append(",");
        }
        builder.append(")");

        Cursor cursor = null;

        database.beginTransaction();

        try {
            cursor = mMusicDB.getReadableDatabase().query(PlaylistInfoColumns.NAME, null, builder.toString(),
                    null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int count = cursor.getInt(cursor.getColumnIndex(PlaylistInfoColumns.SONG_COUNT)) - 1;
                    long playListId = cursor.getLong(cursor.getColumnIndex(PlaylistInfoColumns.PLAYLIST_ID));
                    if (count == 0) {
                        database.delete(PlaylistInfoColumns.NAME, PlaylistInfoColumns.PLAYLIST_ID + "=" + playListId, null);
                    } else {
                        ContentValues values = new ContentValues(2);
                        values.put(PlaylistInfoColumns.PLAYLIST_ID, playListId);
                        values.put(PlaylistInfoColumns.SONG_COUNT, count);
                        database.update(PlaylistInfoColumns.NAME, values, PlaylistInfoColumns.PLAYLIST_ID + "=" + playListId, null);
                    }
                } while (cursor.moveToNext());
                database.setTransactionSuccessful();
            }
        } finally {
            database.endTransaction();
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public synchronized void deletePlaylist(long playlistId) {
        SQLiteDatabase database = mMusicDB.getWritableDatabase();
        database.delete(PlaylistInfoColumns.NAME, PlaylistInfoColumns.PLAYLIST_ID + "=?", new String[]{String.valueOf(playlistId)});
    }

    public synchronized void deletePlaylist(long[] playlistId) {
        SQLiteDatabase database = mMusicDB.getWritableDatabase();

        StringBuilder selection = new StringBuilder();
        selection.append(PlaylistInfoColumns.PLAYLIST_ID + "IN (");
        for (int i = 0; i < playlistId.length; i++) {
            selection.append(playlistId[i]);
            if (i < playlistId.length - 1) selection.append(",");
        }
        selection.append(")");

        database.beginTransaction();

        try {
            database.delete(PlaylistInfoColumns.NAME, selection.toString(), null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void deleteAll() {
        SQLiteDatabase database = mMusicDB.getWritableDatabase();
        database.delete(PlaylistInfoColumns.NAME, null, null);
    }

    public synchronized ArrayList<PlayList> getPlayList() {
        ArrayList<PlayList> result = new ArrayList<>();
        SQLiteDatabase database = mMusicDB.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = database.query(PlaylistInfoColumns.NAME, null, null, null, null, null, null);


            if (cursor != null && cursor.moveToFirst()) {
                result.ensureCapacity(cursor.getCount());

                do {
                    if (cursor.getString(4).equals("local"))
                        result.add(new PlayList(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4)));

                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }

    public synchronized ArrayList<PlayList> getNetPlaylist() {
        ArrayList<PlayList> result = new ArrayList<>();
        SQLiteDatabase database = mMusicDB.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = database.query(PlaylistInfoColumns.NAME, null, null, null, null, null, null);


            if (cursor != null && cursor.moveToFirst()) {
                result.ensureCapacity(cursor.getCount());

                do {
                    if (!cursor.getString(4).equals("local"))
                        result.add(new PlayList(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4)));

                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return result;
    }

    public interface PlaylistInfoColumns {
        /* Table name */
        String NAME = "playlist_info";

        /* Album IDs column */
        String PLAYLIST_ID = "playlist_id";

        /* Time played column */
        String PLAYLIST_NAME = "playlist_name";

        String SONG_COUNT = "count";

        String ALBUM_ART = "album_art";

        String AUTHOR = "author";
    }
}
