package com.xixia.appetizing.Models;

import org.parceler.Parcel;

/**
 * Created by macbook on 8/24/17.
 */
@Parcel
public class SplashPic {
    private String id;
    private String description;
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

    public PhotographerName getUser() {
        return user;
    }

    public PhotographerPage getLinks() {
        return links;
    }
}
