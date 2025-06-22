package com.example.safetyapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends BaseActivity {

    private SwitchMaterial switchSound, switchVibration, switchContacts, switchVoiceDetection;
    private SharedPreferences prefs;

    private static final int REQ_CONTACTS_PERMISSION = 1001;
    private static final int REQ_RECORD_AUDIO_PERMISSION = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // IMPORTANT: Setup base layout with your settings layout, toolbar title, back button, and selected bottom nav item
        setupLayout(R.layout.activity_settings, "Settings", true, R.id.nav_settings);

        prefs = getSharedPreferences("AppSettingsPrefs", MODE_PRIVATE);

        // Initialize switches after layout is set
        switchSound = findViewById(R.id.switch_sound);
        switchVibration = findViewById(R.id.switch_vibration);
        switchContacts = findViewById(R.id.switch_contacts);
        switchVoiceDetection = findViewById(R.id.switch_voice_detection);

        // Load saved states
        switchSound.setChecked(prefs.getBoolean("sound", true));
        switchVibration.setChecked(prefs.getBoolean("vibration", true));
        switchContacts.setChecked(prefs.getBoolean("contacts", false));
        switchVoiceDetection.setChecked(prefs.getBoolean("voice_detection", false));

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("sound", isChecked).apply();
            Toast.makeText(this, "Sound " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("vibration", isChecked).apply();
            Toast.makeText(this, "Vibration " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        switchContacts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            REQ_CONTACTS_PERMISSION);
                }
            }
            prefs.edit().putBoolean("contacts", isChecked).apply();
        });

        switchVoiceDetection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            REQ_RECORD_AUDIO_PERMISSION);
                }
            }
            prefs.edit().putBoolean("voice_detection", isChecked).apply();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ((requestCode == REQ_CONTACTS_PERMISSION || requestCode == REQ_RECORD_AUDIO_PERMISSION)
                && grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission denied. You can enable it from settings.", Toast.LENGTH_LONG).show();

            // Revert the switch toggle if permission denied
            if (requestCode == REQ_CONTACTS_PERMISSION) {
                switchContacts.setChecked(false);
                prefs.edit().putBoolean("contacts", false).apply();
            } else if (requestCode == REQ_RECORD_AUDIO_PERMISSION) {
                switchVoiceDetection.setChecked(false);
                prefs.edit().putBoolean("voice_detection", false).apply();
            }
        }
    }
}
