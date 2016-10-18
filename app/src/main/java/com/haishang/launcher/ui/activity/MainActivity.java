package com.haishang.launcher.ui.activity;

import android.animation.Animator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.haishang.launcher.Constant;
import com.haishang.launcher.LauncherApplication;
import com.haishang.launcher.R;
import com.haishang.launcher.adapter.DataPagerAdapter;
import com.haishang.launcher.anim.FixedSpeedScroller;
import com.haishang.launcher.model.PackageNameInterface;
import com.haishang.launcher.model.WeatherCityInfo;
import com.haishang.launcher.model.WeatherModel;
import com.haishang.launcher.model.bean.RecommendVideo;
import com.haishang.launcher.model.bean.UpdateInfo;
import com.haishang.launcher.model.bean.Weather;
import com.haishang.launcher.presenter.WeatherPresenter;
import com.haishang.launcher.ui.MainView;
import com.haishang.launcher.util.RoundedTransformation;
import com.haishang.launcher.widget.CustomAlertDialog;
import com.haishang.launcher.widget.CustomAlertMessageDialog;
import com.haishang.launcher.widget.HorizontalListView;
import com.haishang.launcher.widget.MyViewPager;
import com.haishang.launcher.widget.PosterPageLayout;
import com.open.androidtvwidget.bridge.OpenEffectBridge;
import com.open.androidtvwidget.view.MainLayout;
import com.open.androidtvwidget.view.MainUpView;
import com.open.androidtvwidget.view.ReflectItemView;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/*import android.os.ServiceManager;
import android.os.storage.IMountService;*/

public class MainActivity extends Activity implements MainView, View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private WeatherPresenter presenter;
    //  View mOldFocus; // 4.3以下版本需要自己保存.
    ImageView playRecord;
    ImageView videoSearch;
    ImageView ktv;
    ImageView mangoTv;
    ImageView migu;
    ImageView setting;
    ImageView xiaoY;
    MyViewPager viewPager;
    List<PosterPageLayout> posterPageLayouts = new ArrayList<>();
    private DataPagerAdapter<PosterPageLayout> adapter;
    private int mPagerCount = 2;
    private int lastPage;

    private boolean firstTime = true;
    private boolean hasShowPosters = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        presenter = new WeatherPresenter(new WeatherModel(), this);
        Display display = this.getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        Log.d(TAG, "density=" + displayMetrics.density + "density dpi=" + displayMetrics.densityDpi + "height px=" + displayMetrics.heightPixels + "width px=" + displayMetrics.widthPixels);
        setAppViews();
        setWifiUpanViews();
        setPosterViewPage();
        presenter.loadRecommendVideo();
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

    private void initListview(final List<RecommendVideo.VideoInfo> recommendVideos)
    {

        HorizontalListView listView = (HorizontalListView) findViewById(R.id.list_view);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return recommendVideos.size();
            }

            @Override
            public Object getItem(int position) {
                return recommendVideos.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_poster, null);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_frame1);
                Picasso.with(MainActivity.this).load(recommendVideos.get(position).getVideoImgList().getVideoImgUrl1()).transform(new RoundedTransformation(12, 0)).into(imageView);
                return convertView;
            }
        });

        final MainUpView mainUpView1 = (MainUpView) findViewById(R.id.mainup_view);

        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    // 子控件置顶，必需使用ListViewTV才行，
                    // 不然焦点会错乱.
                    // 不要忘记这句关键的话哦.
                    view.bringToFront();
                    mainUpView1.setFocusView(view, mOldFocus, 1.2f);
                    mOldFocus = view;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * 全局的移动框
     */
