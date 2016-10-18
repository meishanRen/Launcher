package com.haishang.launcher.ui.activity;

import android.animation.Animator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.haishang.launcher.Constant;
import com.haishang.launcher.LauncherApplication;
import com.haishang.launcher.R;
import com.haishang.launcher.model.PackageNameInterface;
import com.haishang.launcher.model.WeatherCityInfo;
import com.haishang.launcher.model.WeatherModel;
import com.haishang.launcher.model.bean.RecommendVideo;
import com.haishang.launcher.model.bean.UpdateInfo;
import com.haishang.launcher.model.bean.Weather;
import com.haishang.launcher.presenter.WeatherPresenter;
import com.haishang.launcher.ui.MainView;
import com.haishang.launcher.widget.CustomAlertDialog;
import com.haishang.launcher.widget.CustomAlertMessageDialog;
import com.haishang.launcher.widget.PosterPageLayout;
import com.open.androidtvwidget.bridge.OpenEffectBridge;
import com.open.androidtvwidget.view.MainLayout;
import com.open.androidtvwidget.view.MainUpView;
import com.open.androidtvwidget.view.ReflectItemView;
import com.open.androidtvwidget.view.SmoothHorizontalScrollView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class MainSecActivity extends Activity  implements MainView ,View.OnClickListener{
    private WeatherPresenter presenter;
    //  View mOldFocus; // 4.3以下版本需要自己保存.
    ImageView playRecord;
    ImageView videoSearch;
    ImageView ktv;
    ImageView mangoTv;
    ImageView migu;
    ImageView setting;
    ImageView xiaoY;
    PosterPageLayout posterPageLayouts;
//    SmoothHorizontalScrollView posterScrolView;
   ReflectItemView RIposter1,RIposter2;
    private FocuosChangeListenner Focuosls;
    private boolean firstTime = true;
    MainUpView mainUpView;
    private boolean hasShowPosters = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sec);
        //获取天气数据
        presenter = new WeatherPresenter(new WeatherModel(), this);
        Display display = this.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        setWifiUpanViews();
        presenter.loadRecommendVideo();
        setAppViews();
        //  Toast.makeText(this, "this is a test for update  " + "versoin code =3", Toast.LENGTH_SHORT).show();
        registerReceiver();
        /**
         *当把遥控器的代码单独一个apk的时候
         */
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.yobbom.activity", Constant.REMOTE_SERVICE));
        startService(intent);
