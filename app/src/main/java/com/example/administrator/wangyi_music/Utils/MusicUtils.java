package com.example.administrator.wangyi_music.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

/**
 * Created by Administrator on 2017/6/21.
 */

public class MusicUtils {
    public static final int FILTER_SIZE = 1 * 1024 * 1024;// 1MB
    public static final int FILTER_DURATION = 1 * 60 * 1000;// 1分钟


    private static String[] proj_music = new String[]{
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE};
    private static String[] proj_album = new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART,
            MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.NUMBER_OF_SONGS, MediaStore.Audio.Albums.ARTIST};
    private static String[] proj_artist = new String[]{
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
            MediaStore.Audio.Artists._ID};
    private static String[] proj_folder = new String[]{MediaStore.Files.FileColumns.DATA};

    public static String getAlbumData(Context context, long musicId) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj_music, "_id=" + String.valueOf(musicId), null, null);

        if (cursor == null) return null;

        long albumId = -1;

        if (cursor.moveToNext())
            albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

        if (albumId != -1)
            cursor = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, proj_album,
                    MediaStore.Audio.Albums._ID + "=" + String.valueOf(albumId), null, null);

        if (cursor == null) return null;

        String data = "";

        if (cursor.moveToNext())
            data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));//音乐专辑封面

        cursor.close();

        return data;
    }

}
