package com.example.places;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.places.databinding.ActivityFindNearbyBinding;
import com.example.places.models.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FindNearbyActivity extends AppCompatActivity implements GoogleMap.OnMarkerClickListener {

    // Constants
    private static final String TAG = "FindNearbyActivity";
    private static final String KEY_LOCATION = "location";
    private static final int MAX_RADIUS = 20;

    // Attributes
    ActivityFindNearbyBinding binding;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private PermissionsDispatcher dispatcher;
    private List<Place> places;
    private List<Marker> markers;
    private double radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup ViewBinding
        binding = ActivityFindNearbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup actionBar title
        try {
            getSupportActionBar().setTitle("Find Nearby");
        } catch (NullPointerException e) {
            Log.e(TAG, "No support action bar");
        }

        // Since KEY_LOCATION was found in the Bundle, we can be sure that there was a last location saved
        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION) && dispatcher != null) {
            dispatcher.mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        // Instantiate places list
        this.places = new ArrayList<>();
        this.markers = new ArrayList<>();

        // Setup map
        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
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

        // Setup listener of SeekBar
        binding.tvTitle.setText(String.format("Range: %s km", 0));
        this.binding.sbRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    radius = MAX_RADIUS * progress / 100;
                    binding.tvTitle.setText(String.format("Range: %s km", radius));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Set clickListener to search
        this.binding.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.loading.setVisibility(View.VISIBLE);
                binding.btnSearch.setVisibility(View.GONE);

                findNearbyPlaces();
            }
        });
    }

    /**
     * Queries the backend to receive all places within the kilometers passed and displays them
     */
    private void findNearbyPlaces() {
        ParseGeoPoint currentLocation = new ParseGeoPoint(dispatcher.mCurrentLocation.getLatitude(), dispatcher.mCurrentLocation.getLongitude());

        ParseQuery<Place> query = ParseQuery.getQuery(Place.class);
        query.whereWithinKilometers(Place.KEY_LOCATION, currentLocation, this.radius);
        query.include("category");
        query.findInBackground(new FindCallback<Place>() {
            @Override
            public void done(List<Place> _places, ParseException e) {
                binding.loading.setVisibility(View.GONE);
                binding.btnSearch.setVisibility(View.VISIBLE);

                if(e == null) {
                    for(int i=0; i<markers.size(); i++) {
                        markers.get(i).remove();
                    }
                    places.clear();
                    markers.clear();
                    places.addAll(_places);
                    for(int i=0; i<places.size(); i++) {
                        Place place = places.get(i);

                        Marker addedMarker = map.addMarker(new MarkerOptions()
                                .position(new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude()))
                                .title(place.getName()));
                        addedMarker.setTag(place);
                        markers.add(addedMarker);
                    }
                } else {
                    Toast.makeText(FindNearbyActivity.this, "Could not retrieve places", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * When the app stops (by any reason) this method is called to preserve instances
     *  Location is preserved
     * @param savedInstanceState: The instance to be saved
     */
    @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
        if(dispatcher != null) {
            savedInstanceState.putParcelable(KEY_LOCATION, dispatcher.mCurrentLocation);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     *  A permission request was made to the user, such like the "Access this device location"
     *      and it enters this function when the result is given
     * @param requestCode: The code of the request made
     * @param permissions: The permission requested
     * @param grantResults: The permissions granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(dispatcher != null) {
            dispatcher.onRequestPermissionsResult(requestCode, grantResults);
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * When an activity stops and then resumes, this method is called to reestablish information
     */
    @Override
    protected void onResume() {
        super.onResume();

        if(dispatcher != null) {
            dispatcher.firstUpdate = false;
            dispatcher.startLocationUpdatesWithPermissionCheck(this);
        }
    }

    /**
     * Loads the map
     * Creates an instance of PermissionDispatcher to update the map on the user's current
     *  location regularly in the background of the app
     * @param googleMap: the map from the layout
     */
    protected void loadMap(GoogleMap googleMap) {
        this.map = googleMap;
        if (this.map == null) {
            Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show();
            return;
        }

        // Setup marker clickListener
        this.map.setOnMarkerClickListener(this);

        // Setup PermissionsDispatcher to get current location and start updating the map
        dispatcher = new PermissionsDispatcher(map, FindNearbyActivity.this);
        dispatcher.zoom = 10;
        dispatcher.getMyLocationWithPermissionCheck(FindNearbyActivity.this);
        dispatcher.startLocationUpdatesWithPermissionCheck(FindNearbyActivity.this);
    }

    /**
     * Allows the user to see the information of the place the clicked
     * @param marker: the marker clicked
     * @return boolean
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        Place place = (Place) marker.getTag();

        binding.tvPlaceName.setText(place.getName());
        binding.tvPlaceCategory.setText(String.format("CATEGORY: %s", place.getCategory().getString("name")));
        Glide.with(this)
                .load(place.getImage().getUrl())
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(binding.ivPlaceImage);
        binding.rlPlace.setVisibility(View.VISIBLE);

        binding.btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FindNearbyActivity.this, PlaceDetailActivity.class);
                intent.putExtra("place", place.getObjectId());
                startActivity(intent);
            }
        });

        binding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.rlPlace.setVisibility(View.GONE);
            }
        });

        return true;
    }
}