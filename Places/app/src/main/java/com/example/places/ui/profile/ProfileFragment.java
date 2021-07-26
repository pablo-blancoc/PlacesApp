package com.example.places.ui.profile;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.places.FollowersActivity;
import com.example.places.MainActivity;
import com.example.places.ProfileActivity;
import com.example.places.R;
import com.example.places.TakePictureActivity;
import com.example.places.adapters.ProfilePlacesAdapter;
import com.example.places.adapters.SearchResultsAdapter;
import com.example.places.databinding.ActivityProfileBinding;
import com.example.places.databinding.LikedFragmentBinding;
import com.example.places.databinding.ProfileFragmentBinding;
import com.example.places.models.Place;
import com.example.places.models.User;
import com.example.places.ui.liked.LikedViewModel;
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

public class ProfileFragment extends Fragment {

    // Constants
    private final static String TAG = "ProfileFragment";

    // Attributes
    private ProfileViewModel profileViewModel;
    private ProfileFragmentBinding binding;
    private Context context;
    private List<Place> places;
    private ProfilePlacesAdapter adapter;
    User user;
    int followerCount = 0, followingCount = 0;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = ProfileFragmentBinding.inflate(inflater, container, false);
        View root = this.binding.getRoot();
        this.context = getContext();

        // Create adapter and set it
        this.places = new ArrayList<>();
        this.adapter = new ProfilePlacesAdapter(this.context, this.places);
        this.binding.rvPlaces.setAdapter(this.adapter);
        this.binding.rvPlaces.setLayoutManager(new GridLayoutManager(context, 3));

        // Add clickListener to modify the name of the user
        this.binding.tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUpdateTextInformation(context, "Enter new name:", "name");
            }
        });

        // Add clickListener to modify the bio of the user
        this.binding.tvBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUpdateTextInformation(context, "Enter new bio:", "bio");
            }
        });

        // Add clickListener to update profile picture
        this.binding.ivProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TakePictureActivity.class);
                startActivity(intent);
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

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Get logged user and fill information
        this.places.clear();
        this.getUser(ParseUser.getCurrentUser().getObjectId());
    }

    /**
     * Get the information from the user that wants to be shown and bind the information
     * @param uid: The user that wants to be shown
     */
    private void getUser(String uid) {
        this.binding.loading.setVisibility(View.VISIBLE);

        ParseQuery<User> query = ParseQuery.getQuery(User.class);
        query.whereEqualTo(User.KEY_OBJECT_ID, uid);
        query.getFirstInBackground(new GetCallback<User>() {
            @Override
            public void done(User object, ParseException e) {
                if(e == null) {
                    user = object;
                    try {
                        bindInformation();
                        getPlaces();
                    } catch (NullPointerException ex) {
                        Toasty.error(context, "Error while retrieving information. Check internet connection and try again later.", Toast.LENGTH_LONG, true).show();
                    }
                } else {
                    Toasty.error(context, "Invalid user", Toast.LENGTH_LONG, true).show();
                }
            }
        });
    }

    /**
     * Binds the information of the logged user into the page
     */
    private void bindInformation() throws NullPointerException {
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
                    try {
                        followerCount = _object.getInt("followerCount");
                        followingCount = _object.getInt("followingCount");
                        binding.tvFollowersCount.setText(String.format("FOLLOWERS: %d", followerCount));
                        binding.tvFollowingCount.setText(String.format("FOLLOWING: %d", followingCount));
                    } catch (NullPointerException ex) {
                        Toasty.error(context, "Error while retrieving information. Check internet connection and try again later.", Toast.LENGTH_LONG, true).show();
                        Log.e(TAG, "NULL POINTER EXCEPTION ON QUERY RELATIONS");
                    }
                } else {
                    Toasty.error(context, "Error while retrieving information. Check internet connection and try again later.", Toast.LENGTH_LONG, true).show();
                    Log.e(TAG, "Error getting counts", e);
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
        query.orderByDescending(Place.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Place>() {
            @Override
            public void done(List<Place> _places, ParseException e) {
                if(e == null) {
                    places.addAll(_places);
                    adapter.notifyDataSetChanged();
                } else {
                    Toasty.error(context, "Error while retrieving information. Check internet connection and try again later.", Toast.LENGTH_LONG, true).show();
                    Log.e(TAG, "Error while getting places", e);
                }

                binding.loading.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void onUpdateTextInformation(Context context, String title, String data) {
        final EditText taskEditText = new EditText(context);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(taskEditText)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputText = String.valueOf(taskEditText.getText());

                        if(data.equals("name")) {
                            user.setName(inputText);
                            user.saveInBackground();
                            binding.tvName.setText(inputText);
                        } else if(data.equals("bio")) {
                            user.setBio(inputText);
                            user.saveInBackground();
                            binding.tvBio.setText(inputText);
                        }

                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
        dialog.show();
    }

    /**
     * Goes to followers page
     * @param i: type of option clicked
     *          1: Followers
     *         -1: Following
     */
    private void goToFollowers(int i) {
        Intent intent = new Intent(this.context, FollowersActivity.class);
        intent.putExtra("type", i);
        intent.putExtra("user", this.user.getObjectId());
        startActivity(intent);
    }

}