//    private void initMainupView()
//    {
//        final MainUpView mainUpView1 = (MainUpView) findViewById(R.id.mainup_view);
//        //    mainUpView1.setShadowResource(R.drawable.mainupview_shadow);
//        mainUpView1.setDrawShadowPadding(-5);
//        MainLayout main_lay11 = (MainLayout) findViewById(R.id.main_lay);
//        final OpenEffectBridge openEffectBridge = ((OpenEffectBridge) mainUpView1.getEffectBridge());
//        main_lay11.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
//            @Override
//            public void onGlobalFocusChanged(final View oldFocus, final View  newFocus) {
//                //	newFocus.bringToFront(); // 防止放大的view被压在下面. (建议使用MainLayout)
//                float scale = 1.05f;
//                mainUpView1.setFocusView(newFocus, mOldFocus, scale);
//                mOldFocus = newFocus; // 4.3以下需要自己保存.
//                if (!(newFocus instanceof ReflectItemView)) {
//                    openEffectBridge.setVisibleWidget(true);
//                } else {
//                    openEffectBridge.setOnAnimatorListener(new OpenEffectBridge.NewAnimatorListener() {
//                        @Override
//                        public void onAnimationStart(OpenEffectBridge bridge, View view, Animator animation) {
//
//                        }
//
//                        @Override
//                        public void onAnimationEnd(OpenEffectBridge bridge, View view, Animator animation) {
//                            openEffectBridge.setVisibleWidget(false);
//                        }
//                    });
//                }
//            }
//        });
//    }

    void setAppViews() {
        playRecord = (ImageView) findViewById(R.id.iv_play_record);
        videoSearch = (ImageView) findViewById(R.id.iv_video_search);
        ktv = (ImageView) findViewById(R.id.iv_ktv);
        mangoTv = (ImageView) findViewById(R.id.iv_mango_tv);
        migu = (ImageView) findViewById(R.id.iv_migu);
        setting = (ImageView) findViewById(R.id.iv_setting);
        xiaoY = (ImageView) findViewById(R.id.iv_xiaoy);
        playRecord.setOnClickListener(this);
        videoSearch.setOnClickListener(this);
        ktv.setOnClickListener(this);
        mangoTv.setOnClickListener(this);
        migu.setOnClickListener(this);
        setting.setOnClickListener(this);
        xiaoY.setOnClickListener(this);
    }

    /**
     * 三个参数  1.图标的url  2.天气温度多少度到多少度 3.城市
     *
     * @param weather
     */
    @Override
    public void setWeather(Weather weather) {
        TextView weatherView = (TextView) findViewById(R.id.tv_weather_info);
        Log.d(TAG, weather.getHeWeatherDataService30().get(0).getBasic().getCity());
        weatherView.setText(weather.getHeWeatherDataService30().get(0).getBasic().getCity());
    }

    @Override
    public void setWeather(String weatherInfo) {
        TextView weatherView = (TextView) findViewById(R.id.tv_weather_info);
        Log.d(TAG, "weaehterinfo=" + weatherInfo);
        weatherView.setText(weatherInfo);

    }

    @Override
    public void setWeather(WeatherCityInfo weatherCityInfo) {
        String str = LauncherApplication.getContext().getResources().getString(R.string.weather_info);
        String min = weatherCityInfo.getMinTmp();
        String max = weatherCityInfo.getMaxTmp();
        String city = weatherCityInfo.getCity();
        Log.d(TAG, weatherCityInfo.getWeatherIcon());
        ImageView weatherIcon = (ImageView) findViewById(R.id.iv_weather_icon);
        Picasso.with(this).load(weatherCityInfo.getWeatherIcon()).into(weatherIcon);
        String tmp = String.format(str, city, min, max);
        TextView weatherView = (TextView) findViewById(R.id.tv_weather_info);
        weatherView.setText(tmp);
    }

    private OpenEffectBridge mSavebridge;
    private View mOldFocus;

