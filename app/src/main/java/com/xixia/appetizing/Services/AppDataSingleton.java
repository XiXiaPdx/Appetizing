package com.xixia.appetizing.Services;

import com.xixia.appetizing.Models.SplashPic;
import com.xixia.appetizing.Models.DescribedPicture;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by macbook on 8/26/17.
 */

public class AppDataSingleton {
    private static final AppDataSingleton ourInstance = new AppDataSingleton();
    public static AppDataSingleton getInstance() {
        return ourInstance;
    }

    private static List<SplashPic> mAllPictures;
    private static List<DescribedPicture> mDescribedPictures = new ArrayList<>();


    private AppDataSingleton() {
    }

    public static List<SplashPic> getmAllPictures() {
        return mAllPictures;
    }

    public static void setmAllPictures(List<SplashPic> mAllPictures) {
        AppDataSingleton.mAllPictures = mAllPictures;
    }


    public static void addToDescribedPictures(DescribedPicture pic){
        AppDataSingleton.mDescribedPictures.size();
        pic.getFoodDescription();
        AppDataSingleton.mDescribedPictures.add(pic);
    }

    public static List<DescribedPicture> getmDescribedPictures() {
        return mDescribedPictures;
    }

    public static void setmDescribedPictures(List<DescribedPicture> mDescribedPictures) {
        AppDataSingleton.mDescribedPictures = mDescribedPictures;
    }

    public static void clearmDescribedPictures() {
        AppDataSingleton.mDescribedPictures = new ArrayList<>();
    }

    public static void clearAppData(){
        clearmDescribedPictures();
        AppDataSingleton.mAllPictures = new ArrayList<>();
    }
}
