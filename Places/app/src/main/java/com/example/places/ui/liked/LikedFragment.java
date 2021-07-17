package com.example.places.ui.liked;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.example.places.models.Place;
import com.example.places.models.SearchResult;
import com.example.places.ui.home.HomeViewModel;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class LikedFragment extends Fragment {

    // Constants
    private static final String TAG = "LikedFragment";

    // Attributes
    private LikedViewModel likedViewModel;
    private LikedFragmentBinding binding;
    private List<Place> places;
    private FeedAdapter adapter;
    private Context context;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        likedViewModel = new ViewModelProvider(this).get(LikedViewModel.class);

        // Set up binding and context
        binding = LikedFragmentBinding.inflate(inflater, container, false);
        View root = this.binding.getRoot();
        this.context = getContext();

        // Create and setup adapter
        this.places = new ArrayList<>();
        this.adapter = new FeedAdapter(context, this.places);
        this.binding.rvPlaces.setAdapter(this.adapter);
        this.binding.rvPlaces.setLayoutManager(new LinearLayoutManager(context));

        // Query liked places
        getLiked();

        return root;
    }

    /**
     * Get your liked places and display them
     */
    private void getLiked() {
        binding.loading.setVisibility(View.VISIBLE);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Like");
        query.include("place");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.orderByDescending(ParseObject.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                binding.loading.setVisibility(View.GONE);

                if(e == null) {
                    if(objects.size() == 0) {
                        binding.tvNoResults.setVisibility(View.VISIBLE);
                        binding.rvPlaces.setVisibility(View.GONE);
                    } else {
                        for(ParseObject object: objects) {
                            places.add((Place) object.get("place"));
                        }
                        adapter.notifyDataSetChanged();
                        binding.tvNoResults.setVisibility(View.GONE);
                        binding.rvPlaces.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(context, "Error getting liked", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error getting liked", e);
                }
            }
        });


        /*
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

            }
        });

        ParseQuery<Place> outerQuery = ParseQuery.getQuery(Place.class);
        outerQuery.whereMatchesKeyInQuery(Place.KEY_OBJECT_ID, "place", innerQuery);
        outerQuery.include(Place.KEY_USER);
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
                    Toast.makeText(context, "Error getting liked", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error getting liked", e);
                }
            }
        });
        */

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