//    public void initViewMove() {
//        for (View view : posterPageLayouts) {
//            MainUpView mainUpView = (MainUpView) view.findViewById(R.id.mainUpView1);
//            //  mainUpView.setUpRectResource(R.drawable); // 设置移动边框的图片.
//            // mainUpView.setShadowResource(R.drawable.item_shadow); // 设置移动边框的阴影.
//            OpenEffectBridge bridget = (OpenEffectBridge) mainUpView.getEffectBridge();
//            bridget.setTranDurAnimTime(250);
//        }
//    }


    /**
     * 设置Viewpager页
     */
    private void setViewPagerFocus() {

        if (getCurrentFocus() != null) {
            Log.d(TAG, "get current focus" + getCurrentFocus().getId() + getCurrentFocus().toString());
        } else {
            Log.d(TAG, "the has no focus view");
        }

        //     viewPager.requestFocus();
        viewPager.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener()
        {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus)
            {

                Log.d(TAG, "in addOnGlobalFocusChangeListener oldFocusView=" + oldFocus + "newFocusView=" + newFocus.toString());
                int pos = viewPager.getCurrentItem();
                Log.d(TAG, "int focusboserver pos=" + pos);
                pos = pos % mPagerCount;
                final MainUpView mainUpView = (MainUpView) posterPageLayouts.get(pos).findViewById(R.id.mainUpView1);
                //          mainUpView.setDrawShadowPadding(2);
                //第一次显示将焦点定位到中间的海报 PosterPageLayout的构造函数中将中间的空间设置为focus able 为 true
                if (firstTime) {
                    firstTime = false;
                    posterPageLayouts.get(pos).findViewById(R.id.iv_frame1_1).setFocusable(true);
                    posterPageLayouts.get(pos).findViewById(R.id.iv_frame1_3).setFocusable(true);
                    for (int i = 0; i < mPagerCount; i++) {
                        if (pos == i) {
                            continue;
                        }
                        posterPageLayouts.get(i).cleanFocusAttr();
                    }
                }
                //// FIXME: 2016/5/30 0030 raduis有问题 下面为显示效果较好的经验值
                Rect rect = new Rect(1, 2, 1, 1);
                mainUpView.setDrawUpRectPadding(rect);
                //   mainUpView.setVisibility(View.INVISIBLE);

                //图片的下半部显示不好属于阴影的问题  onDrawShadow in BaseEffctBridge
                //Rect rect = new Rect(2, 2, 2, 2);
                //mainUpView.setDrawUpRectPadding(rect);
                final OpenEffectBridge bridge = (OpenEffectBridge) mainUpView.getEffectBridge();
                if (!(newFocus instanceof ReflectItemView)) {
                    mainUpView.setUnFocusView(mOldFocus);
                    bridge.setVisibleWidget(true);
                    mSavebridge = null;
                } else {
                    bridge.setVisibleWidget(true);
                    newFocus.bringToFront();
                    mSavebridge = bridge;
                    // 动画结束才设置边框显示，是为了防止翻页的时候从另一边跑出来的问题.
                    bridge.setOnAnimatorListener(new OpenEffectBridge.NewAnimatorListener() {
                        @Override
                        public void onAnimationStart(OpenEffectBridge bridge, View view, Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(OpenEffectBridge bridge1, View view, Animator animation) {
                            if (mSavebridge == bridge1) {
                                bridge.setVisibleWidget(false);
                            }
                        }
                    });
                    mainUpView.setFocusView(newFocus, mOldFocus, 1.07f);
                }
                mOldFocus = newFocus;
            }

        });
        // TODO: 2016/5/11 0011 设置viewpager响应按键的时间间隔
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(viewPager.getContext(),
                    new AccelerateInterpolator());
            field.set(viewPager, scroller);
            scroller.setmDuration(0);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        //    viewPager.setPageTransformer(true, new ZoomOutPageTransformer());

