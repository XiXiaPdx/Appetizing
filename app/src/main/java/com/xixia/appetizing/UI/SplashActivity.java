package com.xixia.appetizing.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.xixia.appetizing.Adapters.SplashPicsAdapter;
import com.xixia.appetizing.Constants;
import com.xixia.appetizing.Models.SplashPic;
import com.xixia.appetizing.R;
import com.xixia.appetizing.Services.AppDataSingleton;
import com.xixia.appetizing.Services.UnSplashClient;
import com.xixia.appetizing.Services.UnSplashServiceGenerator;

import org.parceler.Parcels;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by macbook on 8/26/17.
 */

public class SplashActivity extends BaseActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unSplash30Call();
    }


    public void unSplash30Call(){
        UnSplashClient client = UnSplashServiceGenerator.createService(UnSplashClient.class);
        Single<List<SplashPic>> call = client.pictures(Constants.UNSPLASH_ID);
        call
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<SplashPic>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull List<SplashPic> splashPics) {
                        AppDataSingleton.setmAllPictures(splashPics);
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.d("ERROR", e.toString());
                    }
                });
    }
}
