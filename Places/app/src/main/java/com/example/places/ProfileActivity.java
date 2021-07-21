package com.example.places;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.places.adapters.ProfilePlacesAdapter;
import com.example.places.databinding.ActivityProfileBinding;
import com.example.places.models.Place;
import com.example.places.models.User;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "ProfileActivity";

    // Attributes
    User user;
    ActivityProfileBinding binding;
    List<Place> places;
    ProfilePlacesAdapter adapter;
    String uid;
    int followerCount = 0, followingCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting up binding
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get userId of the profile that wants to be shown
        Intent intent = getIntent();
        this.uid = intent.getStringExtra("user");

        // Create adapter and set it
        this.places = new ArrayList<>();
        this.adapter = new ProfilePlacesAdapter(this, this.places);
        this.binding.rvPlaces.setAdapter(this.adapter);
        this.binding.rvPlaces.setLayoutManager(new GridLayoutManager(this, 3));

        // Follow clickListener
        this.binding.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, String.valueOf(user.following));
                if(user.following) {
                    binding.btnFollow.setText(R.string.follow);
                    binding.btnFollow.setTextColor(getResources().getColor(R.color.white));
                    binding.btnFollow.setBackgroundColor(getResources().getColor(R.color.primary));
                    unfollow();
                } else {
                    binding.btnFollow.setText(R.string.unfollow);
                    binding.btnFollow.setTextColor(getResources().getColor(R.color.primary));
                    binding.btnFollow.setBackgroundColor(getResources().getColor(R.color.white));
                    follow();
                }
                user.following = !user.following;
            }
        });

        // Notify clickListener
        this.binding.btnNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, String.valueOf(user.notify));
                if(user.notify) {
                    binding.btnNotify.setImageResource(R.drawable.icon_bell);
                    setNoNotify();
                } else {
                    binding.btnNotify.setImageResource(R.drawable.icon_no_bell);
                    setNotify();
                }
                user.notify = !user.notify;
            }
        });

        // Followers clickListener
        this.binding.tvFollowersCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFollowers(1);
            }
        });

        // Following clickListener
        this.binding.tvFollowingCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFollowers(-1);
            }
        });
    }

    /**
     * Goes to followers page
     * @param i: type of option clicked
     *          1: Followers
     *         -1: Following
     */
    private void goToFollowers(int i) {
        Intent intent = new Intent(this, FollowersActivity.class);
        intent.putExtra("type", i);
        intent.putExtra("user", this.user.getObjectId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get user information
        this.places.clear();
        this.getUser();
    }

    /**
     * Set notifications for the user you are seeing
     */
    private void setNotify() {
        // Create notify object in database
        ParseObject notify = new ParseObject("Notification");
        notify.put("to", ParseUser.getCurrentUser());
        notify.put("from", this.user);
        notify.saveInBackground();
    }

    /**
     * Delete notifications for the user you are seeing
     */
    private void setNoNotify() {
        // Delete notify object in database
        ParseQuery<ParseObject> q = ParseQuery.getQuery("Notification");
        q.whereEqualTo("to", ParseUser.getCurrentUser());
        q.whereEqualTo("from", this.user);
        q.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null) {
                    for (ParseObject _object : objects) {
                        _object.deleteInBackground();
                    }
                } else {
                    Log.e(TAG, "Error deleting notify class object", e);
                }
            }
        });
    }

    /**
     * Follow the user in which profile you are in
     */
    private void follow() {
        // Update user's follower count
        this.followerCount++;
        this.binding.tvFollowersCount.setText(String.format("FOLLOWERS: %d", this.followerCount));
        this.saveUserCounts(this.user, this.followerCount, this.followingCount);

        // Update signed in user followings count
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Relations");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e == null) {
                    int following = object.getInt("followingCount");
                    object.put("followingCount", following + 1);
                    object.saveInBackground();
                } else {
                    Log.e(TAG, "Error saving own users counts", e);
                }
            }
        });

        // Create follow object in database
        ParseObject follow = new ParseObject("Follower");
        follow.put("follower", ParseUser.getCurrentUser());
        follow.put("following", this.user);
        follow.saveInBackground();
    }

    /**
     * Follow the user in which profile you are in
     */
    private void unfollow() {
        // Update user's follower count
        this.followerCount--;
        this.binding.tvFollowersCount.setText(String.format("FOLLOWERS: %d", this.followerCount));
        this.saveUserCounts(this.user, this.followerCount, this.followingCount);

        // Update signed in user followings count
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Relations");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e == null) {
                    int following = object.getInt("followingCount");
                    object.put("followingCount", following - 1);
                    object.saveInBackground();
                } else {
                    Log.e(TAG, "Error saving own users counts", e);
                }
            }
        });

        // Delete follow object in database
        ParseQuery<ParseObject> q = ParseQuery.getQuery("Follower");
        q.whereEqualTo("follower", ParseUser.getCurrentUser());
        q.whereEqualTo("following", this.user);
        q.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null) {
                    for (ParseObject _object : objects) {
                        _object.deleteInBackground();
                    }
                } else {
                    Log.e(TAG, "Error deleting follower class object", e);
                }
            }
        });
    }

    /**
     * Get the information from the user that wants to be shown and bind the information
     */
    private void getUser() {
        this.binding.loading.setVisibility(View.VISIBLE);

        ParseQuery<User> query = ParseQuery.getQuery(User.class);
        query.whereEqualTo(User.KEY_OBJECT_ID, this.uid);
        query.getFirstInBackground(new GetCallback<User>() {
            @Override
            public void done(User object, ParseException e) {
                if(e == null) {
                    user = object;
                    knowIfFollowing();
                    knowIfNotify();
                    bindInformation();
                    getPlaces();
                } else {
                    Toast.makeText(ProfileActivity.this, "User not found", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    /**
     * Determines if the current user is following the user owner of this profile
     */
    private void knowIfFollowing() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Follower");
        query.whereEqualTo("follower", ParseUser.getCurrentUser());
        query.whereEqualTo("following", this.user);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null) {
                    user.following = objects.size() == 1;

                    // Set follow button text
                    if(user.following) {
                        binding.btnFollow.setText(R.string.unfollow);
                        binding.btnFollow.setTextColor(getResources().getColor(R.color.primary));
                        binding.btnFollow.setBackgroundColor(getResources().getColor(R.color.white));
                    } else {
                        binding.btnFollow.setText(R.string.follow);
                        binding.btnFollow.setTextColor(getResources().getColor(R.color.white));
                        binding.btnFollow.setBackgroundColor(getResources().getColor(R.color.primary));
                    }
                } else {
                    Log.e(TAG, "Problem searching if user follows another user", e);
                }
            }
        });
    }

    /**
     * Determines if the current user has set notifications for the user it is seeing
     */
    private void knowIfNotify() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Notification");
        query.whereEqualTo("to", ParseUser.getCurrentUser());
        query.whereEqualTo("from", this.user);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null) {
                    user.notify = objects.size() == 1;

                    // Set notify button
                    if(user.notify) {
                        binding.btnNotify.setImageResource(R.drawable.icon_no_bell);
                    } else {
                        binding.btnNotify.setImageResource(R.drawable.icon_bell);
                    }
                } else {
                    Log.e(TAG, "Problem searching if user follows another user", e);
                }
            }
        });
    }

    /**
     * Binds the information of the user retrieved into the page
     */
    private void bindInformation() {
        this.binding.tvName.setText(this.user.getName());
        this.binding.tvUsername.setText(String.format("@%s", this.user.getUsername()));
        String imageUrl;
        try {
            imageUrl = user.getProfilePicture().getUrl();
        } catch (NullPointerException e) {
            imageUrl = "";
            Log.e(TAG, "No image for the user");
        }
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .circleCrop()
                .into(this.binding.ivProfilePicture);
        this.binding.tvBio.setText(this.user.getBio());

        // Get followers and following count
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Relations");
        query.whereEqualTo("user", this.user);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject _object, ParseException e) {
                if(e == null) {
                    followerCount = _object.getInt("followerCount");
                    followingCount = _object.getInt("followingCount");
                    binding.tvFollowersCount.setText(String.format("FOLLOWERS: %d", followerCount));
                    binding.tvFollowingCount.setText(String.format("FOLLOWING: %d", followingCount));
                } else {
                    Log.e(TAG, "Error getting counts", e);
                }
            }
        });
    }

    /**
     * Save followersCount and followingCount
     */
    private void saveUserCounts(User _user, int _followerCount, int _followingCount) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Relations");
        query.whereEqualTo("user", _user);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e == null) {
                    object.put("followerCount", _followerCount);
                    object.put("followingCount", _followingCount);
                    object.saveInBackground();
                } else {
                    Log.e(TAG, "Error saving counts", e);
                }
            }
        });
    }

    /**
     * Gets the places that the user has uploaded
     */
    private void getPlaces() {
        ParseQuery<Place> query = ParseQuery.getQuery(Place.class);
        query.whereEqualTo("user", this.user);
        query.whereEqualTo("public", true);
        query.findInBackground(new FindCallback<Place>() {
            @Override
            public void done(List<Place> _places, ParseException e) {
                if(e == null) {
                    places.addAll(_places);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error while getting places", e);
                }

                binding.loading.setVisibility(View.INVISIBLE);
            }
        });
    }
}
