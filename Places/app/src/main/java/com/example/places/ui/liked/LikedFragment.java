package com.example.places.ui.liked;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.places.R;
import com.example.places.adapters.FeedAdapter;
import com.example.places.adapters.SearchResultsAdapter;
import com.example.places.databinding.FragmentHomeBinding;
import com.example.places.databinding.LikedFragmentBinding;
import com.example.places.models.EndlessRecyclerViewScrollListener;
import com.example.places.models.Place;
import com.example.places.models.SearchResult;
import com.example.places.ui.home.HomeViewModel;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class LikedFragment extends Fragment {

    // Constants
    private static final String TAG = "LikedFragment";

    // Attributes
    private LikedViewModel likedViewModel;
    private LikedFragmentBinding binding;
    private List<Place> places;
    private FeedAdapter adapter;
    private Context context;
    private int pager;
    EndlessRecyclerViewScrollListener scrollListener;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        likedViewModel = new ViewModelProvider(this).get(LikedViewModel.class);

        // Set up binding and context
        binding = LikedFragmentBinding.inflate(inflater, container, false);
        View root = this.binding.getRoot();
        this.context = getContext();

        // Create and setup adapter
        this.pager = 0;
        this.places = new ArrayList<>();
        this.adapter = new FeedAdapter(context, this.places);
        this.binding.rvPlaces.setAdapter(this.adapter);
        this.binding.rvPlaces.setLayoutManager(new LinearLayoutManager(context));

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
                getLiked();
            }
        };
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
            getLiked();
            this.binding.swipeContainer.setRefreshing(false);
        } else {
            places.clear();
            getLiked();
        }
    }

    /**
     * Get your liked places and display them
     */
    private void getLiked() {
        binding.loading.setVisibility(View.VISIBLE);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Like");
        query.include("place");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.setLimit(3);
        query.setSkip(this.pager * 3);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> likes, ParseException e) {

                if(e == null) {
                    for(int i=0; i<likes.size(); i++) {
                        ParseObject object = likes.get(i).getParseObject("place");
                        try {
                            places.add(placeFromParseObject(object));
                        } catch (ParseException parseException) {
                            Log.e(TAG, "Place " + i + "not added");
                            parseException.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if(places.size() == 0) {
                        binding.tvNoResults.setVisibility(View.VISIBLE);
                        binding.rvPlaces.setVisibility(View.GONE);
                    } else {
                        binding.tvNoResults.setVisibility(View.GONE);
                        binding.rvPlaces.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }

                    binding.loading.setVisibility(View.GONE);
                } else {
                    Toasty.error(context, "Error while getting liked places. Please try again later.", Toast.LENGTH_LONG, true).show();
                    Log.e(TAG, "Error getting liked", e);
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private Place placeFromParseObject(ParseObject object) throws ParseException, NullPointerException {
        Place place = new Place();
        place.setObjectId(object.getObjectId());
        place.setName(object.fetchIfNeeded().getString(Place.KEY_NAME));
        place.setImage(object.fetchIfNeeded().getParseFile(Place.KEY_IMAGE));

        // Get other objects (user & category)
        ParseQuery<ParseObject> categoryQuery = ParseQuery.getQuery("Category");
        categoryQuery.getInBackground(
                object.fetchIfNeeded().getParseObject(Place.KEY_CATEGORY).getObjectId(), new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        if(e == null) {
                            place.setCategory(object);
                        }
                    }
                });

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.getInBackground(
                object.fetchIfNeeded().getParseUser(Place.KEY_USER).getObjectId(), new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser object, ParseException e) {
                        if(e == null) {
                            place.setUser(object);
                        }
                    }
                });

        return place;
    }
}
