package com.example.safetyapp;

public class Contact {
    private String id;
    private String name;
    private String phone;
    public Contact() {}
    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
}