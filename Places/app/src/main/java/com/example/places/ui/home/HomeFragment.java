package com.example.places.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.places.R;
import com.example.places.adapters.FeedAdapter;
import com.example.places.databinding.FragmentHomeBinding;
import com.example.places.models.EndlessRecyclerViewScrollListener;
import com.example.places.models.Place;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // Constants
    private static final String TAG = "HomeFragment";

    // Attributes
    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private Context context;
    private List<Place> places;
    private FeedAdapter adapter;
    private int pager;
    EndlessRecyclerViewScrollListener scrollListener;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Set up binding and context
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        this.context = getContext();

        // Setup SwipeContainer and colors for loading
        this.binding.swipeContainer.setColorSchemeResources(R.color.primary, R.color.white, R.color.black);
        this.binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFeed(true);
            }
        });

        // Create and setup adapter
        this.pager = 0;
        this.places = new ArrayList<>();
        this.adapter = new FeedAdapter(context, this.places);
        this.binding.rvPlaces.setAdapter(this.adapter);
        this.binding.rvPlaces.setLayoutManager(new LinearLayoutManager(context));

        // Set up endless scrolling
        this.scrollListener = new EndlessRecyclerViewScrollListener((LinearLayoutManager) this.binding.rvPlaces.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                pager++;
                getFeed();
            }
        };

        // Set on scrollListener
        this.binding.rvPlaces.addOnScrollListener(this.scrollListener);

        // Load data
        this.refreshFeed(false);

        return root;
    }

    /**
     * Gets the newest posts
     */
    private void refreshFeed(boolean fromSwiper) {
        if(fromSwiper) {
            this.pager = 0;
            places.clear();
            getFeed();
            this.binding.swipeContainer.setRefreshing(false);
        } else {
            places.clear();
            getFeed();
        }
    }

    /**
     * Get the most current posts from your feed
     */
    private void getFeed() {
        binding.loading.setVisibility(View.VISIBLE);

        ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery("Follower");
        innerQuery.whereEqualTo("follower", ParseUser.getCurrentUser());
        innerQuery.include("following");

        ParseQuery<Place> outerQuery = ParseQuery.getQuery(Place.class);
        outerQuery.orderByDescending(Place.KEY_CREATED_AT);
        outerQuery.whereMatchesKeyInQuery("user", "following", innerQuery);
        outerQuery.include(Place.KEY_USER);
        outerQuery.include(Place.KEY_CATEGORY);
        outerQuery.setLimit(10);
        outerQuery.setSkip(this.pager * 10);
        outerQuery.whereEqualTo(Place.KEY_PUBLIC, true);

        outerQuery.findInBackground(new FindCallback<Place>() {
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
                    Toast.makeText(context, "Error getting timeline", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error getting timeline", e);
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}