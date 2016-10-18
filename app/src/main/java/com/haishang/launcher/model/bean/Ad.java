package com.haishang.launcher.model.bean;

/**
 * Created by chenrun on 2016/6/29 0029.
 */
public class Ad {
    public String getTime_start() {
        return time_start;
    }

    public void setTime_start(String time_start) {
        this.time_start = time_start;
    }

    public String getTime_end() {
        return time_end;
    }

    public void setTime_end(String time_end) {
        this.time_end = time_end;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    String time_start;
    String time_end;
    String url;
}