//        viewPager.setPageMargin(500);

        // TODO: 2016/5/9 0009 figure out why must be set to 1
        viewPager.setOffscreenPageLimit(1);
        // 设置默认的显示为中间的item 这样可以左右滑动
        viewPager.setCurrentItem((Integer.MAX_VALUE / 2) - 1);
        lastPage = Integer.MAX_VALUE / 2 - 1;
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "position=" + position);
                int realPos = position % mPagerCount;
                if (lastPage > position) {//User Move to left
                    setPageFocusRight(realPos);
                } else {//move to right
                    setPageFocusLeft(realPos);
                }
                lastPage = position;
                // 将前一页的边框隐藏
                if (realPos > 0) {
                    hideMainUpView(realPos - 1);
                } else if (realPos < (viewPager.getChildCount() - 1)) {//将后一页的边框隐藏
                    hideMainUpView(realPos + 1);
                }

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                Log.d(TAG, "arg0=" + arg0 + "arg1=" + arg1 + "arg2" + arg2);
               /* if (arg1 > 0.0) {
                    viewPager.setScrollble(false);
                } else {
                    viewPager.setScrollble(true);
                }*/
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                Log.d(TAG, "positionxxxxxx" + arg0);
                /*if (arg0 == 2) {//开始滑动
                    viewPager.setScrollble(false);
//                    viewPager.setClickable(false);
                    //  viewPager.setScanScroll(false);
                } else if (arg0 == 0) {//滑动结束
                    viewPager.setScrollble(true);
                    // viewPager.setClickable(false);
                    // viewPager.setScanScroll(true);
                }
*/
            }
        });

    }

    /**
     * 隐藏某一页的移动边框
     */
    private void hideMainUpView(int pos) {
        MainUpView mainUpView = (MainUpView) posterPageLayouts.get(pos).findViewById(R.id.mainUpView1);
        OpenEffectBridge bridge = (OpenEffectBridge) mainUpView.getEffectBridge();
        bridge.setVisibleWidget(true);
    }

    private void setPageFocusLeft(int pos) {
        ReflectItemView reflectItemView = (ReflectItemView) posterPageLayouts.get(pos).findViewById(R.id.iv_frame1_1);
        reflectItemView.setFocusable(true);
        reflectItemView.requestFocus();

        ReflectItemView reflectItemView2 = (ReflectItemView) posterPageLayouts.get(pos).findViewById(R.id.iv_frame1_2);
        reflectItemView2.setFocusable(true);
        ReflectItemView reflectItemView3 = (ReflectItemView) posterPageLayouts.get(pos).findViewById(R.id.iv_frame1_3);
        reflectItemView3.setFocusable(true);
    }

    private void setPageFocusRight(int pos) {
        ReflectItemView reflectItemView = (ReflectItemView) posterPageLayouts.get(pos).findViewById(R.id.iv_frame1_3);
        reflectItemView.setFocusable(true);
        reflectItemView.requestFocus();

        ReflectItemView reflectItemView2 = (ReflectItemView) posterPageLayouts.get(pos).findViewById(R.id.iv_frame1_2);
        reflectItemView2.setFocusable(true);
        ReflectItemView reflectItemView1 = (ReflectItemView) posterPageLayouts.get(pos).findViewById(R.id.iv_frame1_1);
        reflectItemView1.setFocusable(true);
    }

    /**
     * http请求还没有返回的时候,显示默认的图片,就必须初始viewpager的多个pagers
     */
    public void setPosterViewPage() {
        viewPager = (MyViewPager) findViewById(R.id.view_pager);
        if (posterPageLayouts != null && posterPageLayouts.size() > 0) {
            posterPageLayouts.clear();
        }
        for (int i = 0; i < mPagerCount; i++) {
            PosterPageLayout posterPageLayout = new PosterPageLayout(this);
            posterPageLayout.setPosterList(null, i, mPagerCount);
            posterPageLayout.initPosters();
            posterPageLayouts.add(posterPageLayout);
        }
        adapter = new DataPagerAdapter<>(this, posterPageLayouts);
        viewPager.setAdapter(adapter);
        setViewPagerFocus();
    }

    /**
     * 得到海报的数据后显示
     *
     * @param videoInfos
     */
    public void setPosters(List<RecommendVideo.VideoInfo> videoInfos) {
        for (int i = 0; i < mPagerCount; i++) {
            posterPageLayouts.get(i).setPosterList(videoInfos, i, mPagerCount);
            posterPageLayouts.get(i).initPosters();
        }
        adapter.notifyDataSetChanged();
    }

 /*   @Override
    public void setRecommendVideo(RecommendVideo recommendVideo) {
        initListview(recommendVideo.getVideoInfos());
    }*/
    /* */

    /**
     * 使用HorizonScrollView来显示海报
     *
     * @param recommendVideo
     */
   /* @Override
    public void setRecommendVideo(final RecommendVideo recommendVideo) {
        ImageView imageView1 = (ImageView) findViewById(R.id.iv_frame1);
        ReflectItemView iv_frame1_1 = (ReflectItemView) findViewById(R.id.iv_frame1_1);
        iv_frame1_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ddddddddddddddddddddddddddddddddd");
                RecommendVideo.VideoInfo videoInfo = recommendVideo.getVideoInfos().get(0);
                showVideoDetail(videoInfo.getVideoId(), videoInfo.getVideoUiStyle());
            }
        });
        Picasso.with(this).load(recommendVideo.getVideoInfos().get(0).getVideoImgList().getVideoImgUrl1()).into(imageView1);
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "sdfffffffffffffffffffffffffff");
                RecommendVideo.VideoInfo videoInfo = recommendVideo.getVideoInfos().get(0);
                showVideoDetail(videoInfo.getVideoId(), videoInfo.getVideoUiStyle());
            }
        });

        ReflectItemView iv_frame1_2 = (ReflectItemView) findViewById(R.id.iv_frame1_2);
        iv_frame1_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ddddddddddddddddddddddddddddddddd");
                RecommendVideo.VideoInfo videoInfo = recommendVideo.getVideoInfos().get(1);
                showVideoDetail(videoInfo.getVideoId(), videoInfo.getVideoUiStyle());
            }
        });
        ImageView imageView2 = (ImageView) findViewById(R.id.iv_frame2);
        Picasso.with(this).load(recommendVideo.getVideoInfos().get(1).getVideoImgList().getVideoImgUrl1()).into(imageView2);


        ReflectItemView iv_frame1_3 = (ReflectItemView) findViewById(R.id.iv_frame1_3);
        iv_frame1_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ddddddddddddddddddddddddddddddddd");
                RecommendVideo.VideoInfo videoInfo = recommendVideo.getVideoInfos().get(2);
                showVideoDetail(videoInfo.getVideoId(), videoInfo.getVideoUiStyle());
            }
        });
        ImageView imageView3 = (ImageView) findViewById(R.id.iv_frame3);
        Picasso.with(this).load(recommendVideo.getVideoInfos().get(2).getVideoImgList().getVideoImgUrl1()).into(imageView3);


        ReflectItemView iv_frame1_4 = (ReflectItemView) findViewById(R.id.iv_frame1_4);
        iv_frame1_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ddddddddddddddddddddddddddddddddd");
                RecommendVideo.VideoInfo videoInfo = recommendVideo.getVideoInfos().get(3);
                showVideoDetail(videoInfo.getVideoId(), videoInfo.getVideoUiStyle());
            }
        });
        ImageView imageView4 = (ImageView) findViewById(R.id.iv_frame4);
        Picasso.with(this).load(recommendVideo.getVideoInfos().get(3).getVideoImgList().getVideoImgUrl1()).into(imageView4);


        ReflectItemView iv_frame1_5 = (ReflectItemView) findViewById(R.id.iv_frame1_5);
        iv_frame1_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ddddddddddddddddddddddddddddddddd");
                RecommendVideo.VideoInfo videoInfo = recommendVideo.getVideoInfos().get(4);
                showVideoDetail(videoInfo.getVideoId(), videoInfo.getVideoUiStyle());
            }
        });
        ImageView imageView5 = (ImageView) findViewById(R.id.iv_frame5);
        Picasso.with(this).load(recommendVideo.getVideoInfos().get(4).getVideoImgList().getVideoImgUrl1()).into(imageView5);


        ReflectItemView iv_frame1_6 = (ReflectItemView) findViewById(R.id.iv_frame1_6);
        iv_frame1_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ddddddddddddddddddddddddddddddddd");
                RecommendVideo.VideoInfo videoInfo = recommendVideo.getVideoInfos().get(5);
                showVideoDetail(videoInfo.getVideoId(), videoInfo.getVideoUiStyle());
            }
        });
        ImageView imageView6 = (ImageView) findViewById(R.id.iv_frame6);
        Picasso.with(this).load(recommendVideo.getVideoInfos().get(5).getVideoImgList().getVideoImgUrl1()).into(imageView6);
    }*/
    @Override
    public void setRecommendVideo(final RecommendVideo recommendVideo) {
        //防止多次显示的问题
        if (!hasShowPosters) {

            setPosters(recommendVideo.getVideoInfos());
            hasShowPosters = true;
        }
    }

    @Override
    public void updateApp(final UpdateInfo updateInfo) {
       /* AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(R.string.update_title).setMessage(updateInfo.getUpdate_message())
                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //         AndroidUtil.downloadApk(updateInfo.getApk_url(), Constant.UPDATE_APK_ID_PREFS, Constant.LAUNCHER_APP);


                        DownloadActivity.startThisActivity(MainActivity.this, updateInfo.getApk_url());

                        // Toast.makeText(MainActivity.this, "后台下载中", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // dialog.dismiss();
                    }
                }).setCancelable(false).create();

        alertDialog.show();*/
        CustomAlertMessageDialog.Builder builder = new CustomAlertMessageDialog.Builder(this);
        builder.setTitle(R.string.update_title);
        builder.setMessage(updateInfo.getUpdate_message());
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //设置你的操作事项
                DownloadActivity.startThisActivity(MainActivity.this, updateInfo.getApk_url());
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

    @Override
    public void setPresenter(WeatherPresenter presenter) {
        this.presenter
                = presenter;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
                DownloadActivity.startThisActivity(MainActivity.this, Constant.XIAO_Y_APP_URL);
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
                DownloadActivity.startThisActivity(MainActivity.this, Constant.XMLY_APP_URL);
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
        Log.d(TAG, "videoid=" + videoId + "videouistyle" + videoUiStyle);
        Intent intent = new Intent();
        intent.setAction("com.hunantv.license");
        intent.putExtra("cmd_ex", "show_video_detail");
        intent.putExtra("video_id", videoId);
        intent.putExtra("video_type", "0");
        intent.putExtra("video_ui_style", videoUiStyle);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver();
        super.onDestroy();
    }

    ImageView ivNetworkState;
    ImageView uPan;
    ImageView tf;

    /* public void tf() {
         AlertDialog dialog = new AlertDialog.Builder(this).setMessage("是否卸载已经挂载的TF卡").
                 setNegativeButton("取消", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                     }
                 }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 unmountTf();
             }
         }).create();
         dialog.show();

     }

     public void uPan() {
         printMountDevicesPath();
         AlertDialog dialog = new AlertDialog.Builder(this).setMessage("是否卸载已经挂载的U盘").
                 setNegativeButton("取消", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                     }
                 }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 unmountUPanAndSata();
             }
         }).create();
         dialog.show();

     }*/
    void setWifiUpanViews() {
        ivNetworkState = (ImageView) findViewById(R.id.iv_network_state);
        uPan = (ImageView) findViewById(R.id.iv_u_pan);
        tf = (ImageView) findViewById(R.id.iv_tf);
        uPan.setOnClickListener(this);
        tf.setOnClickListener(this);

       /* if (hasUPanOrSata()) {
            uPan.setVisibility(View.VISIBLE);
        } else {
            uPan.setVisibility(View.INVISIBLE);
        }
        if (hasTf()) {
            tf.setVisibility(View.VISIBLE);
        } else {
            tf.setVisibility(View.INVISIBLE);
        }*/
    }

    private void registerReceiver() {
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        usbFilter.addAction(Intent.ACTION_UMS_DISCONNECTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbFilter.addDataScheme("file");
        registerReceiver(this.networkStateChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(this.networkStateChangeReceiver, usbFilter);
    }

    private void unregisterReceiver() {
        if (networkStateChangeReceiver != null) {
            unregisterReceiver(networkStateChangeReceiver);
        }
    }

    private boolean d = true;
    /**
     * 网络状态和外部设备插入状态
     */
    private BroadcastReceiver networkStateChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "action=" + intent.getAction());
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo currentNetworkInfo = cm.getActiveNetworkInfo();
                if (currentNetworkInfo != null && currentNetworkInfo.isConnected()) {
            /*    if (BuildConfig.DEBUG)
                    Toast.makeText(context, "Connected", Toast.LENGTH_LONG)
                            .show();*/
                    //    ivNetworkState.setVisibility(View.GONE);
                    ivNetworkState.setVisibility(View.VISIBLE);
                    ivNetworkState.setImageResource(R.drawable.network);
                    presenter.start();

                  /*  presenter.loadUpdateInfo();
                    presenter.loadWeatherData();*/


                } else if (currentNetworkInfo == null) {
           /*     if (BuildConfig.DEBUG)
                    Toast.makeText(context, "no network", Toast.LENGTH_LONG)
                            .show();*/
                    ivNetworkState.setVisibility(View.VISIBLE);
                    ivNetworkState.setImageResource(R.drawable.network_no);
                }
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                //显示有U盘的图标
               /* if (hasTf()) {
                    tf.setVisibility(View.VISIBLE);
                }
                if (hasUPanOrSata()) {
                    uPan.setVisibility(View.VISIBLE);
                }*/
            } else if (action.equals(Intent.ACTION_MEDIA_REMOVED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
              /*  if (!hasUPanOrSata()) {//当有多个设备的时候,先判断还有没有挂载的设备
                    uPan.setVisibility(View.INVISIBLE);
                }
                if (!hasTf()) {
                    tf.setVisibility(View.INVISIBLE);
                }*/
            }
        }
    };

   /* *
     * 卸载U盘和硬盘
     */
   /* private void unmountUPanAndSata() {
        IMountService mountService = getMountService();
        if (mountService == null) {
            return;
        }
        try {
            MountInfo info = new MountInfo(this);
            for (int i = 0; i < info.type.length; i++) {
                if (info.type[i] != 3 && info.path[i] != null) {
                    mountService.unmountVolume(info.path[i], true, false);
                }
            }
        } catch (RemoteException e) {
            Toast.makeText(this, "卸载设备失败,设备是否在使用中?", Toast.LENGTH_SHORT).show();
        }
    }
*/
    /**
     * 卸载外插的tf卡
     *//*
    private void unmountTf() {
        IMountService mountService = getMountService();
        if (mountService == null) {
            return;
        }
        try {
            MountInfo info = new MountInfo(this);
            for (int i = 0; i < info.type.length; i++) {
                if (info.type[i] == 3 && !info.path[i].equals(originMountDevicePath)) {
                    mountService.unmountVolume(info.path[i], true, false);
                }
            }
        } catch (RemoteException e) {
            Toast.makeText(this, "卸载设备失败,设备是否在使用中?", Toast.LENGTH_SHORT).show();
        }
    }

    private void printMountDevicesPath() {
        MountInfo info = new MountInfo(this);
        for (int i = 0; i < info.path.length; i++) {
            if (info.path[i] != null) {
                Log.d(TAG, "mount devices " + i + "" + info.path[i]);
            }
        }
    }

    final static String originMountDevicePath = "/storage/emulated/0"; //自带的存储卡

    *//**
     * 是否有外插的sd(tf)卡(板子本身自带一个sd卡)
     *
     * @return
     *//*
    private boolean hasTf() {
        printMountDevicesPath();
        MountInfo info = new MountInfo(this);
        for (int i = 0; i < info.type.length; i++) {
            if (info.type[i] == 3 && !info.path[i].equals(originMountDevicePath)) {
                return true;
            }
        }
        return false;
    }

    */

    /**
     * 是否有mount的U盘或者硬盘
     *
     * @return
     *//*
    private boolean hasUPanOrSata() {
        printMountDevicesPath();
        MountInfo info = new MountInfo(this);
        for (int i = 0; i < info.path.length; i++) {
            if (info.type[i] != 3 && info.path[i] != null) {//3 表示tf卡
                return true;
            }
        }
        return false;
    }

    private synchronized IMountService getMountService() {
        IMountService iMountService = null;
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            iMountService = IMountService.Stub.asInterface(service);
        } else {
            Log.e(TAG, "Can't get mount service");
        }
        return iMountService;
    }
*/
    @Override
    public void onBackPressed() {

        //super.onBackPressed();
    }
}
