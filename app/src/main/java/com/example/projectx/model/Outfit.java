package com.example.projectx.model;

public class Outfit {
    private String outfitId;
    private String top;
    private String bottom;
    private String shoes;
    private String accessories;
    private String styleTag;
    private String userId; // <-- שדה חדש למשתמש

    // --- Constructors ---
    public Outfit(String outfitId, String top, String bottom, String shoes, String accessories, String styleTag, String userId) {
        this.outfitId = outfitId;
        this.top = top;
        this.bottom = bottom;
        this.shoes = shoes;
        this.accessories = accessories;
        this.styleTag = styleTag;
        this.userId = userId;
    }

    public Outfit(String top, String bottom, String shoes, String accessories, String styleTag, String userId) {
        this(null, top, bottom, shoes, accessories, styleTag, userId);
    }

    public Outfit() {}

    // --- Getters ---
    public String getOutfitId() { return outfitId; }
    public String getTop() { return top; }
    public String getBottom() { return bottom; }
    public String getShoes() { return shoes; }
    public String getAccessories() { return accessories; }
    public String getStyleTag() { return styleTag; }
    public String getUserId() { return userId; } // <-- getter חדש

    // --- Setters ---
    public void setOutfitId(String outfitId) { this.outfitId = outfitId; }
    public void setTop(String top) { this.top = top; }
    public void setBottom(String bottom) { this.bottom = bottom; }
    public void setShoes(String shoes) { this.shoes = shoes; }
    public void setAccessories(String accessories) { this.accessories = accessories; }
    public void setStyleTag(String styleTag) { this.styleTag = styleTag; }
    public void setUserId(String userId) { this.userId = userId; } // <-- setter חדש

    @Override
    public String toString() {
        return "Outfit{" +
                "outfitId='" + outfitId + '\'' +
                ", top='" + top + '\'' +
                ", bottom='" + bottom + '\'' +
                ", shoes='" + shoes + '\'' +
                ", accessories='" + accessories + '\'' +
                ", styleTag='" + styleTag + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}