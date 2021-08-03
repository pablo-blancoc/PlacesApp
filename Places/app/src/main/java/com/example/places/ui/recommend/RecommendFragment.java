package com.example.places.ui.recommend;

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
import android.widget.Toast;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestHeaders;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.places.MainActivity;
import com.example.places.R;
import com.example.places.adapters.FeedAdapter;
import com.example.places.adapters.RecommendationsAdapter;
import com.example.places.databinding.FragmentHomeBinding;
import com.example.places.databinding.LikedFragmentBinding;
import com.example.places.databinding.RecommendFragmentBinding;
import com.example.places.models.EndlessRecyclerViewScrollListener;
import com.example.places.models.Place;
import com.example.places.ui.home.HomeViewModel;
import com.example.places.ui.liked.LikedViewModel;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import okhttp3.Headers;

public class RecommendFragment extends Fragment {

    // Constants
    private static final String TAG = "RecommendFragment";
    private static final String SERVER_URL = "http://192.168.1.90:5000/";

    // Attributes
    private String apiKey;
    private Context context;
    private List<Place> places;
    private RecommendationsAdapter adapter;
    private RecommendViewModel recommendViewModel;
    private RecommendFragmentBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        recommendViewModel = new ViewModelProvider(this).get(RecommendViewModel.class);

        // Set up binding and context
        binding = RecommendFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        this.context = getContext();

        // Set backend apiKey
        this.apiKey = getString(R.string.server_api_key);

        // Create and setup adapter
        this.places = new ArrayList<>();
        this.adapter = new RecommendationsAdapter(context, this.places);
        this.binding.rvPlaces.setAdapter(this.adapter);
        this.binding.rvPlaces.setLayoutManager(new LinearLayoutManager(context));

        // Get your top 5 recommendations
        getRecommendations();

        return root;
    }

    private void getRecommendations() {
        String userId = ParseUser.getCurrentUser().getObjectId();

        // Create a new instance of AsyncHttpClient
        AsyncHttpClient client = new AsyncHttpClient();

        RequestHeaders headers = new RequestHeaders();
        headers.put("x-api-key", this.apiKey);

        RequestParams params = new RequestParams();
        params.put("user", userId);

        client.get(SERVER_URL + "recommend", headers, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Headers headers, JSON json) {
                JSONObject object = json.jsonObject;
                try {
                    JSONArray placesIds = object.getJSONArray("places");
                    for(int j=0; j<placesIds.length(); j++) {
                        String placeId = placesIds.getString(j);
                        ParseQuery<Place> query = ParseQuery.getQuery(Place.class);
                        query.include(Place.KEY_CATEGORY);
                        places.add(query.get(placeId));
                    }
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                binding.loading.setVisibility(View.GONE);
                binding.rvPlaces.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int i, Headers headers, String s, Throwable throwable) {
                // Log the error
                Log.e(TAG, "Call to backend server failed: " + s, throwable);

                if (throwable.getClass().getName().equals(ConnectException.class.getName())) {
                    // Connection error
                    onToastError("Cannot connect to the server.\nTry again later.");
                } else {
                    // Other error
                    onToastError("Unknown error.\nTry again later.");
                }
            }
        });


    }

    private void onToastError(String text) {
        requireActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toasty.error(context, text, Toasty.LENGTH_LONG, true).show();

                // Stop loading spinner
                binding.loading.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}