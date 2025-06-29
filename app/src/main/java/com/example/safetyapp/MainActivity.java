package com.example.safetyapp;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.safetyapp.helper.EmergencyMessageHelper;

public class MainActivity extends BaseActivity {

    private View pulseView;
    private Animation pulseAnimation;
    private PowerButtonReceiver powerButtonReceiver;

    private static final int REQ_NOTIFICATION_PERMISSION = 999;
    private static final int REQ_SMS_PERMISSION = 1001;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeDetector shakeDetector;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLayout(R.layout.activity_main, "Welcome to নির্ভয়!", false, R.id.nav_home);

        pulseView = findViewById(R.id.pulse_view);
        FrameLayout startShakeButton = findViewById(R.id.btn_sos);
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse);

        prefs = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);

        requestNotificationPermission();
        requestSMSPermission();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        shakeDetector = new ShakeDetector(() -> {
            new EmergencyMessageHelper(MainActivity.this).sendMessage("sms");
            Toast.makeText(MainActivity.this, "Emergency SMS Sent by Shake!", Toast.LENGTH_SHORT).show();
        });

        // Load saved shake count from settings and set to detector
        int shakeCountSetting = prefs.getInt("shake_count_threshold", 3);
        shakeDetector.setRequiredShakeCount(shakeCountSetting);

        startShakeButton.setOnClickListener(v -> {
            pulseView.startAnimation(pulseAnimation);
            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("SOS")
                    .setMessage("Send emergency message via WhatsApp or SMS?")
                    .setPositiveButton("WhatsApp", (dialog, which) -> {
                        EmergencyMessageHelper helper = new EmergencyMessageHelper(MainActivity.this);
                        helper.sendMessage("whatsapp");
                        helper.postToFacebookFeed("🚨 Emergency Alert! I might be in danger. Please check on me. #SafeApp");
                    })
                    .setNegativeButton("SMS", (dialog, which) -> {
                        EmergencyMessageHelper helper = new EmergencyMessageHelper(MainActivity.this);
                        helper.sendMessage("sms");
                        helper.postToFacebookFeed("🚨 Emergency Alert! I might be in danger. Please check on me. #SafeApp");
                    })
                    .show();
        });


        powerButtonReceiver = new PowerButtonReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(powerButtonReceiver, filter);

        findViewById(R.id.btn_save_contatcs).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SaveSMSActivity.class)));

        findViewById(R.id.btn_ai_voice).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AIVoiceActivity.class)));

        findViewById(R.id.btn_share_location).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LiveLocation.class)));

        findViewById(R.id.btn_emergency_mode).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, InCaseEmergencyActivity.class)));

        findViewById(R.id.btn_safe_zone).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SafeZoneActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload shake count in case user changed it in settings
        int shakeCountSetting = prefs.getInt("shake_count_threshold", 3);
        if (shakeDetector != null) {
            shakeDetector.setRequiredShakeCount(shakeCountSetting);
        }

        if (sensorManager != null && shakeDetector != null) {
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null && shakeDetector != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (powerButtonReceiver != null) {
            unregisterReceiver(powerButtonReceiver);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFICATION_PERMISSION);
            }
        }
    }

    private void requestSMSPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQ_SMS_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enable notifications from settings for better alerts", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQ_SMS_PERMISSION) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission denied. Shake SOS won't work.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
