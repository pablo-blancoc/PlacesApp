package com.example.places;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.places.databinding.ActivityPlaceDetailBinding;
import com.example.places.models.Place;
import com.example.places.models.User;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class PlaceDetailActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "PlaceDetailActivity";

    // Attributes
    private ActivityPlaceDetailBinding binding;
    private Place place;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private int deleteStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting up binding
        binding = ActivityPlaceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get intent with objectId of the place
        Intent intent = getIntent();
        String uid = intent.getStringExtra("place");
        this.getPlace(uid);

        // Setup map with WorkaroundFragment so that drag & move still work
        mapFragment = ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        } else {
            Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * Loads the map
     * Uses the special class WorkaroundMapFragment to intercept any touchEvent from
     *  ScrollView so that when touching the map the screen stays the same.
     * @param googleMap: the map from the layout
     */
    protected void loadMap(GoogleMap googleMap) {
        this.map = googleMap;
        if (this.map == null) {
            Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set map settings, and create listener to intercept clicks so that map is able to move
        this.map.getUiSettings().setZoomControlsEnabled(false);
        ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .setListener(new WorkaroundMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch() {
                        binding.scrollView.requestDisallowInterceptTouchEvent(true);
                    }
                });
    }

    /**
     * Gets the information of the place passed from Parse server backed
     * @param _id: The unique objectId of the place that wants to be retrieved
     */
    private void getPlace(String _id) {
        this.binding.loading.setVisibility(View.VISIBLE);

        ParseQuery<Place> query = ParseQuery.getQuery(Place.class);
        query.whereEqualTo(Place.KEY_OBJECT_ID, _id);
        query.include(Place.KEY_CATEGORY);
        query.include(Place.KEY_USER);
        query.getFirstInBackground(new GetCallback<Place>() {
            @Override
            public void done(Place object, ParseException e) {
                if(e == null) {
                    place = object;
                    bindInformation();
                } else {
                    Toast.makeText(PlaceDetailActivity.this, "Place not found", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    /**
     * Binds the information of the place with the corresponding part of the layout
     */
    private void bindInformation() {
        // Fill information
        this.binding.tvName.setText(this.place.getName());
        this.binding.tvDescription.setText(this.place.getDescription());
        this.binding.chipLikes.setText(String.format("%d likes", this.place.getLikeCount()));
        this.binding.tvAddress.setText(this.place.getAddress());
        this.binding.tvCategory.setText(this.place.getCategory().getString("name"));
        this.binding.rbPrice.setRating(this.place.getPrice());

        // Add marker to map
        LatLng placePosition = new LatLng(this.place.getLat(), this.place.getLng());
        this.map.addMarker(new MarkerOptions()
                .position(placePosition)
                .title(this.place.getName()));

        // Move camera to marker
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(placePosition, 17);
        this.map.animateCamera(cameraUpdate);

        // Load image
        Glide.with(PlaceDetailActivity.this)
                .load(this.place.getImage().getUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .centerCrop()
                .into(this.binding.ivImage);

        // If author is same as logged user then display edit actions
        if(this.place.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            this.binding.rlAuthor.setVisibility(View.GONE);
            this.binding.rlEdit.setVisibility(View.VISIBLE);
        } else {
            this.binding.rlAuthor.setVisibility(View.VISIBLE);
            this.binding.rlEdit.setVisibility(View.GONE);

            this.binding.rlAuthor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PlaceDetailActivity.this, ProfileActivity.class);
                    intent.putExtra("user", place.getUser().getObjectId());
                    startActivity(intent);
                }
            });
        }

        // Set author's information
        String authorProfileImage, authorName, authorUsername;
        try {
            authorProfileImage = this.place.getUser().getParseFile(User.KEY_PROFILE_PICTURE).getUrl();
        } catch (NullPointerException e) {
            authorProfileImage = "";
            Log.e(TAG, "Author doesn't have image");
        }
        try {
            authorName = this.place.getUser().get(User.KEY_NAME).toString();
        } catch (NullPointerException e) {
            authorName = "[NO NAME]";
            Log.e(TAG, "Author doesn't have image");
        }
        try {
            authorUsername = String.format("@%s", this.place.getUser().get("username").toString());
        } catch (NullPointerException e) {
            authorUsername = "[NO NAME USERNAME]";
            Log.e(TAG, "Author doesn't have image");
        }
        this.binding.tvAuthorName.setText(authorName);
        this.binding.tvAuthorUsername.setText(authorUsername);
        Glide.with(PlaceDetailActivity.this)
                .load(authorProfileImage)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .circleCrop()
                .into(this.binding.ivAuthorImage);


        // Set edit actions
        if(this.place.getPublic()) {
            this.binding.btnPublic.setText(R.string.make_private);
        } else {
            this.binding.btnPublic.setText(R.string.make_public);
        }
        this.binding.btnPublic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlacePrivacy();
            }
        });

        // Set delete listener
        this.binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePlace();
            }
        });

        // Set call fab action listener
        this.binding.fabCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:" + place.getPhone();

                if(uri.length() < 5) {
                    Toast.makeText(PlaceDetailActivity.this, "This place doesn't have a phone", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });


        this.binding.loading.setVisibility(View.GONE);
    }

    /**
     * Deletes the place
     */
    private void deletePlace() {
        if(this.deleteStep == 0) {
            this.binding.btnDelete.setText(R.string.sure_question);
            this.deleteStep++;
        } else {
            this.deleteStep = 0;
            this.place.deleteInBackground(new DeleteCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null) {
                        Toast.makeText(PlaceDetailActivity.this, "Place deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(PlaceDetailActivity.this, "Could not delete place", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * Changes the privacy of the place (public <-> private)
     */
    private void changePlacePrivacy() {
        this.place.setPublic(!this.place.getPublic());
        this.place.saveInBackground();

        if(this.place.getPublic()) {
            this.binding.btnPublic.setText(R.string.make_private);
        } else {
            this.binding.btnPublic.setText(R.string.make_public);
        }
    }
}