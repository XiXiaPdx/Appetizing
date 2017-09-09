package com.xixia.appetizing.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.xixia.appetizing.Models.Restaurant;
import com.xixia.appetizing.Models.SplashPic;
import com.xixia.appetizing.Models.DescribedPicture;
import com.xixia.appetizing.Models.UserProfile;
import com.xixia.appetizing.R;
import com.xixia.appetizing.Services.AppDataSingleton;
import com.xixia.appetizing.Services.EndLessScrollListener;
import com.xixia.appetizing.Services.GpsService;
import com.xixia.appetizing.Services.SpinnerService;
import com.xixia.appetizing.Services.SplashCustomRecyclerView;
import com.xixia.appetizing.Services.UnSplashClient;
import com.xixia.appetizing.Services.UnSplashServiceGenerator;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements SplashPicsAdapter.OpenBottomSheet, GpsService.RevealSearch, View.OnClickListener {
    private FirebaseDatabase mFireBaseDatabase;
    private FirebaseAuth mFireBaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mFirebaseUser;
    private static final int RC_SIGN_IN = 1;
    private SplashCustomRecyclerView mPicsRecyclerView;
    private SplashPicsAdapter mSplashPicsAdapter;
    private StaggeredGridLayoutManager mPicGridLayOut;
    private EndLessScrollListener mEndLessScrollListener;
    private TextWatcher mDescriptionTextWatcher;
    private List<SplashPic> mAllPictures = new ArrayList<>();
    private Boolean notCurrentlyLoading;
    private CustomBottomSheet mBottomSheetBehavior;
    private SplashPic mSelectedPic;
    private ChildEventListener mDescribedFoodListener;
    private SpinnerService mSpinnerService;
    private YelpAPIFactory apiFactory;
    private CoordinateOptions mSearchCoordinate;
    private FragmentManager mFragmentManager;
    @BindView(R.id.coordinator) CoordinatorLayout mCoordinator;
    @BindView(R.id.bottom_sheet) View mBottomSheet;
    @BindView(R.id.largeSplashPic) ImageView mLargeSpashPic;
    @BindView(R.id.cardViewLargePic) CardView mCardView;
    @BindView(R.id.descriptionText) TextView mDescriptionText;
    @BindView(R.id.viewSwitcher) ViewSwitcher mViewSwitcher;
    @BindView(R.id.editDescriptionText) EditText mEditTextField;
    @BindView(R.id.submitEdit) ImageButton mSubmitEditButton;
    @BindView(R.id.searchButton) ImageButton mSearchButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mFragmentManager = getSupportFragmentManager();
        mSubmitEditButton.setOnClickListener(this);
        mSearchButton.setOnClickListener(this);
        mDescriptionText.setOnClickListener(this);
        mEditTextField.setOnClickListener(this);
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        mViewSwitcher.setInAnimation(in);
        mViewSwitcher.setOutAnimation(out);
        mBottomSheetBehavior = new CustomBottomSheet(this);
        mBottomSheetBehavior= (CustomBottomSheet) BottomSheetBehavior.from(mBottomSheet);
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
        mSpinnerService = new SpinnerService(this);
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
                    //If logout intent has to go to Main (which it currently does not...and working) a fix is add && mAllPictures is 0 or not 0, I forget. in the scenario of a logout and log back, the bug is the setDescribed is accidentally called twice. Once in AuthAttach and once in Result Ok for sign in. Need a boolean to only have one.
                    Log.d("USER NOT NULL", "USER NOT NULL");
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

    public void setDescriptionTextWatcher(){
        mDescriptionTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                if (mEditTextField.getText().length() > 0) {
                    if (mSubmitEditButton.getVisibility() == View.INVISIBLE) {
                        mSubmitEditButton.setVisibility(View.VISIBLE);
                        mSubmitEditButton.setAlpha(0.0f);
                        mSubmitEditButton.animate().alpha(1.0f);
                    }
                } else {
                    mSubmitEditButton.setAlpha(1.0f);
                    mSubmitEditButton.animate().alpha(0.0f)
                            .setListener(new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    mSubmitEditButton.setVisibility(View.INVISIBLE);
                                    mSubmitEditButton.animate().setListener(null);
                                }
                            });
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        mEditTextField.addTextChangedListener(mDescriptionTextWatcher);

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

    public void yelpCall(String searchTerm){
        final List<Restaurant> searchedRestaurants= new ArrayList<>();
        apiFactory = new YelpAPIFactory(Constants.YELP_CONSUMER_KEY, Constants.YELP_CONSUMER_SECRET, Constants.YELP_TOKEN, Constants.YELP_TOKEN_SECRET);
        YelpAPI yelpAPI = apiFactory.createAPI();
        Map<String, String> params = new HashMap<>();
        params.put("category_filter", "restaurants");
        params.put("term", searchTerm);
        Call<SearchResponse> call = yelpAPI.search(mSearchCoordinate, params);
        Callback<SearchResponse> callback = new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                mSpinnerService.removeSpinner();
                SearchResponse searchResponse = response.body();
                StringBuilder stringBuilder = new StringBuilder();
                int count = 0;
                for (Business business: searchResponse.businesses()){
                    stringBuilder.append(business.name()+", ");
                    Restaurant newRestaurant = createRestaurant(business);
                    searchedRestaurants.add(newRestaurant);
                    count++;
                    if (count == 5){ break;}
                }

                Toast.makeText(MainActivity.this, stringBuilder.toString(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent (MainActivity.this, MapsActivity.class);
                intent.putExtra("restaurants", Parcels.wrap(searchedRestaurants));
                intent.putExtra("myLat", mSearchCoordinate.latitude());
                intent.putExtra("myLong", mSearchCoordinate.longitude());
                intent.putExtra("searchTerm", mDescriptionText.getText().toString().trim());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
            }
            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.d("FAILED YELP", t.toString());
            }
        };
        call.enqueue(callback);
    }

    public Restaurant createRestaurant(Business business){
         Double mLat = business.location().coordinate().latitude();
        Double mLong = business.location().coordinate().longitude();
        String mName = business.name();

        return new Restaurant(mLat, mLong, mName);
    }


    @Override
    public void onStart(){
        super.onStart();
        Log.d("START", "STARt");
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
                                } else {
                                    //check if AllPictures is null.
                                    if (mAllPictures.size()==0) {
//                                        Intent intent = new Intent(getBaseContext(), SplashActivity.class);
//                                        startActivity(intent);
//                                        finish();
                                        unSplash30Call();
                                    } else
                                    setDescribedPictures();
                                }
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
        if (mAuthListener != null) {
            Log.d("AUTHLISTENER", "AUTHLISTNER ADDED");
            mFireBaseAuth.addAuthStateListener(mAuthListener);
        }
        createRecyclerEndLessScroll();
        if (mEndLessScrollListener != null){
            mPicsRecyclerView.addOnScrollListener(mEndLessScrollListener);
        }
        setDescriptionTextWatcher();
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
        if (mDescriptionTextWatcher != null) {
            mDescriptionText.removeTextChangedListener(mDescriptionTextWatcher);
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
                    DescribedPicture description = dataSnapshot.getValue(DescribedPicture.class);
                    AppDataSingleton.addToDescribedPictures(description);
                    matchDescriptionWithAllPics(description);
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

        @Override
    public void openSheet(int pictureIndex) {
            mSearchButton.setVisibility(View.INVISIBLE);
            mSubmitEditButton.setVisibility(View.INVISIBLE);
            switch (mBottomSheetBehavior.getState()){
            case BottomSheetBehavior.STATE_EXPANDED:
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
        }
            setLargePic(pictureIndex);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            //this is just needed once, on first time open bottom sheet. Without, the stateChange callback doesn't pick up the changes in state for some reason
            mCoordinator.setVisibility(View.VISIBLE);
        }

    @Override
    public void revealSearchButton(CoordinateOptions coordinate){
        mSearchCoordinate = coordinate;
        String searchPhrase = mDescriptionText.getText().toString().trim();
        if (!searchPhrase.contains("Describe food") ){
            mSearchButton.setVisibility(View.VISIBLE);
            mSearchButton.setAlpha(0.0f);
            mSearchButton.animate().alpha(1.0f);
        }
    }

    public void setBottomSheetCallBack(){
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(mViewSwitcher.getCurrentView() != mDescriptionText && newState == 4) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    closeKeyShowNext(imm);
                }

                Log.d("STATE STATE", String.valueOf(newState));
                if(newState == 4) {
                    Log.d("GONE GONE GONE", "GONE GONE GONE");
                    mCoordinator.setVisibility(View.GONE);
                }
                if(newState == 3) {
                    Log.d("STATE VISIBLE", "STATE VISIBLE");
                    mCoordinator.setVisibility(View.VISIBLE);
                    mLargeSpashPic.setScaleType(ImageView.ScaleType.CENTER);
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

        int screenWidth = display.getWidth();
        mSelectedPic = mAllPictures.get(pictureIndex);
        Picasso
                .with(this)
                .load(mSelectedPic.getUrls()
                        .getRegular())
                .resize(screenWidth, screenWidth)
                .onlyScaleDown()
                .into(mLargeSpashPic);
        mCardView.setLayoutParams(new ConstraintLayout.LayoutParams(circleDiameter,circleDiameter));
        mCardView.setRadius(circleDiameter/2);
        String foodDescription = mSelectedPic.getFoodDescription();
            if (foodDescription == null || foodDescription.length() == 0){
                mDescriptionText.setText(getString(R.string.what_to_eat));
            } else {
                mDescriptionText.setText(foodDescription);
            }
    }

    @Override
    public void onClick(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view == mDescriptionText){
            if (mViewSwitcher.getCurrentView() == mDescriptionText ) {
                mViewSwitcher.showNext();
                mEditTextField.requestFocus();
                imm.showSoftInput(mEditTextField, InputMethodManager.SHOW_IMPLICIT);
            } else {
                closeKeyShowNext(imm);
            }
        }
        if(view == mEditTextField){
            mSubmitEditButton.setVisibility(View.INVISIBLE);
            closeKeyShowNext(imm);
        }
        if(view == mSubmitEditButton){
            String foodDescription = mEditTextField.getText().toString().trim();
                saveDescriptionToFirebase(foodDescription);
                mDescriptionText.setText(foodDescription);
                closeKeyShowNext(imm);
            mSearchButton.setVisibility(View.VISIBLE);
            mSearchButton.setAlpha(0.0f);
            mSearchButton.animate().alpha(1.0f);
        }
        if(view == mSearchButton){
            mSpinnerService.showSpinner();
            String searchPhrase = mDescriptionText.getText().toString().trim();
            yelpCall(mDescriptionText.getText().toString().trim());
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
                //this isn't permanent change...???
//                pic.setFoodDescription(description.getFoodDescription());
                // this permanently changes the data in the app that this picture is now described.
                mAllPictures.get(count).setFoodDescription(description.getFoodDescription());
                AppDataSingleton.setmAllPictures(mAllPictures);
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.GET_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("GRANTED", "GRANTED");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                Log.d("DENIED", "DENIED");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
