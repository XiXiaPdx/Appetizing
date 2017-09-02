package com.xixia.appetizing.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.xixia.appetizing.R;
import com.xixia.appetizing.Services.AppDataSingleton;

public abstract class BaseActivity extends AppCompatActivity {

    Toolbar toolbar;

    //this will get the String name for the activity that is active
//    String activityName = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                if(getClass() != InstructionsActivity.class) {
                    Intent intent = new Intent(getBaseContext(), InstructionsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                }
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
}
