package com.xixia.appetizing.UI;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.xixia.appetizing.Models.Restaurant;
import com.xixia.appetizing.R;
import com.yelp.clientlib.entities.Business;

import org.parceler.Parcels;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Restaurant> mRestaurants;
    private Double mLat;
    private Double mLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mRestaurants = Parcels.unwrap(getIntent().getParcelableExtra("restaurants"));
        mLat = getIntent().getDoubleExtra("myLat", 0.00);
        mLong = getIntent().getDoubleExtra("myLong",0.00);
        Log.d("LAT", Double.toString(mLat));
        Log.d("LONG", Double.toString(mLong));
        Log.d("Restaurant", mRestaurants.get(0).getmName());
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

        // Add a marker in Sydney and move the camera
        LatLng currentLocation = new LatLng(mLat, mLong);
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

        int count = 0;
        for (Restaurant restaurant: mRestaurants){
           addToMap(restaurant);
            count++;
            if (count == 5){ break;}
        }

    }

    public void addToMap(Restaurant restaurant){
        Restaurant currentRestaurant = restaurant;
        LatLng restaurantLocation = new LatLng(restaurant.getmLat(), restaurant.getmLong());
        mMap.addMarker(new MarkerOptions().position(restaurantLocation).title(currentRestaurant.getmName()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(restaurantLocation));
    }
}
