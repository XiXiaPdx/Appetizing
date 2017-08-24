package com.xixia.appetizing.Models;

/**
 * Created by macbook on 8/24/17.
 */

public class UserProfile {
    private String mUserName;
    private String mUserEmail;
    private String mUserUID;

    public UserProfile(String userName, String userEmail, String userUID){
        mUserName = userName;
        mUserEmail = userEmail;
        mUserUID = userUID;
    }

    public String getmUserName() {
        return mUserName;
    }

    public void setmUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getmUserEmail() {
        return mUserEmail;
    }

    public void setmUserEmail(String mUserEmail) {
        this.mUserEmail = mUserEmail;
    }

    public String getmUserUID() {
        return mUserUID;
    }

    public void setmUserUID(String mUserUID) {
        this.mUserUID = mUserUID;
    }
}
