package com.xixia.appetizing.Services;

import com.xixia.appetizing.Models.SplashPic;

import java.util.List;

/**
 * Created by macbook on 8/26/17.
 */

public class AppDataSingleton {
    private static final AppDataSingleton ourInstance = new AppDataSingleton();
    public static AppDataSingleton getInstance() {
        return ourInstance;
    }

    public static List<SplashPic> mAllPictures;


    private AppDataSingleton() {
    }

    public static List<SplashPic> getmAllPictures() {
        return mAllPictures;
    }

    public static void setmAllPictures(List<SplashPic> mAllPictures) {
        AppDataSingleton.mAllPictures = mAllPictures;
    }
}
