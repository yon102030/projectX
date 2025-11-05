package com.example.projectx.model;

public class Outfit {
    String top;
    String bottom;
    String shoes;
    String accessories;
    String styleTag;


    public Outfit(String top, String bottom, String shoes, String accessories, String styleTag) {
        this.top = top;
        this.bottom = bottom;
        this.shoes = shoes;
        this.accessories = accessories;
        this.styleTag = styleTag;
    }


    public Outfit() {}

    // --- Getters ---
    public String getTop() {
        return top;
    }

    public String getBottom() {
        return bottom;
    }

    public String getShoes() {
        return shoes;
    }

    public String getAccessories() {
        return accessories;
    }

    public String getStyleTag() {
        return styleTag;
    }

    // --- Setters ---
    public void setTop(String top) {
        this.top = top;
    }

    public void setBottom(String bottom) {
        this.bottom = bottom;
    }

    public void setShoes(String shoes) {
        this.shoes = shoes;
    }

    public void setAccessories(String accessories) {
        this.accessories = accessories;
    }

    public void setStyleTag(String styleTag) {
        this.styleTag = styleTag;
    }


    @Override
    public String toString() {
        return "Outfit{" +
                "top='" + top + '\'' +
                ", bottom='" + bottom + '\'' +
                ", shoes='" + shoes + '\'' +
                ", accessories='" + accessories + '\'' +
                ", styleTag='" + styleTag + '\'' +
                '}';
    }
}
