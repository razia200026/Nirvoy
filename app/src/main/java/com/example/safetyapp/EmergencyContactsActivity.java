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

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String userId;
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
        RecyclerView recyclerView = findViewById(R.id.rv_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter(contacts, this::deleteContact);
        recyclerView.setAdapter(adapter);

        // Load existing data
        loadEmergencyData();

        // Setup click listeners
        findViewById(R.id.fab_add_contact).setOnClickListener(v -> showAddContactDialog());
    }

    private void loadEmergencyData() {
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