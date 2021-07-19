package com.example.places;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.places.adapters.SearchResultsAdapter;
import com.example.places.databinding.ActivityFollowersBinding;
import com.example.places.models.SearchResult;
import com.example.places.models.User;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FollowersActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = "FollowersActivity";

    // Attributes
    private ActivityFollowersBinding binding;
    private SearchResultsAdapter adapter;
    private List<SearchResult> users;
    private int key;
    private ParseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        // Setup ViewBinding
        binding = ActivityFollowersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get intent
        Intent intent = getIntent();
        String uid = intent.getStringExtra("user");
        int type = intent.getIntExtra("type", 0);
        if(type == 1 || type == -1) {
            this.key = type;
        } else if(uid.isEmpty()) {
            Toast.makeText(this, "Unknown error", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Unknown error", Toast.LENGTH_LONG).show();
            finish();
        }
        getUser(uid);

        // Setup actionBar title
        if(this.key == 1) {
            getSupportActionBar().setTitle("Followers");
        } else {
            getSupportActionBar().setTitle("Following");
        }

        // Set up and initialize adapter
        this.users = new ArrayList<>();
        this.adapter = new SearchResultsAdapter(this, this.users);
        this.binding.rlUsers.setAdapter(this.adapter);
        this.binding.rlUsers.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Gets the user who's profile the user is seeing
     */
    private void getUser(String uid) {
        this.binding.rlUsers.setVisibility(View.GONE);
        this.binding.loading.setVisibility(View.VISIBLE);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(User.KEY_OBJECT_ID, uid);
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser _user, ParseException e) {
                if(e == null) {
                    user = _user;
                    queryUsers();
                } else {
                    Log.e(TAG, "Error retrieving user: " + uid, e);
                    Toast.makeText(FollowersActivity.this, "Unknown error", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    /**
     * Gets the list of users to display on screen
     */
    private void queryUsers() {
        String key;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Follower");
        query.orderByDescending("createdAt");

        if(this.key == 1) {
            // Followers
            query.whereEqualTo("following", this.user);
            key = "follower";
        } else {
            // Following
            query.whereEqualTo("follower", this.user);
            key = "following";
        }
        query.include(key);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null) {
                    String imageUrl;

                    for(int i=0; i<objects.size(); i++) {
                        imageUrl = "";
                        ParseUser user = objects.get(i).getParseUser(key);

                        try {
                            imageUrl = user.getParseFile(User.KEY_PROFILE_PICTURE).getUrl();
                        } catch (NullPointerException ex) {
                            imageUrl = "";
                        }

                        try {
                            users.add(new SearchResult(true, user.getString(User.KEY_NAME), user.getString("username"), imageUrl, user.getObjectId()));
                        } catch (NullPointerException ex) {
                            Log.e(TAG, "User not added: " + user.getObjectId(), ex);
                        }
                    }

                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(FollowersActivity.this, "Unknown error", Toast.LENGTH_LONG).show();
                    finish();
                }

                binding.rlUsers.setVisibility(View.VISIBLE);
                binding.loading.setVisibility(View.GONE);
            }
        });
    }
}