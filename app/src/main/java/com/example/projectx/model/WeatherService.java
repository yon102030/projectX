package com.example.projectx.model;

public class WeatherService {
    double temperature;
    double humidity;
    double windSpeed;
    String condition;


    public WeatherService(double temperature, double humidity, double windSpeed, String condition) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.condition = condition;
    }


    public WeatherService() {
    }


    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }


    public boolean isRainy() {
        return condition != null && condition.toLowerCase().contains("rain");
    }

    public boolean isHot() {
        return temperature > 28;
    }

    public boolean isCold() {
        return temperature < 15;
    }

    // --- toString() ---
    @Override
    public String toString() {
        return "WeatherService{" +
                "temperature=" + temperature +
                ", humidity=" + humidity +
                ", windSpeed=" + windSpeed +
                ", condition='" + condition + '\'' +
                '}';
    }
}
