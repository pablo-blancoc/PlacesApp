package com.example.places.models;

public class SearchResult {
    public boolean isUser;
    public String name;
    public String secondaryInfo;
    public String imageUrl;
    public String objectId;

    public SearchResult(boolean isUser, String name, String secondaryInfo, String imageUrl, String objectId) {
        this.isUser = isUser;
        this.name = name;
        this.secondaryInfo = secondaryInfo;
        this.imageUrl = imageUrl;
        this.objectId = objectId;
    }
}
