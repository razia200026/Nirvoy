//package com.example.safetyapp;
//
//import android.content.Intent;
//import android.os.Bundle;
//
//import androidx.appcompat.app.ActionBarDrawerToggle;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.core.view.GravityCompat;
//import androidx.drawerlayout.widget.DrawerLayout;
//import com.google.android.material.navigation.NavigationView;
//import com.google.firebase.auth.FirebaseAuth;
//
//public class MainActivity extends AppCompatActivity {
//    private FirebaseAuth mAuth;
//    private DrawerLayout drawerLayout;
//    private ActionBarDrawerToggle toggle;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // Toolbar as ActionBar
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        mAuth = FirebaseAuth.getInstance();
//
//        // Setup DrawerLayout
//        drawerLayout = findViewById(R.id.drawer_layout);
//
//        // Setup ActionBarDrawerToggle
//        toggle = new ActionBarDrawerToggle(
//                this,
//                drawerLayout,
//                toolbar,
//                R.string.navigation_drawer_open,
//                R.string.navigation_drawer_close
//        );
//        drawerLayout.addDrawerListener(toggle);
//        toggle.syncState();
//
//        // Setup NavigationView and handle menu clicks
//        NavigationView navigationView = findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(item -> {
//            int id = item.getItemId();
//
//            if (id == R.id.nav_profile) {
//                // Open ProfileActivity
//                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
//                drawerLayout.closeDrawer(GravityCompat.START);
//                return true;
//            } else if (id == R.id.nav_logout) {
//                logoutUser();
//                drawerLayout.closeDrawer(GravityCompat.START);
//                return true;
//            }
//            return false;
//        });
//
//        // Button click listeners
//        findViewById(R.id.btn_emergency_message).setOnClickListener(v ->
//                startActivity(new Intent(MainActivity.this, EmergencyMessageActivity.class))
//        );
//
//        findViewById(R.id.btn_emergency_contact).setOnClickListener(v ->
//                startActivity(new Intent(MainActivity.this, EmergencyContactsActivity.class))
//        );
//
//        // Make sure the drawer is CLOSED at startup (optional but safe)
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawer(GravityCompat.START);
//        }
//    }
//
//    private void logoutUser() {
//        mAuth.signOut();
//        redirectToLogin();
//    }
//
//    private void redirectToLogin() {
//        Intent intent = new Intent(this, LoginActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        finish();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (mAuth.getCurrentUser() == null) {
//            redirectToLogin();
//        }
//    }
//}

package com.example.safetyapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar as ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hide the App Name from Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mAuth = FirebaseAuth.getInstance();

        // Setup DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // Setup ActionBarDrawerToggle
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup NavigationView and handle menu clicks
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                // Open ProfileActivity
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_logout) {
                logoutUser();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
            return false;
        });

        // Button click listeners
        findViewById(R.id.btn_emergency_message).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, EmergencyMessageActivity.class))
        );

        findViewById(R.id.btn_emergency_contact).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, EmergencyContactsActivity.class))
        );

        // Make sure the drawer is CLOSED at startup
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            redirectToLogin();
        }
    }
}
