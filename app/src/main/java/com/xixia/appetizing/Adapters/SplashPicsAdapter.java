package com.xixia.appetizing.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.xixia.appetizing.Constants;
import com.xixia.appetizing.Models.SplashPic;
import com.xixia.appetizing.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by macbook on 8/25/17.
 */

public class SplashPicsAdapter extends RecyclerView.Adapter<SplashPicsAdapter.PictureViewHolder>{
    private List<SplashPic> mPictures;
    private Context context;
    private OpenBottomSheet mOpenBottomsheet;

    public SplashPicsAdapter(){}

    public SplashPicsAdapter(Context context, List<SplashPic> pictures) {
        super ();
        mOpenBottomsheet = (OpenBottomSheet) context;
        this.context = context;
        mPictures = pictures;
    }

    @Override
    public PictureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_pic_cardview, parent, false);
        PictureViewHolder viewHolder = new PictureViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PictureViewHolder holder, int position) {
        holder.bindPicture(mPictures.get(position));
    }

    @Override
    public int getItemCount() {
        return mPictures.size();
    }

    public void morePicturesLoaded(List<SplashPic> morePictures){
        int itemStart = mPictures.size();
        mPictures = morePictures;
        Log.d("Size", String.valueOf(mPictures.size()));
        notifyItemRangeInserted(itemStart, morePictures.size());
    }

    public void descriptionAdded(int position, List<SplashPic> newPictures){
        mPictures = newPictures;
        Log.d("Description ADDED MATCH", "MATCH AT "+String.valueOf(position));
        notifyItemChanged(position);
    }

    public class PictureViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.pictureItemView) ImageView mPictureView;
        @BindView(R.id.photographer) TextView mPhotographerName;
        @BindView(R.id.foodDescription) TextView mFoodDescription;
        private Context pictureViewContext;

        public PictureViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
            pictureViewContext = itemView.getContext();
            mPhotographerName.setOnClickListener(this);
            mPictureView.setOnClickListener(this);
        };

        public void bindPicture (SplashPic picture){
            Picasso
                    .with(pictureViewContext)
                    .load(picture.getUrls()
                            .getRegular())
                    .resize(Constants.MAX_Width, Constants.MAX_Height)
                    .onlyScaleDown()
                    .centerCrop()
                    .into(mPictureView);
            //set Name here
            mPhotographerName.setText("by " + picture.getUser().getFirst_name());
            if (picture.getFoodDescription() == null) {
                Log.d("Description NULL", "INVISIBLE");
                mFoodDescription.setText("I can set text");
                mFoodDescription.setVisibility(View.GONE);
            } else {
                Log.d("Make Visible", "VISIBLE NOW");
                mFoodDescription.setText(picture.getFoodDescription());
                mFoodDescription.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View view) {
            if (view == mPhotographerName){
                String url = mPictures.get(getAdapterPosition()).getLinks().getHtml();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            }

            if (view == mPictureView) {
                mOpenBottomsheet.openSheet(getAdapterPosition());
//                GpsService.getCurrentLocation();
            }
        }
    }

    public interface OpenBottomSheet {
        public void openSheet(int adapterPosition);
    }
}
