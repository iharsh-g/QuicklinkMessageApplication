package com.android.example.chatapp.models;

public class UserProfile {

    private String mUserName, mUserId, mPhoneNum, mEmailId;

    public UserProfile() {

    }

    public UserProfile(String mUserName, String mUserId, String num, String email) {
        this.mUserName = mUserName;
        this.mUserId = mUserId;
        this.mPhoneNum = num;
        this.mEmailId = email;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getPhoneNum() {
        return mPhoneNum;
    }

    public String getEmailId() {
        return mEmailId;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public void setPhoneNum(String mPhoneNum) {
        this.mPhoneNum = mPhoneNum;
    }

    public void setEmailId(String mEmailId) {
        this.mEmailId = mEmailId;
    }
}
