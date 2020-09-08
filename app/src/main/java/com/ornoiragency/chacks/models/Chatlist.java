package com.ornoiragency.chacks.models;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep public class Chatlist {
    public String id;

    public Chatlist(String id) {
        this.id = id;
    }

    public Chatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
