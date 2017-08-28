package com.xixia.appetizing.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.xixia.appetizing.R;
import com.xixia.appetizing.Services.GpsService;

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
            case R.id.action_settings:
                if(getClass() != SearchActivity.class) {
                    GpsService.getLocation(this);
                    Intent intent = new Intent(getBaseContext(), SearchActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                }
                break;
            case R.id.action_logout:
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                AuthUI.getInstance().signOut(this);
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
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
                toolbar.setVisibility(View.GONE);
            }
        }
    }
}
