package com.haishang.launcher.remote.yobbom.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.haishang.launcher.R;
import com.haishang.launcher.remote.utils.CacheUtil;
import com.haishang.launcher.remote.utils.MyUtils;
import com.haishang.launcher.remote.utils.StreamUtil;
import com.haishang.launcher.remote.utils.ToastShow;
import com.haishang.launcher.remote.utils.Tools;
import com.haishang.launcher.remote.yobbom.data.MainData;
import com.haishang.launcher.remote.yobbom.service.MainService;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

//*************************************

public class MainActivity extends Activity {
    private TextView _textView;
    private TextView _textViewDeviceName;
    private TextView _textViewVersion;
    private ImageView _imgViewStatus;
    private Button button1, button2;
    private LinearLayout ly1, ly2;
    private Intent _intent;
    private MainService mService;
    /**
     * 需要更新, 弹对话框
     */
    protected static final int SHOW_UPDATE_DIALOG = 1;
    protected static final int URL_ERROR = 2;
    protected static final int NET_ERROR = 3;
    protected static final int JSON_ERROR = 4;
    protected static final int SERVER_ERROR = 5;
    protected static final int ENTER_HOME = 6;
    private static final int GO_GUIDE = 7;
    private static final int REQUEST_CODE_UPDATE = 100;
    protected int mServerVersionCode;
    private int mServerVersion;
    private String mDownloadUrl;
    private String mDesc;
    private int mVersionNow; // member
    private static final long SPLASH_DELAY_MILLIS = 2000;
    boolean isUpdate = false;
    private Context mContext;
    private byte[] keyback;
    private Handler mHander = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_UPDATE_DIALOG:
                    showUpdateDialog();
                    break;
                case URL_ERROR:
                    Toast.makeText(getApplicationContext(), "网址错误",
                            Toast.LENGTH_SHORT).show();

                    break;
                case NET_ERROR:
                    Toast.makeText(getApplicationContext(), "网络错误",
                            Toast.LENGTH_SHORT).show();

                    break;
                case JSON_ERROR:
                    Toast.makeText(getApplicationContext(), "数据解析错误",
                            Toast.LENGTH_SHORT).show();

                    break;
                case SERVER_ERROR:
                    Toast.makeText(getApplicationContext(), "服务器错误",
                            Toast.LENGTH_SHORT).show();

