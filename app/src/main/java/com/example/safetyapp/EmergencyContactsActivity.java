package com.example.safetyapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmergencyContactsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String userId;
    private EditText etMessage;
    private ContactsAdapter adapter;
    private List<Contact> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize Views
        etMessage = findViewById(R.id.et_emergency_message);
        RecyclerView recyclerView = findViewById(R.id.rv_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter(contacts, this::deleteContact);
        recyclerView.setAdapter(adapter);

        // Load existing data
        loadEmergencyData();
        setupMessageListener();

        // Setup click listeners
        findViewById(R.id.btn_save_message).setOnClickListener(v -> saveMessage());
        findViewById(R.id.fab_add_contact).setOnClickListener(v -> showAddContactDialog());
    }

    private void loadEmergencyData() {
        // Load message
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if(document.exists() && document.contains("emergencyMessage")) {
                        etMessage.setText(document.getString("emergencyMessage"));
                    }
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(this, "Failed to load emergency data" , Toast.LENGTH_SHORT).show();
                });

        // Load contacts
        db.collection("users").document(userId)
                .collection("emergencyContacts")
                .addSnapshotListener((value, error) -> {
                    if(error != null) return;

                    contacts.clear();
                    for(QueryDocumentSnapshot doc : value) {
                        Contact contact = doc.toObject(Contact.class);
                        contact.setId(doc.getId());
                        contacts.add(contact);
                    }
                    adapter.notifyDataSetChanged();
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


    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);
        EditText etName = view.findViewById(R.id.et_name);
        EditText etPhone = view.findViewById(R.id.et_phone);

        builder.setView(view)
                .setTitle("Add Contact")
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    if(!name.isEmpty() && !phone.isEmpty()) {
                        addContact(new Contact(name, phone));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addContact(Contact contact) {
        db.collection("users").document(userId)
                .collection("emergencyContacts")
                .add(contact)
                .addOnSuccessListener(document ->
                        Toast.makeText(this, "Contact added!", Toast.LENGTH_SHORT).show());
    }

    private void deleteContact(String contactId) {
        db.collection("users").document(userId)
                .collection("emergencyContacts")
                .document(contactId)
                .delete()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show());
    }
}