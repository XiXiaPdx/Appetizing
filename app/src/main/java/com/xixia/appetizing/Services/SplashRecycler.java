package com.xixia.appetizing.Services;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by macbook on 8/26/17.
 */

public class SplashRecycler extends RecyclerView {
    public SplashRecycler(Context context) {
        super(context);
    }

    public SplashRecycler(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SplashRecycler(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        velocityY *=.4;
        return super.fling(velocityX, velocityY);
    }
}
