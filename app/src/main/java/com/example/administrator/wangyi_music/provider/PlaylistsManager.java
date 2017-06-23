package com.example.administrator.wangyi_music.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.administrator.wangyi_music.Utils.IConstants;
import com.example.administrator.wangyi_music.Utils.MusicUtils;
import com.example.administrator.wangyi_music.info.MusicInfo;
import com.example.administrator.wangyi_music.service.MusicTrack;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/6/20.
 */

public class PlaylistsManager {
    private static PlaylistsManager sInstance = null;

    private MusicDB mMusicDatabase = null;
    private long favPlaylistId = IConstants.FAV_PLAYLIST;

    public PlaylistsManager(Context context) {
        mMusicDatabase = MusicDB.getsInstance(context.getApplicationContext());
    }

    public static PlaylistsManager getsInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PlaylistsManager(context.getApplicationContext());
        }
        return sInstance;
    }

    //建立播放列表表设置播放列表id和歌曲id为复合主键
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PlaylistsColumns.NAME + " ("
                + PlaylistsColumns.PLAYLIST_ID + " LONG NOT NULL," + PlaylistsColumns.TRACK_ID + " LONG NOT NULL,"
                + PlaylistsColumns.TRACK_NAME + " CHAR NOT NULL," + PlaylistsColumns.ALBUM_ID + " LONG,"
                + PlaylistsColumns.ALBUM_NAME + " CHAR," + PlaylistsColumns.ALBUM_ART + " CHAR,"
                + PlaylistsColumns.ARTIST_ID + " LONG," + PlaylistsColumns.ARTIST_NAME + " CHAR,"
                + PlaylistsColumns.IS_LOCAL + " BOOLEAN ," + PlaylistsColumns.PATH + " CHAR,"
                + PlaylistsColumns.TRACK_ORDER + " LONG NOT NULL, primary key ( " + PlaylistsColumns.PLAYLIST_ID
                + ", " + PlaylistsColumns.TRACK_ID + "));");
    }

    public ArrayList<MusicTrack> getPlayList(long playllistid) {
        ArrayList<MusicTrack> result = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null, PlaylistsColumns.PLAYLIST_ID + "=" + String.valueOf(playllistid),
                    null, null, null, PlaylistsColumns.TRACK_ORDER + "ASC");
            if (cursor != null && cursor.moveToFirst()) {
                result.ensureCapacity(cursor.getCount());
                do {
                    result.add(new MusicTrack(cursor.getLong(1), cursor.getInt(0)));
                } while (cursor.moveToNext());
            }
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public long[] getPlaylistIds(long playlistId) {
        long[] result = null;
        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null, PlaylistsColumns.PLAYLIST_ID + "=" + playlistId, null, null,
                    null, PlaylistsColumns.TRACK_ORDER + "ASC");
            if (cursor != null && cursor.moveToFirst()) {
                result = new long[cursor.getCount()];
                int i = 0;
                do {
                    result[i] = cursor.getLong(1);
                    i++;
                } while (cursor.moveToNext());
            }
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public ArrayList<MusicInfo> getMusicInfos(final long playlistid) {
        ArrayList<MusicInfo> result = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null, PlaylistsColumns.PLAYLIST_ID + "=" + playlistid,
                    null, null, null, PlaylistsColumns.TRACK_ORDER + "ASC");
            if (cursor != null && cursor.moveToFirst()) {
                result.ensureCapacity(cursor.getCount());
                do {
                    MusicInfo info = new MusicInfo();
                    info.songId = cursor.getLong(1);
                    info.musicName = cursor.getString(2);
                    info.albumId = cursor.getInt(3);
                    info.albumName = cursor.getString(4);
                    info.albumData = cursor.getString(5);
                    info.artistId = cursor.getLong(6);
                    info.artist = cursor.getString(7);
                    //SQLite does not have a separate Boolean storage class. Instead, Boolean values are stored as integers 0 (false) and 1 (true).
                    info.islocal = cursor.getInt(8) > 0;
                    result.add(info);
                } while (cursor.moveToNext());
            }
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public synchronized void insert(Context context, long playlistId, long id, int order) {
        ArrayList<MusicTrack> m = getPlayList(playlistId);
        for (MusicTrack item : m) {
            if (item.mId == id) return;
        }
        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(3);
            values.put(PlaylistsColumns.PLAYLIST_ID, playlistId);
            values.put(PlaylistsColumns.TRACK_ID, id);
            values.put(PlaylistsColumns.TRACK_ORDER, m.size());
            database.insert(PlaylistsColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        PlaylistInfo playlistInfo = PlaylistInfo.getsInstance(context);
        playlistInfo.update(playlistId, m.size());
    }

    public synchronized void insertMusic(Context context, long playListId, MusicInfo info) {
        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(11);
            values.put(PlaylistsColumns.PLAYLIST_ID, playListId);
            values.put(PlaylistsColumns.TRACK_ID, info.songId);
            values.put(PlaylistsColumns.TRACK_ORDER, getPlayList(playListId).size());
            values.put(PlaylistsColumns.TRACK_NAME, info.musicName);
            values.put(PlaylistsColumns.ALBUM_ID, info.albumId);
            values.put(PlaylistsColumns.ALBUM_NAME, info.albumName);
            values.put(PlaylistsColumns.ALBUM_ART, info.albumData);
            values.put(PlaylistsColumns.ARTIST_NAME, info.artist);
            values.put(PlaylistsColumns.ARTIST_ID, info.artistId);
            values.put(PlaylistsColumns.PATH, info.data);
            values.put(PlaylistsColumns.IS_LOCAL, info.islocal);

            database.insertWithOnConflict(PlaylistsColumns.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        PlaylistInfo playlistInfo = PlaylistInfo.getsInstance(context);
        String albumart = info.albumData;

        if (info.islocal) {

            if (albumart.equals(MusicUtils.getAlbumData(context, info.songId))) {
                playlistInfo.update(playListId, getPlayList(playListId).size(), info.albumData);
            } else playlistInfo.update(playListId, getPlayList(playListId).size());

        } else if (!albumart.isEmpty()) {
            playlistInfo.update(playListId, getPlayList(playListId).size(), info.albumData);
        } else {
            playlistInfo.update(playListId, getPlayList(playListId).size());
        }

    }

    public synchronized void insertLists(Context context, long playlistId, ArrayList<MusicInfo> musicInfos) {

        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            for (MusicInfo item : musicInfos) {
                ContentValues values = new ContentValues(11);
                values.put(PlaylistsColumns.PLAYLIST_ID, playlistId);
                values.put(PlaylistsColumns.TRACK_ID, item.songId);
                values.put(PlaylistsColumns.TRACK_ORDER, getPlayList(playlistId).size());
                values.put(PlaylistsColumns.TRACK_NAME, item.musicName);
                values.put(PlaylistsColumns.ALBUM_ID, item.albumId);
                values.put(PlaylistsColumns.ALBUM_NAME, item.albumName);
                values.put(PlaylistsColumns.ALBUM_ART, item.albumData);
                values.put(PlaylistsColumns.ARTIST_NAME, item.artist);
                values.put(PlaylistsColumns.ARTIST_ID, item.artistId);
                values.put(PlaylistsColumns.PATH, item.data);
                values.put(PlaylistsColumns.IS_LOCAL, item.islocal);

                database.insertWithOnConflict(PlaylistsColumns.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        PlaylistInfo playlistInfo = PlaylistInfo.getsInstance(context);

        String albumart = null;

        for (MusicInfo item : musicInfos) {
            albumart = item.albumData;
            if (item.islocal) {
                String art = MusicUtils.getAlbumData(context, item.songId);
                if (art != null) {
                    break;
                } else albumart = null;
            } else if (!albumart.isEmpty()) {
                break;
            }
        }
        if (albumart != null) {
            playlistInfo.update(playlistId, getPlayList(playlistId).size(), albumart);
        } else {
            playlistInfo.update(playlistId, getPlayList(playlistId).size());
        }
    }

    public synchronized void update(long playlistid, long id, int order) {
        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(1);
            values.put(PlaylistsColumns.TRACK_ORDER, order);
            database.update(PlaylistsColumns.NAME, values, PlaylistsColumns.PLAYLIST_ID + "=?" + "AND"
                    + PlaylistsColumns.TRACK_ID + "=?", new String[]{playlistid + "", id + ""});
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized void update(long playlistId, long[] id, int[] order) {
        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            for (int i = 0; i < id.length; i++) {
                ContentValues values = new ContentValues(1);
                values.put(PlaylistsColumns.TRACK_ORDER, order[i]);
                database.update(PlaylistsColumns.NAME, values, PlaylistsColumns.PLAYLIST_ID + "=?" + "AND"
                        + PlaylistsColumns.TRACK_ID + "=?", new String[]{playlistId + "", id[i] + ""});
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void removeItem(Context context, final long playlistId, long songId) {
        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistsColumns.NAME, PlaylistsColumns.PLAYLIST_ID + "=?" + "AND" + PlaylistsColumns.TRACK_ID + "=?",
                new String[]{playlistId + "", songId + ""});
        PlaylistInfo playlistInfo = PlaylistInfo.getsInstance(context);
        playlistInfo.update(playlistId, getPlayList(playlistId).size());
    }

    public void delete(long playlistId) {
        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistsColumns.NAME, PlaylistsColumns.PLAYLIST_ID + "=" + playlistId, null);
    }

    //删除播放列表中的记录的音乐 ，删除本地文件时调用
    public synchronized void deleteMusic(Context context, final long musicId) {
        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null,
                    PlaylistsColumns.TRACK_ID + "=" + musicId, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                long[] deletedPlaylistIds = new long[cursor.getCount()];
                int i = 0;
                do {
                    deletedPlaylistIds[i] = cursor.getLong(0);
                    i++;
                } while (cursor.moveToNext());
                PlaylistInfo.getsInstance(context).updatePlaylistMusicCount(deletedPlaylistIds);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        database.delete(PlaylistsColumns.NAME, PlaylistsColumns.TRACK_ID + "=" + musicId, null);
    }

    public interface PlaylistsColumns {
        /* Table name */
        String NAME = "playlists";

        /* Album IDs column */
        String PLAYLIST_ID = "playlist_id";

        /* Time played column */
        String TRACK_ID = "track_id";

        String TRACK_ORDER = "track_order";

        String TRACK_NAME = "track_name";

        String ARTIST_NAME = "artist_name";

        String ARTIST_ID = "artist_id";

        String ALBUM_NAME = "album_name";

        String ALBUM_ID = "album_id";

        String IS_LOCAL = "is_local";

        String PATH = "path";

        String ALBUM_ART = "album_art";
    }
}
