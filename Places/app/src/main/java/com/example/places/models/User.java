package com.example.places.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseUser;

@ParseClassName("_User")
public class User extends ParseUser {

    // Constants
    public static final String KEY_PROFILE_PICTURE = "profilePicture";
    public static final String KEY_BIO = "bio";
    public static final String KEY_NAME = "name";
    public static final String KEY_ONE_SIGNAL = "OneSignal";

    // Attributes
    public boolean following;
    public boolean notify;

    public ParseFile getProfilePicture() {
        return getParseFile(KEY_PROFILE_PICTURE);
    }

    public void setProfilePicture(ParseFile image) {
        put(KEY_PROFILE_PICTURE, image);
    }

    public String getBio() {
        return getString(KEY_BIO);
    }

    public void setBio(String bio) {
        put(KEY_BIO, bio);
    }

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public String getOneSignal() {
        return getString(KEY_ONE_SIGNAL);
    }

    public void setOneSignal(String id) {
        put(KEY_ONE_SIGNAL, id);
    }

}