                    break;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_main);
        mService = new MainService();
        _intent = new Intent(this, MainService.class);
        startService(_intent);
        _imgViewStatus = (ImageView) this.findViewById(R.id.imageViewStatus);

        _textView = (TextView) this.findViewById(R.id.textView1);
        _textViewDeviceName = (TextView) this
                .findViewById(R.id.textViewDeviceName);
        _textViewVersion = (TextView) this.findViewById(R.id.textViewVersion);

        String s = String.format(
                this.getResources().getString(R.string.version_info),
                MyUtils.getVersionName(this));
        _textViewVersion.setText(s);

        String deviceName = MainData.get_profile_string_value(this,
                MainData.PROFILE_SERVER_NAME, MainData.DEFAULT_SERVER_NAME);
        s = String.format(this.getResources().getString(R.string.device_name),
                deviceName);
        _textViewDeviceName.setText(s);
        button2 = (Button) this.findViewById(R.id.bt_button2);
        button1 = (Button) this.findViewById(R.id.bt_button1);

        ly1 = (LinearLayout) this.findViewById(R.id.ly_bt_one);
        ly2 = (LinearLayout) this.findViewById(R.id.ly_bt_two);
        if (button1.isFocused()) {
            ly1.setBackgroundColor(Color.BLUE);
        }
        // 控制是否开启手机遥控功能
        button1.setOnFocusChangeListener(new ViewisFocused());
        button2.setOnFocusChangeListener(new ViewisFocused());
        updateServiceStatus();
        // 是否检查更新版本
        button2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//				checkVersion();
                mService.uartopen("/dev/ttyAMA2");
            }

        });
        // 检查是否开启服务
        button1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                int flag = ((Integer) v.getTag()).intValue();

                if (flag == 1) {
                    // 停止
                    stopService(_intent);
                } else {
                    // 启动
                    startService(_intent);
                }

                updateServiceStatus();

            }

        });
    }

    // 遥控服务开启时控制按钮状态
    public void updateServiceStatus() {
        boolean b = isServiceRunning(this, "com.yobbom.service.MainService");

        if (b) {
            _imgViewStatus.setImageResource(R.drawable.service_running);
            _textView.setText(this.getResources().getString(
                    R.string.status_running));// "运行中...");
            // _button.setText(this.getResources().getString(R.string.btn_stop_service));
            button1.setTag(1);
            button1.setBackgroundResource(R.drawable.btn_service_stop_selector);
        } else {
            _imgViewStatus.setImageResource(R.drawable.service_stopped);
            _textView.setText(this.getResources().getString(
                    R.string.status_stopped));
            // _button.setText(this.getResources().getString(R.string.btn_start_service));
            button1.setTag(0);
            button1.setBackgroundResource(R.drawable.btn_service_start_selector);
        }
    }

    public boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    /**
     * 检查更新
     */
    private void checkVersion() {
        int keys[] = {0x2f, 0};
        int a = mService.msgsend(0x16, 0x00, keys);
        keyback = mService.msgrecv();
        int[] version = {keyback[0], keyback[1], keyback[2], keyback[3]};


        Tools.Log(keyback[0] + keyback[1] + keyback[2] + keyback[3] + "返回值");
        int isversionUP = mService.deviceupdatecheck(version);
        Tools.Log("返回是否更新" + isversionUP);
        // 访问服务器
        new Thread() {

            public void run() {
                // 用户开始访问网络的时间
                long startTime = System.currentTimeMillis();
                HttpURLConnection connection = null;
                Message msg = Message.obtain();
                try {
                    // 10.0.2.2 专门让模拟器访问电脑
                    URL url = new URL(
                            "http://yobbom.com/yobbom_helper/yobbomurl.json");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        // // 访问成功, 拿到数据, 解析数据
                        // InputStream inStream = connection.getInputStream();
                        // ParseXmlService service = new ParseXmlService();
                        // mHashMap = service.parseXml(inStream);
                        // mServerVersionCode =
                        // Integer.valueOf(mHashMap.get("version"));
                        // mDownloadUrl =mHashMap.get("url");
                        // GlobalParams.yphoneurl=mHashMap.get("ypurl");

                        // Tools.Log(GlobalParams.yphoneurl);
                        InputStream is = connection.getInputStream();
                        String jsonStr = StreamUtil.streamToString(is);
                        // 解析JSON
                        JSONObject jsonObject = new JSONObject(jsonStr);
                        mServerVersion = jsonObject.getInt("version");
                        mDownloadUrl = jsonObject.getString("url");
                        mDesc = new String(jsonObject
                                .getString("updatecontent").getBytes(), "utf-8");
                        int version = keyback[0] + keyback[1] + keyback[2]
                                + keyback[3];
                        // 版本判断
                        if (true) {
                            // 有新版本, 需要更新
                            // 在子线程中不能更新主线程UI
                            // showUpdateDialog();
                            // java.lang.RuntimeException: Can't create handler
                            // inside thread
                            // that has not called Looper.prepare()
                            // 下载音频
                            downloadUpd();
                            msg.what = SHOW_UPDATE_DIALOG;
                        }
                        // else {
                        // // 不需要更新
                        // msg.what = ENTER_HOME;
                        // }
                    } else {
                        // 没拿到数据, 3xx, 4xx, 5xx
                        msg.what = SERVER_ERROR;
                    }
                } catch (Exception e) {
                    // URL异常
                    msg.what = URL_ERROR;
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        // 断开连接
                        connection.disconnect();
                    }
                    // 访问网络结束
                    long endTime = System.currentTimeMillis();
                    long usedTime = endTime - startTime;
                    // 如果用户访问网络事件超过了2000毫秒, 就不要睡眠了
                    // 如果不超过2000毫秒, 睡够2000秒
                    if (usedTime < 2000) {
                        SystemClock.sleep(2000 - usedTime);
                    }
                    mHander.sendMessage(msg);
                }
            }

            ;
        }.start();
    }

    /**
     * 更新提示对话框
     */
    protected void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // 这个Context必须是Activity的this
        builder.setTitle("有新的音频版本");
        builder.setMessage(mDesc);
        // builder.setCancelable(false); // 表示不能取消, 一般不用
        builder.setPositiveButton("立即更新",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // System.out.println("立即更新");
                        int[] nowVersion = {0, 0, 3, 0};
                        int isversionUP = deviceupdatecheck(nowVersion);
                        Tools.Log("返回是否更新" + isversionUP);
                        CacheUtil.putBoolean(mContext, "isUpdate", true);
                    }
                });
        builder.setNegativeButton("下次再说",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("下次再说");

                    }
                });
        // 取消对话框时的回调
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
        // builder.show(); // 都可以
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 下载apk
     */
    protected void downloadUpd() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            // 使用xUtils下载
            HttpUtils http = new HttpUtils();
            // 进度对话框
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("正在下载...");
            String target = Environment.getExternalStorageDirectory()
                    + "/rom.upd";
            Tools.Log("下载的路径为：" + target);
            http.download(mDownloadUrl, target, new RequestCallBack<File>() {
                @Override
                public void onStart() {
                    super.onStart();
                    progressDialog.show();
                }

                // 下载过程中调用
                @Override
                public void onLoading(long total, long current,
                                      boolean isUploading) {
                    super.onLoading(total, current, isUploading);
                    progressDialog.setMax((int) total);
                    progressDialog.setProgress((int) current);
                }

                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    System.out.println("length:" + responseInfo.contentLength);

                    // 下载完成后, 让进度对话框消失
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(HttpException e, String error) {
                    e.printStackTrace();
                    System.out.println(error);
                    progressDialog.dismiss();

                }
            });
        } else {
            ToastShow.showToast(getApplicationContext(), "SD卡不可用", 1000);

        }
    }

    // 判断哪个控件获取了焦点
    private class ViewisFocused implements
            View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                // 此处为得到焦点时的处理内容
                switch (v.getId()) {
                    case R.id.bt_button1:
                        ly1.setBackgroundColor(0xFFFFFF33);
                        break;
                    case R.id.bt_button2:
                        ly2.setBackgroundColor(0xFFFFFF33);
                        break;

                    default:
                        break;
                }
            } else {
                // 此处为得到焦点时的处理内容
                switch (v.getId()) {
                    case R.id.bt_button1:
                        ly1.setBackgroundColor(0x000000FF);
                        break;
                    case R.id.bt_button2:
                        ly2.setBackgroundColor(0x000000FF);
                        break;

                    default:
                        break;
                }
            }
        }
    }

    public native int deviceupdatecheck(int reserved_data[]);

}
