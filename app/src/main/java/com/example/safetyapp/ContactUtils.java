package com.example.safetyapp;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ContactUtils {

    public interface ContactCallback {
        void onContactsFetched(List<String> contactList);
    }

    public static void getEmergencyContacts(ContactCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.e("ContactUtils", "User not logged in");
            callback.onContactsFetched(new ArrayList<>());
            return;
        }

        String uid = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid).child("contacts");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> contactList = new ArrayList<>();
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    String phone = contactSnapshot.child("phone").getValue(String.class);
                    if (phone != null && !phone.isEmpty()) {
                        contactList.add(phone);
                    }
                }
                callback.onContactsFetched(contactList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("ContactUtils", "Database error: " + error.getMessage());
                callback.onContactsFetched(new ArrayList<>());
            }
        });
    }
}
