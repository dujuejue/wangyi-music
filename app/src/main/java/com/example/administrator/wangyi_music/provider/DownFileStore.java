package com.example.administrator.wangyi_music.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.administrator.wangyi_music.downmusic.DownloadDBEntity;
import com.example.administrator.wangyi_music.downmusic.DownloadStatus;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/6/21.
 */

public class DownFileStore {
    private static DownFileStore sInstance = null;
    private MusicDB mMusicDB = null;

    public DownFileStore(Context context) {
        mMusicDB = MusicDB.getsInstance(context.getApplicationContext());
    }

    public static DownFileStore getsInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DownFileStore(context);
        }
        return sInstance;
    }

    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS" + DownFileStoreColumns.NAME + "("
                + DownFileStoreColumns.ID + " TEXT NOT NULL PRIMARY KEY," + DownFileStoreColumns.TOOL_SIZE + " INT NOT NULL,"
                + DownFileStoreColumns.FILE_LENGTH + " INT NOT NULL, " + DownFileStoreColumns.URL + " TEXT NOT NULL,"
                + DownFileStoreColumns.DIR + " TEXT NOT NULL," + DownFileStoreColumns.FILE_NAME + " TEXT NOT NULL,"
                + DownFileStoreColumns.ARTIST_NAME + " TEXT NOT NULL,"
                + DownFileStoreColumns.DOWNSTATUS + " INT NOT NULL);");
    }

    public synchronized void insert(DownloadDBEntity entity) {
        final SQLiteDatabase database = mMusicDB.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(8);
            values.put(DownFileStoreColumns.ID, entity.getDownloadId());
            values.put(DownFileStoreColumns.TOOL_SIZE, entity.getTotalSize());
            values.put(DownFileStoreColumns.FILE_LENGTH, entity.getCompletedSize());
            values.put(DownFileStoreColumns.URL, entity.getUrl());
            values.put(DownFileStoreColumns.DIR, entity.getSaveDirPath());
            values.put(DownFileStoreColumns.FILE_NAME, entity.getFileName());
            values.put(DownFileStoreColumns.ARTIST_NAME, entity.getArtist());
            values.put(DownFileStoreColumns.DOWNSTATUS, entity.getDownloadStatus());
            database.replace(DownFileStoreColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized void update(DownloadDBEntity entity) {
        SQLiteDatabase database = mMusicDB.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(6);
            values.put(DownFileStoreColumns.TOOL_SIZE, entity.getTotalSize());
            values.put(DownFileStoreColumns.FILE_LENGTH, entity.getCompletedSize());
            values.put(DownFileStoreColumns.URL, entity.getUrl());
            values.put(DownFileStoreColumns.DIR, entity.getSaveDirPath());
            values.put(DownFileStoreColumns.FILE_NAME, entity.getFileName());
            values.put(DownFileStoreColumns.DOWNSTATUS, entity.getDownloadStatus());
            database.update(DownFileStoreColumns.NAME, values, DownFileStoreColumns.ID + "=" + entity.getDownloadId(), null);
        } finally {
            database.endTransaction();
        }
    }

    public void deleteTask(String id) {
        SQLiteDatabase database = mMusicDB.getWritableDatabase();
        database.delete(DownFileStoreColumns.NAME, DownFileStoreColumns.ID + "=" + id, null);
    }

    public void deleteTask(String[] id) {
        SQLiteDatabase database = mMusicDB.getWritableDatabase();
        database.delete(DownFileStoreColumns.NAME, DownFileStoreColumns.ID + "=?", id);

    }

    public void deleteDowningTask() {
        ArrayList<String> result = new ArrayList<>();
        SQLiteDatabase database = mMusicDB.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = mMusicDB.getReadableDatabase().query(DownFileStoreColumns.NAME, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                result.ensureCapacity(cursor.getCount());
                do {
                    if (cursor.getInt(7) == DownloadStatus.DOWNLOAD_STATUS_COMPLETED)
                        result.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            StringBuilder selection = new StringBuilder();
            selection.append(DownFileStoreColumns.ID + "IN (");
            for (int i = 0; i < result.size(); i++) {
                selection.append(result.get(i));
                if (i < result.size() - 1) selection.append(",");
            }
            selection.append(")");
            database.delete(DownFileStoreColumns.NAME, selection.toString(), null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public synchronized DownloadDBEntity getDownLoadedList(String Id) {
        Cursor cursor = null;
        DownloadDBEntity entity = null;
        try {
            cursor = mMusicDB.getReadableDatabase().query(DownFileStoreColumns.NAME, null, DownFileStoreColumns.ID + "=" + Id, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    entity = new DownloadDBEntity(cursor.getString(0), cursor.getLong(1), cursor.getLong(2),
                            cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7));
                } while (cursor.moveToNext());
                return entity;
            } else return null;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public synchronized ArrayList<DownloadDBEntity> getDownLoadedListAllDowning() {
        ArrayList<DownloadDBEntity> entities = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mMusicDB.getReadableDatabase().query(DownFileStoreColumns.NAME, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                entities.ensureCapacity(cursor.getCount());
                do {
                    if (cursor.getInt(7) != DownloadStatus.DOWNLOAD_STATUS_COMPLETED) {
                        DownloadDBEntity entity = new DownloadDBEntity(cursor.getString(0), cursor.getLong(1), cursor.getLong(2),
                                cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7));
                        entities.add(entity);
                    }
                } while (cursor.moveToNext());
                return entities;
            } else return null;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public synchronized String[] getDownLoadedListAllDowningIds() {
        ArrayList<String> strings = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mMusicDB.getReadableDatabase().query(DownFileStoreColumns.NAME, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(7) != DownloadStatus.DOWNLOAD_STATUS_COMPLETED) {
                        strings.add(cursor.getString(0));
                    }
                } while (cursor.moveToNext());
                String[] result = new String[strings.size()];
                for (int i = 0; i < strings.size(); i++) {
                    result[i] = strings.get(i);
                }
                return result;
            } else return null;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public synchronized ArrayList<DownloadDBEntity> getDownLoadedListAll() {
        ArrayList<DownloadDBEntity> entities = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mMusicDB.getReadableDatabase().query(DownFileStoreColumns.NAME, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                entities.ensureCapacity(cursor.getCount());
                do {

                    DownloadDBEntity entity = new DownloadDBEntity(cursor.getString(0), cursor.getLong(1), cursor.getLong(2),
                            cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7));
                    entities.add(entity);

                } while (cursor.moveToNext());
            }
            return entities;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public interface DownFileStoreColumns {
        /* Table name */
        String NAME = "downfile_info";

        /* Album IDs column */
        String ID = "id";

        /* Time played column */
        String TOOL_SIZE = "totalsize";

        String FILE_LENGTH = "complete_length";

        String URL = "url";

        String DIR = "dir";
        String FILE_NAME = "file_name";
        String ARTIST_NAME = "artist";
        String DOWNSTATUS = "notification_type";
    }
}
