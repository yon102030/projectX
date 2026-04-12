package com.example.projectx.model;

public class Outfit {

    private String outfitId;
    private String userId;

    // תמונות של הפריטים (Base64)
    private String top;
    private String outer;
    private String bottom;

    public Outfit() {}

    public Outfit(String outfitId,
                  String userId,
                  String top,
                  String outer,
                  String bottom) {

        this.outfitId = outfitId;
        this.userId = userId;
        this.top = top;
        this.outer = outer;
        this.bottom = bottom;
    }

    // getters
    public String getOutfitId() { return outfitId; }
    public String getUserId() { return userId; }

    public String getTop() { return top; }
    public String getOuter() { return outer; }
    public String getBottom() { return bottom; }

    // setters
    public void setOutfitId(String outfitId) { this.outfitId = outfitId; }
    public void setUserId(String userId) { this.userId = userId; }

    public void setTop(String top) { this.top = top; }
    public void setOuter(String outer) { this.outer = outer; }
    public void setBottom(String bottom) { this.bottom = bottom; }
}