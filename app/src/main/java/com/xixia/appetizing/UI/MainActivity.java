package com.xixia.appetizing.UI;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.xixia.appetizing.Adapters.SplashPicsAdapter;
import com.xixia.appetizing.Constants;
import com.xixia.appetizing.Models.SplashPic;
import com.xixia.appetizing.Models.UserProfile;
import com.xixia.appetizing.R;
import com.xixia.appetizing.Services.AppDataSingleton;
import com.xixia.appetizing.Services.EndLessScrollListener;
import com.xixia.appetizing.Services.GpsService;
import com.xixia.appetizing.Services.SplashCustomRecyclerView;
import com.xixia.appetizing.Services.UnSplashClient;
import com.xixia.appetizing.Services.UnSplashServiceGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity {
    private FirebaseDatabase mFireBaseDatabase;
    private FirebaseAuth mFireBaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final int RC_SIGN_IN = 1;
    private SplashCustomRecyclerView mPicsRecyclerView;
    private SplashPicsAdapter mSplashPicsAdapter;
    private StaggeredGridLayoutManager mPicGridLayOut;
    private EndLessScrollListener mEndLessScrollListener;
    private List<SplashPic> mAllPictures = new ArrayList<>();
    private Boolean notCurrentlyLoading;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPicsRecyclerView = findViewById(R.id.PicsRecycler);
        mSplashPicsAdapter = new SplashPicsAdapter();
        //what other features of staggered grid can we do???
        mPicGridLayOut = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mPicsRecyclerView.setLayoutManager(mPicGridLayOut);
        mFireBaseDatabase = FirebaseDatabase.getInstance();
        mFireBaseAuth = FirebaseAuth.getInstance();
        setAuthListner();
        mAllPictures = AppDataSingleton.getmAllPictures();
        mSplashPicsAdapter = new SplashPicsAdapter(this, mAllPictures);
        //this picture setting deserves further research
        mPicsRecyclerView.setHasFixedSize(true);
        mPicsRecyclerView.setAdapter(mSplashPicsAdapter);
        notCurrentlyLoading = true;
    }

    public void setAuthListner(){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null){
                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setTheme(R.style.FirebaseAuthUITheme)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    public void setRecyclerEndLessScroll(){
        mEndLessScrollListener = new EndLessScrollListener(mPicGridLayOut) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                while(notCurrentlyLoading) {
                    notCurrentlyLoading = false;
                    unSplash30Call();
                }
            }
        };
        mPicsRecyclerView.addOnScrollListener(mEndLessScrollListener);
    }

    public void unSplash30Call(){
        UnSplashClient client = UnSplashServiceGenerator.createService(UnSplashClient.class);
        Single<List<SplashPic>> call = client.pictures(Constants.UNSPLASH_ID);
        call
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<SplashPic>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull List<SplashPic> splashPics) {
                        mAllPictures.addAll(splashPics);
                        AppDataSingleton.setmAllPictures(mAllPictures);
                        mSplashPicsAdapter.morePicturesLoaded(mAllPictures);
                        notCurrentlyLoading = true;
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Log.d("ERROR", e.toString());

                    }
                });
    }

    @Override
    public void onResume(){
        super.onResume();
        if (mAuthListener != null) {
            mFireBaseAuth.addAuthStateListener(mAuthListener);
        }
        setRecyclerEndLessScroll();
        GpsService.getInstance(this);
    }

    @Override
    public void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        switch (request){
            case RC_SIGN_IN:
                switch (result){
                    case RESULT_OK:
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        final DatabaseReference mUserRef = mFireBaseDatabase.getReference(getString(R.string.user_node));

                        //query for current user UID. If it exists, the snapshot will be NOT NULl. No new profile created. If null, new user and new profile created.
                        mUserRef.orderByChild("mUserUID").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() == null){
                                    String username = user.getDisplayName();
                                    String useremail = user.getEmail();
                                    String userUID = user.getUid();
                                    UserProfile newUser = new UserProfile(username, useremail, userUID);
                                    mUserRef.child(userUID).setValue(newUser);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        break;
                    case RESULT_CANCELED:
                        finish();
                        break;
                }
                break;
            case 1000:
                switch (result) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        GpsService.getLocation(this);
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(this, "Location Service not Enabled", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if (mAuthListener != null) {
            mFireBaseAuth.removeAuthStateListener(mAuthListener);
        }
        if (mEndLessScrollListener != null){
            mPicsRecyclerView.removeOnScrollListener(mEndLessScrollListener);
        }
    }
}
