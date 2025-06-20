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
        setupLayout(R.layout.activity_main); // Your main content layout

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
        findViewById(R.id.btn_send_message).setOnClickListener(v ->
                startActivity(new android.content.Intent(MainActivity.this, EmergencyMessageActivity.class)));

        findViewById(R.id.btn_ai_voice).setOnClickListener(v ->
                startActivity(new android.content.Intent(MainActivity.this, EmergencyContactsActivity.class)));

        findViewById(R.id.btn_share_location).setOnClickListener(v ->
                startActivity(new android.content.Intent(MainActivity.this, LiveLocation.class)));
    }
}
