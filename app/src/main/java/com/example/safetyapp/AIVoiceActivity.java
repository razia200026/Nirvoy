package com.example.safetyapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.content.Intent;
import android.widget.*;
import androidx.annotation.Nullable;
import java.util.*;

public class AIVoiceActivity extends BaseActivity {

    private Switch switchEnableVoice;
    private EditText editTriggerPhrase;
    private Button btnSavePhrase, btnTestDetection;
    private TextView textDetectionStatus;

    private SharedPreferences prefs;
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;

    private static final String PREFS_NAME = "VoicePrefs";
    private static final String KEY_TRIGGER_PHRASE = "TriggerPhrase";
    private static final String KEY_VOICE_ENABLED = "VoiceEnabled";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLayout(R.layout.activity_ai_voice, "AI Voice Detection", true,R.id.nav_home);

        // Bind views
        switchEnableVoice = findViewById(R.id.switchEnableVoice);
        editTriggerPhrase = findViewById(R.id.editTriggerPhrase);
        btnSavePhrase = findViewById(R.id.btnSavePhrase);
        btnTestDetection = findViewById(R.id.btnTestDetection);
        textDetectionStatus = findViewById(R.id.textDetectionStatus);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load saved values
        String savedPhrase = prefs.getString(KEY_TRIGGER_PHRASE, "help me");
        boolean isVoiceEnabled = prefs.getBoolean(KEY_VOICE_ENABLED, false);

        editTriggerPhrase.setText(savedPhrase);
        switchEnableVoice.setChecked(isVoiceEnabled);

        // Save toggle state
        switchEnableVoice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_VOICE_ENABLED, isChecked).apply();
            Toast.makeText(this, isChecked ? "Voice detection enabled" : "Disabled", Toast.LENGTH_SHORT).show();
        });

        btnSavePhrase.setOnClickListener(v -> {
            String phrase = editTriggerPhrase.getText().toString().trim();
            if (!phrase.isEmpty()) {
                prefs.edit().putString(KEY_TRIGGER_PHRASE, phrase).apply();
                Toast.makeText(this, "Trigger phrase saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Enter a valid phrase", Toast.LENGTH_SHORT).show();
            }
        });

        btnTestDetection.setOnClickListener(v -> {
            startListening();
        });

        setupSpeechRecognizer();
    }

    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) { }
            @Override public void onBeginningOfSpeech() { }
            @Override public void onRmsChanged(float rmsdB) { }
            @Override public void onBufferReceived(byte[] buffer) { }
            @Override public void onEndOfSpeech() { }
            @Override public void onError(int error) {
                textDetectionStatus.setText("Error: " + error);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String trigger = prefs.getString(KEY_TRIGGER_PHRASE, "help me").toLowerCase();

                if (matches != null) {
                    for (String phrase : matches) {
                        if (phrase.toLowerCase().contains(trigger)) {
                            textDetectionStatus.setText("✅ Phrase matched: " + phrase);
                            return;
                        }
                    }
                    textDetectionStatus.setText("❌ Phrase not detected.");
                }
            }

            @Override public void onPartialResults(Bundle partialResults) { }
            @Override public void onEvent(int eventType, Bundle params) { }
        });
    }

    private void startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
            return;
        }

        speechRecognizer.startListening(speechIntent);
        textDetectionStatus.setText("🎤 Listening...");
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}
