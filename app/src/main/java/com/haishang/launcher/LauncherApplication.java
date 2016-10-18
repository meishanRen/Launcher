package com.haishang.launcher;

import android.app.Application;
import android.content.Context;

/**
 * Created by chenrun on 2016/4/13 0013.
 */
public class LauncherApplication extends Application {
    static Context context;






    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
