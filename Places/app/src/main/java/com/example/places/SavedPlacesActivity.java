package com.example.places;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.places.adapters.FeedAdapter;
import com.example.places.databinding.ActivityProfileBinding;
import com.example.places.databinding.ActivitySavedPlacesBinding;
import com.example.places.databinding.FragmentHomeBinding;
import com.example.places.models.Place;
import com.example.places.models.User;
import com.example.places.ui.home.HomeViewModel;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class SavedPlacesActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "SavedPlacesActivity";

    // Attributes
    ActivitySavedPlacesBinding binding;
    private List<Place> places;
    private FeedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting up binding
        binding = ActivitySavedPlacesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("Private places");

        // Create and setup adapter
        this.places = new ArrayList<>();
        this.adapter = new FeedAdapter(SavedPlacesActivity.this, this.places);
        this.binding.rvPlaces.setAdapter(this.adapter);
        this.binding.rvPlaces.setLayoutManager(new LinearLayoutManager(SavedPlacesActivity.this));

        // Query your feed
        getSaved();
    }

    /**
     * Get your privately saved posts
     */
    private void getSaved() {
        binding.loading.setVisibility(View.VISIBLE);

        ParseQuery<Place> query = ParseQuery.getQuery(Place.class);
        query.orderByDescending(Place.KEY_CREATED_AT);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.include(Place.KEY_USER);
        query.include(Place.KEY_CATEGORY);
        query.whereEqualTo(Place.KEY_PUBLIC, false);

        query.findInBackground(new FindCallback<Place>() {
            @Override
            public void done(List<Place> objects, ParseException e) {
                binding.loading.setVisibility(View.GONE);

                if(e == null) {
                    if(objects.size() == 0) {
                        binding.tvNoResults.setVisibility(View.VISIBLE);
                        binding.rvPlaces.setVisibility(View.GONE);
                    } else {
                        places.addAll(objects);
                        adapter.notifyDataSetChanged();
                        binding.tvNoResults.setVisibility(View.GONE);
                        binding.rvPlaces.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(SavedPlacesActivity.this, "Error getting places", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error getting saved places", e);
                }
            }
        });

    }
}