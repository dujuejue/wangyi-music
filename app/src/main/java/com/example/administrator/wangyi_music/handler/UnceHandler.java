package com.example.administrator.wangyi_music.handler;

import com.example.administrator.wangyi_music.MainApplication;

/**
 * Created by Administrator on 2017/6/8.
 */
//TODO
public class UnceHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    public static final String TAG="CatchExcept";
    MainApplication application;

    public UnceHandler(MainApplication application) {
        //获取系统默认的UncaughtException处理器
        mDefaultHandler=Thread.getDefaultUncaughtExceptionHandler();
        this.application = application;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {

    }
}
