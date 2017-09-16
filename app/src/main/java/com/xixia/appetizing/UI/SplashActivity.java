package com.xixia.appetizing.UI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.xixia.appetizing.Constants;
import com.xixia.appetizing.Models.SplashPic;
import com.xixia.appetizing.R;
import com.xixia.appetizing.Services.NetworkChangeReceiver;
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

    private AlertDialog mAlertDialog;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(NetworkChangeReceiver.isOnline(this)) {
                unSplash30Call();
        } else  {
            displayWarningDialog();
        }
    }

//    private boolean isOnline(Context context) {
//        try {
//            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo netInfo = cm.getActiveNetworkInfo();
//            //should check null because in airplane mode it will be null
//            return (netInfo != null && netInfo.isConnectedOrConnecting());
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    public void displayWarningDialog() {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("Appetizing requires an Internet connection to start")
                .setTitle("No Internet")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                    dialog.dismiss();
                                    finish();
                            }
                        }
                );
        mAlertDialog = builder.create();
        mAlertDialog.show();
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
                            if (splashPics == null){
                                //it can happen that UnSplash fails...but the respones.body would be null and REtrofit lets it go here. Check for that.
                                displayApiCallErrorDialog();
                            } else {
//                                AppDataSingleton.setmAllPictures(splashPics);
                                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                intent.putExtra("splashPics", Parcels.wrap(splashPics));
                                startActivity(intent);
                                finish();
                                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                            }
                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            //this is displaying for SocketTimeOut , which does happen.
                            displayApiCallErrorDialog();
                        }
                    });
    }

    public void displayApiCallErrorDialog() {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("The interwebs are gummed up. Try again!")
                .setTitle("Something is stuck... ")
                .setNeutralButton("Try again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                                unSplash30Call();
                            }
                        }
                );
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

}
