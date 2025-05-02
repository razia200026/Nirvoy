package com.example.safetyapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.safetyapp.helper.EmergencyMessageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class EmergencyMessageActivity extends AppCompatActivity {

    private Button btnSend, btnSave;
    private EditText etMessage;
    private TextView tvSavedMessage;

    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private String savedTemplate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_message);

        btnSend = findViewById(R.id.btn_send);
        btnSave = findViewById(R.id.btn_save_message);
        etMessage = findViewById(R.id.et_emergency_message);
        tvSavedMessage = findViewById(R.id.tv_saved_message);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        loadSavedMessage();

        btnSend.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Send Emergency Message")
                    .setMessage("Choose how to send your message:")
                    .setPositiveButton("WhatsApp", (dialog, which) -> new EmergencyMessageHelper(EmergencyMessageActivity.this).sendMessage("whatsapp"))
                    .setNegativeButton("SMS", (dialog, which) -> new EmergencyMessageHelper(EmergencyMessageActivity.this).sendMessage("sms"))
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
}
