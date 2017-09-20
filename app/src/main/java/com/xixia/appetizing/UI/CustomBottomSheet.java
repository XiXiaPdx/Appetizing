package com.xixia.appetizing.UI;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
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
    private Matrix matrix;
    private View rootView;
    private ImageView largeSplashPic;
    private float scaleFactor = 1.f;
    private Boolean vibrated = false;

    public CustomBottomSheet(Context context) {
        super();
        mContext = context;
        matrix = new Matrix();
        rootView = ((Activity)mContext).getWindow().getDecorView().findViewById(android.R.id.content);
        largeSplashPic = rootView.findViewById(R.id.largeSplashPic);
    }

    public CustomBottomSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
        Log.d("MOTION", event.toString());
        // true sends the event to OnTouch, where UI changes should happen.
       if(event.getPointerCount() == 2 ) {
           return true;
       }
       return false;
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event){
        final int action = event.getActionMasked();
        Log.d("TOUCH EVENT", event.toString());

        if(parent.isShown()) {
            switch (action) {
                case MotionEvent.ACTION_POINTER_UP:
                    Log.d("POINTER UP", "POINTER UP");
                    vibrated = false;
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d("UP", " UP");
                    vibrated = false;
                    break;
            }

            if ((event.getPointerCount() == 2 && !vibrated) && (event.getAction() != MotionEvent.ACTION_UP || event.getAction() != MotionEvent.ACTION_CANCEL || event.getAction() != MotionEvent.ACTION_POINTER_UP)) {
                child.findViewById(R.id.largeSplashPic).performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                vibrated = true;
            }
            ScaleGestureDetector mScaleDetector = new ScaleGestureDetector(parent.getContext(), new ScaleListener());
            mScaleDetector.onTouchEvent(event);
            return true;
        } else return false;
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            largeSplashPic.setScaleType(ImageView.ScaleType.MATRIX);

            //this also sets the pivot point for scaling. So can scale from touch point
            float scalePivotY = detector.getFocusY();
            float scalePivotX = detector.getFocusX();

            Log.d("SCALE PIVOT", "YYY: "+ scalePivotY +"  XXX: "+ scalePivotX);

            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(1.0f, Math.min(scaleFactor, 5.0f));
            int squareImageHeight = largeSplashPic.getMeasuredHeight();
            matrix.setScale(scaleFactor, scaleFactor, scalePivotX - (squareImageHeight/4) , scalePivotY);
            matrix.postTranslate(squareImageHeight/8, squareImageHeight/8);
            largeSplashPic.setImageMatrix(matrix);
            largeSplashPic.invalidate();
            return true;
        }
    }
}
