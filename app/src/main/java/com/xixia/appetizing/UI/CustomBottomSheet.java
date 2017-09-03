package com.xixia.appetizing.UI;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import com.xixia.appetizing.R;

import butterknife.BindView;

/**
 * Created by macbook on 9/1/17.
 */

public class CustomBottomSheet<V extends View> extends BottomSheetBehavior<V> {
    private Context mContext;
    private static ScaleGestureDetector mScaleDetector;
    private  static Matrix matrix;
    private static View rootView;
    private static ImageView largeSplashPic;
    private static Boolean isScaling;
    private float focusY;
    private float focusX;
    public CustomBottomSheet(Context context) {
        super();
        mContext = context;
        mScaleDetector = new ScaleGestureDetector(mContext, new ScaleListener());
        matrix = new Matrix();
        rootView = ((Activity)mContext).getWindow().getDecorView().findViewById(android.R.id.content);
        largeSplashPic = rootView.findViewById(R.id.largeSplashPic);
        isScaling = false;
    }

    public CustomBottomSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
        Log.d("MOTION", event.toString());
        // true sends the event to OnTouch, where UI changes should happen.
       if(event.getPointerCount() == 2) {
           return true;
       }
       if(isScaling){
           if(event.getAction() == MotionEvent.ACTION_UP) {
               largeSplashPic.setScaleType(ImageView.ScaleType.CENTER);
               isScaling = false;
           }
       }
       return false;
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event){
        final int action = event.getActionMasked();

        if(parent.isShown()) {
            if(action == MotionEvent.ACTION_MOVE && !mScaleDetector.isInProgress()){
                Log.d("MOVING", "YYY: " + mScaleDetector.getFocusY() + "  XXX: " + mScaleDetector.getFocusX());
                matrix.setTranslate(mScaleDetector.getFocusX(), mScaleDetector.getFocusY());
                largeSplashPic.setImageMatrix(matrix);
            }

            mScaleDetector.onTouchEvent(event);
            return true;
        } else return false;
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float scaleFactor = 1.f;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            isScaling = true;
            largeSplashPic.setScaleType(ImageView.ScaleType.MATRIX);
            focusY = detector.getFocusY();
            focusX = detector.getFocusX();
            Log.d("SCALING", "YYY: "+focusY +"  XXX: "+focusX);
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.01f, Math.min(scaleFactor, 5.0f));
            matrix.setScale(scaleFactor, scaleFactor);

            largeSplashPic.setImageMatrix(matrix);
            return true;
        }
    }
}
