package com.example.places.ui.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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

import com.bumptech.glide.Glide;
import com.example.places.MainActivity;
import com.example.places.PlaceDetailActivity;
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

import es.dmoral.toasty.Toasty;

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

        // Take out foreground to start activity
        binding.swipeContainer.getForeground().setAlpha(0);

        // Present the promotions if there are any
        this.presentPromotions();

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
                    Toasty.error(context, "Error while getting timeline. Please try again later.", Toast.LENGTH_LONG, true).show();
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

    /**
     * Checks on Parse if there are any promotions that should be shown to the user.
     *  If so, it makes sure the user sees them and acts on them
     */
    private void presentPromotions() {

        // Know if there is a promotion to be shown
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PendingPromotion");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.include("promotion");

        try {
            // Get promotion if there is one
            ParseObject pendingPromotion = query.getFirst();
            String promotionId = pendingPromotion.getParseObject("promotion").getObjectId();
            String placeId = pendingPromotion.getParseObject("promotion").fetchIfNeeded().getParseObject("place").getObjectId();

            // If there is a place. Else there is an error getting the promotion
            if(placeId != null) {
                // get place passed
                ParseQuery<Place> queryPlace = ParseQuery.getQuery(Place.class);
                Place place = queryPlace.get(placeId);

                // Setup the view
                this.binding.promotionName.setText(place.getName());
                Glide.with(this)
                        .load(place.getImage().getUrl())
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error)
                        .centerCrop()
                        .into(this.binding.promotionImage);

                // Update the promotion's viewed attribute
                ParseQuery<ParseObject> queryPromotion = ParseQuery.getQuery("Promotion");
                ParseObject promotion = queryPromotion.get(promotionId);
                promotion.increment("viewed", 1);
                promotion.saveInBackground();

                // Ignore the promotion
                this.binding.promotionIgnore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        binding.rlPromotion.setVisibility(View.GONE);
                        binding.swipeContainer.getForeground().setAlpha(0);
                    }
                });

                // View the place of the promotion
                this.binding.promotionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        binding.rlPromotion.setVisibility(View.GONE);
                        binding.swipeContainer.getForeground().setAlpha(0);

                        // update the clicked attribute of the promotion
                        promotion.increment("clicked", 1);
                        promotion.saveInBackground();

                        // go to the place viewed
                        Intent intentPlace = new Intent(context, PlaceDetailActivity.class);
                        intentPlace.putExtra("place", placeId);
                        startActivity(intentPlace);
                    }
                });

                binding.rlPromotion.setVisibility(View.VISIBLE);
                binding.swipeContainer.getForeground().setAlpha(240);
            }

            // Delete gotten promotion
            pendingPromotion.deleteInBackground();

        } catch (ParseException | NullPointerException e) {
            e.printStackTrace();
        }
    }

}