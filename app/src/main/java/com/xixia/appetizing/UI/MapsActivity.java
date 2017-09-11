package com.xixia.appetizing.UI;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.xixia.appetizing.Adapters.RestaurantAdapter;
import com.xixia.appetizing.Constants;
import com.xixia.appetizing.Models.GooglePlaces.GoogePlace;
import com.xixia.appetizing.Models.GooglePlaces.Result;
import com.xixia.appetizing.Models.SplashPic;
import com.xixia.appetizing.R;
import com.xixia.appetizing.Services.SpinnerService;
import com.xixia.appetizing.Services.UnSplashClient;
import com.xixia.appetizing.Services.UnSplashServiceGenerator;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;

import org.parceler.Parcels;

import java.util.ArrayList;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private Double mLat;
    private Double mLong;
    private String mSearchTerm;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private YelpAPIFactory apiFactory;
    private SpinnerService mSpinnerService;
    private StaggeredGridLayoutManager mRestaurantGridManager;
    private RestaurantAdapter mRestaurantAdapter;

    @BindView(R.id.restaurantRecycler) RecyclerView mRestaurantScroller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ButterKnife.bind(this);
        mRestaurantGridManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
        mRestaurantScroller.setLayoutManager(mRestaurantGridManager);
        mSearchTerm = getIntent().getStringExtra("searchTerm");
        Log.d("SEARCH TERM", mSearchTerm);
        mSpinnerService = new SpinnerService(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(MapsActivity.this, String.valueOf(marker.getTag()), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mSpinnerService.showSpinner();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        Log.d("DOuble LAT", String.valueOf(latLng.latitude));
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

//        final List<Restaurant> searchedRestaurants= new ArrayList<>();
        apiFactory = new YelpAPIFactory(Constants.YELP_CONSUMER_KEY, Constants.YELP_CONSUMER_SECRET, Constants.YELP_TOKEN, Constants.YELP_TOKEN_SECRET);
        YelpAPI yelpAPI = apiFactory.createAPI();
        Map<String, String> params = new HashMap<>();
        params.put("category_filter", "restaurants");
        params.put("term", mSearchTerm);
        Call<SearchResponse> call = yelpAPI.search(latLng, params);
        Callback<SearchResponse> callback = new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                SearchResponse searchResponse = response.body();
                StringBuilder stringBuilder = new StringBuilder();
                int count = 0;
                for (Business business: searchResponse.businesses()){
                    stringBuilder.append(business.name()+", ");
                    count++;
                    if (count == 5){ break;}
                }
                mSpinnerService.removeSpinner();
                showNearbyPlaces(searchResponse.businesses());
                setRestaurantsView(searchResponse.businesses());

            }
            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.d("FAILED YELP", t.toString());
            }
        };
        call.enqueue(callback);


//        Log.d("LATLONG", latLng);
//        UnSplashClient client = UnSplashServiceGenerator.createService(UnSplashClient.class);
//        Single<GoogePlace> call = client.nearbyPlaces("https://maps.googleapis.com/maps/api/place/nearbysearch/json?rankby=distance&type=restaurant", latLng, mSearchTerm, getString(R.string.google_maps_key));
//        call.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new SingleObserver<GoogePlace>() {
//                    @Override
//                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onSuccess(@io.reactivex.annotations.NonNull GoogePlace googePlace) {
//                        showNearbyPlaces(googePlace.getResults());
//
//                    }
//
//                    @Override
//                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
//
//                    }
//                });
    }

    private void showNearbyPlaces(List<Business> nearbyPlacesList) {
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();

            double lat = nearbyPlacesList.get(i).location().coordinate().latitude();
            double lng = nearbyPlacesList.get(i).location().coordinate().longitude();
            String placeName = nearbyPlacesList.get(i).name();
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng).title(placeName);
            mMap.addMarker(markerOptions).setTag(i);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
    }

    public void setRestaurantsView(List<Business> restaurants){
        mRestaurantAdapter = new RestaurantAdapter(this, restaurants);
        mRestaurantScroller.setAdapter(mRestaurantAdapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mRestaurantScroller);
    }
}

