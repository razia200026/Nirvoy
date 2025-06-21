package com.example.safetyapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safetyapp.adapter.EmergencyServiceAdapter;
import com.example.safetyapp.service.EmergencyService;

import java.util.Arrays;
import java.util.List;

public class InCaseEmergencyActivity extends BaseActivity {

    private RecyclerView rvServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLayout(R.layout.activity_in_case_emergency, "Emergency Helplines", true);

        rvServices = findViewById(R.id.rv_services);
        rvServices.setLayoutManager(new LinearLayoutManager(this));

        List<EmergencyService> services = Arrays.asList(
                new EmergencyService("Police", "999", R.drawable.e911_emergency_24px),
                new EmergencyService("Ambulance", "199", R.drawable.ambulance_24px),
                new EmergencyService("Fire Service", "998", R.drawable.fire_truck_24px),
                new EmergencyService("Local Hospital", "12345", R.drawable.ambulance_24px),
                new EmergencyService("Child Helpline", "1098", R.drawable.account_child_24px),
                new EmergencyService("Women Helpline", "10921", R.drawable.account_child_24px)
        );

        EmergencyServiceAdapter adapter = new EmergencyServiceAdapter(services, service -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + service.getPhone()));
            startActivity(callIntent);
        });

        rvServices.setAdapter(adapter);
    }
}
