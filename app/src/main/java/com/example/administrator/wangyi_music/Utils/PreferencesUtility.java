package com.example.administrator.wangyi_music.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Administrator on 2017/6/8.
 */
//TODO
public final class PreferencesUtility {
    public static final String ARTIST_SORT_ORDER = "artist_sort_order";
    public static final String ARTIST_SONG_SORT_ORDER = "artist_song_sort_order";
    public static final String ARTIST_ALBUM_SORT_ORDER = "artist_album_sort_order";
    public static final String ALBUM_SORT_ORDER = "album_sort_order";
    public static final String ALBUM_SONG_SORT_ORDER = "album_song_sort_order";
    public static final String SONG_SORT_ORDER = "song_sort_order";
    private static final String NOW_PLAYING_SELECTOR = "now_paying_selector";
    private static final String TOGGLE_ANIMATIONS = "toggle_animations";
    private static final String TOGGLE_SYSTEM_ANIMATIONS = "toggle_system_animations";
    private static final String TOGGLE_ARTIST_GRID = "toggle_artist_grid";
    private static final String TOGGLE_ALBUM_GRID = "toggle_album_grid";
    private static final String TOGGLE_HEADPHONE_PAUSE = "toggle_headphone_pause";
    private static final String THEME_PREFERNCE = "theme_preference";
    private static final String START_PAGE_INDEX = "start_page_index";
    private static final String START_PAGE_PREFERENCE_LASTOPENED = "start_page_preference_latopened";
    private static final String NOW_PLAYNG_THEME_VALUE = "now_playing_theme_value";
    private static final String FAVRIATE_MUSIC_PLAYLIST = "favirate_music_playlist";
    private static final String DOWNMUSIC_BIT = "down_music_bit";
    private static final String CURRENT_DATE = "currentdate";

    private static PreferencesUtility sInstance;

    private static SharedPreferences mPreferences;

    public PreferencesUtility(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferencesUtility getsInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PreferencesUtility(context.getApplicationContext());
        }
        return sInstance;
    }

    public long lastExit(){
        return mPreferences.getLong("last_err_exit",0);
    }

    public void setExitTime(){
        SharedPreferences.Editor editor=mPreferences.edit();
        editor.putLong("last_err_exit",System.currentTimeMillis());
        editor.commit();
    }

    public boolean isCurrentDayFirst(String str){
        return mPreferences.getString(CURRENT_DATE,"").equals(str);
    }


    public void  setCurrentDate(String s){
        SharedPreferences.Editor editor=mPreferences.edit();
        editor.putString(CURRENT_DATE,s);
        editor.apply();
    }

    public void setPlayLink(long id,String link){
        SharedPreferences.Editor editor=mPreferences.edit();
        editor.putString(id+"",link);
        editor.apply();
    }

    public String getPlayLink(long id){
        return mPreferences.getString(id+"",null);
    }

    public void   setFavriateMusicPlaylist(boolean b){
        SharedPreferences.Editor editor=mPreferences.edit();
        editor.putBoolean(FAVRIATE_MUSIC_PLAYLIST,b);
        editor.apply();
    }

    public boolean getFavriateMusicPlaylist(){
        return mPreferences.getBoolean(FAVRIATE_MUSIC_PLAYLIST,false);
    }
}
