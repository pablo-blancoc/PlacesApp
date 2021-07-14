package com.example.places;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.places.adapters.ProfilePlacesAdapter;
import com.example.places.databinding.ActivityProfileBinding;
import com.example.places.models.Place;
import com.example.places.models.User;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "ProfileActivity";

    // Attributes
    User user;
    ActivityProfileBinding binding;
    List<Place> places;
    ProfilePlacesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting up binding
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get userId of the profile that wants to be shown
        Intent intent = getIntent();
        String uid = intent.getStringExtra("user");
        this.getUser(uid);

        // Create adapter and set it
        this.places = new ArrayList<>();
        this.adapter = new ProfilePlacesAdapter(this, this.places);
        this.binding.rvPlaces.setAdapter(this.adapter);
        this.binding.rvPlaces.setLayoutManager(new GridLayoutManager(this, 3));

    }

    /**
     * Get the information from the user that wants to be shown and bind the information
     * @param uid: The user that wants to be shown
     */
    private void getUser(String uid) {
        this.binding.loading.setVisibility(View.VISIBLE);

        ParseQuery<User> query = ParseQuery.getQuery(User.class);
        query.whereEqualTo(User.KEY_OBJECT_ID, uid);
        query.getFirstInBackground(new GetCallback<User>() {
            @Override
            public void done(User object, ParseException e) {
                if(e == null) {
                    user = object;
                    bindInformation();
                    getPlaces();
                } else {
                    Toast.makeText(ProfileActivity.this, "User not found", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    /**
     * Binds the information of the user retrieved into the page
     */
    private void bindInformation() {
        this.binding.tvName.setText(this.user.getName());
        this.binding.tvUsername.setText(String.format("@%s", this.user.getUsername()));
        String imageUrl;
        try {
            imageUrl = user.getProfilePicture().getUrl();
        } catch (NullPointerException e) {
            imageUrl = "";
            Log.e(TAG, "No image for the user", e);
        }
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .circleCrop()
                .into(this.binding.ivProfilePicture);
        this.binding.tvFollowersCount.setText(String.format("FOLLOWERS: %d", this.user.getFollowersCount()));
        this.binding.tvFollowingCount.setText(String.format("FOLLOWERS: %d", this.user.getFollowingCount()));
        this.binding.tvBio.setText(this.user.getBio());
    }

    /**
     * Gets the places that the user has uploaded
     */
    private void getPlaces() {
        ParseQuery<Place> query = ParseQuery.getQuery(Place.class);
        query.whereEqualTo("user", this.user);
        query.findInBackground(new FindCallback<Place>() {
            @Override
            public void done(List<Place> _places, ParseException e) {
                if(e == null) {
                    places.addAll(_places);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error while getting places", e);
                }

                binding.loading.setVisibility(View.INVISIBLE);
            }
        });
    }
}
