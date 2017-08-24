package com.xixia.appetizing.UI;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.xixia.appetizing.Models.UserProfile;
import com.xixia.appetizing.R;

import java.util.Arrays;

public class MainActivity extends BaseActivity {
    private FirebaseDatabase mFireBaseDatabase;
    private FirebaseAuth mFireBaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final int RC_SIGN_IN = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFireBaseDatabase = FirebaseDatabase.getInstance();
        mFireBaseAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null){
                    Toast.makeText(MainActivity.this, currentUser.getUid(), Toast.LENGTH_SHORT).show();
                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    public void onActivityResult(int request, int result, Intent data){
        super.onActivityResult(request, result, data);
        if (request == RC_SIGN_IN){
            if(result == RESULT_OK){
                //account created for first time or user is logging in again. if created for first time, create profile.
                // if first time, UID should be unique in FB under Users.
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String username = user.getDisplayName();
                String useremail = user.getEmail();
                String userUID = user.getUid();
                UserProfile newUser = new UserProfile(username, useremail, userUID);
                mFireBaseDatabase.getReference(getString(R.string.user_node)).push().setValue(newUser);

            } else if (result == RESULT_CANCELED){
                finish();
            }
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        if (mAuthListener != null) {
            mFireBaseAuth.addAuthStateListener(mAuthListener);
        }
    }


    @Override
    public void onPause(){
        super.onPause();
        if (mAuthListener != null) {
            mFireBaseAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
