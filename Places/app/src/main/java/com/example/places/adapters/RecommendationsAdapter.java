package com.example.places.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.places.PlaceDetailActivity;
import com.example.places.ProfileActivity;
import com.example.places.R;
import com.example.places.models.Place;
import com.example.places.models.User;
import com.parse.ParseUser;

import java.util.List;

public class RecommendationsAdapter extends RecyclerView.Adapter<RecommendationsAdapter.ViewHolder> {

    // Attributes
    private Context context;
    private List<Place> places;

    public RecommendationsAdapter(Context context, List<Place> places) {
        this.context = context;
        this.places = places;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.item_recommendation_place, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place place = this.places.get(position);
        holder.bind(place);
    }

    @Override
    public int getItemCount() {
        return this.places.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        // Attributes
        private ImageView ivImage;
        private TextView tvName, tvCategory;
        private RelativeLayout rlPlace;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find items on View
            this.ivImage = itemView.findViewById(R.id.ivImage);
            this.tvName = itemView.findViewById(R.id.tvName);
            this.tvCategory = itemView.findViewById(R.id.tvCategory);
            this.rlPlace = itemView.findViewById(R.id.rlPlace);
        }

        /**
         * Bind the information from the place to it's view
         * @param place: The place which information wants to be displayed
         */
        public void bind(Place place) {
            // Bind place's information
            this.tvName.setText(place.getName());

            try {
                this.tvCategory.setText(String.format("CATEGORY: %s", place.getCategory().get("name")));
            } catch (NullPointerException e) {
                Log.e("RecommendationsAdapter", "Place: " + place.getName() + " doesn't have category", e);
                this.tvCategory.setText(R.string.loading);
                return;
            }

            String imageUrl;
            try {
                imageUrl = place.getImage().getUrl();
            } catch (NullPointerException e) {
                imageUrl = "";
            }
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .centerCrop()
                    .into(ivImage);

            // Click listener to go to place's detail
            this.rlPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PlaceDetailActivity.class);
                    intent.putExtra("place", place.getObjectId());
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context);
                    context.startActivity(intent, options.toBundle());
                }
            });
        }

    }

}
