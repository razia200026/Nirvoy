package com.example.safetyapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class EmergencyMessageHelper {

    private final Context context;
    private static final String CHANNEL_ID = "emergency_alert_channel";

    public EmergencyMessageHelper(Context context) {
        this.context = context;
    }

    public void sendMessage(String method) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("EmergencyHelper", "User not authenticated");
            return;
        }

        String uid = user.getUid();
        DatabaseReference contactsRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid).child("contacts");

        DatabaseReference msgRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid).child("emergency_message");

        msgRef.get().addOnSuccessListener(msgSnap -> {
            String message = msgSnap.getValue(String.class);
            if (message == null || message.isEmpty()) {
                message = "This is an emergency. Please help me!";
            }

            String finalMessage = message;
            contactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<String> contactList = new ArrayList<>();
                    for (DataSnapshot contactSnap : snapshot.getChildren()) {
                        String phone = contactSnap.child("phone").getValue(String.class);
                        if (phone != null && !phone.isEmpty()) {
                            contactList.add(phone);
                        }
                    }

                    for (String number : contactList) {
                        if ("sms".equalsIgnoreCase(method)) {
                            sendSMS(number, finalMessage);
                        } else {
                            sendWhatsApp(number, finalMessage);
                        }
                    }

                    showNotification("Emergency Alert Sent", "Your emergency message was sent successfully.");
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("EmergencyHelper", "Failed to fetch contacts: " + error.getMessage());
                }
            });
        });
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
        } catch (Exception e) {
            Log.e("EmergencyHelper", "SMS failed: " + e.getMessage());
        }
    }

    private void sendWhatsApp(String phoneNumber, String message) {
        try {
            String url = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("EmergencyHelper", "WhatsApp failed: " + e.getMessage());
        }
    }

    private void showNotification(String title, String message) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Emergency Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Used for emergency alert notifications");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 5000}); // 5 seconds vibration
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Replace with your icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Lock screen visibility
                .setVibrate(new long[]{0, 5000}) // 5 sec vibration
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify(1001, builder.build());
    }
}
