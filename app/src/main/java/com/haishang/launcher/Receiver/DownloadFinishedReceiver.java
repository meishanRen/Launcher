package com.haishang.launcher.Receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.haishang.launcher.Constant;

public class DownloadFinishedReceiver extends BroadcastReceiver {

    private static final String TAG = DownloadFinishedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            long launcherId = sp.getLong(Constant.UPDATE_APK_ID_PREFS, -1L);
            long xiaoYId = sp.getLong(Constant.XIAO_Y_APK_ID_PREFS, -1L);
            if (downloadId == launcherId || downloadId == xiaoYId) {
                installApk(context, downloadId);
            }
        }
    }

    private void installApk(Context context, long downloadApkId) {
        DownloadManager dManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri downloadFileUri = dManager.getUriForDownloadedFile(downloadApkId);
        if (downloadFileUri != null) {
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, false);
            intent.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Log.e(TAG, "can not get download apk");
        }
    }
}