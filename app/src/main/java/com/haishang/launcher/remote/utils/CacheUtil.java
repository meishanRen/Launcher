package com.haishang.launcher.remote.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SP 工具类
  */
public class CacheUtil {
    private static SharedPreferences mSp;

    private static SharedPreferences getSharedPreferences(Context context) {
        if (mSp == null) {
            mSp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        return mSp;
    }

    public static void putBoolean(Context context, String key, boolean value) {
        getSharedPreferences(context).edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return getSharedPreferences(context).getBoolean(key, defValue);
    }

    public static void putString(Context context, String key, String value) {
        getSharedPreferences(context).edit().putString(key, value).commit();
    }

    public static String getString(Context context, String key, String defValue) {
        return getSharedPreferences(context).getString(key, defValue);
    }

    public static void remove(Context context, String key) {
        getSharedPreferences(context).edit().remove(key).commit();
    }

    public static void putInt(Context context, String key, int value) {
        getSharedPreferences(context).edit().putInt(key, value).commit();
    }

    public static int getInt(Context context, String key, int defValue) {
        return getSharedPreferences(context).getInt(key, defValue);
    }
}
