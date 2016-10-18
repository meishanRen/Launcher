package com.haishang.launcher.ui;

import com.haishang.launcher.model.WeatherCityInfo;
import com.haishang.launcher.model.bean.RecommendVideo;
import com.haishang.launcher.model.bean.UpdateInfo;
import com.haishang.launcher.model.bean.Weather;
import com.haishang.launcher.presenter.WeatherPresenter;

/**
 * Created by chenrun on 2016/4/25 0025.
 */
public interface MainView extends BaseView<WeatherPresenter> {

    void setWeather(Weather weather);

    void setWeather(String weatherInfo);

    void setWeather(WeatherCityInfo weatherCityInfo);

    void setRecommendVideo(RecommendVideo recommendVideo);

    void updateApp(UpdateInfo updateInfo);
}
