package com.example.projectx.model;

import java.util.HashMap;
import java.util.Map;

public class User {

    String userId;
    String fName;
    String lName;
    String phone;
    String email;
    String password;
    boolean isAdmin;
    Map<String, Integer> colorStats;

    public User() {
        this.colorStats = new HashMap<>();
    }

    public User(String userId, String fName, String lName, String phone, String email, String password, boolean isAdmin) {
        this.userId = userId;
        this.fName = fName;
        this.lName = lName;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.colorStats = new HashMap<>();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Map<String, Integer> getColorStats() {
        return colorStats;
    }

    public void setColorStats(Map<String, Integer> colorStats) {
        this.colorStats = colorStats;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", fName='" + fName + '\'' +
                ", lName='" + lName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}