package com.xixia.appetizing.Models;

import org.parceler.Parcel;

/**
 * Created by macbook on 8/26/17.
 */
@Parcel
public class PhotographerName {
    private String first_name;
    private String last_name;
    private String name;

    public PhotographerName (){}

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getName() {
        return name;
    }
}
