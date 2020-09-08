package com.ornoiragency.chacks.models;

import androidx.annotation.Keep;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.util.ArrayList;

@Keep public class Chat  {



    private String sender;
    private String receiver;
    private String message,pdf,docFile,imageFile,
            audio,video,videoLegend,videoCover,
            messageVideoDuration,fileName,fileType,
            type,messageId,timestamp,
            audioDuration,audioFile;
    private boolean isseen;

    private ArrayList<String> mediaUrlList = new ArrayList<>();

    public Chat() {}

    public Chat(String sender, String receiver, String message, String pdf, String docFile, String imageFile,
                String audio, String video, String videoLegend, String videoCover, String messageVideoDuration,
                String fileName, String fileType, String type, String messageId,
                String timestamp, String audioDuration, String audioFile, boolean isseen, ArrayList<String> mediaUrlList) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.pdf = pdf;
        this.docFile = docFile;
        this.imageFile = imageFile;
        this.audio = audio;
        this.video = video;
        this.videoLegend = videoLegend;
        this.videoCover = videoCover;
        this.messageVideoDuration = messageVideoDuration;
        this.fileName = fileName;
        this.fileType = fileType;
        this.type = type;
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.audioDuration = audioDuration;
        this.audioFile = audioFile;
        this.isseen = isseen;
        this.mediaUrlList = mediaUrlList;
    }

    public void parseObject(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {

            String str4 = "media";
            if (dataSnapshot.child(str4).getChildrenCount() > 0) {
                for (DataSnapshot mediaSnapshot : dataSnapshot.child(str4).getChildren()) {
                    this.mediaUrlList.add(mediaSnapshot.getValue().toString());
                }
            }

        }
    }

    public ArrayList<String> getMediaUrlList() {
        return this.mediaUrlList;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getMessageVideoDuration() {
        return messageVideoDuration;
    }

    public void setMessageVideoDuration(String messageVideoDuration) {
        this.messageVideoDuration = messageVideoDuration;
    }

    public String getAudioDuration() {
        return audioDuration;
    }

    public void setAudioDuration(String audioDuration) {
        this.audioDuration = audioDuration;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    public String getVideoLegend() {
        return videoLegend;
    }

    public void setVideoLegend(String videoLegend) {
        this.videoLegend = videoLegend;
    }

    public void setMediaUrlList(ArrayList<String> mediaUrlList) {
        this.mediaUrlList = mediaUrlList;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getDocFile() {
        return docFile;
    }

    public void setDocFile(String docFile) {
        this.docFile = docFile;
    }

    public String getPdf() {
        return pdf;
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }
}
