package com.xixia.appetizing.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import com.firebase.ui.auth.AuthUI;
import com.xixia.appetizing.Adapters.InstructionPagerAdapter;
import com.xixia.appetizing.R;
import com.xixia.appetizing.Services.AppDataSingleton;
import com.xixia.appetizing.UI.InstructionFragments.InstructionOne;
import com.xixia.appetizing.UI.InstructionFragments.InstructionThree;
import com.xixia.appetizing.UI.InstructionFragments.InstructionTwo;

public abstract class BaseActivity extends AppCompatActivity {

    Toolbar toolbar;
    private FragmentManager mFragmentManager ;
    private InstructionPagerAdapter mIPA;
    private GestureDetector mTapListener;
    private ViewPager mViewPager;

    //this will get the String name for the activity that is active
//    String activityName = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager=getSupportFragmentManager();
        mIPA = new InstructionPagerAdapter(mFragmentManager);
        mTapListener = new GestureDetector(this, new TapListener());
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        ifMainClearAppData();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_instructions:

                mViewPager = this.findViewById(R.id.viewpager);
                mViewPager.setVisibility(View.VISIBLE);
                mViewPager.setAlpha(0.0f);
                mViewPager.animate().alpha(1.0f).translationY(toolbar.getHeight()).setDuration(500).setListener(null);

                mIPA.addFrag(new InstructionOne(), "ONE");
                mIPA.addFrag(new InstructionTwo(), "TWO");
                mIPA.addFrag(new InstructionThree(), "THREE");
                mViewPager.setAdapter(mIPA);
                mViewPager.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        mTapListener.onTouchEvent(motionEvent);
                        return false;
                    }
                });


//                FrameLayout mInstructionFrame = this.findViewById(R.id.instructionFragmentFrame);

//                mInstructionFrame.setVisibility(View.VISIBLE);
//                mInstructionFrame.setAlpha(0.0f);
//                mInstructionFrame.animate().alpha(1.0f);
//                mInstructionFrame.animate().setDuration(700);
//
//                InstructionFragment fragment = InstructionFragment.newInstance();
//                Fade enterFade = new Fade ();
//                enterFade.setDuration(500);
//                fragment.setEnterTransition(enterFade);
//                // set transition fade for exit of fragment
//                Fade exitFade = new Fade();
//                exitFade.setDuration(200);
//                fragment.setExitTransition(exitFade);
//
//                mFragmentManager.beginTransaction().replace(R.id.instructionFragmentFrame, fragment).addToBackStack(null).commit();

                //load instruction fragment in
//                    Intent intent = new Intent (this, MapsActivity.class);
//                    startActivity(intent);
//                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);


                break;
            case R.id.action_logout:
                AppDataSingleton.clearAppData();
                //current manual testing shows this basically restarts the app from fresh start
                Intent intent = new Intent(getBaseContext(), SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                AuthUI.getInstance().signOut(this);
                break;
            case android.R.id.home:
                ifMainClearAppData();
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    protected boolean useToolbar() {
        return true;
    }

    public void ifMainClearAppData(){
        if (getClass() == MainActivity.class){
            AppDataSingleton.clearmDescribedPictures();
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        View view = getLayoutInflater().inflate(layoutResID, null);
        configureToolbar(view);
        super.setContentView(view);
    }

    private void configureToolbar(View view) {
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            if (useToolbar()) {
                String activityName = getClass().getSimpleName();

                setSupportActionBar(toolbar);
                if (activityName.equals(MainActivity.class.getSimpleName())){
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                } else {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            } else {
                toolbar.setVisibility(View.GONE);
            }
        }
    }

    private class TapListener
            extends GestureDetector.SimpleOnGestureListener {


        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mViewPager.animate().alpha(0.0f).translationY(0).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mViewPager.setVisibility(View.GONE);
                }
            });
            return super.onSingleTapConfirmed(e);
        }
    }
}
