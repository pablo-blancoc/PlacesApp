package com.example.places;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Toast;

import com.example.places.adapters.PromotionsAdapter;
import com.example.places.databinding.ActivityFindNearbyBinding;
import com.example.places.databinding.ActivityPromotionsBinding;
import com.example.places.models.EndlessRecyclerViewScrollListener;
import com.example.places.models.Promotion;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class PromotionsActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = "PromotionsActivity";

    // Attributes
    ActivityPromotionsBinding binding;
    List<Promotion> promotions;
    PromotionsAdapter adapter;
    EndlessRecyclerViewScrollListener scrollListener;
    int pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup ViewBinding
        binding = ActivityPromotionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup actionBar title
        try {
            getSupportActionBar().setTitle("My promotions");
        } catch (NullPointerException e) {
            Log.e(TAG, "No support action bar");
        }

        // Setup adapter
        this.pager = 0;
        this.promotions = new ArrayList<>();
        this.adapter = new PromotionsAdapter(this, this.promotions);
        this.binding.rvPromotions.setAdapter(this.adapter);
        this.binding.rvPromotions.setLayoutManager(new LinearLayoutManager(this));

        // Setup SwipeContainer and colors for loading
        this.binding.swipeContainer.setColorSchemeResources(R.color.primary, R.color.white, R.color.black);
        this.binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryPromotions(true);
            }
        });

        // Set up endless scrolling
        this.scrollListener = new EndlessRecyclerViewScrollListener((LinearLayoutManager) this.binding.rvPromotions.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                pager++;
                getPromotions();
            }
        };
        this.binding.rvPromotions.addOnScrollListener(this.scrollListener);

        // Get promotions
        this.queryPromotions(false);
    }

    /**
     * Defines how to query for more promotions
     * @param fromSwiper: if the method is called from infinite swiper
     */
    private void queryPromotions(boolean fromSwiper) {
        if(fromSwiper) {
            this.pager = 0;
            this.promotions.clear();
            getPromotions();
            this.binding.swipeContainer.setRefreshing(false);
        } else {
            promotions.clear();
            getPromotions();
        }
    }

    private void getPromotions() {
        binding.loading.setVisibility(View.VISIBLE);

        ParseQuery<Promotion> query = ParseQuery.getQuery(Promotion.class);
        query.whereEqualTo(Promotion.KEY_USER, ParseUser.getCurrentUser());
        query.orderByDescending(Promotion.KEY_CREATED_AT);
        query.setLimit(10);
        query.setSkip(this.pager * 10);
        query.include(Promotion.KEY_PLACE);
        query.findInBackground(new FindCallback<Promotion>() {
            @Override
            public void done(List<Promotion> _promotions, ParseException e) {
                binding.loading.setVisibility(View.GONE);

                if(e == null) {
                    promotions.addAll(_promotions);
                    adapter.notifyDataSetChanged();
                    if(promotions.size() == 0) {
                        binding.tvNoResults.setVisibility(View.VISIBLE);
                        binding.swipeContainer.setVisibility(View.GONE);
                    } else {
                        binding.tvNoResults.setVisibility(View.GONE);
                        binding.swipeContainer.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toasty.error(PromotionsActivity.this, "Error while retrieving results", Toast.LENGTH_SHORT, true).show();
                }
            }
        });
    }


}