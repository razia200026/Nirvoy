package com.example.safetyapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class LiveLocation extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 101;
    private static final int SMS_PERMISSION_CODE = 102;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private List<Contact> contacts = new ArrayList<>();
    private DatabaseReference dbRef;
    private FirebaseUser currentUser;

    private Button btnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_location);

        btnShare = findViewById(R.id.btnShareLocation);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUser.getUid()).child("emergencyContacts");

        loadContacts();

        btnShare.setOnClickListener(v -> {
            if (contacts.isEmpty()) {
                Toast.makeText(this, "No emergency contacts found", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Share Location")
                    .setMessage("Do you want to share your location via WhatsApp or SMS?")
                    .setPositiveButton("WhatsApp", (dialog, which) -> shareLocation("whatsapp"))
                    .setNegativeButton("SMS", (dialog, which) -> shareLocation("sms"))
                    .show();
        });
    }

    private void loadContacts() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                contacts.clear();
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    Contact contact = contactSnapshot.getValue(Contact.class);
                    if (contact != null) {
                        contacts.add(contact);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LiveLocation.this, "Failed to load contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareLocation(String method) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
            return;
        }

        if ("sms".equals(method) &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        String message = createLocationMessage(location);
                        for (Contact contact : contacts) {
                            if ("whatsapp".equals(method)) {
                                sendWhatsApp(contact.getPhone(), message);
                            } else {
                                sendSmsDirectly(contact.getPhone(), message);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String createLocationMessage(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        return "Here is my location: https://www.google.com/maps?q=" + lat + "," + lng;
    }

    private void sendWhatsApp(String phoneNumber, String message) {
        try {
            phoneNumber = phoneNumber.replace("+", "").replaceAll("\\s", "");
            String url = "https://wa.me/" + phoneNumber + "?text=" + URLEncoder.encode(message, "UTF-8");

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);  // No need to check for installed package; browser will handle it

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error sending WhatsApp message", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSmsDirectly(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "Location sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS to " + phoneNumber, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE || requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted, tap the button again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}