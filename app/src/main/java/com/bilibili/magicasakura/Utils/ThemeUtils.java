package com.bilibili.magicasakura.Utils;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;

/**
 * Created by Administrator on 2017/6/8.
 */

public class ThemeUtils {
    public interface switchColor{
        @ColorInt
        int replaceColorByID(Context context, @ColorRes int colorID);

        @ColorInt
        int replaceColor(Context context,@ColorInt int color);
    }

    public static switchColor mSwitchColor;

    public static void  setSwitchColor(switchColor switchColor){
        mSwitchColor=switchColor;
    }
}
