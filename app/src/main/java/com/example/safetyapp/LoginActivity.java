package com.example.safetyapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // UI
        etEmail = findViewById(R.id.inputEmail);
        etPassword = findViewById(R.id.inputPassword);
        progressBar = findViewById(R.id.progress_bar);

        // Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Buttons
        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.btnNewAccount).setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        findViewById(R.id.googleSignInBtn).setOnClickListener(v -> signInWithGoogle());
        findViewById(R.id.forgetPassword).setOnClickListener(v -> showForgotPasswordDialog());
        findViewById(R.id.btnFacebook).setOnClickListener(v -> signInWithFacebook());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Exception e = task.getException();
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Signed in as: " + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Exception e = task.getException();
                        Toast.makeText(this, "Google authentication failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                });
    }

    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Facebook login cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Facebook login error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Facebook login success!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        Exception e = task.getException();
                        Toast.makeText(LoginActivity.this, "Facebook auth failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Facebook sign-in error", e);
                        e.printStackTrace();
                    }
                });
    }

    private void showForgotPasswordDialog() {
        EditText emailInput = new EditText(this);
        emailInput.setHint("Enter your registered email");

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("We'll send a reset link to your email.")
                .setView(emailInput)
                .setPositiveButton("Send", (dialog, which) -> {
                    String email = emailInput.getText().toString().trim();
                    if (!TextUtils.isEmpty(email)) {
                        sendResetEmail(email);
                    } else {
                        Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reset link sent to your email.", Toast.LENGTH_LONG).show();
                    } else {
                        Exception e = task.getException();
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                });
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Facebook callback
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        // Google sign-in
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            navigateToMain();
        }
    }
}
