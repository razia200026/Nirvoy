// MainActivity.java
package com.example.safetyapp;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLayout(R.layout.activity_main, "", false); // Add title and back button flag

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
                    .setPositiveButton("WhatsApp", (dialog, which) ->
                            new EmergencyMessageHelper(MainActivity.this).sendMessage("whatsapp"))
                    .setNegativeButton("SMS", (dialog, which) ->
                            new EmergencyMessageHelper(MainActivity.this).sendMessage("sms"))
                    .show();
        });

        // Button click listeners for card items
        //Save contact and SMS
        findViewById(R.id.btn_save_contatcs).setOnClickListener(v ->
                startActivity(new android.content.Intent(MainActivity.this, SaveSMSActivity.class)));
        //AI Voice Detector
        findViewById(R.id.btn_ai_voice).setOnClickListener(v ->
                startActivity(new android.content.Intent(MainActivity.this, SaveSMSActivity.class)));
        //Live Location
        findViewById(R.id.btn_share_location).setOnClickListener(v ->
                startActivity(new android.content.Intent(MainActivity.this, LiveLocation.class)));
        //IncaseEmargency
        findViewById(R.id.btn_emergency_mode).setOnClickListener(v ->
                startActivity(new android.content.Intent(MainActivity.this, InCaseEmergencyActivity.class)));
        //Safe Zone
        findViewById(R.id.btn_safe_zone).setOnClickListener(v ->
                startActivity(new android.content.Intent(MainActivity.this, SafeZoneActivity.class)));
    }
}
