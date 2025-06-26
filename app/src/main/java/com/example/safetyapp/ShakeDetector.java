package com.example.safetyapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {

    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;
    private static final int REQUIRED_SHAKE_COUNT = 3;

    private final OnShakeListener listener;
    private int shakeCount = 0;
    private long lastShakeTimestamp = 0;

    public interface OnShakeListener {
        void onShake();
    }

    public ShakeDetector(OnShakeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            final long now = System.currentTimeMillis();

            if (lastShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                shakeCount = 0;
            }

            lastShakeTimestamp = now;
            shakeCount++;

            if (shakeCount >= REQUIRED_SHAKE_COUNT) {
                shakeCount = 0;
                listener.onShake();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
