package com.xixia.appetizing.Services;

import com.xixia.appetizing.Constants;
import com.xixia.appetizing.Models.SplashPic;

import java.util.List;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by macbook on 8/24/17.
 */

public interface UnSplashClient {
    @Headers("Accept-Version: v1")
    @GET("?query=food&count=30")
    Single<List<SplashPic>> pictures(
            @Query(Constants.UNSPLASH_CLIENT_ID) String clientID
    );
}
