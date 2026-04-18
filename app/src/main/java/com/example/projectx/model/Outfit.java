package com.example.projectx.model;

public class Outfit {
    private String outfitId;
    private String userId;
    private String top;
    private String outer;
    private String bottom;
    private boolean isMale;

    // בנאי ריק עבור Firebase
    public Outfit() {}

    public Outfit(String outfitId, String userId, String top, String outer, String bottom, boolean isMale) {
        this.outfitId = outfitId;
        this.userId = userId;
        this.top = top;
        this.outer = outer;
        this.bottom = bottom;
        this.isMale = isMale;
    }

    // Getters ו-Setters
    public String getOutfitId() { return outfitId; }
    public void setOutfitId(String outfitId) { this.outfitId = outfitId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTop() { return top; }
    public void setTop(String top) { this.top = top; }

    public String getOuter() { return outer; }
    public void setOuter(String outer) { this.outer = outer; }

    public String getBottom() { return bottom; }
    public void setBottom(String bottom) { this.bottom = bottom; }

    public boolean isMale() { return isMale; }
    public void setMale(boolean male) { isMale = male; }
}