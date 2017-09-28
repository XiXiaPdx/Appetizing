package com.xixia.appetizing.UI;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.xixia.appetizing.Adapters.RestaurantAdapter;
import com.xixia.appetizing.Constants;
import com.xixia.appetizing.R;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private String mSearchTerm;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private YelpAPIFactory apiFactory;
    private LinearLayoutManager mRLM;
    private RestaurantAdapter mRestaurantAdapter;
    private List<Business> mRestaurantList;
    private Marker mOpenMarker;
    private List<Marker> mAllMarkers;
    private RecyclerView.OnScrollListener scrollListener;
    private GoogleMap.OnMarkerClickListener clickListener;
    private View spin;
    private FrameLayout view;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int REQUEST_CHECK_SETTINGS = 7;
    @BindView(R.id.restaurantRecycler) RecyclerView mRestaurantScroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mAllMarkers = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ButterKnife.bind(this);
        mRLM = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL ,false);
        mRestaurantScroller.setLayoutManager(mRLM);
        mSearchTerm = getIntent().getStringExtra("searchTerm");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode){
                    case Activity.RESULT_OK:
                        break;
                    case Activity.RESULT_CANCELED:
                        finish();
                        break;
                }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setPadding(0,150,0,0);

        clickListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showSelectedMarker(marker);
                Log.d("MARKER CLICKED", "MARKER CLICKED");
                return true;
            }
        };

        mMap.setOnMarkerClickListener(clickListener);

        // check that loation services is On here.
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        Task<LocationSettingsResponse> task =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    Log.d("LOCATION SERVICES ON", "LOCATION SERVICES ON");

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // REQUEST is arbitary value and I don't handle it
                                Log.d("LOCATION SERVICES OFF", "LOCATION SERVICES OFF");
                                resolvable.startResolutionForResult(
                                        MapsActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.

                            break;
                    }
                }
            }
        });

        //Initialize Google Play Services. If Android is 23+, check permission at run time
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // no permission, ask for it. Results goes to OnRequestPermissionResult
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                Log.d("GRANTED API 23+", "ELSE IF build google client");
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
            // if Android is less than 22, permission should be given at install. Check
        } else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //build map
            Log.d("GRANTED API 19", "build google client");
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission. ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        buildGoogleApiClient();
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    //user chose to not turn on location servcices. Exit this activity.
                    finish();
                }
                return;
            }

        }
    }

    protected synchronized void buildGoogleApiClient() {
        Log.d("Google Client", "BUIDLING");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        showSpinner();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("Location Updates", "REQUESTED");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("SUSPENDED", "SUSPENDED");


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("FAILED", connectionResult.getErrorMessage());

    }

    @Override
    public void onLocationChanged(Location location) {
//        if (mCurrLocationMarker != null) {
//            mCurrLocationMarker.remove();
//        }
        Log.d("LOCATION", location.toString());

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        String mLatLng = makeString(latLng);
        getNearbyPlaces(mLatLng);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    public String makeString(LatLng latLng){

        return String.valueOf(latLng.latitude)+","+String.valueOf(latLng.longitude);
    }

    private void getNearbyPlaces(String latLng){

        apiFactory = new YelpAPIFactory(Constants.YELP_CONSUMER_KEY, Constants.YELP_CONSUMER_SECRET, Constants.YELP_TOKEN, Constants.YELP_TOKEN_SECRET);
        YelpAPI yelpAPI = apiFactory.createAPI();
        Map<String, String> params = new HashMap<>();
        params.put("category_filter", "restaurants");
        params.put("term", mSearchTerm);
        Call<SearchResponse> call = yelpAPI.search(latLng, params);
        Callback<SearchResponse> callback = new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                mRestaurantList = response.body().businesses();
                removeSpinner();
                showNearbyPlaces(mRestaurantList);
                setRestaurantsView(mRestaurantList);
                apiFactory=null;
            }
            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
            }
        };
        call.enqueue(callback);
    }

    private void showNearbyPlaces(List<Business> nearbyPlacesList) {
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();


            double lat = nearbyPlacesList.get(i).location().coordinate().latitude();
            double lng = nearbyPlacesList.get(i).location().coordinate().longitude();
            String placeName = nearbyPlacesList.get(i).name();
            LatLng latLng = new LatLng(lat, lng);

            markerOptions.position(latLng).title(placeName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            Marker newMarker = mMap.addMarker(markerOptions);
            newMarker.setTag(i);
            mAllMarkers.add(newMarker);

        }
    }

    public void setRestaurantsView(List<Business> restaurants){
        mRestaurantAdapter = new RestaurantAdapter(restaurants);
        mRestaurantScroller.setAdapter(mRestaurantAdapter);
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRestaurantScroller);
        mRestaurantScroller.smoothScrollToPosition(0);

         scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0) {
                    int currentVisible = mRLM.findFirstCompletelyVisibleItemPosition();
                    if (currentVisible != -1) {
                        setMarkerToRestaurant(currentVisible);
                    }
                }
            }
        };
        mRestaurantScroller.addOnScrollListener(scrollListener);
    }

    public void setMarkerToRestaurant(int currentVisible){
        Marker marker = mAllMarkers.get(currentVisible);
        showSelectedMarker(marker);
    }

    public void showSelectedMarker(Marker marker) {
        if (mOpenMarker != null) {
            mOpenMarker.hideInfoWindow();
        }
        marker.showInfoWindow();

        mOpenMarker = marker;
        int index = (int) marker.getTag();
        Business restaurant = mRestaurantList.get(index);
        double lat = restaurant.location().coordinate().latitude();
        double lng = restaurant.location().coordinate().longitude();
        LatLng latLng = new LatLng(lat, lng);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));
        mRestaurantScroller.smoothScrollToPosition(index);
    }

    public void showSpinner(){
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(400,400);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        spin = inflater.inflate(R.layout.spinner, null);
        view=getWindow().getDecorView().findViewById(android.R.id.content);
        view.addView(spin);
    }

    public void removeSpinner(){
        if (spin != null) {
            view.removeView(spin);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    //add back press remove on Marker clicked Listener
}

