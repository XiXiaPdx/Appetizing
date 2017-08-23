package com.xixia.appetizing.UI;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.xixia.appetizing.R;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

//        //this will get the String name for the activity that is active
//        String activityName = getClass().getSimpleName();
//        //use activityName to change what happens here
//        if (activityName.equals(LoginActivity.class.getSimpleName())){
//            getSupportActionBar().setCustomView(R.layout.abs_layout_login);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        } else {
//            getSupportActionBar().setCustomView(R.layout.abs_layout_generic);
//            TextView barTitle = ((TextView)getSupportActionBar().getCustomView().findViewById(R.id.actionBarTitle));
//            if (activityName.equals(MakeSelectionActivity.class.getSimpleName())){
//                barTitle.setText(getResources().getString(R.string.make));
//            } else if (activityName.equals(CarSelectionActivity.class.getSimpleName())){
//                barTitle.setText(getResources().getString(R.string.year_and_model));
//            } else if (activityName.equals(BonusOfferingsActivity.class.getSimpleName())){
//                barTitle.setText(getResources().getString(R.string.reward_details));
//            }
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
//
//        getSupportActionBar().getCustomView().findViewById(R.id.action_bar_settings).setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent (getBaseContext(), SettingsActivity.class);
//                startActivity(intent);
//                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
//            }
//        });
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
            return true;
        }
        return false;
    }
}