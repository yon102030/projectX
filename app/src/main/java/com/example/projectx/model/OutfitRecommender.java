package com.example.projectx.model;

public class OutfitRecommender {

    Outfit hotOutfit;
    Outfit coldOutfit;
    Outfit rainyOutfit;
    Outfit defaultOutfit;


    public OutfitRecommender() {
        hotOutfit = new Outfit("חולצה קצרה", "מכנס קצר", "סנדלים", "משקפי שמש", "קיץ / Casual");
        coldOutfit = new Outfit("סוודר", "ג'ינס", "מגפיים", "צעיף", "חורף / Cozy");
        rainyOutfit = new Outfit("מעיל גשם", "מכנס ג'ינס", "נעליים סגורות", "מטריה", "גשום / Urban");
        defaultOutfit = new Outfit("חולצה ארוכה", "מכנס רגיל", "נעלי ספורט", "שעון", "סתיו / אביב");
    }

    public Outfit recommendOutfit(WeatherService weather) {
        if (weather.isHot()) return hotOutfit;
        if (weather.isCold()) return coldOutfit;
        if (weather.isRainy()) return rainyOutfit;
        return defaultOutfit;
    }

    @Override
    public String toString() {
        return "OutfitRecommender{" +
                "hotOutfit=" + hotOutfit +
                ", coldOutfit=" + coldOutfit +
                ", rainyOutfit=" + rainyOutfit +
                ", defaultOutfit=" + defaultOutfit +
                '}';
    }
}
