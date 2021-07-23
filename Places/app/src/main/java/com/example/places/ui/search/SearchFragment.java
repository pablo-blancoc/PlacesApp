package com.example.places.ui.search;

import androidx.lifecycle.ViewModelProvider;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.places.MainActivity;
import com.example.places.R;
import com.example.places.adapters.SearchResultsAdapter;
import com.example.places.databinding.LikedFragmentBinding;
import com.example.places.databinding.SearchFragmentBinding;
import com.example.places.models.EndlessRecyclerViewScrollListener;
import com.example.places.models.Place;
import com.example.places.models.SearchResult;
import com.example.places.models.User;
import com.example.places.ui.liked.LikedViewModel;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    // Constants
    private final static String TAG = "SearchFragment";
    private static final int MAX_RESULTS = 20;

    // Attributes
    private SearchViewModel searchViewModel;
    private SearchFragmentBinding binding;
    private boolean isSearchUsers = true;
    private List<SearchResult> results;
    private SearchResultsAdapter adapter;
    private String searchText;
    private int pager;
    EndlessRecyclerViewScrollListener scrollListener;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        this.binding = SearchFragmentBinding.inflate(inflater, container, false);
        View root = this.binding.getRoot();

        // Create instance of searchResults
        this.results = new ArrayList<>();

        // Instantiate adapter
        this.adapter = new SearchResultsAdapter(getContext(), this.results);
        this.binding.rvResults.setAdapter(adapter);
        this.binding.rvResults.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up endless scrolling
        this.scrollListener = new EndlessRecyclerViewScrollListener((LinearLayoutManager) this.binding.rvResults.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                pager++;
                search();
            }
        };

        // Set on scrollListener
        this.binding.rvResults.addOnScrollListener(this.scrollListener);

        // Search clickListener
        this.binding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                results.clear();
                adapter.notifyDataSetChanged();
                searchText = binding.etSearch.getText().toString();
                pager = 0;
                search();
            }
        });

        // Change search type
        this.binding.rgSearchType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Check which radio button was clicked
                switch(checkedId) {
                    case R.id.radio_users:
                        isSearchUsers = true;
                        break;
                    case R.id.radio_places:
                        isSearchUsers = false;
                        break;
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Search for results
     */
    private void search() {
        if(this.isSearchUsers) {
            searchUsers();
        } else {
            searchPlaces();
        }
    }

    /**
     * Searches for users with the given search words
     */
    private void searchUsers() {
        this.onStartLoading();

        ParseQuery<User> query = ParseQuery.getQuery(User.class);
        query.whereContains(User.KEY_NAME, this.searchText);
        query.whereNotEqualTo(User.KEY_OBJECT_ID, ParseUser.getCurrentUser().getObjectId());
        query.setLimit(MAX_RESULTS);
        query.setSkip(this.pager * MAX_RESULTS);
        query.findInBackground(new FindCallback<User>() {
            @Override
            public void done(List<User> users, ParseException e) {
                if(e == null) {
                    for(int i=0; i<users.size(); i++) {
                        User user = users.get(i);
                        String imageUrl;
                        try {
                            imageUrl = user.getProfilePicture().getUrl();
                        } catch (NullPointerException ex) {
                            imageUrl = "";
                        }
                            results.add(new SearchResult(true, user.getName(), user.getUsername(), imageUrl, user.getObjectId()));
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error while searching users", e);
                }

                onStopLoading();
            }
        });
    }

    /**
     * Searches for places with the given search words
     */
    private void searchPlaces() {
        this.onStartLoading();

        ParseQuery<Place> query = ParseQuery.getQuery(Place.class);
        query.whereContains(Place.KEY_NAME, this.searchText);
        query.include("category");
        query.whereEqualTo("public", true);
        query.setLimit(MAX_RESULTS);
        query.setSkip(this.pager * MAX_RESULTS);
        query.findInBackground(new FindCallback<Place>() {
            @Override
            public void done(List<Place> places, ParseException e) {
                if(e == null) {
                    for(int i=0; i<places.size(); i++) {
                        Place place = places.get(i);
                        try {
                            results.add(new SearchResult(false, place.getName(), place.getCategory().getString("name"), "", place.getObjectId()));
                        } catch (NullPointerException ex) {
                            Log.e(TAG, "user not added: " + i, ex);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error while searching users", e);
                }

                onStopLoading();
            }
        });
    }

    private void onStartLoading() {
        this.binding.loading.setVisibility(View.VISIBLE);
        this.binding.tvNoResults.setVisibility(View.GONE);
    }

    private void onStopLoading() {
        if(this.results.size() > 0) {
            this.binding.rvResults.setVisibility(View.VISIBLE);
        } else {
            this.binding.tvNoResults.setVisibility(View.VISIBLE);
        }
        this.binding.loading.setVisibility(View.GONE);
    }

}