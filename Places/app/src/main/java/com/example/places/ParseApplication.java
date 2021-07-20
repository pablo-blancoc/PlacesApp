package com.example.places;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import com.example.places.models.Place;
import com.example.places.models.User;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;
import com.parse.Parse;
import com.parse.ParseObject;

import org.json.JSONException;
import org.json.JSONObject;

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

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // Initialize Onesignal
        OneSignal.initWithContext(this);
        OneSignal.setAppId(getString(R.string.one_signal_app_id));
        OneSignal.unsubscribeWhenNotificationsAreDisabled(true);

        OneSignal.setNotificationOpenedHandler(new OneSignal.OSNotificationOpenedHandler() {
                    @Override
                    public void notificationOpened(OSNotificationOpenedResult result) {
                        // String actionId = result.getAction().getActionId();
                        // String type = String.valueOf(result.getAction().getType()); // "ActionTaken" | "Opened"

                        JSONObject additionalData = result.getNotification().getAdditionalData();
                        try {
                            String placeId = additionalData.getString("place");
                            Log.d("notificationOpened", "PLACE: " + placeId);
                            if(!placeId.isEmpty()) {
                                Intent intent = new Intent(ParseApplication.this, PlaceDetailActivity.class);
                                intent.putExtra("place", placeId);
                                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}