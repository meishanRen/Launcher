package com.haishang.launcher.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.haishang.launcher.LauncherApplication;
import com.haishang.launcher.model.ApiService;
import com.haishang.launcher.model.WeatherCityInfo;
import com.haishang.launcher.model.WeatherModel;
import com.haishang.launcher.model.bean.RecommendVideo;
import com.haishang.launcher.model.bean.UpdateInfo;
import com.haishang.launcher.model.bean.Weather;
import com.haishang.launcher.ui.activity.MainActivity;
import com.haishang.launcher.ui.activity.MainSecActivity;
import com.haishang.launcher.util.AndroidUtil;
import com.haishang.launcher.util.StringConverterFactory;
import com.haishang.launcher.util.cache.ACache;
import com.haishang.launcher.util.cache.DiskCacheObservable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by chenrun on 2016/4/25 0025.
 *
 * presenter 获得数据应该是持有model的引用 然后 调用model的方法给一个回调获得数据 所以说网络请求应该是在
 * model中进行的
 */
public class WeatherPresenter implements BasePresenter {

    private final String TAG = getClass().getSimpleName();
    private MainActivity weatherView;
    private MainSecActivity weathersecView;

    public WeatherPresenter(@NonNull WeatherModel model, @NonNull MainActivity weatherView) {
        this.weatherView = weatherView;
        weatherView.setPresenter(this);
    }
    public WeatherPresenter(@NonNull WeatherModel model, @NonNull MainSecActivity weatherView) {
        this.weathersecView = weatherView;
        weatherView.setPresenter(this);
    }

    @Override
    public void start() {
        loadUpdateInfo();
        loadRecommendVideo();
        loadWeatherData();

    }

