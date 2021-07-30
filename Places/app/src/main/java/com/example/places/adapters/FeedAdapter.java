package com.example.places.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
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
import com.example.places.MainActivity;
import com.example.places.PlaceDetailActivity;
import com.example.places.ProfileActivity;
import com.example.places.R;
import com.example.places.models.Place;
import com.example.places.models.SearchResult;
import com.example.places.models.User;
import com.parse.ParseUser;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    // Attributes
    private Context context;
    private List<Place> places;

    public FeedAdapter(Context context, List<Place> places) {
        this.context = context;
        this.places = places;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.item_place_feed, parent, false);
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
        private ImageView ivImage, ivUserImage;
        private TextView tvAuthorName, tvUsername, tvName, tvCategory;
        private RelativeLayout rlAuthor, rlPlace;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find items on View
            this.ivImage = itemView.findViewById(R.id.ivImage);
            this.ivUserImage = itemView.findViewById(R.id.ivUserImage);
            this.tvName = itemView.findViewById(R.id.tvName);
            this.tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            this.tvUsername = itemView.findViewById(R.id.tvUsername);
            this.tvCategory = itemView.findViewById(R.id.tvCategory);
            this.rlAuthor = itemView.findViewById(R.id.rlAuthor);
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
                this.tvCategory.setText(place.getCategory().get("name").toString());
            } catch (NullPointerException e) {
                Log.e("FeedAdapter", "Place: " + place.getName() + " doesn't have category", e);
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

            // Bind user's information
            ParseUser user = place.getUser();
            try {
                this.tvUsername.setText(String.format("@%s",user.getUsername()));
                this.tvAuthorName.setText(user.getString(User.KEY_NAME));
            } catch (NullPointerException ex) {
                this.tvUsername.setText(R.string.loading);
                this.tvAuthorName.setText(R.string.loading);
            }

            String authorImage;
            try {
                authorImage = user.getParseFile(User.KEY_PROFILE_PICTURE).getUrl();
            } catch (NullPointerException e) {
                authorImage = "";
            }
            Glide.with(context)
                    .load(authorImage)
                    .placeholder(R.drawable.avatar)
                    .error(R.drawable.avatar)
                    .circleCrop()
                    .into(ivUserImage);


            // Click listener to go to author
            this.rlAuthor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra("user", user.getObjectId());
                    context.startActivity(intent);
                }
            });

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