package com.xixia.appetizing.Services;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.ContentFrameLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.xixia.appetizing.R;

/**
 * Created by macbook on 8/26/17.
 */

public class SpinnerService {
    private Activity mActivity;
    private View spin;
    private FrameLayout view;


    public SpinnerService (Activity activity){
        mActivity = activity;
    }

    public void showSpinner(){
        view = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(400,400);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        spin = inflater.inflate(R.layout.spinner, null);
        view.addView(spin);
    }

    public void removeSpinner(){
        if (spin != null) {
            view.removeView(spin);
        }
    }
}
