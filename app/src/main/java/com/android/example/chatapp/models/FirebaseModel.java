package com.android.example.chatapp.models;

public class FirebaseModel {

    private String mName;
    private String mImage;
    private String mUid;
    private String mStatus;
    private String mPhoneNo;

    public FirebaseModel(String name, String image, String uid, String status, String phone) {
        this.mName = name;
        this.mImage = image;
        this.mUid = uid;
        this.mStatus = status;
        this.mPhoneNo = phone;
    }

    public FirebaseModel() {
    }

    public String getName() {
        return mName;
    }

    public String getImage() {
        return mImage;
    }

    public String getUid() {
        return mUid;
    }

    public String getStatus() {
        return mStatus;
    }

    public String getPhoneNo() { return mPhoneNo; }



    public void setName(String name) {
        this.mName = name;
    }

    public void setImage(String image) {
        this.mImage = image;
    }

    public void setUid(String uid) {
        this.mUid = uid;
    }

    public void setStatus(String status) {
        this.mStatus = status;
    }

    public void setPhoneNo(String mPhoneNo) {
        this.mPhoneNo = mPhoneNo;
    }
}
