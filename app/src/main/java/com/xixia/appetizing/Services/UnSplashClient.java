package com.xixia.appetizing.Services;

import com.xixia.appetizing.Constants;
import com.xixia.appetizing.Models.SplashPic;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by macbook on 8/24/17.
 */

public interface UnSplashClient {
    @GET("?query=food&count=30")
    Call<List<SplashPic>> pictures(
            @Query(Constants.UNSPLASH_CLIENT_ID) String clientID
    );
}
