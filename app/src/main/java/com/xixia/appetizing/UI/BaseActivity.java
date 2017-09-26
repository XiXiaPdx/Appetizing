package com.xixia.appetizing.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.design.widget.TabLayout;
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
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.xixia.appetizing.Adapters.InstructionPagerAdapter;
import com.xixia.appetizing.R;
import com.xixia.appetizing.Fragments.InstructionOne;
import com.xixia.appetizing.Fragments.InstructionThree;
import com.xixia.appetizing.Fragments.InstructionTwo;
import com.xixia.appetizing.Services.NetworkChangeReceiver;

import java.util.Arrays;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    Toolbar toolbar;
    private FragmentManager mFragmentManager ;
    private InstructionPagerAdapter mIPA;
    private GestureDetector mTapListener;
    private ViewPager mViewPager;
    private BroadcastReceiver broadcastReceiver;
    private FirebaseAuth mFireBaseAuth;
    private TabLayout mDotsTab;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager=getSupportFragmentManager();
        mIPA = new InstructionPagerAdapter(mFragmentManager);
        mTapListener = new GestureDetector(this, new TapListener());
        mFireBaseAuth = FirebaseAuth.getInstance();
    }


    @Override
    protected void onResume(){
        super.onResume();

        // Listen to network changes throughout the application. This is UnRegistered in the OnPause of BaseActivity

        broadcastReceiver = new NetworkChangeReceiver();
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }



    @Override
    public void onBackPressed(){
        super.onBackPressed();
        this.finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.overflow_menu, menu);
        MenuItem instructionMenuItem = menu.findItem(R.id.action_instructions);
        MenuItem logoutMenuItem = menu.findItem(R.id.action_logout);
        MenuItem loginMenuItem = menu.findItem(R.id.action_login);

        if (getClass().getSimpleName().equals(MapsActivity.class.getSimpleName())){
            instructionMenuItem.setVisible(false);
        }
        if (mFireBaseAuth.getCurrentUser() == null){
            logoutMenuItem.setVisible(false);
        }
        if (mFireBaseAuth.getCurrentUser() != null){
            loginMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_instructions:
                mViewPager = this.findViewById(R.id.viewpager);
                mDotsTab = this.findViewById(R.id.dots_layout);

                if(!mViewPager.isShown()) {
                    mViewPager.setVisibility(View.VISIBLE);
                    mViewPager.setAlpha(0.0f);
                    mViewPager.animate().alpha(1.0f).translationY(toolbar.getHeight()).setDuration(500).setListener(null);

                    mDotsTab.setupWithViewPager(mViewPager, false);
                    mDotsTab.setVisibility(View.VISIBLE);
                    mDotsTab.setAlpha(0.0f);
                    mDotsTab.animate().alpha(1.0f).translationY(toolbar.getHeight()).setDuration(500).setListener(null);

                    if (mIPA.getCount() == 0) {
                        mIPA.addFrag(new InstructionOne(), "ONE");
                        mIPA.addFrag(new InstructionTwo(), "TWO");
                        mIPA.addFrag(new InstructionThree(), "THREE");
                    }
                    mViewPager.setAdapter(mIPA);
                    mViewPager.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            mTapListener.onTouchEvent(motionEvent);
                            return false;
                        }
                    });
                    mDotsTab.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            mTapListener.onTouchEvent(motionEvent);
                            return false;
                        }
                    });
                } else {
                    closeInstructions();
                }
                break;
            case R.id.action_logout:
                //current manual testing shows this basically restarts the app from fresh start

                AuthUI.getInstance().signOut(this);
                Intent intent = new Intent(this, SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

                break;
            case R.id.action_login:
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setTheme(R.style.FirebaseAuthUITheme)
                                .setAvailableProviders(
                                        Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                .build(),
                        1);
                break;
            case android.R.id.home:
                this.finish();
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
                closeInstructions();
            return super.onSingleTapConfirmed(e);
        }
    }

    public void closeInstructions(){
        mViewPager.animate().alpha(0.0f).translationY(0).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mViewPager.setVisibility(View.GONE);
            }
        });

        mDotsTab.animate().alpha(0.0f).translationY(0).setDuration(500).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mDotsTab.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onPause (){
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}