    public void loadUpdateInfo() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ip.chinaz.com/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ApiService service = retrofit.create(ApiService.class);
        final Observable<UpdateInfo> observable = service.getUpdateInfo();
       /* observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<UpdateInfo>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(UpdateInfo updateInfo) {
                Log.d(TAG, "get updateinfo =" + updateInfo.getVersioncode());

            }
        });*/
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<UpdateInfo>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(UpdateInfo updateInfo) {
                int currentVersionCode = AndroidUtil.getAppVersionCode(LauncherApplication.getContext());
                int remoteVersionCode = updateInfo.getVersioncode();
                if (currentVersionCode < remoteVersionCode) {
                    weatherView.updateApp(updateInfo);
                }
            }
        });
    }

    public void loadWeatherData() {

        /**
         * 使用Rxjava
         * todo 每天请求一次 12点 不关机的情况  一天开关机很多次程序每次启动请求一次
         * 定义缓存 时间一天 如果这个时候没开机的话 怎么办 到每天的12点缓存失效 天气预报
         */
        final String cityName;
        Log.d(TAG, "loadWeatherData");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ip.chinaz.com/")
                .addConverterFactory(StringConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ApiService service = retrofit.create(ApiService.class);
        final Observable<String> observable = service.getIpRx();

   /*     service.getIpRx().flatMap(new Func1<String, Observable<Weather>>() {

            @Override
            public Observable<Weather> call(String s) {
                //ip地址末尾有一个\n,不去掉的话会返回北京的数据
                //todo 满足IP的格式
                if (TextUtils.isEmpty(s)) {
                    //   return Observable.error(new Throwable("can not get right ip"));
                    return service.getWeatherRx(s.trim(), "da7ca09192b747c38e32a051c537d601");
                } else {
                    Log.d(TAG, "ip====" + s.trim());
                    return service.getWeatherRx(s.trim(), "da7ca09192b747c38e32a051c537d601");
                }
            }
        }).flatMap(new Func1<Weather, Observable<Weather.DailyForecast>>() {
            @Override
            public Observable<Weather.DailyForecast> call(Weather weather) {
                 *//*   enum Status {
                    *//**//**接口正常*//**//*
                    OK,

                    *//**//**错误的用户 key *//**//*
                    INVALID_KEY,
                    *//**//** unknown city *//**//*
                    UNKOWN_CITY,
                    *//**//**超过访问次数*//**//*
                    NO_MORE_REQUESTS,
                    *//**//**服务无响应或超时*//**//*
                    ANR,
                    *//**//**没有访问权限 *//**//*
                    PERMISSION_DENIED,
*//**//*
                    ok	接口正常
                    invalid key	错误的用户 key
                    unknown city	未知城市
                    no more requests	超过访问次数
                    anr	服务无响应或超时
                    permission denied	没有访问权限*//**//*
                    }*//*
                Log.d(TAG, "fffffffffffffffffffffffffffffffffffffffffffffff");
                List<Weather.DailyForecast> list = weather.getHeWeatherDataService30().get(0).getDailyForecast();
                if (weather.getHeWeatherDataService30().get(0).getStatus().equals("ok")) {
                    String city = weather.getHeWeatherDataService30().get(0).getBasic().getCity();
                    //  weatherView.setWeather(city);
                    return Observable.from(list);
                } else {
                    return Observable.from(list);
                    // return Observable.error(new Throwable(weather.getHeWeatherDataService30().get(0).getStatus()));
                }
            }
        }).filter(new Func1<Weather.DailyForecast, Boolean>() {
            @Override
            public Boolean call(Weather.DailyForecast dailyForecast) {
                Calendar mCalendar = Calendar.getInstance();
                mCalendar.setTimeInMillis(System.currentTimeMillis());
                SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
                String date = dataFormat.format(mCalendar.getTime());
                //没有对应到时间
                return dailyForecast.getDate().equals(date);
            }
        }).map(new Func1<Weather.DailyForecast, String>() {
            @Override
            public String call(Weather.DailyForecast dailyForecast) {
                String str = LauncherApplication.getContext().getResources().getString(R.string.weather_info);
                String min = dailyForecast.getTmp().getMin();
                String max = dailyForecast.getTmp().getMax();
                String city = "";

                String tmp = String.format(str, city, min, max);
                Log.d(TAG, "tmp" + tmp);
                return tmp;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String weatherInfo) {
                Log.d(TAG, "weatherInfo" + weatherInfo);
                weatherView.setWeather(weatherInfo);
            }
        });*/

        service.getIpRx().flatMap(new Func1<String, Observable<Weather>>() {

            @Override
            public Observable<Weather> call(String s) {
                //ip地址末尾有一个\n,不去掉的话会返回北京的数据
                //todo 满足IP的格式
                if (TextUtils.isEmpty(s)) {
                    //   return Observable.error(new Throwable("can not get right ip"));
                    return service.getWeatherRx(s.trim(), "da7ca09192b747c38e32a051c537d601");
                } else {
                    Log.d(TAG, "ip====" + s.trim());
                    return service.getWeatherRx(s.trim(), "da7ca09192b747c38e32a051c537d601");
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Weather>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(Weather weather) {
    /*   enum Status {
                    *//**接口正常*//*
                    OK,

                    *//**错误的用户 key *//*
                    INVALID_KEY,
                    *//** unknown city *//*
                    UNKOWN_CITY,
                    *//**超过访问次数*//*
                    NO_MORE_REQUESTS,
                    *//**服务无响应或超时*//*
                    ANR,
                    *//**没有访问权限 *//*
                    PERMISSION_DENIED,
*//*
                    ok	接口正常
                    invalid key	错误的用户 key
                    unknown city	未知城市
                    no more requests	超过访问次数
                    anr	服务无响应或超时
                    permission denied	没有访问权限*//*
                    }*/
                Log.d(TAG, "fffffffffffffffffffffffffffffffffffffffffffffff");
                if (weather.getHeWeatherDataService30().get(0).getStatus().equals("ok")) {

                    List<Weather.DailyForecast> list = weather.getHeWeatherDataService30().get(0).getDailyForecast();
                    Calendar mCalendar = Calendar.getInstance();
                    mCalendar.setTimeInMillis(System.currentTimeMillis());
                    if (System.currentTimeMillis() == 0) {
                        Log.d(TAG, "时间没有同步成功");
                    }
                    // TODO: 2016/5/15 0015 得不到今天的时间 没得办法获得到天气情况
                    SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String date = dataFormat.format(mCalendar.getTime());
                    String city = weather.getHeWeatherDataService30().get(0).getBasic().getCity();
                    //  weatherView.setWeather(city);
           /*         for (Weather.DailyForecast dailyForecast : list) {
                        if (dailyForecast.getDate().equals(date)) {
                            String condCode = dailyForecast.getCond().getCodeD();
                            String condCodeUrl = "http://files.heweather.com/cond_icon/" + condCode + ".png";
                            String min = dailyForecast.getTmp().getMin();
                            String max = dailyForecast.getTmp().getMax();
                            WeatherCityInfo weatherCityInfo = new WeatherCityInfo(condCodeUrl, city, min, max);
                            weatherView.setWeather(weatherCityInfo);
                            break;
                        }
                    }*/
                    //返回的json数据第一个就是今天的天气
                    Weather.DailyForecast dailyForecast = list.get(0);
                    String condCode = dailyForecast.getCond().getCodeD();
                    String condCodeUrl = "http://files.heweather.com/cond_icon/" + condCode + ".png";
                    String min = dailyForecast.getTmp().getMin();
                    String max = dailyForecast.getTmp().getMax();
                    WeatherCityInfo weatherCityInfo = new WeatherCityInfo(condCodeUrl, city, min, max);
                    weatherView.setWeather(weatherCityInfo);


                } else {
                    onError(new Throwable("weather api error" + weather.getHeWeatherDataService30().get(0).getStatus()));
                }

            }
        });
    }

    public void loadRecommendVideo() {
        final String url = "http://msybfactory.interface.hifuntv.com/mgtv/FactoryIndex?Func=GetRecommendHome&PageSize=10&Version=4.7.100.285.2.MS.3.7_Release";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://cs.interface.hifuntv.com/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final ApiService service = retrofit.create(ApiService.class);
        final Observable<RecommendVideo> observable = service.getRecommendVideo();


        Observable netObservable = observable.doOnNext(new Action1<RecommendVideo>() {
            @Override
            public void call(RecommendVideo recommendVideo) {
                Log.d(TAG, "save to disk");
                //TODO  计算保存的时间
                //得到当前的是否正确
                recommendVideo.getUpdateTime();
                String mFormat = "yyyyMMddhhmmss";
                SimpleDateFormat dataFormat = new SimpleDateFormat(mFormat);
                try {
                    Calendar calendar = Calendar.getInstance();
                    Date begin = dataFormat.parse(recommendVideo.getUpdateTime());
                    calendar.setTime(begin);
                    calendar.add(Calendar.DATE, 7);//7七天后的日期
                    Date dueDate = calendar.getTime();
                    Log.d(TAG, dueDate.toString());
                    Calendar currentCalendar = Calendar.getInstance();
                    //假如得到的是没有同步到的时间 saveTime就会很大
                    Date currentDate = currentCalendar.getTime();
                    if (currentDate.getTime() > 1000 * 60) {
                        long l = dueDate.getTime() - currentDate.getTime();
                        long saveTime = l / (1000);
                        Log.d(TAG, "saveTime=" + saveTime);
                        ACache.get(LauncherApplication.getContext()).put(url, recommendVideo, (int) saveTime);
                    } else {
                        Log.d(TAG, "没有同步到时间");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        Observable diskCacheObservable = new DiskCacheObservable<RecommendVideo>().getObservable(url);

        Observable.concat(diskCacheObservable, netObservable).first(new Func1<RecommendVideo, Boolean>() {
            @Override
            public Boolean call(RecommendVideo recommendVideo) {
                Log.d(TAG, "reconmeng video is null or not");
                return recommendVideo != null;
                //  return false;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<RecommendVideo>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "on sub next ");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, e.toString());
                e.printStackTrace();
            }

            @Override
            public void onNext(RecommendVideo recommendVideo) {
                if (recommendVideo != null) {
                    Log.d(TAG, "recommadvido is not null");
                    weatherView.setRecommendVideo(recommendVideo);
                } else {
                    Log.d(TAG, "recommavidio is null");
                }
            }
        });
    }
}