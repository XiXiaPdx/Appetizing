package com.xixia.appetizing.UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.xixia.appetizing.Adapters.SplashPicsAdapter;
import com.xixia.appetizing.Constants;
import com.xixia.appetizing.Models.SplashPic;
import com.xixia.appetizing.Models.DescribedPicture;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity implements SplashPicsAdapter.OpenBottomSheet, View.OnClickListener {
    private FirebaseDatabase mFireBaseDatabase;
    private FirebaseAuth mFireBaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mFirebaseUser;
    private static final int RC_SIGN_IN = 1;
    private SplashCustomRecyclerView mPicsRecyclerView;
    private SplashPicsAdapter mSplashPicsAdapter;
    private StaggeredGridLayoutManager mPicGridLayOut;
    private EndLessScrollListener mEndLessScrollListener;
    private List<SplashPic> mAllPictures = new ArrayList<>();
    private Boolean notCurrentlyLoading;
    private BottomSheetBehavior mBottomSheetBehavior;
    private SplashPic mSelectedPic;
    private ChildEventListener mDescribedFoodListener;
    @BindView(R.id.bottom_sheet) View mBottomSheet;
    @BindView(R.id.largeSplashPic) ImageView mLargeSpashPic;
    @BindView(R.id.cardViewLargePic) CardView mCardView;
    @BindView(R.id.descriptionText) TextView mDescriptionText;
    @BindView(R.id.viewSwitcher) ViewSwitcher mViewSwitcher;
    @BindView(R.id.editDescriptionButton) ImageButton mEditButton;
    @BindView(R.id.editDescriptionText) EditText mEditTextField;
    @BindView(R.id.submitEdit) ImageButton mSubmitEditButton;
    @BindView(R.id.searchButton) ImageButton mSearchButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mEditButton.setOnClickListener(this);
        mSubmitEditButton.setOnClickListener(this);
        mSearchButton.setOnClickListener(this);
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        mViewSwitcher.setInAnimation(in);
        mViewSwitcher.setOutAnimation(out);
        mBottomSheetBehavior=BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setPeekHeight(0);
        setBottomSheetCallBack();
        mPicsRecyclerView = findViewById(R.id.PicsRecycler);
        mSplashPicsAdapter = new SplashPicsAdapter();
        //what other features of staggered grid can we do???
        mPicGridLayOut = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mPicsRecyclerView.setLayoutManager(mPicGridLayOut);
        mFireBaseDatabase = FirebaseDatabase.getInstance();
        mFireBaseAuth = FirebaseAuth.getInstance();
        mAllPictures = AppDataSingleton.getmAllPictures();
        Log.d("CREATE", "SIZE OF PICTURES "+ mAllPictures.size());
        setmPicsRecyclerView();
        notCurrentlyLoading = true;
        createAuthListener();
    }

    public void createAuthListener(){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null){
                    mFirebaseUser = currentUser;
                    if (mDescribedFoodListener == null) {
                        setDescribedPictures();
                    }
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

    public void createRecyclerEndLessScroll(){
        Log.d("SCROLL CREATEd", "SCROLL CREATED");
        mEndLessScrollListener = new EndLessScrollListener(mPicGridLayOut) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                while(notCurrentlyLoading) {
                    notCurrentlyLoading = false;
                    unSplash30Call();
                }
            }
        };
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
                        //possible this might get triggered too soon when main loads...

                        //filter new pics through described
                        List<SplashPic> descriptionAddedPics =  matchNewPicsWithDescribed(splashPics);

                        mAllPictures.addAll(descriptionAddedPics);
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
    public void onStart(){
        super.onStart();
        Log.d("START", "STARt");
        if (mAuthListener != null) {
            mFireBaseAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        Log.d("RESULT", "RESULT");

        switch (request){
            case RC_SIGN_IN:
                switch (result){
                    case RESULT_OK:
                        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        final DatabaseReference mUserRef = mFireBaseDatabase.getReference(getString(R.string.user_node));

                        //query for current user UID. If it exists, the snapshot will be NOT NULl. No new profile created. If null, new user and new profile created.
                        mUserRef.orderByChild("mUserUID").equalTo(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() == null){
                                    String username = mFirebaseUser.getDisplayName();
                                    String useremail = mFirebaseUser.getEmail();
                                    String userUID = mFirebaseUser.getUid();
                                    UserProfile newUser = new UserProfile(username, useremail, userUID);
                                    mUserRef.child(userUID).setValue(newUser);
                                } else
                                    setDescribedPictures();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        break;
                    case RESULT_CANCELED:
                        Log.d("BACK ARROW", "BACK ARROW IN SIGN IN SCREEN");
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
    public void onResume(){
        super.onResume();
        Log.d("onRESUME", "RESUME");
        createRecyclerEndLessScroll();
        if (mEndLessScrollListener != null){
            mPicsRecyclerView.addOnScrollListener(mEndLessScrollListener);
        }
        GpsService.getInstance(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d("PAUSE", "PAUSE");

        if (mAuthListener != null) {
            mFireBaseAuth.removeAuthStateListener(mAuthListener);
        }
        if (mEndLessScrollListener != null){
            mPicsRecyclerView.removeOnScrollListener(mEndLessScrollListener);
        }
    }

    public void setDescribedPictures(){
        if (mDescribedFoodListener == null) {
            DatabaseReference mUserDescriptionsRef = mFireBaseDatabase.getReference(getString(R.string.user_food_description));
            mDescribedFoodListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    DescribedPicture description = dataSnapshot.getValue(DescribedPicture.class);
                    AppDataSingleton.addToDescribedPictures(description);
                    //filter each described pic through allPictures and add description. notifyitemchanged on that position.
                    Log.d("SIZE", String.valueOf(AppDataSingleton.getmDescribedPictures().size()));
                    matchDescriptionWithAllPics(description);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mUserDescriptionsRef.child(mFirebaseUser.getUid()).addChildEventListener(mDescribedFoodListener);
        }
    }

    public void setmPicsRecyclerView(){
        Log.d("RECYCLER", "RECYCLER");
        mSplashPicsAdapter = new SplashPicsAdapter(this, mAllPictures);
//        mPicsRecyclerView.setHasFixedSize(true);
        mPicsRecyclerView.setAdapter(mSplashPicsAdapter);
    }

    public void addDescribedPicture(){
        DatabaseReference mUserDescriptionsRef = mFireBaseDatabase.getReference(getString(R.string.user_food_description));

    }

        @Override
    public void openSheet(int pictureIndex) {
        switch (mBottomSheetBehavior.getState()){
            case BottomSheetBehavior.STATE_EXPANDED:
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            case BottomSheetBehavior.STATE_COLLAPSED:
                setLargePic(pictureIndex);
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
        }
    }

    public void setBottomSheetCallBack(){
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(mViewSwitcher.getCurrentView() != mDescriptionText && newState == 4) {
                    Log.d("NOT ", String.valueOf(newState));
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    closeKeyShowNext(imm);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    public void setLargePic(int pictureIndex){
        android.view.Display display = ((android.view.WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int circleDiameter = (int)((double) display.getWidth()* .8);
        mSelectedPic = mAllPictures.get(pictureIndex);
        Picasso
                .with(this)
                .load(mSelectedPic.getUrls()
                        .getRegular())
                .resize(Constants.MAX_Width+250, Constants.MAX_Height+250)
                .onlyScaleDown()
                .centerCrop()
                .into(mLargeSpashPic);
        mCardView.setLayoutParams(new ConstraintLayout.LayoutParams(circleDiameter,circleDiameter));
        mCardView.setRadius(circleDiameter/2);
        try {
            String foodDescription = mSelectedPic.getFoodDescription();
            if (foodDescription.length() == 0){
                mDescriptionText.setText(getString(R.string.what_to_eat));
            } else {
                mDescriptionText.setText(foodDescription);
            }
        } catch (NullPointerException e){

        }
    }

    @Override
    public void onClick(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view == mEditButton){
            if (mViewSwitcher.getCurrentView() == mDescriptionText ) {
                mViewSwitcher.showNext();
                mEditTextField.requestFocus();
                imm.showSoftInput(mEditTextField, InputMethodManager.SHOW_IMPLICIT);
            } else {
                closeKeyShowNext(imm);
            }
        }
        if(view == mSubmitEditButton){
            String foodDescription = mEditTextField.getText().toString().trim();
            if(foodDescription.length()!=0) {
                saveDescriptionToFirebase(foodDescription);
                mDescriptionText.setText(foodDescription);
                closeKeyShowNext(imm);
            } else
                Toast.makeText(this, "Need a description", Toast.LENGTH_SHORT).show();
        }
    }

    public void closeKeyShowNext(InputMethodManager imm){
        imm.hideSoftInputFromWindow(mEditTextField.getWindowToken(), 0);
        mEditTextField.setText("");
        mViewSwitcher.showNext();
    }

    public void saveDescriptionToFirebase(String foodDescription){
         DatabaseReference mUserDescriptionsRef = mFireBaseDatabase.getReference(getString(R.string.user_food_description));
        DescribedPicture newDescription = new DescribedPicture(mSelectedPic.getId(), foodDescription);
         mUserDescriptionsRef.child(mFirebaseUser.getUid()).child(mSelectedPic.getId()).setValue(newDescription);
    }

    public void matchDescriptionWithAllPics(DescribedPicture description){
        int count = 0;
        for(SplashPic pic: mAllPictures){
            if(pic.getId().equals(description.getPicID())){
                pic.setFoodDescription(description.getFoodDescription());
                mSplashPicsAdapter.descriptionAdded(count);
            }
            count++;
        }
    }

    public List<SplashPic> matchNewPicsWithDescribed(List<SplashPic> newPics){
        int newPicCount = 0;
        List <DescribedPicture> allDescribed = AppDataSingleton.getmDescribedPictures();
        List<SplashPic> modifiedPics = new ArrayList<>();
        for(SplashPic pic: newPics){
            for (DescribedPicture description: allDescribed){
             if (pic.getId().equals(description.getPicID())){
                 Log.d("MATCH MATCH", "MATCH");
                 pic.setFoodDescription(description.getFoodDescription());
                 break;
             }
            }
            modifiedPics.add(pic);
            Log.d("Picture Number", String.valueOf(newPicCount));
            newPicCount++;
        }
        return modifiedPics;
    }

}
