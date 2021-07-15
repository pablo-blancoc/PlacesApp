package com.example.places;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.places.databinding.ActivityPlaceDetailBinding;
import com.example.places.models.Place;
import com.example.places.models.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

public class PlaceDetailActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "PlaceDetailActivity";

    // Attributes
    private ActivityPlaceDetailBinding binding;
    private Place place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting up binding
        binding = ActivityPlaceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get intent with objectId of the place
        Intent intent = getIntent();
        String uid = intent.getStringExtra("place");
        this.getPlace(uid);


    }

    /**
     * Gets the information of the place passed from Parse server backed
     * @param _id: The unique objectId of the place that wants to be retrieved
     */
    private void getPlace(String _id) {
        this.binding.loading.setVisibility(View.VISIBLE);

        ParseQuery<Place> query = ParseQuery.getQuery(Place.class);
        query.whereEqualTo(Place.KEY_OBJECT_ID, _id);
        query.include(Place.KEY_CATEGORY);
        query.include(Place.KEY_USER);
        query.getFirstInBackground(new GetCallback<Place>() {
            @Override
            public void done(Place object, ParseException e) {
                if(e == null) {
                    place = object;
                    bindInformation();
                } else {
                    Toast.makeText(PlaceDetailActivity.this, "Place not found", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    /**
     * Binds the information of the place with the corresponding part of the layout
     */
    private void bindInformation() {
        // Fill information
        this.binding.tvName.setText(this.place.getName());
        this.binding.tvDescription.setText(this.place.getDescription());
        this.binding.chipLikes.setText(String.format("%d likes", this.place.getLikeCount()));
        this.binding.tvAddress.setText(this.place.getAddress());
        this.binding.tvCategory.setText(this.place.getCategory().getString("name"));
        this.binding.rbPrice.setRating(this.place.getPrice());

        // Load image
        Glide.with(PlaceDetailActivity.this)
                .load(this.place.getImage().getUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .centerCrop()
                .into(this.binding.ivImage);


        this.binding.loading.setVisibility(View.GONE);
    }
}