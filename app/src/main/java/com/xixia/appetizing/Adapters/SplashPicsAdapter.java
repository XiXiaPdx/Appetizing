package com.xixia.appetizing.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

    public SplashPicsAdapter(){}

    public SplashPicsAdapter(Context context, List<SplashPic> pictures) {
        super ();
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

    public class PictureViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.pictureItemView) ImageView mPictureView;
        private Context pictureViewContext;

        public PictureViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
            pictureViewContext = itemView.getContext();
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
        }

        @Override
        public void onClick(View view) {

        }
    }
}
