package com.xixia.appetizing.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.xixia.appetizing.Models.SplashPic;
import com.xixia.appetizing.R;
import com.yelp.clientlib.entities.Business;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by macbook on 9/10/17.
 */

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {
    //fields here
    private List<Business> mRestaurants;

    public RestaurantAdapter (){};
    public RestaurantAdapter (List<Business> restaurants){
        super();
        mRestaurants = restaurants;
    }

    @Override
    public RestaurantAdapter.RestaurantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.singel_restaurant_view, parent, false);
        RestaurantAdapter.RestaurantViewHolder viewHolder = new RestaurantViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RestaurantAdapter.RestaurantViewHolder holder, int position) {
        holder.mRestaurantName.setText(mRestaurants.get(position).name());
    }

    @Override
    public int getItemCount() {
        return mRestaurants.size();
    }

    public class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //view fields on the single restaurant view here
        @BindView(R.id.restaurantName) TextView mRestaurantName;

        public RestaurantViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        public void bindPicture (String pictureUrl) {
            //Picasso
        }

        @Override
        public void onClick(View view) {

        }
    }
}
