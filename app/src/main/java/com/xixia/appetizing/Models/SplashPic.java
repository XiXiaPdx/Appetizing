package com.xixia.appetizing.Models;

import org.parceler.Parcel;

/**
 * Created by macbook on 8/24/17.
 */
@Parcel
public class SplashPic {
    private String id;
    private String foodDescription;
    private PicUrls urls;
    private PhotographerName user;
    private PhotographerPage links;

    public SplashPic (){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFoodDescription() {
        return foodDescription;
    }

    public void setFoodDescription(String foodDescription) {
        this.foodDescription = foodDescription;
    }

    public PicUrls getUrls() {
        return urls;
    }

    public void setUrls(PicUrls urls) {
        this.urls = urls;
    }

    public PhotographerName getUser() {
        return user;
    }

    public PhotographerPage getLinks() {
        return links;
    }
}
