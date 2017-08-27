package com.xixia.appetizing.Models;

import org.parceler.Parcel;

/**
 * Created by macbook on 8/24/17.
 */
@Parcel
public class PicUrls {
    private String full;
    private String regular;
    private String small;

    public PicUrls(){}


    public String getFull() {
        return full;
    }

    public void setFull(String full) {
        this.full = full;
    }

    public String getRegular() {
        return regular;
    }

    public void setRegular(String regular) {
        this.regular = regular;
    }

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }
}
