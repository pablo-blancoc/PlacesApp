package com.example.places.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

@ParseClassName("Place")
public class Place extends ParseObject {

    // Constants
    public static final String KEY_LIKE_COUNT = "likeCount";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_PRICE = "price";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_PUBLIC = "public";
    public static final String KEY_PHONE = "phoneNumber";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_NAME = "name";
    public static final String KEY_IMAGE = "image";

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public ParseObject getCategory() {
        return getParseObject(KEY_CATEGORY);
    }

    public void setCategory(ParseObject category) {
        put(KEY_CATEGORY, category);
    }

    public String getPhone() {
        return getString(KEY_PHONE);
    }

    public void setPhone(String phone) {
        put(KEY_PHONE, phone);
    }

    public Boolean getPublic() {
        return getBoolean(KEY_PUBLIC);
    }

    public void setPublic(Boolean flag) {
        put(KEY_PUBLIC, flag);
    }

    public String getAddress() {
        return getString(KEY_ADDRESS);
    }

    public void setAddress(String address) {
        put(KEY_ADDRESS, address);
    }

    public Double getLat() {
        return getDouble(KEY_LAT);
    }

    public void setLat(Double lat) {
        put(KEY_LAT, lat);
    }

    public Double getLng() {
        return getDouble(KEY_LNG);
    }

    public void setLng(Double lng) {
        put(KEY_LNG, lng);
    }

    public int getPrice() {
        return getInt(KEY_PRICE);
    }

    public void setPrice(int price) {
        put(KEY_PRICE, price);
    }

    public int getLikeCount() {
        return getInt(KEY_LIKE_COUNT);
    }

    public void setLikeCount(int likes) {
        put(KEY_LIKE_COUNT, likes);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

}
