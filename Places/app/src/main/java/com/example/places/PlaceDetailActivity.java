package com.example.places;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Transition;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
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
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class PlaceDetailActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "PlaceDetailActivity";

    // Attributes
    private ActivityPlaceDetailBinding binding;
    private Place place;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private int deleteStep = 0;
    private Transition.TransitionListener transitionListener;

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
            Toasty.error(this, "Error loading map", Toast.LENGTH_LONG, true).show();
        }
    }

    /**
     * When the user exits the activity the exit transition should be triggered
     */
    @Override
    public void onBackPressed() {
        exitReveal();

        super.onBackPressed();
    }

    /**
     * Starts the transition to enter the DetailsActivity page
     */
    void enterReveal() {
        // get the center for the clipping circles
        int fabCall_x = binding.fabCall.getMeasuredWidth() / 2;
        int fabCall_y = binding.fabCall.getMeasuredHeight() / 2;
        int fabLike_x = binding.fabLike.getMeasuredWidth() / 2;
        int fabLike_y = binding.fabLike.getMeasuredHeight() / 2;

        // get the final radius for the clipping circle
        int fabCall_finalRadius = Math.max(binding.fabCall.getWidth(), binding.fabCall.getHeight()) / 2;
        int fabLike_finalRadius = Math.max(binding.fabLike.getWidth(), binding.fabLike.getHeight()) / 2;

        // create the animator for this view (the start radius is zero)
        Animator fabCall_anim = ViewAnimationUtils.createCircularReveal(binding.fabCall, fabCall_x, fabCall_y, 0, fabCall_finalRadius);
        Animator fabLike_anim = ViewAnimationUtils.createCircularReveal(binding.fabLike, fabLike_x, fabLike_y, 0, fabLike_finalRadius);

        // Add Listener for button animation
        fabCall_anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                getWindow().getEnterTransition().removeListener(transitionListener);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        fabLike_anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                getWindow().getEnterTransition().removeListener(transitionListener);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        // make the view visible and start the animation
        binding.fabCall.setVisibility(View.VISIBLE);
        binding.fabLike.setVisibility(View.VISIBLE);
        fabCall_anim.start();
        fabLike_anim.start();
    }

    /**
     * Starts the transition to exis the DetailsActivity
     */
    void exitReveal() {
        // get the center for the clipping circle
        int fabCall_x = binding.fabCall.getMeasuredWidth() / 2;
        int fabCall_y = binding.fabCall.getMeasuredHeight() / 2;
        int fabLike_x = binding.fabLike.getMeasuredWidth() / 2;
        int fabLike_y = binding.fabLike.getMeasuredHeight() / 2;

        // get the initial radius for the clipping circle
        int fabCall_initialRadius = binding.fabCall.getWidth() / 2;
        int fabLike_initialRadius = binding.fabLike.getWidth() / 2;

        // create the animation (the final radius is zero)
        Animator fabCall_anim = ViewAnimationUtils.createCircularReveal(binding.fabCall, fabCall_x, fabCall_y, fabCall_initialRadius, 0);
        Animator fabLike_anim = ViewAnimationUtils.createCircularReveal(binding.fabLike, fabLike_x, fabLike_y, fabLike_initialRadius, 0);

        // make the view invisible when the animation is done
        fabCall_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                binding.fabCall.setVisibility(View.INVISIBLE);
            }
        });
        fabLike_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                binding.fabLike.setVisibility(View.INVISIBLE);
            }
        });

        // Add listener to finish activity when animation finished
        fabCall_anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                finishAfterTransition();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        fabLike_anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                finishAfterTransition();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        // start the animation
        fabCall_anim.start();
        fabLike_anim.start();
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
        this.binding.main.setVisibility(View.GONE);
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
                    enterReveal();
                } else {
                    Toast.makeText(PlaceDetailActivity.this, "Place not found", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        // Set up elements visibility
        binding.fabCall.setVisibility(View.INVISIBLE);
        binding.fabLike.setVisibility(View.INVISIBLE);
        binding.main.setVisibility(View.VISIBLE);
        binding.loading.setVisibility(View.GONE);
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
        LatLng placePosition = new LatLng(this.place.getLocation().getLatitude(), this.place.getLocation().getLongitude());
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
        this.binding.fabCall.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.green)));
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

        // Set fab like colors
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Like");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.whereEqualTo("place", this.place);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    place.liked = objects.size() == 1;

                    // Set follow button text
                    if (place.liked) {
                        binding.fabLike.setImageTintList(ColorStateList.valueOf(getColor(R.color.primary)));
                        binding.fabLike.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.primary)));
                    } else {
                        binding.fabLike.setImageTintList(ColorStateList.valueOf(getColor(R.color.black)));
                        binding.fabLike.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.black)));
                    }
                } else {
                    Log.e(TAG, "Problem knowing if place is liked", e);
                }
            }
        });

        // Set like actions listener
        this.binding.fabLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(place.liked) {
                    binding.fabLike.setImageTintList(ColorStateList.valueOf(getColor(R.color.black)));
                    binding.fabLike.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.black)));
                    unlike();
                } else {
                    binding.fabLike.setImageTintList(ColorStateList.valueOf(getColor(R.color.primary)));
                    binding.fabLike.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.primary)));
                    like();
                }
                place.liked = !place.liked;
            }
        });

        // Promote a place
        this.binding.btnPromote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.rlPromote.setVisibility(View.VISIBLE);
            }
        });

        // Promotion cancelled
        this.binding.btnPromoteCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.rlPromote.setVisibility(View.GONE);
            }
        });

        // Promote now
        this.binding.btnPromoteNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.rlPromote.setVisibility(View.GONE);
                Toasty.success(PlaceDetailActivity.this, "Place promoted successfully!", Toasty.LENGTH_SHORT, true).show();
            }
        });
    }

    /**
     * Likes the place
     */
    private void like() {
        // Update place likeCount
        this.place.setLikeCount(this.place.getLikeCount() + 1);
        this.place.saveInBackground();
        this.binding.chipLikes.setText(String.format("%d likes", this.place.getLikeCount()));

        // Create like object on database
        ParseObject like = new ParseObject("Like");
        like.put("user", ParseUser.getCurrentUser());
        like.put("place", this.place);
        like.saveInBackground();
    }

    /**
     * Unlikes the place
     */
    private void unlike() {
        // Update place likeCount
        this.place.setLikeCount(this.place.getLikeCount() - 1);
        this.place.saveInBackground();
        this.binding.chipLikes.setText(String.format("%d likes", this.place.getLikeCount()));

        // Create like object on database
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Like");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.whereEqualTo("place", this.place);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e == null) {
                    object.deleteInBackground();
                } else {
                    Log.e(TAG, "place not unliked", e);
                }
            }
        });
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