package com.example.projectx.model;

public class Clothe {

    private String itemId;
    private String name;
    private String type;
    private String color;
    private String imageUrl;
    private String season;
    private boolean isFavorite;


    public Clothe(String itemId, String name, String type, String color, String imageUrl, String season, boolean isFavorite) {
        this.itemId = itemId;
        this.name = name;
        this.type = type;
        this.color = color;
        this.imageUrl = imageUrl;
        this.season = season;
        this.isFavorite = isFavorite;
    }

    // --- Getters & Setters ---
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    // --- toString() ---
    @Override
    public String toString() {
        return "Clothe{" +
                "itemId='" + itemId + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", color='" + color + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", season='" + season + '\'' +
                ", isFavorite=" + isFavorite +
                '}';
    }
}
