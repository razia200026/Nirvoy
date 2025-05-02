// EmergencyMessageHelper.java
package com.example.safetyapp.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.safetyapp.Contact;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class EmergencyMessageHelper {
    private static final int LOCATION_PERMISSION_CODE = 101;
    private static final int SMS_PERMISSION_CODE = 102;

    private final Activity activity;
    private final FusedLocationProviderClient locationProvider;
    private final FirebaseUser currentUser;
    private final DatabaseReference userRef;
    private String savedTemplate = "";
    private final List<Contact> contacts = new ArrayList<>();

    public EmergencyMessageHelper(Activity activity) {
        this.activity = activity;
        this.locationProvider = LocationServices.getFusedLocationProviderClient(activity);
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
    }

    public void sendMessage(String method) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            return;
        }

        if ("sms".equals(method) && ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
            return;
        }

        userRef.child("emergencyContacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                contacts.clear();
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    Contact contact = contactSnapshot.getValue(Contact.class);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                }

                if (contacts.isEmpty()) {
                    Toast.makeText(activity, "No emergency contacts found", Toast.LENGTH_SHORT).show();
                    return;
                }

                userRef.child("emergency_message_template").addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        savedTemplate = snapshot.exists() ? snapshot.getValue(String.class) : "Help me!";

                        locationProvider.getLastLocation()
                                .addOnSuccessListener(location -> {
                                    if (location != null) {
                                        String message = savedTemplate + "\n\nMy location: https://www.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();
                                        for (Contact contact : contacts) {
                                            if ("sms".equals(method)) {
                                                sendSms(contact.getPhone(), message);
                                            } else {
                                                sendWhatsApp(contact.getPhone(), message);
                                            }
                                        }
                                    } else {
                                        Toast.makeText(activity, "Unable to get location", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(activity, "Failed to load message", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(activity, "Failed to load contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSms(String phone, String message) {
        try {
            SmsManager.getDefault().sendTextMessage(phone, null, message, null, null);
            Toast.makeText(activity, "SMS sent to " + phone, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(activity, "Failed to send SMS to " + phone, Toast.LENGTH_SHORT).show();
        }
    }

    private void sendWhatsApp(String phone, String message) {
        try {
            phone = phone.replace("+", "").replaceAll("\\s", "");
            String url = "https://wa.me/" + phone + "?text=" + URLEncoder.encode(message, "UTF-8");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity, "Error sending WhatsApp message", Toast.LENGTH_SHORT).show();
        }
    }
}
