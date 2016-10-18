package com.haishang.launcher.util.cache;

import android.util.Log;

import com.haishang.launcher.LauncherApplication;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by chenrun on 2016/5/15 0015.
 */
public class DiskCacheObservable<T> extends CacheObservable<T> {
    @Override
    public Observable<T> getObservable(final String url) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                Log.d("DiskCacheObservable", "read data from disk");
                //读取数据从disk
                ACache aCache = ACache.get(LauncherApplication.getContext());
                String urltmp = url;
                T t = (T) aCache.getAsObject(url);
                subscriber.onNext(t);
                subscriber.onCompleted();
               /* if (t != null) {
                    //当数据缓存过期的时候应该返回什么
                    subscriber.onNext(t);
                } else {
                    subscriber.onCompleted();
                }*/
            }
        });
    }

    /**
     * 缓存到本地
     *
     * @param url
     * @param t
     */
    public void putData(String url, final T t) {
        Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                ACache aCache = ACache.get(LauncherApplication.getContext());
                aCache.put("", t.toString());

            }
        });
    }

}
