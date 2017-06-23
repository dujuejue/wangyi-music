package com.example.administrator.wangyi_music.provider;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/6/10.
 */

public class MusicDB extends SQLiteOpenHelper {
    public static final String DATABASENAME = "musicdb.db";
    private static final int VERSION = 4;
    private static MusicDB sInstance = null;

    private final Context mContext;

    public MusicDB(Context context) {
        super(context, DATABASENAME, null, VERSION);
        mContext = context;
    }

    public static synchronized MusicDB getsInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MusicDB(context.getApplicationContext());
        }
        return sInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        MusicPlaybackState.getInstance(mContext).oncreate(db);
        RecentStore.getInstance(mContext).onCreate(db);
        SongPlayCount.getsInstance(mContext).onCreate(db);
        SearchHistory.getsInstance(mContext).onCreate(db);
        PlaylistInfo.getsInstance(mContext).onCreate(db);
        PlaylistsManager.getsInstance(mContext).onCreate(db);
        DownFileStore.getsInstance(mContext).onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
