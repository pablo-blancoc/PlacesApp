package com.example.places;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.places.adapters.FeedAdapter;
import com.example.places.databinding.ActivityProfileBinding;
import com.example.places.databinding.ActivitySavedPlacesBinding;
import com.example.places.databinding.FragmentHomeBinding;
import com.example.places.models.EndlessRecyclerViewScrollListener;
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

import es.dmoral.toasty.Toasty;

public class SavedPlacesActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "SavedPlacesActivity";

    // Attributes
    ActivitySavedPlacesBinding binding;
    private List<Place> places;
    private FeedAdapter adapter;
    private int pager;
    EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting up binding
        binding = ActivitySavedPlacesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("Private places");

        // Create and setup adapter
        this.pager = 0;
        this.places = new ArrayList<>();
        this.adapter = new FeedAdapter(SavedPlacesActivity.this, this.places);
        this.binding.rvPlaces.setAdapter(this.adapter);
        this.binding.rvPlaces.setLayoutManager(new LinearLayoutManager(SavedPlacesActivity.this));

        // Setup SwipeContainer and colors for loading
        this.binding.swipeContainer.setColorSchemeResources(R.color.primary, R.color.white, R.color.black);
        this.binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFeed(true);
            }
        });

        // Set up endless scrolling
        this.scrollListener = new EndlessRecyclerViewScrollListener((LinearLayoutManager) this.binding.rvPlaces.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                pager++;
                getSaved();
            }
        };
        this.binding.rvPlaces.addOnScrollListener(this.scrollListener);
    }

    /**
     * Gets the newest posts
     */
    private void refreshFeed(boolean fromSwiper) {
        if(fromSwiper) {
            this.pager = 0;
            places.clear();
            getSaved();
            this.binding.swipeContainer.setRefreshing(false);
        } else {
            places.clear();
            getSaved();
        }
    }

    @Override
    protected void onResume() {
        this.refreshFeed(true);

        super.onResume();
    }

    /**
     * Get your privately saved posts
     */
    private void getSaved() {
        binding.loading.setVisibility(View.VISIBLE);

        ParseQuery<Place> query = ParseQuery.getQuery(Place.class);
        query.orderByDescending(Place.KEY_CREATED_AT);
        query.whereEqualTo(Place.KEY_USER, ParseUser.getCurrentUser());
        query.include(Place.KEY_USER);
        query.include(Place.KEY_CATEGORY);
        query.setLimit(10);
        query.setSkip(this.pager * 10);
        query.whereEqualTo(Place.KEY_PUBLIC, false);

        query.findInBackground(new FindCallback<Place>() {
            @Override
            public void done(List<Place> objects, ParseException e) {
                binding.loading.setVisibility(View.GONE);

                if(e == null) {
                    places.addAll(objects);
                    adapter.notifyDataSetChanged();
                    if(places.size() == 0) {
                        binding.tvNoResults.setVisibility(View.VISIBLE);
                        binding.rvPlaces.setVisibility(View.GONE);
                    } else {
                        binding.tvNoResults.setVisibility(View.GONE);
                        binding.rvPlaces.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toasty.error(SavedPlacesActivity.this, "Error while retrieving places. Check your internet connection and try again later.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error getting saved places", e);
                }
            }
        });

    }
}