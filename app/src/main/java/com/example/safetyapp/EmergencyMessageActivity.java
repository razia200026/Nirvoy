package com.example.safetyapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class EmergencyMessageActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 101;
    private static final int SMS_PERMISSION_CODE = 102;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private List<Contact> contacts = new ArrayList<>();
    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    private Button btnSend, btnSave;
    private EditText etMessage;
    private TextView tvSavedMessage;

    private String savedTemplate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_message);

        // Initialize views
        btnSend = findViewById(R.id.btn_send);
        btnSave = findViewById(R.id.btn_save_message);
        etMessage = findViewById(R.id.et_emergency_message);
        tvSavedMessage = findViewById(R.id.tv_saved_message);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        loadContacts();
        loadSavedMessage();

        btnSend.setOnClickListener(v -> {
            if (contacts.isEmpty()) {
                Toast.makeText(this, "No emergency contacts found", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Send Emergency Message")
                    .setMessage("Do you want to send the emergency message via WhatsApp or SMS?")
                    .setPositiveButton("WhatsApp", (dialog, which) -> sendEmergencyMessage("whatsapp"))
                    .setNegativeButton("SMS", (dialog, which) -> sendEmergencyMessage("sms"))
                    .show();
        });

        btnSave.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            userRef.child("emergency_message_template").setValue(message)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Message saved successfully", Toast.LENGTH_SHORT).show();
                        tvSavedMessage.setText(message);
                        savedTemplate = message;
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save message", Toast.LENGTH_SHORT).show());
        });
    }

    private void loadContacts() {
        userRef.child("emergencyContacts").addValueEventListener(new ValueEventListener() {
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
                Toast.makeText(EmergencyMessageActivity.this, "Failed to load contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSavedMessage() {
        userRef.child("emergency_message_template").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    savedTemplate = snapshot.getValue(String.class);
                    tvSavedMessage.setText(savedTemplate);
                } else {
                    tvSavedMessage.setText("No saved message");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EmergencyMessageActivity.this, "Failed to load saved message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEmergencyMessage(String method) {
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
                        String message = createEmergencyMessage(location);
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

    private String createEmergencyMessage(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        String locationUrl = "https://www.google.com/maps?q=" + lat + "," + lng;
        return savedTemplate + "\n\nMy location: " + locationUrl;
    }

//    private void sendWhatsApp(String phoneNumber, String message) {
//        try {
//            phoneNumber = phoneNumber.replace("+", "").replaceAll("\\s", "");
//            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber
//                    + "&text=" + URLEncoder.encode(message, "UTF-8");
//
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setData(Uri.parse(url));
//            intent.setPackage("com.whatsapp");
//
//            if (intent.resolveActivity(getPackageManager()) != null) {
//                startActivity(intent);
//            } else {
//                Toast.makeText(this, "WhatsApp not found", Toast.LENGTH_SHORT).show();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Error sending via WhatsApp", Toast.LENGTH_SHORT).show();
//        }
//    }
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
