package com.example.administrator.wangyi_music.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.administrator.wangyi_music.service.MusicTrack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Administrator on 2017/6/10.
 */

public class MusicPlaybackState {
    private static MusicPlaybackState sInstance = null;
    private MusicDB mMusicDatabase = null;

    public MusicPlaybackState(Context context) {
        mMusicDatabase = MusicDB.getsInstance(context);
    }

    public static final synchronized MusicPlaybackState getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MusicPlaybackState(context.getApplicationContext());
        }
        return sInstance;
    }

    public void oncreate(SQLiteDatabase db) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(PlaybackQueueColumns.NAME);
        builder.append("(");


        builder.append(PlaybackQueueColumns.TRACK_ID);
        builder.append(" LONG NOT NULL,");

        builder.append(PlaybackQueueColumns.SOURCE_POSITION);
        builder.append(" INT NOT NULL);");

        db.execSQL(builder.toString());

        builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(PlaybackHistoryColumns.NAME);
        builder.append("(");

        builder.append(PlaybackHistoryColumns.POSITION);
        builder.append(" INT NOT NULL);");

        db.execSQL(builder.toString());
    }

    public synchronized void Insert(MusicTrack musicTrack) {
        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(2);
            values.put(PlaybackQueueColumns.TRACK_ID, musicTrack.mId);
            values.put(PlaybackQueueColumns.SOURCE_POSITION, musicTrack.mSourcePosition);
            database.insert(PlaybackQueueColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized void Delete(long id) {
        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            database.delete(PlaybackQueueColumns.NAME, PlaybackQueueColumns.TRACK_ID + "+" + id, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized void clearQueue() {
        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        try {
            database.beginTransaction();
            database.delete(PlaybackQueueColumns.NAME, null, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized void saveState(ArrayList<MusicTrack> queue, LinkedList<Integer> history) {
        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            database.delete(PlaybackQueueColumns.NAME, null, null);
            database.delete(PlaybackHistoryColumns.NAME, null, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        final int NUM_PROCESS = 20;
        int position = 0;
        while (position < queue.size()) {
            database.beginTransaction();
            try {
                for (int i = position; i < queue.size() && i < position + NUM_PROCESS; i++) {
                    MusicTrack track = queue.get(i);
                    ContentValues values = new ContentValues(2);
                    values.put(PlaybackQueueColumns.TRACK_ID, track.mId);
                    values.put(PlaybackQueueColumns.SOURCE_POSITION, track.mSourcePosition);
                    database.insert(PlaybackQueueColumns.NAME, null, values);
                }
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
                position += NUM_PROCESS;
            }
        }

        if (history != null) {
            Iterator<Integer> iterator = history.iterator();
            while (iterator.hasNext()) {
                database.beginTransaction();
                try {
                    for (int i = 0; iterator.hasNext() && i < NUM_PROCESS; i++) {
                        ContentValues values = new ContentValues(1);
                        values.put(PlaybackHistoryColumns.POSITION, iterator.next());
                        database.insert(PlaybackHistoryColumns.NAME, null, values);
                    }
                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }
            }
        }
    }

    public ArrayList<MusicTrack> getQueue() {
        ArrayList<MusicTrack> result = new ArrayList<>();
        Cursor cursor = null;
        SQLiteDatabase database = mMusicDatabase.getReadableDatabase();
        try {
            cursor = database.query(PlaybackQueueColumns.NAME, null, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                result.ensureCapacity(cursor.getCount());
                do {
                    result.add(new MusicTrack(cursor.getLong(0), cursor.getInt(1)));

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

    public LinkedList<Integer>  getHistory(int playListSize){
        LinkedList<Integer> result=new LinkedList<>();
        Cursor cursor=null;
        SQLiteDatabase database=mMusicDatabase.getReadableDatabase();
        try {
            cursor=database.query(PlaybackHistoryColumns.NAME,null,null,null,null,null,null);
            if (cursor!=null&&cursor.moveToFirst()){
                do {
                   int pos=cursor.getInt(0);
                    if (pos>=0&&pos<playListSize) result.add(pos);
                }while (cursor.moveToNext());
            }
            return result;
        }finally {
            if (cursor!=null){
                cursor.close();
                cursor=null;
            }
        }
    }

    public class PlaybackQueueColumns {

        public static final String NAME = "playbacklist";
        public static final String TRACK_ID = "trackid";
        public static final String SOURCE_POSITION = "sourceposition";
    }

    public class PlaybackHistoryColumns {

        public static final String NAME = "playbackhistory";

        public static final String POSITION = "position";
    }
}