/**
 * 负责启动遥控器 如果没有启动的话 就启动service
 */
    }
    //给按钮设置监听
    void setAppViews() {
        playRecord = (ImageView) findViewById(R.id.iv_play_record);
        videoSearch = (ImageView) findViewById(R.id.iv_video_search);
        ktv = (ImageView) findViewById(R.id.iv_ktv);
        mangoTv = (ImageView) findViewById(R.id.iv_mango_tv);
        migu = (ImageView) findViewById(R.id.iv_migu);
        setting = (ImageView) findViewById(R.id.iv_setting);
        xiaoY = (ImageView) findViewById(R.id.iv_xiaoy);
//        posterScrolView=(SmoothHorizontalScrollView) findViewById(R.id.sc_poster);
        Focuosls=new FocuosChangeListenner();
        RIposter1=(ReflectItemView) findViewById(R.id.iv_frame1_1_sec);
        RIposter1.setOnFocusChangeListener(new FocuosChangeListenner());
        RIposter2=(ReflectItemView) findViewById(R.id.iv_frame1_2_sec);
        RIposter2.setOnFocusChangeListener(new FocuosChangeListenner());
        playRecord.setOnClickListener(this);
        videoSearch.setOnClickListener(this);
        ktv.setOnClickListener(this);
        mangoTv.setOnClickListener(this);
        migu.setOnClickListener(this);
        setting.setOnClickListener(this);
        xiaoY.setOnClickListener(this);
    }
    //控制天气的代码》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》》
    @Override
    public void setPresenter(WeatherPresenter presenter) {
        this.presenter
                = presenter;
    }
    /**
     * 三个参数  1.图标的url  2.天气温度多少度到多少度 3.城市
     *
     * @param weather
     */
    @Override
    public void setWeather(Weather weather) {
        TextView weatherView = (TextView) findViewById(R.id.tv_weather_info);
        weatherView.setText(weather.getHeWeatherDataService30().get(0).getBasic().getCity());
    }

    @Override
    public void setWeather(String weatherInfo) {
        TextView weatherView = (TextView) findViewById(R.id.tv_weather_info);
        weatherView.setText(weatherInfo);

    }

    @Override
    public void setWeather(WeatherCityInfo weatherCityInfo) {
        String str = LauncherApplication.getContext().getResources().getString(R.string.weather_info);
        String min = weatherCityInfo.getMinTmp();
        String max = weatherCityInfo.getMaxTmp();
        String city = weatherCityInfo.getCity();
        ImageView weatherIcon = (ImageView) findViewById(R.id.iv_weather_icon);
        Picasso.with(this).load(weatherCityInfo.getWeatherIcon()).into(weatherIcon);
        String tmp = String.format(str, city, min, max);
        TextView weatherView = (TextView) findViewById(R.id.tv_weather_info);
        weatherView.setText(tmp);
    }

    @Override
    public void setRecommendVideo(RecommendVideo recommendVideo) {

    }

    //更新app》》》》》》》》》》》》》》》》》》》》》》》》》》
    @Override
    public void updateApp(final UpdateInfo updateInfo) {
        CustomAlertMessageDialog.Builder builder = new CustomAlertMessageDialog.Builder(this);
        builder.setTitle(R.string.update_title);
        builder.setMessage(updateInfo.getUpdate_message());
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //设置你的操作事项
                DownloadActivity.startThisActivity(MainSecActivity.this, updateInfo.getApk_url());
            }
        });

        builder.setNegativeButton("取消",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        CustomAlertMessageDialog customAlertMessageDialog = builder.create();
     /*   WindowManager.LayoutParams lp = customAlertMessageDialog.getWindow().getAttributes();
        lp.width = 700;//定义宽度
        lp.height = 300;//定义高度
        customAlertMessageDialog.getWindow().setAttributes(lp);*/
        customAlertMessageDialog.show();
        // customAlertMessageDialog.getWindow().setLayout((int) getResources().getDimension(R.dimen.dialog_width), (int) getResources().getDimension(R.dimen.dialog_height));

    }
//控制按钮的点击事件的处理
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_u_pan:
                //  uPan();
                break;
            case R.id.iv_tf:
                //   tf();
                break;
            case R.id.iv_play_record:
                playRecord();
                break;
            case R.id.iv_video_search:


                //  videoSearch();
