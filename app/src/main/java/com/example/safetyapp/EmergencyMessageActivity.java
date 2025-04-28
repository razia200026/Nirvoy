package com.example.safetyapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class EmergencyMessageActivity extends AppCompatActivity {

    private DatabaseReference dbRef;
    private FirebaseUser currentUser;
    private EditText etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_message);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        // Initialize Views
        etMessage = findViewById(R.id.et_emergency_message);

        // Load existing message
        loadEmergencyMessage();

        // Setup real-time listener
        setupMessageListener();

        // Setup Save button
        findViewById(R.id.btn_save_message).setOnClickListener(v -> saveMessage());
    }

    private void loadEmergencyMessage() {
        dbRef.child("emergencyMessage").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String message = snapshot.getValue(String.class);
                    if (message != null) {
                        etMessage.setText(message);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EmergencyMessageActivity.this, "Failed to load emergency message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveMessage() {
        String message = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("emergencyMessage", message);

        dbRef.updateChildren(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EmergencyMessageActivity.this, "Message saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EmergencyMessageActivity.this, "Failed to save message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMessageListener() {
        dbRef.child("emergencyMessage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String savedMessage = snapshot.getValue(String.class);
                    if (savedMessage != null && !savedMessage.equals(etMessage.getText().toString())) {
                        etMessage.setText(savedMessage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // No action needed
            }
        });
    }
}
