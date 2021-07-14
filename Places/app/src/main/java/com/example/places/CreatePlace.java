package com.example.places;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.places.databinding.ActivityCreatePlaceBinding;
import com.example.places.databinding.ActivityLoginBinding;
import com.example.places.databinding.ActivityMainBinding;
import com.example.places.models.Place;
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
import com.google.common.util.concurrent.ListenableFuture;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.PermissionUtils;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class CreatePlace extends AppCompatActivity {

    // Constants
    private final static String TAG = "CreatePlace";
    private final static String KEY_LOCATION = "location";
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    // Attributes
    private ActivityCreatePlaceBinding binding;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private PermissionsDispatcher dispatcher;
    private Boolean addedMarker = false;
    private ImageCapture imageCapture;
    private File outputDirectory;
    private ExecutorService cameraExecutor;
    private ParseObject category;
    File image;
    Marker placeLocation;
    private boolean sharePost = true;

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

        // Listener to take picture
        binding.btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        // setting up the output directory and creating new thread for taking pictures
        outputDirectory = getOutputDirectory();
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Start camera
        if(hasCameraPermission()) {
            enableCamera();
        } else {
            requestCameraPermission();
        }

        // listener to post a place
        binding.btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.etDescription.getText().toString().isEmpty()) {
                    Toast.makeText(CreatePlace.this, "Description can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!addedMarker) {
                    Toast.makeText(CreatePlace.this, "A marker hasn't been created", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!(binding.rbPrice.getRating() > 0)) {
                    Toast.makeText(CreatePlace.this, "Price hasn't been set", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(binding.etAddress.getText().toString().isEmpty()) {
                    Toast.makeText(CreatePlace.this, "Address can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(category == null) {
                    Toast.makeText(CreatePlace.this, "You need to select a category", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(binding.etName.getText().toString().isEmpty()) {
                    Toast.makeText(CreatePlace.this, "Place's name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(image == null || binding.ivImage.getDrawable() == null) {
                    Toast.makeText(CreatePlace.this, "You need to take an image", Toast.LENGTH_SHORT).show();
                    return;
                }
                post();
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
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                enableCamera();
            } else {
                Toast.makeText(this, "Permissions not grated by the user", Toast.LENGTH_SHORT).show();
                finish();
            }
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
                placeLocation = map.addMarker(new MarkerOptions()
                        .position(position)
                        .draggable(true));

            }
        });
    }


    /**
     * Returns whether the user has granted access to use the camera
     * @return boolean
     */
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests the user the permission to use the camera
     */
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, CAMERA_PERMISSION, CAMERA_REQUEST_CODE);
    }

    /**
     * Takes a picture using CameraX
     */
    private void takePhoto() {
        if(imageCapture == null){
            return;
        }

        // set up the photo file for storing the photo
        File photoFile = new File(outputDirectory, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg");

        // save the image and wait for callback.
        ImageCapture.OutputFileOptions fileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(fileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Uri savedUri = Uri.fromFile(photoFile);
                String msg = "Photo Capture succeeded " + savedUri.toString();
                Log.d(TAG, msg);

                // View image on page and take our button to take picture
                binding.btnImage.setVisibility(View.GONE);
                binding.pvImage.setVisibility(View.GONE);
                binding.ivImage.setVisibility(View.VISIBLE);

                // Save image into File variable
                image = new File(savedUri.getPath());

                // Load image with Glide
                Glide.with(CreatePlace.this)
                        .load(image)
                        .into(binding.ivImage);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(CreatePlace.this, "Image could not be saved. Try again later.", Toast.LENGTH_SHORT).show();
                Log.i("Image Capture", exception.toString());
            }
        });
    }

    /**
     * Enables the camera in order to take a picture
     */
    private void enableCamera() {
        ListenableFuture<ProcessCameraProvider> processCameraProvider = ProcessCameraProvider.getInstance(this);

        // the main logic is in a listener.
        processCameraProvider.addListener(new Runnable() {
            @Override
            public void run() {
                ProcessCameraProvider cameraProvider = null;
                try {
                    cameraProvider = processCameraProvider.get();
                } catch (Exception e) {
                    Log.e(TAG, "Exception on enableCamera", e);
                }

                // set up preview window
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.pvImage.createSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // tie the preview, camera selector and imageCapture together via the cameraProvider
                try {
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(CreatePlace.this, cameraSelector, preview, imageCapture);
                } catch (Exception e){
                    Log.e(TAG, "Exception on enableCamera", e);
                }
            }


        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Gets the output directory of the picture if it is to be taken
     * @return File
     */
    private File getOutputDirectory() {

        File mediaDirs = getExternalMediaDirs()[0];
        File newFiles = null;

        if (mediaDirs != null) {
            newFiles = new File(mediaDirs, getResources().getString(R.string.app_name));
            newFiles.mkdirs();
        }
        if (newFiles != null && mediaDirs.exists()) {
            return mediaDirs;
        } else
            return getFilesDir();
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        if (!((RadioButton) view).isChecked()) {
            return;
        }

        // Create query to get category
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Category");
        String objectId = "";

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_japanese:
                    objectId = "c3tgKGq7PB";
                    break;
            case R.id.radio_italian:
                    objectId = "vXScNFQ7I1";
                    break;
            case R.id.radio_drinks:
                    objectId = "cESIVJdndh";
                    break;
            case R.id.radio_coffee:
                    objectId = "xiaCOmzklb";
                    break;
            case R.id.radio_fast_food:
                    objectId = "HrfhIk7otS";
                    break;
        }

        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e == null) {
                    category = object;
                } else {
                    Toast.makeText(CreatePlace.this, "Error while selecting category", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error while selecting category", e);
                }
            }
        });

        binding.swPublic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    sharePost = true;
                } else {
                    // The toggle is disabled
                    sharePost = false;
                }
            }
        });
    }

    /**
     * This functions creates a Place and uploads it into Parse server
     */
    private void post() {
        // Set loading spinner
        binding.loading.setVisibility(View.VISIBLE);
        binding.btnPost.setVisibility(View.GONE);

        // Create place
        Place place = new Place();
        place.setLikeCount(0);
        place.setDescription(binding.etDescription.getText().toString());
        place.setPrice((int) binding.rbPrice.getRating());
        place.setLat(placeLocation.getPosition().latitude);
        place.setLng(placeLocation.getPosition().longitude);
        place.setAddress(binding.etAddress.getText().toString());
        place.setPublic(this.sharePost);
        place.setPhone(binding.etPhoneNumber.getText().toString());
        place.setCategory(this.category);
        place.setName(binding.etName.getText().toString());

        // Reduce image size and add it into the place
        Bitmap fillSizeBitmap = BitmapFactory.decodeFile(image.toPath().toString());
        Bitmap reducedBitmap = ImageResizer.reduceBitmapSize(fillSizeBitmap, 480000); // Reduce to 600x400 pixels
        image = getBitmapFile(reducedBitmap);
        place.setImage(new ParseFile(image));

        // Save place
        place.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                binding.loading.setVisibility(View.GONE);
                binding.btnPost.setVisibility(View.VISIBLE);
                if(e == null) {
                    Toast.makeText(CreatePlace.this, "Nice place!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(CreatePlace.this, "Error occurred while posting place", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error while posting place", e);
                }
            }
        });
    }

    /**
     * Get File from Bitmap and return it
     * @param reducedBitmap: the bitmap to convert to file
     */
    private File getBitmapFile(Bitmap reducedBitmap) {
        File file = new File(outputDirectory, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        reducedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapData = bos.toByteArray();

        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            return file;
        } catch (IOException e) {
            Log.e(TAG, "Error while reducing image and converting it to file", e);
            e.printStackTrace();
        }

        return file;
    }

}