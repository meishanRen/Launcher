package com.haishang.launcher.util;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.haishang.launcher.Constant;
import com.haishang.launcher.LauncherApplication;
import com.haishang.launcher.model.bean.UpdateInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by chenrun on 2016/4/26 0026.
 */
public class AndroidUtil {
    private static String TAG = "AndroidUtil";

    public static void startActivity(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "未安装该应用", Toast.LENGTH_SHORT).show();
        }
    }

    public static int getAppVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return Integer.MAX_VALUE;
    }

    /**
     * 下载Apk, 并设置Apk地址,
     * 默认位置: /storage/sdcard0/Download
     *
     * @param context    上下文
     * @param updateInfo 更新信息
     * @param infoName   通知名称
     * @param storeApk   存储的Apk
     */
    public static void downloadApk(
            Context context, UpdateInfo updateInfo,
            String infoName, String storeApk
    ) {
        String appUrl = "";
        DownloadManager.Request request;
        try {
            request = new DownloadManager.Request(Uri.parse(appUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        request.setTitle(infoName);
        request.setDescription("");
   /*     if (Build.VERSION.SDK_INT & gtBuild.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }*/
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, storeApk);
        Context appContext = context.getApplicationContext();
        DownloadManager manager = (DownloadManager)
                appContext.getSystemService(Context.DOWNLOAD_SERVICE);

        // 存储下载Key
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(appContext);
        sp.edit().putLong(Constant.UPDATE_APK_ID_PREFS, manager.enqueue(request)).apply();
    }

    /**
     * @param url         源url
     * @param storeIdName 存到sp中的id
     * @param storeName   存储的名字
     */
    public static long downloadApk(String url, String storeIdName, String storeName) {
        DownloadManager.Request request;
        try {
            request = new DownloadManager.Request(Uri.parse(url));
        } catch (Exception e) {
            throw new RuntimeException("" + e.toString());
        }
        request.setTitle("title");
        request.setDescription("description");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, storeName);
        Context appContext = LauncherApplication.getContext();
        DownloadManager manager = (DownloadManager)
                appContext.getSystemService(Context.DOWNLOAD_SERVICE);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(appContext);
        long downloadId = manager.enqueue(request);
        sp.edit().putLong(storeIdName, downloadId).apply();
        return downloadId;
    }

    public static void cancelDownload(long id) {
        DownloadManager manager = (DownloadManager)
                LauncherApplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        manager.remove(id);
    }

    /**
     * install slient
     *
     * @param context
     * @param filePath
     * @return 0 means normal, 1 means file not exist, 2 means other exception error
     */
    public static int installSlient(Context context, String filePath) {
        File file = new File(filePath);
        if (filePath == null || filePath.length() == 0 || (file = new File(filePath)) == null || file.length() <= 0
                || !file.exists() || !file.isFile()) {
            return 1;
        }
        String[] args = { "pm", "install", "-r", filePath };
        ProcessBuilder processBuilder = new ProcessBuilder(args);

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;

            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }

            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = 2;
        } catch (Exception e) {
            e.printStackTrace();
            result = 2;
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }
        // TODO should add memory is not enough here
        if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
            result = 0;
            Log.d("installSlient", "successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
        } else {
            result = 2;
            Log.d("installSlient", " ErrorMsg:" + errorMsg);
        }
        return result;
    }


}
