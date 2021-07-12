package com.example.places.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseUser;

@ParseClassName("_User")
public class User extends ParseUser {

    // Constants
    public static final String KEY_PROFILE_PICTURE = "profilePicture";
    public static final String KEY_BIO = "bio";
    public static final String KEY_FOLLOWERS = "followersCount";
    public static final String KEY_FOLLOWING = "followingCount";
    public static final String KEY_NAME = "name";

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

    public int getFollowersCount() {
        return getInt(KEY_FOLLOWERS);
    }

    public void setFollowersCount(int followers) {
        put(KEY_FOLLOWERS, followers);
    }

    public int getFollowingCount() {
        return getInt(KEY_FOLLOWING);
    }

    public void setFollowingCount(int followers) {
        put(KEY_FOLLOWING, followers);
    }

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

}
