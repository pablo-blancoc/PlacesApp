package com.example.places.ui.search;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestHeaders;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import okhttp3.Headers;

public class SearchFragment extends Fragment {

    // Constants
    private final static String TAG = "SearchFragment";
    private static final int MAX_RESULTS = 20;
    private static final String SERVER_URL = "http://192.168.1.90:5000/";

    // Attributes
    private String apiKey;
    private SearchViewModel searchViewModel;
    private SearchFragmentBinding binding;
    private boolean isSearchUsers = true;
    private List<SearchResult> results;
    private SearchResultsAdapter adapter;
    private Context context;
    private String searchText;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        this.binding = SearchFragmentBinding.inflate(inflater, container, false);
        View root = this.binding.getRoot();
        this.context = getContext();

        // Set backend apiKey
        this.apiKey = getString(R.string.server_api_key);

        // Create instance of searchResults
        this.results = new ArrayList<>();

        // Instantiate adapter
        this.adapter = new SearchResultsAdapter(getContext(), this.results);
        this.binding.rvResults.setAdapter(adapter);
        this.binding.rvResults.setLayoutManager(new LinearLayoutManager(getContext()));

        // Search clickListener
        this.binding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                results.clear();
                adapter.notifyDataSetChanged();
                searchText = binding.etSearch.getText().toString();
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
                    Toasty.error(context, "Error while searching users. Please try again later", Toast.LENGTH_LONG, true).show();
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

        // Create a new instance of AsyncHttpClient
        AsyncHttpClient client = new AsyncHttpClient();

        RequestHeaders headers = new RequestHeaders();
        headers.put("x-api-key", this.apiKey);

        RequestParams params = new RequestParams();
        params.put("query", this.searchText);

        client.get(SERVER_URL + "search", headers, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Headers headers, JSON json) {
                JSONObject object = json.jsonObject;
                try {
                    JSONArray placesIds = object.getJSONArray("places");
                    for(int j=0; j<placesIds.length(); j++) {
                        String placeId = placesIds.getString(j);
                        ParseQuery<Place> query = ParseQuery.getQuery(Place.class);
                        query.include(Place.KEY_CATEGORY);
                        Place place = query.get(placeId);
                        results.add(new SearchResult(false, place.getName(), place.getCategory().getString("name"), "", place.getObjectId()));
                    }
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }

                adapter.notifyDataSetChanged();
                onStopLoading();
            }

            @Override
            public void onFailure(int i, Headers headers, String s, Throwable throwable) {
                Log.e(TAG, "Could not query places");
                Log.e(TAG, "Call to backend server failed: " + s, throwable);
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