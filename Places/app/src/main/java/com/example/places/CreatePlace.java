package com.example.places;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.Dialog;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.places.databinding.ActivityCreatePlaceBinding;
import com.example.places.databinding.ActivityLoginBinding;
import com.example.places.databinding.ActivityMainBinding;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jetbrains.annotations.NotNull;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.PermissionUtils;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class CreatePlace extends AppCompatActivity {

    // Constants
    private final static String TAG = "CreatePlace";
    private final static String KEY_LOCATION = "location";

    // Attributes
    private ActivityCreatePlaceBinding binding;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private PermissionsDispatcher dispatcher;
    private Boolean addedMarker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup ViewBinding
        binding = ActivityCreatePlaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Since KEY_LOCATION was found in the Bundle, we can be sure that there was a last location saved
        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION) && dispatcher != null) {
            dispatcher.mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

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
            dispatcher.displayLocation();
            dispatcher.startLocationUpdatesWithPermissionCheck(this);
        }
    }

    /**
     * Loads the map
     * Uses the special class WorkaroundMapFragment to intercept any touchEvent from
     *  ScrollView so that when touching the map the screen stays the same.
     *  Also creates an instance of PermissionDispatcher to update the map on the user's current
     *  location regularly in the background of the app
     * @param googleMap: the map from the layout
     */
    protected void loadMap(GoogleMap googleMap) {
        this.map = googleMap;
        if (this.map == null) {
            Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show();
            return;
        }

        // Setup PermissionsDispatcher to get current location and start updating the map
        dispatcher = new PermissionsDispatcher(map, CreatePlace.this);
        dispatcher.getMyLocationWithPermissionCheck(CreatePlace.this);
        dispatcher.startLocationUpdatesWithPermissionCheck(CreatePlace.this);

        // Set map settings, and create listener to intercept clicks so that map is able to move
        this.map.getUiSettings().setZoomControlsEnabled(false);
        ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .setListener(new WorkaroundMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch() {
                        binding.scrollView.requestDisallowInterceptTouchEvent(true);
                    }
                });

        // Set map onLongClickListener to place a marker on it
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng position) {
                if(addedMarker) {
                    return;
                }

                addedMarker = true;
                map.addMarker(new MarkerOptions()
                        .position(position)
                        .draggable(true));

            }
        });
    }


}