package com.example.administrator.wangyi_music;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;

import com.bilibili.magicasakura.Utils.ThemeUtils;
import com.example.administrator.wangyi_music.Utils.IConstants;
import com.example.administrator.wangyi_music.Utils.PreferencesUtility;
import com.example.administrator.wangyi_music.Utils.ThemeHelper;
import com.example.administrator.wangyi_music.handler.UnceHandler;
import com.example.administrator.wangyi_music.permission.Nammu;
import com.example.administrator.wangyi_music.provider.PlaylistInfo;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.google.gson.Gson;

/**
 * Created by Administrator on 2017/6/8.
 */

public class MainApplication extends Application implements ThemeUtils.switchColor {
    public static Context context;

    private static int MAX_MEM = (int) (Runtime.getRuntime().maxMemory() / 4);

    private long favPlayList = IConstants.FAV_PLAYLIST;

    private static Gson gson;

    public static Gson gsonInstance() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    //初始化fresco的配置
    private ImagePipelineConfig getConfigureCashes(Context context) {
        final MemoryCacheParams bitmapCacheParams = new MemoryCacheParams(
                MAX_MEM,// 内存缓存中总图片的最大大小,以字节为单位。
                Integer.MAX_VALUE,// 内存缓存中图片的最大数量。
                MAX_MEM,// 内存缓存中准备清除但尚未被删除的总图片的最大大小,以字节为单位。
                Integer.MAX_VALUE,// 内存缓存中准备清除的总图片的最大数量。
                Integer.MAX_VALUE / 10// 内存缓存中单个图片的最大大小。
        );
        Supplier<MemoryCacheParams> memoryCacheParamsSupplier = new Supplier<MemoryCacheParams>() {
            @Override
            public MemoryCacheParams get() {
                return bitmapCacheParams;
            }
        };

        ImagePipelineConfig.Builder builder = ImagePipelineConfig.newBuilder(context)
                .setDownsampleEnabled(true)
                .setBitmapMemoryCacheParamsSupplier(memoryCacheParamsSupplier);

        DiskCacheConfig smallDiskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(context.getApplicationContext().getCacheDir())
                .build();

        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(Environment.getExternalStorageDirectory().getAbsoluteFile())
                .build();

        return builder.build();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        //清空内存缓存（包括Bitmap缓存和未解码图片的缓存）
        imagePipeline.clearMemoryCaches();
        //清空硬盘缓存，一般在设置界面供用户手动清理
        //imagePipeline.clearDiskCaches();

        //同时清理内存缓存和硬盘缓存
        //imagePipeline.clearCaches();
    }

    private void frescoInit() {
        Fresco.initialize(this, getConfigureCashes(this));
    }

    //捕获全局exception
    public void initCacheException() {
        //TODO
        UnceHandler catchExcept = new UnceHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(catchExcept);
    }

    @Override
    public void onCreate() {
        frescoInit();
        super.onCreate();
        context = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //TODO
            Nammu.init(this);
        }
        ThemeUtils.setSwitchColor(this);
        initCacheException();
        if (!PreferencesUtility.getsInstance(this).getFavriateMusicPlaylist()) {
            PlaylistInfo.getsInstance(context).addPlayList(favPlayList, getResources().getString(R.string.my_fav_playlist), 0,
                    "res:/" + R.mipmap.lay_protype_default, "local");
            PreferencesUtility.getsInstance(context).setFavriateMusicPlaylist(true);
        }
    }

    @Override
    public int replaceColorByID(Context context, @ColorRes int colorID) {
        if (ThemeHelper.isDefaultTheme(context)) {
            return context.getResources().getColor(colorID);
        }

        String theme = getTheme(context);

        if (theme != null) {
            colorID = getThemeColorId(context, colorID, theme);
        }
        return context.getResources().getColor(colorID);
    }


    @Override
    public int replaceColor(Context context, @ColorInt int originColor) {
        if (ThemeHelper.isDefaultTheme(context)) return originColor;

        return 0;
    }

    private String getTheme(Context context) {
        if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_STORM) {
            return "blue";
        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_HOPE) {
            return "purple";
        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_WOOD) {
            return "green";
        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_LIGHT) {
            return "green_light";
        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_THUNDER) {
            return "yellow";
        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_SAND) {
            return "orange";
        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_FIREY) {
            return "red";
        }
        return null;
    }

    @ColorRes
    private int getThemeColorId(Context context, int colorId, String theme) {
        switch (colorId) {
            case R.color.theme_color_primary:
                return context.getResources().getIdentifier(theme, "color", getPackageName());
            case R.color.theme_color_primary_dark:
                return context.getResources().getIdentifier(theme + "_dark", "color", getPackageName());
            case R.color.playbarProgressColor:
                return context.getResources().getIdentifier(theme + "_trans", "color", getPackageName());
        }
        return colorId;
    }

    @ColorRes
    private int getThemeColor(Context context, int color, String theme) {
        switch (color) {
            case 0xd20000:
                return context.getResources().getIdentifier(theme, "color", getPackageName());
        }
        return -1;
    }

}
