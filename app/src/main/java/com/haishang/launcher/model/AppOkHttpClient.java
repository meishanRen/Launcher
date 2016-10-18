package com.haishang.launcher.model;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;


public class AppOkHttpClient extends OkHttpClient {

    private final String HTTP_CACHE_FILENAME = "HttpCache";

 /*   public AppOkHttpClient() {
        File httpCacheDirectory = new File(LauncherApplication.getContext().getCacheDir().getAbsolutePath(),
                HTTP_CACHE_FILENAME);
        Cache cache = new Cache(httpCacheDirectory, 10 * 1024);
        newBuilder().cache(cache);
        this.networkInterceptors().add(REWRITE_CACHE_CONTROL_INTERCEPTOR);
    }*/

    public static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", String.format("max-age=%d", 1000 * 60 * 60 * 24 * 8))
                    .build();
        }
    };
}