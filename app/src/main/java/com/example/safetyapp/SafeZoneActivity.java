package com.example.safetyapp;

import android.os.Bundle;
import android.widget.*;

import com.example.safetyapp.helper.EmergencyMessageHelper;

public class SafeZoneActivity extends BaseActivity {

    private EmergencyMessageHelper helper;
    private Spinner methodSpinner;
    private Button btnSendOk;
    private EditText etCustomMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLayout(R.layout.activity_safe_zone, "Safe Zone", true);

        helper = new EmergencyMessageHelper(this);

        methodSpinner = findViewById(R.id.spinner_method);
        btnSendOk = findViewById(R.id.btn_send_ok);
        etCustomMessage = findViewById(R.id.et_custom_message);

        // Spinner setup
        String[] options = {"SMS", "WhatsApp"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options);
        methodSpinner.setAdapter(adapter);

        btnSendOk.setOnClickListener(v -> {
            String selected = methodSpinner.getSelectedItem().toString().toLowerCase(); // "sms" or "whatsapp"
            String customMessage = etCustomMessage.getText().toString().trim();

            String messageToSend = customMessage.isEmpty()
                    ? "Hi, I'm safe now. Just wanted to let you know!"
                    : customMessage;

            helper.sendCustomMessage(selected, messageToSend);
        });
    }
}
