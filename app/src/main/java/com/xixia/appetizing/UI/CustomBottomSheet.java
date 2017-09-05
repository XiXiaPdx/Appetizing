package com.xixia.appetizing.UI;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import com.xixia.appetizing.R;

/**
 * Created by macbook on 9/1/17.
 */

public class CustomBottomSheet<V extends View> extends BottomSheetBehavior<V> {
    private Context mContext;
    private static ScaleGestureDetector mScaleDetector;
    private  static Matrix matrix;
    private static View rootView;
    private static Boolean settingOrigin;
    private static ImageView largeSplashPic;
    private static float scaleFactor = 1.f;
    private float initialTouchY;
    private float initialTouchX;
    public CustomBottomSheet(Context context) {
        super();
        mContext = context;
        mScaleDetector = new ScaleGestureDetector(mContext, new ScaleListener());
        matrix = new Matrix();
        rootView = ((Activity)mContext).getWindow().getDecorView().findViewById(android.R.id.content);
        largeSplashPic = rootView.findViewById(R.id.largeSplashPic);
        settingOrigin = true;
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
       return false;
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event){
        final int action = event.getActionMasked();
        Log.d("TOUCH EVENT", event.toString());

        if(parent.isShown()) {
            if(action == MotionEvent.ACTION_POINTER_UP){
                largeSplashPic.setScaleType(ImageView.ScaleType.CENTER);
                settingOrigin = true;
            }


            if(action == MotionEvent.ACTION_MOVE && !mScaleDetector.isInProgress()){
                Log.d("MOVING", "YYY: " + mScaleDetector.getFocusY()  + "  XXX: " + mScaleDetector.getFocusX() );

                final float newOriginY = initialTouchY - (mScaleDetector.getFocusY() - initialTouchY);

                final float newOriginX = initialTouchX - (mScaleDetector.getFocusX() - initialTouchX);
                
                matrix.setTranslate(newOriginX, newOriginY);
                largeSplashPic.setImageMatrix(matrix);
            }

            mScaleDetector.onTouchEvent(event);
            return true;
        } else return false;
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            largeSplashPic.setScaleType(ImageView.ScaleType.MATRIX);
            if(settingOrigin) {
                //negative because what is under the finger IS NOT THE ORIGIN. But should be relative to it.
                initialTouchY = detector.getFocusY();
                initialTouchX = detector.getFocusX();
                settingOrigin = false;
            }
            Log.d("ORIGIN", "YYY: "+ initialTouchY +"  XXX: "+ initialTouchX);
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
            matrix.setScale(scaleFactor, scaleFactor);
            largeSplashPic.setImageMatrix(matrix);
            return true;
        }
    }
}
