package com.haishang.launcher.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.haishang.launcher.Constant;
import com.haishang.launcher.LauncherApplication;
import com.haishang.launcher.model.ApiService;
import com.haishang.launcher.model.bean.Ad;
import com.haishang.launcher.util.Util;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by chenrun on 2016/6/8 0008.
 */
public class TimeChangeReceiver extends BroadcastReceiver {
    final String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "get time change broadcast " + intent.getAction());
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://baidu.com/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ApiService service = retrofit.create(ApiService.class);
        final Observable<Ad> observable = service.getAdInfo();
        observable.subscribeOn(Schedulers.io()).subscribe(new Subscriber<Ad>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Log.d(TAG, "get ad json error" + e.toString());
            }

            @Override
            public void onNext(Ad ad) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(LauncherApplication.getContext());
                if (Util.nowIsBetween(ad.getTime_start(), ad.getTime_end())) {
                    sp.edit().putBoolean(Constant.IS_SHOW_AD, true).apply();
                    // TODO: 2016/6/30 0030 find a way to avoid download ad image everytime
                    OkHttpUtils.get().url(ad.getUrl()).build()
                            .execute(new FileCallBack(context.getCacheDir().getAbsolutePath(), Constant.AD_IMAGE_NAME)//
                            {
                                @Override
                                public void onError(okhttp3.Call call, Exception e, int id) {

                                    Log.d(TAG, "download ad image error");

                                }

                                @Override
                                public void onResponse(File response, int id) {

                                    Log.d(TAG, "download ad image file success");
                                }
                            });
                } else {
                    sp.edit().putBoolean(Constant.IS_SHOW_AD, false).apply();
                    String adImagePath = context.getCacheDir().getAbsolutePath() + "/" + Constant.AD_IMAGE_NAME;
                    new File(adImagePath).delete();
                }

            }
        });
    }
}
