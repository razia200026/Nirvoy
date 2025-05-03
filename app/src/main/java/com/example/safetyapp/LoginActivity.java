//package com.example.safetyapp;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.auth.FirebaseAuth;
//
//public class LoginActivity extends AppCompatActivity {
//
//    private EditText etEmail, etPassword;
//    private ProgressBar progressBar;
//    private FirebaseAuth mAuth;
//
//    @SuppressLint("MissingInflatedId")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        // Initialize Firebase Auth
//        mAuth = FirebaseAuth.getInstance();
//
//        // Initialize Views
//        etEmail = findViewById(R.id.inputEmail);
//        etPassword = findViewById(R.id.inputPassword);
//        progressBar = findViewById(R.id.progress_bar);
//
//        // Set up listeners
//        findViewById(R.id.imageView5).setOnClickListener(v -> attemptLogin());
//        findViewById(R.id.btnNewAccount).setOnClickListener(v ->
//                startActivity(new Intent(this, SignupActivity.class))
//        );
//    }
//
//    private void attemptLogin() {
//        String email = etEmail.getText().toString().trim();
//        String password = etPassword.getText().toString().trim();
//
//        if (validateInputs(email, password)) {
//            performLogin(email, password);
//        }
//    }
//
//    private boolean validateInputs(String email, String password) {
//        if (TextUtils.isEmpty(email)) {
//            etEmail.setError("Email is required");
//            etEmail.requestFocus();
//            return false;
//        }
//        if (TextUtils.isEmpty(password)) {
//            etPassword.setError("Password is required");
//            etPassword.requestFocus();
//            return false;
//        }
//        return true;
//    }
//
//    private void performLogin(String email, String password) {
//        progressBar.setVisibility(View.VISIBLE);
//
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    progressBar.setVisibility(View.GONE);
//                    if (task.isSuccessful()) {
//                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
//                        navigateToMain();
//                    } else {
//                        Toast.makeText(LoginActivity.this, "Authentication failed: " +
//                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
//                                Toast.LENGTH_LONG).show();
//                    }
//                });
//    }
//
//    private void navigateToMain() {
//        startActivity(new Intent(LoginActivity.this, MainActivity.class));
//        finish();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (mAuth.getCurrentUser() != null) {
//            navigateToMain();
//        }
//    }
//}
package com.example.safetyapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;

    private EditText etEmail, etPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase Auth instance
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        etEmail = findViewById(R.id.inputEmail);
        etPassword = findViewById(R.id.inputPassword);
        progressBar = findViewById(R.id.progress_bar);

        // Google Sign-In configuration
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // Defined in strings.xml
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Listeners
        findViewById(R.id.imageView5).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.btnNewAccount).setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class))
        );
        findViewById(R.id.googleSignInBtn).setOnClickListener(v -> signInWithGoogle());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (validateInputs(email, password)) {
            performLogin(email, password);
        }
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void performLogin(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Signed in as: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Toast.makeText(this, "Firebase Google login failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            navigateToMain();
        }
    }
}
