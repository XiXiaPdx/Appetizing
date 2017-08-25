package com.xixia.appetizing.Models;

/**
 * Created by macbook on 8/24/17.
 */

public class SplashPic {
    private String id;
    private String description;
    private PicUrls urls;

    public SplashPic (){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PicUrls getUrls() {
        return urls;
    }

    public void setUrls(PicUrls urls) {
        this.urls = urls;
    }
}
