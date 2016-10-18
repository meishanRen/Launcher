package com.haishang.launcher.model;


import com.haishang.launcher.model.bean.Ad;
import com.haishang.launcher.model.bean.Ip;
import com.haishang.launcher.model.bean.RecommendVideo;
import com.haishang.launcher.model.bean.UpdateInfo;
import com.haishang.launcher.model.bean.Weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by chenrun on 2016/4/21 0021.
 */
public interface ApiService {
    public static String key = "da7ca09192b747c38e32a051c537d601";
/*
    @GET("users/{user}/repos")
    Call<List<Repo>> listRepos(@Path("user") String user);*/

    @GET("https://api.heweather.com/x3/weather/")
    Call<Weather> getWeather(@Query("cityip") String cityIp, @Query("key") String heweatherKey);

    @GET("https://api.heweather.com/x3/weather/?key=da7ca09192b747c38e32a051c537d601")
    Observable<Weather> getWeatherRx(@Query("cityip") String cityIp);

    //https 有问题
    @GET("http://api.heweather.com/x3/weather/")
    Observable<Weather> getWeatherRx(@Query("cityip") String cityIp, @Query("key") String heweatherKey);

    @GET("http://ip.chinaz.com/getip.aspx")
    Call<Ip> getIp();

    @GET("http://members.3322.org/dyndns/getip")
    Call<String> getIpx();

    @GET("http://members.3322.org/dyndns/getipsd")
    Observable<String> getIpRx();

    //测试地址
    //@GET("http://cs.interface.hifuntv.com/mgtv/FactoryIndex?Func=GetRecommendHome&PageSize=10&Version=")
    // Observable<RecommendVideo> getRecommendVideo();
    //正式地址
    @GET("http://msybfactory.interface.hifuntv.com/mgtv/FactoryIndex?Func=GetRecommendHome&PageSize=10&Version=4.7.100.285.2.MS.3.7_Release")
    Observable<RecommendVideo> getRecommendVideo();

/*//    //更新测试地址
@GET("http://www.yobbom.com/yobbom_launcher/updateinfo_test.json")
Observable<UpdateInfo> getUpdateInfo();*/

    //正式地址
    @GET("http://www.yobbom.com/yobbom_launcher/updateinfo.json")
    Observable<UpdateInfo> getUpdateInfo();

    @GET("http://www.yobbom.com/yobbom_launcher/ad.json")
    Observable<Ad> getAdInfo();
}
