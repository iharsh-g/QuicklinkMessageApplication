package com.android.example.chatapp.models;

public class Messages {

    String mMessage;
    String mSenderId;
    long mTimestamp;
    String mCurrentTime;
    String mToken;
    private boolean isImage;

    public Messages() {

    }

    public Messages(String message, String senderId, long timestamp, String currentTime, String token, boolean image) {
        this.mMessage = message;
        this.mSenderId = senderId;
        this.mTimestamp = timestamp;
        this.mCurrentTime = currentTime;
        this.mToken = token;
        this.isImage = image;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public void setSenderId(String senderId) {
        this.mSenderId = senderId;
    }

    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    public void setCurrentTime(String currentTime) {
        this.mCurrentTime = currentTime;
    }

    public void setToken(String mToken) {
        this.mToken = mToken;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getSenderId() {
        return mSenderId;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getCurrentTime() {
        return mCurrentTime;
    }

    public String getToken() {
        return mToken;
    }

    public boolean isImage() {
        return isImage;
    }
}
