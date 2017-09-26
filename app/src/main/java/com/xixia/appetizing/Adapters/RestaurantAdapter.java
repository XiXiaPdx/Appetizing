package com.xixia.appetizing.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
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
        Business restaurant = mRestaurants.get(position);
        holder.mRestaurantName.setText(restaurant.name());
        holder.mRestaurantRating.setText(String.valueOf(restaurant.rating())+"/5.0");
        holder.mRestaurantAddress.setText(restaurant.location().address().get(0));
        holder.mCityState.setText(restaurant.location().city() +", "+restaurant.location().stateCode());
        holder.bindPicture(restaurant);
    }

    @Override
    public int getItemCount() {
        return mRestaurants.size();
    }

    public class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //view fields on the single restaurant view here
        @BindView(R.id.restaurantName) TextView mRestaurantName;
        @BindView(R.id.restaurantImage) ImageView mRestaurantImage;
        @BindView(R.id.rating) TextView mRestaurantRating;
        @BindView(R.id.streetAddress) TextView mRestaurantAddress;
        @BindView(R.id.ratingIcon) ImageView mRatingIcon;
        @BindView(R.id.cityState) TextView mCityState;
        private Context mContext;


        public RestaurantViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mContext = itemView.getContext();
            mRestaurantName.setOnClickListener(this);

        }

        public void bindPicture (Business business) {
            String pictureUrl = business.imageUrl();
            String ratingIcon = business.ratingImgUrlSmall();
            Picasso
                    .with(mContext.getApplicationContext())
                    .load(pictureUrl)
                    .resize(500, 500)
                    .onlyScaleDown()
                    .centerCrop()
                    .into(mRestaurantImage);
            Picasso
                    .with(mContext.getApplicationContext())
                    .load(ratingIcon)
                    .fit()
                    .centerCrop()
                    .into(mRatingIcon);
        }

        @Override
        public void onClick(View view) {
            if (view == mRestaurantName) {
                String url = mRestaurants.get(getAdapterPosition()).url();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                view.getContext().startActivity(intent);
            }

        }
    }
}