//改成了启动喜马拉雅
                startActivity(PackageNameInterface.XMLY);

                break;
            case R.id.iv_ktv:
                //TODO  临时修改 将ktv换成媒体中心
                mediaCenter();
                //  startActivity(PackageNameInterface.KTV_TIANLAI);
                break;
            case R.id.iv_mango_tv:
                startActivity(PackageNameInterface.MANGO_TV);
                break;
            case R.id.iv_migu:
                startActivity(PackageNameInterface.MIGU_MUSIC);
                break;
            case R.id.iv_setting:
                startActivity(PackageNameInterface.SETTING);
                break;
            case R.id.iv_xiaoy:
                startActivity(PackageNameInterface.XIAO_Y);
                break;
        }
    }
    private void startActivity(String packageName) {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        //   intent = pm.getLeanbackLaunchIntentForPackage(packageName);
        if (intent != null) {
            startActivity(intent);
        } else {
            if (packageName.equals(PackageNameInterface.XIAO_Y)) {
                //小y游戏未安装,是否下载安装
                showDownloadXiaoYDialog();
            } else if (packageName.equals(PackageNameInterface.XMLY)) {
                //喜马拉雅没有安装
                showDownloadXIMaLaYaDialog();
            } else {
                Toast.makeText(this, "未安装该应用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDownloadXiaoYDialog() {
        CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(this);
        // builder.setMessage("没有安装小y游戏,是否下载安装");
        builder.setTitle("没有安装小y游戏,是否下载安装?");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //设置你的操作事项
                DownloadActivity.startThisActivity(MainSecActivity.this, Constant.XIAO_Y_APP_URL);
            }
        });

        builder.setNegativeButton("否",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        CustomAlertDialog customAlertDialog = builder.create();
        customAlertDialog.show();


    }


    private void showDownloadXIMaLaYaDialog() {

        CustomAlertDialog.Builder builder = new CustomAlertDialog.Builder(this);
        builder.setTitle("没有安装喜马拉雅,是否下载安装?");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //设置你的操作事项
                DownloadActivity.startThisActivity(MainSecActivity.this, Constant.XMLY_APP_URL);
            }
        });

        builder.setNegativeButton("否",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        CustomAlertDialog customAlertDialog = builder.create();
        customAlertDialog.show();


    }
    public void mediaCenter() {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(PackageNameInterface.MEDIA_CENTER);
        //点击媒体中心时判断有kodi进入kodi，没有kodi进入原来的媒体中心
        if (intent == null) {
            startActivity(PackageNameInterface.MEDIA_CENTER_BENCH);
        } else {
            startActivity(PackageNameInterface.MEDIA_CENTER);
        }
    }
    private void playRecord() {
        Intent intent = new Intent();
        intent.setAction("com.hunantv.license");
        intent.putExtra("cmd_ex", "show_play_list");
        startActivity(intent);
    }

    private void videoSearch() {
        Intent intent = new Intent();
        intent.setAction("com.hunantv.license");
        intent.putExtra("cmd_ex", "show_search");
        startActivity(intent);
    }


    private void showVideoDetail(String videoId, String videoUiStyle) {

        Intent intent = new Intent();
        intent.setAction("com.hunantv.license");
        intent.putExtra("cmd_ex", "show_video_detail");
        intent.putExtra("video_id", videoId);
        intent.putExtra("video_type", "0");
        intent.putExtra("video_ui_style", videoUiStyle);
        startActivity(intent);
    }
    ImageView ivNetworkState;
    ImageView uPan;
    ImageView tf;
    void setWifiUpanViews() {
        ivNetworkState = (ImageView) findViewById(R.id.iv_network_state);
        uPan = (ImageView) findViewById(R.id.iv_u_pan);
        tf = (ImageView) findViewById(R.id.iv_tf);
        uPan.setOnClickListener(this);
        tf.setOnClickListener(this);
    }

    /*
    建一个内部类用来处理所有控件是否获取到了焦点的侦听
     */
    class FocuosChangeListenner implements android.view.View.OnFocusChangeListener{
        @Override
        public void onFocusChange(View newFocus, boolean b)
        {
        }
    }

    /**
     * 隐藏某一页的的边框的方法，是将该页面的所有的边框全部隐藏，当scrollView内的view全部隐藏
     */
    private void hideMainUpView() {
        MainUpView mainUpView = (MainUpView)findViewById(R.id.mainup_view_sec);
        OpenEffectBridge bridge = (OpenEffectBridge) mainUpView.getEffectBridge();
        bridge.setVisibleWidget(true);
    }
    /*
    这是为了让焦点移动的
     */
    private void setPageFocusLeft(int pos) {
        ReflectItemView reflectItemView = (ReflectItemView)findViewById(R.id.iv_frame1_1);
        reflectItemView.setFocusable(true);
        reflectItemView.requestFocus();

        ReflectItemView reflectItemView2 = (ReflectItemView)findViewById(R.id.iv_frame1_2);
        reflectItemView2.setFocusable(true);
        ReflectItemView reflectItemView3 = (ReflectItemView) findViewById(R.id.iv_frame1_3);
        reflectItemView3.setFocusable(true);
    }

    private void setPageFocusRight(int pos) {
        ReflectItemView reflectItemView = (ReflectItemView)findViewById(R.id.iv_frame1_3);
        reflectItemView.setFocusable(true);
        reflectItemView.requestFocus();

        ReflectItemView reflectItemView2 = (ReflectItemView)findViewById(R.id.iv_frame1_2);
        reflectItemView2.setFocusable(true);
        ReflectItemView reflectItemView1 = (ReflectItemView) findViewById(R.id.iv_frame1_1);
        reflectItemView1.setFocusable(true);
    }
    private void registerReceiver() {
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        usbFilter.addAction(Intent.ACTION_UMS_DISCONNECTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbFilter.addDataScheme("file");

    }
}
