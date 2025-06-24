package com.example.safetyapp;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;

import com.example.safetyapp.helper.EmergencyMessageHelper;

public class MainActivity extends BaseActivity {

    private View pulseView;
    private Animation pulseAnimation;
    private PowerButtonReceiver powerButtonReceiver; // 👈 Add receiver reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLayout(R.layout.activity_main, "Welcome to নির্ভয়!", false, R.id.nav_home);

        // Pulse Effect Setup
        pulseView = findViewById(R.id.pulse_view);
        FrameLayout startShakeButton = findViewById(R.id.btn_sos);

        // Load the pulse animation
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse);

        startShakeButton.setOnClickListener(v -> {
            pulseView.startAnimation(pulseAnimation);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("SOS")
                    .setMessage("Send emergency message via WhatsApp or SMS?")
                    .setPositiveButton("WhatsApp", (dialog, which) -> {
                        new EmergencyMessageHelper(MainActivity.this).sendMessage("whatsapp");
                    })
                    .setNegativeButton("SMS", (dialog, which) -> {
                        new EmergencyMessageHelper(MainActivity.this).sendMessage("sms");
                    })
                    .show();
        });


        // Register Power Button Receiver
        powerButtonReceiver = new PowerButtonReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(powerButtonReceiver, filter);

        // Button click listeners for card items
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
    protected void onDestroy() {
        super.onDestroy();
        if (powerButtonReceiver != null) {
            unregisterReceiver(powerButtonReceiver);
        }
    }
}
