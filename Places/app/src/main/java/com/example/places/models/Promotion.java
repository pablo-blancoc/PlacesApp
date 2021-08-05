package com.example.places.models;

import androidx.annotation.Nullable;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Promotion")
public class Promotion extends ParseObject {

    // Constants
    public static final String KEY_PLACE = "place";
    public static final String KEY_USER = "user";
    public static final String KEY_SENT = "sent";
    public static final String KEY_VIEWED = "viewed";
    public static final String KEY_CLICKED = "clicked";

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public ParseObject getPlace() {
        return getParseObject(KEY_PLACE);
    }

    public void setPlace(Place place) {
        put(KEY_PLACE, place);
    }

    public int getSent() {
        return getInt(KEY_SENT);
    }

    public int getViewed() {
        return getInt(KEY_VIEWED);
    }

    public int getClicked() {
        return getInt(KEY_CLICKED);
    }

    public void setSent(int value) {
        put(KEY_SENT, value);
    }

    public void setViewed(int value) {
        put(KEY_VIEWED, value);
    }

    public void setClicked(int value) {
        put(KEY_CLICKED, value);
    }
}
