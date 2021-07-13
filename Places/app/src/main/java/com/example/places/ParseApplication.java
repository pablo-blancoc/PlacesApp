package com.example.places;

import android.app.Application;

import com.example.places.models.Place;
import com.example.places.models.User;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Declare Parse subclasses
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Place.class);

        // Initialize Parse
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build());
    }
}