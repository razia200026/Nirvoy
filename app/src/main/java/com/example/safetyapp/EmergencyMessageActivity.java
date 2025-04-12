package com.example.safetyapp;


import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class EmergencyMessageActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String userId;
    private EditText etMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_message);
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize Views
        etMessage = findViewById(R.id.et_emergency_message);
        // Load existing data
        loadEmergencyData();
        setupMessageListener();

        // Setup click listeners
        findViewById(R.id.btn_save_message).setOnClickListener(v -> saveMessage());
    }

    private void loadEmergencyData() {
        // Load message
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && document.contains("emergencyMessage")) {
                        etMessage.setText(document.getString("emergencyMessage"));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load emergency data", Toast.LENGTH_SHORT).show();
                });
    }
    // Save message
    private void saveMessage() {
        String message = etMessage.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("emergencyMessage", message);

        db.collection("users").document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused ->
                {
                    Toast.makeText(this, "Message saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                {
                    Toast.makeText(this, "Failed to save message" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    //real-time update to the message field
    private void setupMessageListener() {
        db.collection("users").document(userId)
                .addSnapshotListener((document, error) -> {
                    if(error != null) return;

                    if(document != null && document.exists()) {
                        String savedMessage = document.getString("emergencyMessage");
                        if(savedMessage != null && !savedMessage.equals(etMessage.getText().toString())) {
                            etMessage.setText(savedMessage);
                        }
                    }
                });
    }

}