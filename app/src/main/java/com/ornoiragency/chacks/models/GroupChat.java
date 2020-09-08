package com.ornoiragency.chacks.models;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep public class GroupChat  {

    String message,type,timestamp,sender;


    public GroupChat(){}

    public GroupChat(String message, String type, String timestamp, String sender) {
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
