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

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.places.databinding.ActivityTakePictureBinding;
import com.example.places.models.Place;
import com.example.places.models.User;
import com.google.common.util.concurrent.ListenableFuture;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TakePictureActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "ProfileActivity";
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    // Attributes
    private User user;
    private ActivityTakePictureBinding binding;
    private ImageCapture imageCapture;
    private File outputDirectory;
    private ExecutorService cameraExecutor;
    private File image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting up binding
        binding = ActivityTakePictureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Listener to take picture
        binding.btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        // Listener to retake picture
        binding.btnRetake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnTake.setVisibility(View.VISIBLE);
                binding.pvImage.setVisibility(View.VISIBLE);
                binding.ivImage.setVisibility(View.GONE);
                binding.btnRetake.setVisibility(View.GONE);
                binding.btnPost.setVisibility(View.GONE);
            }
        });

        // Listener to post picture
        binding.btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnTake.setVisibility(View.GONE);
                binding.pvImage.setVisibility(View.GONE);
                binding.ivImage.setVisibility(View.VISIBLE);
                binding.btnRetake.setVisibility(View.GONE);
                binding.btnPost.setVisibility(View.GONE);
                binding.loading.setVisibility(View.VISIBLE);

                postImage();
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
                binding.btnTake.setVisibility(View.GONE);
                binding.pvImage.setVisibility(View.GONE);
                binding.ivImage.setVisibility(View.VISIBLE);
                binding.btnRetake.setVisibility(View.VISIBLE);
                binding.btnPost.setVisibility(View.VISIBLE);

                // Save image into File variable
                image = new File(savedUri.getPath());

                // Load image with Glide
                Glide.with(TakePictureActivity.this)
                        .load(image)
                        .into(binding.ivImage);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(TakePictureActivity.this, "Image could not be saved. Try again later.", Toast.LENGTH_SHORT).show();
                Log.i("Image Capture", exception.toString());
            }
        });
    }


    /**
     * Get File from Bitmap and return it
     * @param reducedBitmap: the bitmap to convert to file
     */
    private File getBitmapFile(Bitmap reducedBitmap) {
        // Rotate bitmap to correct issues
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(reducedBitmap, 0, 0, reducedBitmap.getWidth(), reducedBitmap.getHeight(), matrix, true);

        File file = new File(outputDirectory, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
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
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                // tie the preview, camera selector and imageCapture together via the cameraProvider
                try {
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(TakePictureActivity.this, cameraSelector, preview, imageCapture);
                } catch (Exception e){
                    Log.e(TAG, "Exception on enableCamera", e);
                }
            }


        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * This functions creates a Place and uploads it into Parse server
     */
    private void postImage() {
        // Reduce image size and add it into the place
        Bitmap fullSizeBitmap = BitmapFactory.decodeFile(image.toPath().toString());
        Bitmap reducedBitmap = ImageResizer.reduceBitmapSize(fullSizeBitmap, 480000); // Reduce to 600x400 pixels
        image = getBitmapFile(reducedBitmap);

        ParseUser.getCurrentUser().put(User.KEY_PROFILE_PICTURE, new ParseFile(image));
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                binding.btnTake.setVisibility(View.VISIBLE);
                binding.pvImage.setVisibility(View.VISIBLE);
                binding.ivImage.setVisibility(View.GONE);
                binding.btnRetake.setVisibility(View.GONE);
                binding.btnPost.setVisibility(View.GONE);
                binding.loading.setVisibility(View.GONE);

                if(e == null) {
                    Toast.makeText(TakePictureActivity.this, "Nice picture!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(TakePictureActivity.this, "Error occurred while processing image", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error while posting place", e);
                }
            }
        });
    }
}