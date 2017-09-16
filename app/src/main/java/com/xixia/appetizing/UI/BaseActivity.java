package com.xixia.appetizing.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.xixia.appetizing.Adapters.InstructionPagerAdapter;
import com.xixia.appetizing.R;
import com.xixia.appetizing.Fragments.InstructionOne;
import com.xixia.appetizing.Fragments.InstructionThree;
import com.xixia.appetizing.Fragments.InstructionTwo;

public abstract class BaseActivity extends AppCompatActivity {

    Toolbar toolbar;
    private FragmentManager mFragmentManager ;
    private InstructionPagerAdapter mIPA;
    private GestureDetector mTapListener;
    private ViewPager mViewPager;
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("BASE ACTIVITY", "ACTIVATED");
        mFragmentManager=getSupportFragmentManager();
        mIPA = new InstructionPagerAdapter(mFragmentManager);
        mTapListener = new GestureDetector(this, new TapListener());
        registerReceiver(broadcastReceiver, new IntentFilter("INTERNET_LOST"));
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            displayWarningDialog();
            unregisterReceiver(broadcastReceiver);
        }
    };

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.overflow_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_instructions);
        if (getClass().getSimpleName().equals(MapsActivity.class.getSimpleName())){
            menuItem.setVisible(false);
        }
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
                break;
            case R.id.action_logout:
                //current manual testing shows this basically restarts the app from fresh start
                AuthUI.getInstance().signOut(this);
                Intent intent = new Intent(getBaseContext(), SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case android.R.id.home:
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

    public void displayWarningDialog() {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("Many Appetizing capabilities will not work without internet")
                .setTitle("No Internet")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        }
                );
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("BROADCAST", "UN_REGISTERED");
        unregisterReceiver(broadcastReceiver);
    }
}
