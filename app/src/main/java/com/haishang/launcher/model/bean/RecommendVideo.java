package com.haishang.launcher.model.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecommendVideo implements Serializable {

    @SerializedName("src_identification")
    @Expose
    private String srcIdentification;
    @SerializedName("error")
    @Expose
    private Error error;
    @SerializedName("video_infos")
    @Expose
    private List<VideoInfo> videoInfos = new ArrayList<VideoInfo>();
    @SerializedName("video_total_count")
    @Expose
    private int videoTotalCount;
    @SerializedName("video_count")
    @Expose
    private int videoCount;
    @SerializedName("version_time")
    @Expose
    private int versionTime;
    @SerializedName("update_time")
    @Expose
    private String updateTime;

    /**
     * @return The srcIdentification
     */
    public String getSrcIdentification() {
        return srcIdentification;
    }

    /**
     * @param srcIdentification The src_identification
     */
    public void setSrcIdentification(String srcIdentification) {
        this.srcIdentification = srcIdentification;
    }

    /**
     * @return The error
     */
    public Error getError() {
        return error;
    }

    /**
     * @param error The error
     */
    public void setError(Error error) {
        this.error = error;
    }

    /**
     * @return The videoInfos
     */
    public List<VideoInfo> getVideoInfos() {
        return videoInfos;
    }

    /**
     * @param videoInfos The video_infos
     */
    public void setVideoInfos(List<VideoInfo> videoInfos) {
        this.videoInfos = videoInfos;
    }

    /**
     * @return The videoTotalCount
     */
    public int getVideoTotalCount() {
        return videoTotalCount;
    }

    /**
     * @param videoTotalCount The video_total_count
     */
    public void setVideoTotalCount(int videoTotalCount) {
        this.videoTotalCount = videoTotalCount;
    }

    /**
     * @return The videoCount
     */
    public int getVideoCount() {
        return videoCount;
    }

    /**
     * @param videoCount The video_count
     */
    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    /**
     * @return The versionTime
     */
    public int getVersionTime() {
        return versionTime;
    }

    /**
     * @param versionTime The version_time
     */
    public void setVersionTime(int versionTime) {
        this.versionTime = versionTime;
    }

    /**
     * @return The updateTime
     */
    public String getUpdateTime() {
        return updateTime;
    }

    /**
     * @param updateTime The update_time
     */
    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }


    public class Error implements Serializable {

        @SerializedName("code")
        @Expose
        private String code;
        @SerializedName("info")
        @Expose
        private String info;

        /**
         * @return The code
         */
        public String getCode() {
            return code;
        }

        /**
         * @param code The code
         */
        public void setCode(String code) {
            this.code = code;
        }

        /**
         * @return The info
         */
        public String getInfo() {
            return info;
        }

        /**
         * @param info The info
         */
        public void setInfo(String info) {
            this.info = info;
        }

    }

    public class VideoImgList implements Serializable {

        @SerializedName("video_img_url_1")
        @Expose
        private String videoImgUrl1;
        @SerializedName("video_img_url_2")
        @Expose
        private String videoImgUrl2;
        @SerializedName("video_img_url_3")
        @Expose
        private String videoImgUrl3;
        @SerializedName("video_img_url_4")
        @Expose
        private String videoImgUrl4;
        @SerializedName("video_img_url_5")
        @Expose
        private String videoImgUrl5;
        @SerializedName("video_img_url_6")
        @Expose
        private String videoImgUrl6;

        /**
         * @return The videoImgUrl1
         */
        public String getVideoImgUrl1() {
            return videoImgUrl1;
        }

        /**
         * @param videoImgUrl1 The video_img_url_1
         */
        public void setVideoImgUrl1(String videoImgUrl1) {
            this.videoImgUrl1 = videoImgUrl1;
        }

        /**
         * @return The videoImgUrl2
         */
        public String getVideoImgUrl2() {
            return videoImgUrl2;
        }

        /**
         * @param videoImgUrl2 The video_img_url_2
         */
        public void setVideoImgUrl2(String videoImgUrl2) {
            this.videoImgUrl2 = videoImgUrl2;
        }

        /**
         * @return The videoImgUrl3
         */
        public String getVideoImgUrl3() {
            return videoImgUrl3;
        }

        /**
         * @param videoImgUrl3 The video_img_url_3
         */
        public void setVideoImgUrl3(String videoImgUrl3) {
            this.videoImgUrl3 = videoImgUrl3;
        }

        /**
         * @return The videoImgUrl4
         */
        public String getVideoImgUrl4() {
            return videoImgUrl4;
        }

        /**
         * @param videoImgUrl4 The video_img_url_4
         */
        public void setVideoImgUrl4(String videoImgUrl4) {
            this.videoImgUrl4 = videoImgUrl4;
        }

        /**
         * @return The videoImgUrl5
         */
        public String getVideoImgUrl5() {
            return videoImgUrl5;
        }

        /**
         * @param videoImgUrl5 The video_img_url_5
         */
        public void setVideoImgUrl5(String videoImgUrl5) {
            this.videoImgUrl5 = videoImgUrl5;
        }

        /**
         * @return The videoImgUrl6
         */
        public String getVideoImgUrl6() {
            return videoImgUrl6;
        }

        /**
         * @param videoImgUrl6 The video_img_url_6
         */
        public void setVideoImgUrl6(String videoImgUrl6) {
            this.videoImgUrl6 = videoImgUrl6;
        }

    }

    public class VideoInfo implements Serializable {

        @SerializedName("video_id")
        @Expose
        private String videoId;
        @SerializedName("video_name")
        @Expose
        private String videoName;
        @SerializedName("video_category_name")
        @Expose
        private String videoCategoryName;
        @SerializedName("video_category_id")
        @Expose
        private String videoCategoryId;
        @SerializedName("video_kind")
        @Expose
        private List<String> videoKind = new ArrayList<String>();
        @SerializedName("video_ui_style")
        @Expose
        private String videoUiStyle;
        @SerializedName("video_img_list")
        @Expose
        private VideoImgList videoImgList;
        @SerializedName("video_actors")
        @Expose
        private List<String> videoActors = new ArrayList<String>();
        @SerializedName("video_duration")
        @Expose
        private String videoDuration;
        @SerializedName("video_director")
        @Expose
        private List<String> videoDirector = new ArrayList<String>();
        @SerializedName("video_description")
        @Expose
        private String videoDescription;
        @SerializedName("video_desc")
        @Expose
        private String videoDesc;

        /**
         * @return The videoId
         */
        public String getVideoId() {
            return videoId;
        }

        /**
         * @param videoId The video_id
         */
        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }

        /**
         * @return The videoName
         */
        public String getVideoName() {
            return videoName;
        }

        /**
         * @param videoName The video_name
         */
        public void setVideoName(String videoName) {
            this.videoName = videoName;
        }

        /**
         * @return The videoCategoryName
         */
        public String getVideoCategoryName() {
            return videoCategoryName;
        }

        /**
         * @param videoCategoryName The video_category_name
         */
        public void setVideoCategoryName(String videoCategoryName) {
            this.videoCategoryName = videoCategoryName;
        }

        /**
         * @return The videoCategoryId
         */
        public String getVideoCategoryId() {
            return videoCategoryId;
        }

        /**
         * @param videoCategoryId The video_category_id
         */
        public void setVideoCategoryId(String videoCategoryId) {
            this.videoCategoryId = videoCategoryId;
        }

        /**
         * @return The videoKind
         */
        public List<String> getVideoKind() {
            return videoKind;
        }

        /**
         * @param videoKind The video_kind
         */
        public void setVideoKind(List<String> videoKind) {
            this.videoKind = videoKind;
        }

        /**
         * @return The videoUiStyle
         */
        public String getVideoUiStyle() {
            return videoUiStyle;
        }

        /**
         * @param videoUiStyle The video_ui_style
         */
        public void setVideoUiStyle(String videoUiStyle) {
            this.videoUiStyle = videoUiStyle;
        }

        /**
         * @return The videoImgList
         */
        public VideoImgList getVideoImgList() {
            return videoImgList;
        }

        /**
         * @param videoImgList The video_img_list
         */
        public void setVideoImgList(VideoImgList videoImgList) {
            this.videoImgList = videoImgList;
        }

        /**
         * @return The videoActors
         */
        public List<String> getVideoActors() {
            return videoActors;
        }

        /**
         * @param videoActors The video_actors
         */
        public void setVideoActors(List<String> videoActors) {
            this.videoActors = videoActors;
        }

        /**
         * @return The videoDuration
         */
        public String getVideoDuration() {
            return videoDuration;
        }

        /**
         * @param videoDuration The video_duration
         */
        public void setVideoDuration(String videoDuration) {
            this.videoDuration = videoDuration;
        }

        /**
         * @return The videoDirector
         */
        public List<String> getVideoDirector() {
            return videoDirector;
        }

        /**
         * @param videoDirector The video_director
         */
        public void setVideoDirector(List<String> videoDirector) {
            this.videoDirector = videoDirector;
        }

        /**
         * @return The videoDescription
         */
        public String getVideoDescription() {
            return videoDescription;
        }

        /**
         * @param videoDescription The video_description
         */
        public void setVideoDescription(String videoDescription) {
            this.videoDescription = videoDescription;
        }

        /**
         * @return The videoDesc
         */
        public String getVideoDesc() {
            return videoDesc;
        }

        /**
         * @param videoDesc The video_desc
         */
        public void setVideoDesc(String videoDesc) {
            this.videoDesc = videoDesc;
        }

    }


}





