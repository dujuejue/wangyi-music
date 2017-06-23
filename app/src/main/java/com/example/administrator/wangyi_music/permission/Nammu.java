package com.example.administrator.wangyi_music.permission;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2017/6/8.
 */
//TODO
public class Nammu {
    private static final String TAG = Nammu.class.getSimpleName();
    private static final String KEY_PREV_PERMISSIONS = "previous_permissions";
    private static final String KEY_IGNORED_PERMISSIONS = "ignored_permissions";
    private static Context context;
    private static SharedPreferences sharedPreferences;


    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences("pl.tajchert.runtimepermissionhelper", Context.MODE_PRIVATE);
        Nammu.context = context;
    }
}
