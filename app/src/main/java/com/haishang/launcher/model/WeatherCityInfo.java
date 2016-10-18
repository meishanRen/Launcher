package com.haishang.launcher.model;

/**
 * Created by chenrun on 2016/4/27 0027.
 */
public class WeatherCityInfo {
    /**
     * 天气的图标 本地或者http
     */
    String weatherIcon;
    String minTmp;
    String maxTmp;
    String city;

    public WeatherCityInfo(String iconurl, String city, String minTmp, String maxTmp) {
        this.weatherIcon = iconurl;
        this.minTmp = minTmp;
        this.maxTmp = maxTmp;
        this.city = city;

    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public void setWeatherIcon(String weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getMaxTmp() {
        return maxTmp;
    }

    public void setMaxTmp(String maxTmp) {
        this.maxTmp = maxTmp;
    }

    public String getMinTmp() {
        return minTmp;
    }

    public void setMinTmp(String minTmp) {
        this.minTmp = minTmp;
    }


}
