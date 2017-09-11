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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;

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
import com.xixia.appetizing.R;
import com.xixia.appetizing.Services.SpinnerService;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
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
    private LinearLayoutManager mRLM;
    private RestaurantAdapter mRestaurantAdapter;
    private List<Business> mRestaurantList;
    private Marker mOpenMarker;
    private List<Marker> mAllMarkers;

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
        mRLM = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL ,false);
        mRestaurantScroller.setLayoutManager(mRLM);
        mSearchTerm = getIntent().getStringExtra("searchTerm");
        Log.d("SEARCH TERM", mSearchTerm);
        mSpinnerService = new SpinnerService(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showSelectedMarker(marker);
                return true;
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
                mRestaurantList = searchResponse.businesses();
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

            markerOptions.position(latLng).title(placeName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            Marker newMarker = mMap.addMarker(markerOptions);
            newMarker.setTag(i);
            mAllMarkers.add(newMarker);


//            markerOptions.position(latLng).title(placeName);
//            mMap.addMarker(markerOptions).setTag(i);
//            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
    }

    public void setRestaurantsView(List<Business> restaurants){
        mRestaurantAdapter = new RestaurantAdapter(this, restaurants);
        mRestaurantScroller.setAdapter(mRestaurantAdapter);
        PagerSnapHelper snapHelper = new PagerSnapHelper();
//        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mRestaurantScroller);
        mRestaurantScroller.smoothScrollToPosition(0);
        mRestaurantScroller.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0) {
                    int currentVisible = mRLM.findFirstCompletelyVisibleItemPosition();
                    setMarkerToRestaurant(currentVisible);
                }

            }
        });
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

    //add back press remove on Marker clicked Listener
}

