package com.haishang.launcher.model.bean;

/**
 * Created by chenrun on 2016/5/10 0010.
 */
public class UpdateInfo {
    public int getVersioncode() {
        return versioncode;
    }

    public void setVersioncode(int versioncode) {
        this.versioncode = versioncode;
    }

    int versioncode;

    public String getUpdate_message() {
        return update_message;
    }

    public void setUpdate_message(String update_message) {
        this.update_message = update_message;
    }

    String update_message;

    public String getApk_url() {
        return apk_url;
    }

    public void setApk_url(String apk_url) {
        this.apk_url = apk_url;
    }

    String apk_url;
}
