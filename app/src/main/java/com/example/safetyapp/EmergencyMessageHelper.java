package com.example.safetyapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Vibrator;
import android.telephony.SmsManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

public class EmergencyMessageHelper {

    private final Context context;

    public EmergencyMessageHelper(Context context) {
        this.context = context;
    }

    public void sendMessage(String method) {
        String message = "This is your emergency message. I need help!";

        if ("sms".equalsIgnoreCase(method)) {
            sendSmsToContacts(message);
            showPushNotification("Emergency SMS Sent", "Your SOS has been sent to emergency contacts.");
        } else if ("whatsapp".equalsIgnoreCase(method)) {
            // Placeholder for WhatsApp
            showPushNotification("WhatsApp Triggered", "SOS will be sent via WhatsApp.");
        }
    }

    private void sendSmsToContacts(String message) {
        try {
            // Replace with your actual contact list from Firebase or local DB
            List<String> contacts = List.of("01700000000", "01800000000");

            SmsManager smsManager = SmsManager.getDefault();
            for (String number : contacts) {
                smsManager.sendTextMessage(number, null, message, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPushNotification(String title, String message) {
        String CHANNEL_ID = "safety_alert_channel";
        String CHANNEL_NAME = "Safety Alerts";

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Intent to open MainActivity when notification tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
                PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flags);

        // Create notification channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("SOS emergency alerts");
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 1000, 1000}); // 3 seconds vibration
            channel.setSound(null, null); // Disable sound
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)  // Change to your app icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setVibrate(new long[]{0, 1000, 1000, 1000})
                .setSound(null)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(202, builder.build());

        vibratePhone();
    }

    private void vibratePhone() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            long[] pattern = {0, 1000, 1000, 1000}; // vibrate for 3 seconds total
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1));
            } else {
                vibrator.vibrate(pattern, -1);
            }
        }
    }
}
