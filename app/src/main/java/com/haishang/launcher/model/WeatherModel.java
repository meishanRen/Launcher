package com.haishang.launcher.model;

import com.haishang.launcher.model.bean.Weather;

/**
 * Created by chenrun on 2016/4/25 0025.
 */
public class WeatherModel {

    public final static String TAG = "WeatherModel";

    public static Weather getWeather() {

        return new Weather();
    }
}
