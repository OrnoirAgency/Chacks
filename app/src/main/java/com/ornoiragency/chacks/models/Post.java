package com.ornoiragency.chacks.models;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep public class Post {

     String uid, pId, pImage,type ,pTime,pText,pVideoDuration,
             pDescr, uName,uImage,pComments,search,pVideo,videoCover,artist,title,
    pAudio,pAudioCover,audioDesc;

     long pAudioDuration;

    public Post(){}

    public Post(String uid, String pId,String type,String pVideoDuration,String audioDesc,
                String pText,String search,String pVideo,String pAudio,long pAudioDuration,
                String pImage, String pTime, String pDescr,String pAudioCover,String videoCover,
                String uName, String uImage, String pComments,String artist,String title) {
        this.uid = uid;
        this.pId = pId;
        this.pImage = pImage;
        this.pTime = pTime;
        this.pDescr = pDescr;
        this.uName = uName;
        this.uImage = uImage;
        this.videoCover = videoCover;
        this.type = type;
        this.pText = pText;
        this.artist = artist;
        this.title = title;
        this.pComments = pComments;
        this.search = search;
        this.pVideo = pVideo;
        this.pVideoDuration = pVideoDuration;
        this.pAudio = pAudio;
        this.pAudioDuration = pAudioDuration;
        this.pAudioCover = pAudioCover;
        this.audioDesc = audioDesc;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    public String getAudioDesc() {
        return audioDesc;
    }

    public void setAudioDesc(String audioDesc) {
        this.audioDesc = audioDesc;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getpAudioCover() {
        return pAudioCover;
    }

    public void setpAudioCover(String pAudioCover) {
        this.pAudioCover = pAudioCover;
    }

    public String getpAudio() {
        return pAudio;
    }

    public void setpAudio(String pAudio) {
        this.pAudio = pAudio;
    }

    public long getpAudioDuration() {
        return pAudioDuration;
    }

    public void setpAudioDuration(long pAudioDuration) {
        this.pAudioDuration = pAudioDuration;
    }

    public String getpVideoDuration() {
        return pVideoDuration;
    }

    public void setpVideoDuration(String pVideoDuration) {
        this.pVideoDuration = pVideoDuration;
    }

    public String getpVideo() {
        return pVideo;
    }

    public void setpVideo(String pVideo) {
        this.pVideo = pVideo;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getpText() {
        return pText;
    }

    public void setpText(String pText) {
        this.pText = pText;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpImage() {
        return pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getpDescr() {
        return pDescr;
    }

    public void setpDescr(String pDescr) {
        this.pDescr = pDescr;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuImage() {
        return uImage;
    }

    public void setuImage(String uImage) {
        this.uImage = uImage;
    }

    public String getpComments() {
        return pComments;
    }

    public void setpComments(String pComments) {
        this.pComments = pComments;
    }
}
