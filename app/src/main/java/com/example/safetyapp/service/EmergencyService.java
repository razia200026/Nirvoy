package com.example.safetyapp.service;
public class EmergencyService {
    private String name;
    private String phone;
    private int iconResId;

    public EmergencyService(String name, String phone, int iconResId) {
        this.name = name;
        this.phone = phone;
        this.iconResId = iconResId;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public int getIconResId() { return iconResId; }
}
