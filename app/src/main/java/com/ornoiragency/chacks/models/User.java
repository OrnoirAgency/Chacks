package com.ornoiragency.chacks.models;


import androidx.annotation.Keep;

@Keep public class User  {

    String image,name,cover,uid,phone,notificationKey;
    String status, states;
    String search;


    public User(){}

    public User(String image,String cover, String name, String phone, String uid, String notificationKey, String status,String states, String search) {
        this.image = image;
        this.name = name;
        this.uid = uid;
        this.status = status;
        this.search = search;
        this.notificationKey = notificationKey;
        this.phone = phone;
        this.states = states;
        this.cover = cover;


    }



    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getStates() {
        return states;
    }

    public void setStates(String states) {
        this.states = states;
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
