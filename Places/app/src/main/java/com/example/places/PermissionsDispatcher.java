package com.example.places;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.String;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.PermissionUtils;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

final class PermissionsDispatcher extends AppCompatActivity {

    // Constants
    private static final String TAG = "PermissionsDispatcher";
    private static final int REQUEST_GETMYLOCATION = 0;
    private static final String[] PERMISSION_GETMYLOCATION = new String[] {"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION"};
    private static final int REQUEST_STARTLOCATIONUPDATES = 1;
    private static final String[] PERMISSION_STARTLOCATIONUPDATES = new String[] {"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION"};

    // Attributes
    private LocationRequest mLocationRequest;
    public Location mCurrentLocation;
    private GoogleMap map;
    private Context context;
    private boolean firstUpdate = true;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */

    /**
     * Constructor for the class
     * @param map: the map it is updating
     * @param context: the context from where it is called
     */
    public PermissionsDispatcher(GoogleMap map, Context context) {
        this.map = map;
        this.context = context;
    }

    /**
     * Get the phone location using by requesting permissions
     */
    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this.context);
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Starts to look for location changes in the intervals specified
     */
    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this.context);
        settingsClient.checkLocationSettings(locationSettingsRequest);
        //noinspection MissingPermission
        getFusedLocationProviderClient(this.context).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    /**
     * Get the user location checking for the pertinent permissions
     * @param activity: The activity it was called from
     */
    void getMyLocationWithPermissionCheck(@NonNull Activity activity) {
        if (PermissionUtils.hasSelfPermissions(activity, PERMISSION_GETMYLOCATION)) {
            getMyLocation();
        } else {
            ActivityCompat.requestPermissions(activity, PERMISSION_GETMYLOCATION, REQUEST_GETMYLOCATION);
        }
    }

    /**
     * Starts updating the location on the map regularly checking the pertinent permissions
     * @param activity: the activity it was called from
     */
    void startLocationUpdatesWithPermissionCheck(@NonNull Activity activity) {
        if (PermissionUtils.hasSelfPermissions(activity, PERMISSION_STARTLOCATIONUPDATES)) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(activity, PERMISSION_STARTLOCATIONUPDATES, REQUEST_STARTLOCATIONUPDATES);
        }
    }

    /**
     * It is called from main thread with the results of the permission check so that the actions
     *  to listen to the location start accordingly, if allowed by the user
     * @param requestCode: the requestCode returned from the requestPermission action
     * @param grantResults: the permissions that where granted by the user
     */
    void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GETMYLOCATION:
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    getMyLocation();
                }
                break;
            case REQUEST_STARTLOCATIONUPDATES:
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    startLocationUpdates();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Updates the map locations everytime a new locations is received. Checks if the location is
     *  not null (which may mean the GPS may be turned off)
     * This method is necessary to leave the one that updates the location without parameters
     *  so that it can be called from other classes
     * @param location: the location received
     */
    private void onLocationChanged(Location location) {
        // GPS may be turned off
        if (location == null) {
            Toast.makeText(context, "The GPS may be turned off", Toast.LENGTH_SHORT).show();
            return;
        }

        mCurrentLocation = location;
        if(this.firstUpdate) {
            displayLocation();
            this.firstUpdate = false;
        }
    }

    /**
     * Displays the location on the map that was received when the class was instantiated. This is
     *  the actual function that updates the maps location
     */
    public void displayLocation() {
        if (mCurrentLocation != null) {
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            this.map.animateCamera(cameraUpdate);
        } else {
            Log.d(TAG, "GPS location not found, enable GPS");
        }
    }
}
