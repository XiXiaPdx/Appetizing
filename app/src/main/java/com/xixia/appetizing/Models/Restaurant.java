package com.xixia.appetizing.Models;

import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.Category;
import com.yelp.clientlib.entities.Deal;
import com.yelp.clientlib.entities.GiftCertificate;
import com.yelp.clientlib.entities.Location;
import com.yelp.clientlib.entities.Review;

import org.parceler.Parcel;

import java.util.ArrayList;

/**
 * Created by macbook on 9/8/17.
 */

@Parcel
public class Restaurant {
    public Double mLat;
    public Double mLong;
    public String mName;

    public Restaurant (){

    }

    public Restaurant(Double mLat, Double mLong, String mName) {
        this.mLat = mLat;
        this.mLong = mLong;
        this.mName = mName;
    }

    public Double getmLat() {
        return mLat;
    }

    public Double getmLong() {
        return mLong;
    }

    public String getmName() {
        return mName;
    }
}
