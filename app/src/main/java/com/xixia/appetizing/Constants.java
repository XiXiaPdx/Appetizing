package com.xixia.appetizing;

/**
 * Created by macbook on 8/22/17.
 */

public class Constants {
    public static final String UNSPLASH_ID=BuildConfig.UNSPLASH_ID;
    public static final String UNSPLASH_URL = "https://api.unsplash.com/photos/random/";
    public static final String UNSPLASH_CLIENT_ID = "client_id";

    //Yelp
    //Yelp
    public static final String YELP_CONSUMER_KEY = BuildConfig.YELP_CONSUMER_KEY;
    public static final String YELP_CONSUMER_SECRET = BuildConfig.YELP_CONSUMER_SECRET;
    public static final String YELP_TOKEN = BuildConfig.YELP_TOKEN;
    public static final String YELP_TOKEN_SECRET = BuildConfig.YELP_TOKEN_SECRET;
    public static final String YELP_BASE_URL = "https://api.yelp" +
            ".com/v2/search?category_filter=restaurants";
    public static final String YELP_LOCATION_QUERY_PARAMETER = "location";
    public static final String YELP_FOOD_QUERY_PARAMETER = "term";

    public static final int MAX_Width = 350;
    public static final int MAX_Height = 350;
    public static final int GET_LOCATION_PERMISSION = 1;

}
