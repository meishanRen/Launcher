package com.haishang.launcher.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.haishang.launcher.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;

import okhttp3.Call;

public class DownloadActivity extends Activity {
    public final String TAG = getClass().getSimpleName();
    ProgressBar progressBar;
    TextView progressText;
    public static final String ID = "id";
    public static final String REMOTE_URL = "url";
    public static final String TMP_APK = "new_download_apk.apk";
    RequestCall call;
    MyHandler handler = new MyHandler();

    public static void startThisActivity(Activity context, long id) {
        Intent intent = new Intent(context, DownloadActivity.class);
        Class<?> cl = DownloadActivity.class;
        intent.putExtra(ID, id);
        context.startActivity(intent);
    }

    public static void startThisActivity(Activity context, String url) {
        Intent intent = new Intent(context, DownloadActivity.class);
        intent.putExtra(REMOTE_URL, url);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressText = (TextView) findViewById(R.id.tv_progress);
        long downloadId = getIntent().getLongExtra(ID, -1);
        String url = getIntent().getStringExtra(REMOTE_URL);

        //    DownloadObserver downloadObserver = new DownloadObserver(handler, this, downloadId);
        //    getContentResolver().registerContentObserver(Uri.parse("content://downloads/"), true, downloadObserver);

//取消下载
        // Toast.makeText(MainActivity.this, "后台下载中", Toast.LENGTH_SHORT).show();


        call = OkHttpUtils.get().url(url).build();
        call.execute(new MyFileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(), TMP_APK));
    }

    class MyFileCallBack extends FileCallBack {
        public MyFileCallBack(String destFileDir, String destFileName) {
            super(destFileDir, destFileName);
        }

        @Override
        public void onError(Call call, Exception e, int id) {
            Toast.makeText(DownloadActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        public void onResponse(File response, int id) {

        }

        @Override
        public void inProgress(float progressPercent, long total, int id) {
            int progress = (int) (100 * progressPercent);
            Log.d(TAG, "download progress" + progress);
            progressBar.setProgress(progress);
            progressText.setText("正在下载安装包" + progress + "%" + ",请稍等!");
            if (100 == progress) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + TMP_APK;
                Uri downloadFileUri = Uri.parse("file://" + apkPath);
                if (downloadFileUri != null) {
                    //调用安装
                    intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, false);
                    intent.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    DownloadActivity.this.startActivity(intent);
                    finish();

/*
                    //静默安装应该不需要root权限 只需要system权限
                    http://www.trinea.cn/android/android-install-silent/
                    //测试静默安装
                    finish();
                    AndroidUtil.installSlient(getApplication(), apkPath);*/

                }
            }
        }
    }


    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //这里调用setprocess为什么就不行呢... // TODO: 2016/6/29 0029
         /*   final int what = msg.what;
            progressBar.setMax(100);
            progressBar.setProgress(what);

            progressBar.setProgress(0);


            progressBar.setMax(100);
            progressBar.setProgress(what);


            Log.d(TAG, "msg  use what to progress" + what);*/
          /*  progressBar.
                    progressBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(what);
                }
            }, 200);*/
        }

    }

    @Override
    protected void onDestroy() {
        call.cancel();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //不让其返回
    }
}
