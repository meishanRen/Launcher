package com.haishang.launcher.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.haishang.launcher.R;
import com.haishang.launcher.model.AppOkHttpClient;
import com.haishang.launcher.model.bean.RecommendVideo;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.open.androidtvwidget.view.ReflectItemView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * Created by chenrun on 2016/5/5 0005.
 */
public class PosterPageLayout extends FrameLayout implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private final static int radius = 0;
    Context context;

    public PosterPageLayout(Context context) {
        this(context, null, 0);
    }

    public PosterPageLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PosterPageLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        View v = LayoutInflater.from(context).inflate(R.layout.item_page_poster, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        v.findViewById(R.id.iv_frame1_2).setFocusable(true);

        addView(v);
    }

    List<RecommendVideo.VideoInfo> videoInfos;
    int pageIndex = 0;

    public void setPosterList(List<RecommendVideo.VideoInfo> videoInfos, int pagerIndex, int pagerCount) {
        this.videoInfos = videoInfos;
        this.pageIndex = pagerIndex;
    }

    int[] posterIds = {R.id.iv_frame1, R.id.iv_frame2, R.id.iv_frame3};
    int[] posterReflectIds = {R.id.iv_frame1_1, R.id.iv_frame1_2, R.id.iv_frame1_3};

    public void initPosters() {
        if (videoInfos != null) {
            for (int i = 0; i < 3; i++) {
                ImageView imageView = (ImageView) findViewById(posterIds[i]);
//缓存图片
                File customCacheDirectory = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/MyCache");
                OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(AppOkHttpClient.REWRITE_CACHE_CONTROL_INTERCEPTOR).cache(new Cache(customCacheDirectory, Integer.MAX_VALUE)).build();
                OkHttp3Downloader okHttp3Downloader = new OkHttp3Downloader(okHttpClient);
                Picasso picasso = new Picasso.Builder(context).downloader(okHttp3Downloader).build();
                //     picasso.setIndicatorsEnabled(true);
                picasso.load(videoInfos.get(3 * pageIndex + i).getVideoImgList().getVideoImgUrl1()).into(imageView);
                //Picasso.with(context).load(videoInfos.get(3 * pageIndex + i).getVideoImgList().getVideoImgUrl1()).transform(new RoundedTransformation(12, 0)).into(imageView);
                ReflectItemView reflectItemView = (ReflectItemView) findViewById(posterReflectIds[i]);
                reflectItemView.setOnClickListener(this);
            }
        } else {
            for (int i = 0; i < 3; i++) {
                ImageView imageView = (ImageView) findViewById(posterIds[i]);
                Picasso.with(context).load(R.drawable.poster_default).into(imageView);
            }
        }
    }

    public void cleanFocusAttr() {
        findViewById(R.id.iv_frame1_1).setFocusable(false);
        findViewById(R.id.iv_frame1_2).setFocusable(false);
        findViewById(R.id.iv_frame1_3).setFocusable(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_frame1_1:
                RecommendVideo.VideoInfo videoInfo = videoInfos.get(3 * pageIndex);
                this.showVideoDetail(videoInfo);
                break;
            case R.id.iv_frame1_2:
                RecommendVideo.VideoInfo videoInfo1 = videoInfos.get(3 * pageIndex + 1);
                this.showVideoDetail(videoInfo1);
                break;
            case R.id.iv_frame1_3:
                RecommendVideo.VideoInfo videoInfo2 = videoInfos.get(3 * pageIndex + 2);
                this.showVideoDetail(videoInfo2);
                break;
            default:
                break;

        }
    }


    private void showVideoDetail(RecommendVideo.VideoInfo videoInfo) {
        //  Log.d(TAG, "videoid=" + videoId + "videouistyle" + videoUiStyle);
        Intent intent = new Intent();
        //  intent.setAction("com.starcor.hunan.mgtv");
        intent.setAction("com.hunantv.license");
        intent.putExtra("cmd_ex", "show_video_detail");
        intent.putExtra("video_id", videoInfo.getVideoId());
        intent.putExtra("video_type", "0");
        intent.putExtra("video_ui_style", videoInfo.getVideoUiStyle());
        context.startActivity(intent);
    }
}
