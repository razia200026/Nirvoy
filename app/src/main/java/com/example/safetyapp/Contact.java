package com.example.safetyapp;

public class Contact {
    private String name;
    private String phone;
    private String id; // Optional, if you use it to track Firebase keys

    // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
    public Contact() { }

    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    // Getters and setters
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getId() { return id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setId(String id) { this.id = id; }
}
