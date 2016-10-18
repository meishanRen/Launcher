package com.haishang.launcher.util.cache;

import rx.Observable;

public abstract class CacheObservable<T> {
    public abstract Observable<T> getObservable(String url);
}