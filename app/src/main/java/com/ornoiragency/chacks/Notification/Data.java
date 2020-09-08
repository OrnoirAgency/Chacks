package com.ornoiragency.chacks.Notification;

import androidx.annotation.Keep;

@Keep public class Data {
    private String user;
    private int icon;
    private String image;
    private String body;
    private String title;
    private String sented;
    private String notificationType;
    private String type;
    private String pId;



    public Data(String user,String image, int icon, String body, String title,String pId,
                String sented, String notificationType,String type) {
        this.user = user;
        this.icon = icon;
        this.image = image;
        this.type = type;
        this.body = body;
        this.title = title;
        this.pId = pId;
        this.sented = sented;
        this.notificationType = notificationType;
    }

    public Data(){

    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSented() {
        return sented;
    }

    public void setSented(String sented) {
        this.sented = sented;
    }
}

