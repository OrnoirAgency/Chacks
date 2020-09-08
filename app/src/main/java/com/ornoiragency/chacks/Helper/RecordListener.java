package com.ornoiragency.chacks.Helper;

public interface RecordListener {
    void onStart();
    void onCancel();
    void onFinish(long time);
    void onLessTime();
}