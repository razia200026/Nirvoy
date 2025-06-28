package com.example.safetyapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {
    // Recommended: start with a lower threshold — adjust to your testing
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;

    // Ignore shake events that happen too close together (in ms)
    private static final int SHAKE_SLOP_TIME_MS = 500;

    // If no shake happens within this time, reset the count (in ms)
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;

    // How many valid shakes to trigger the listener
    private static final int REQUIRED_SHAKE_COUNT = 3;

    private final OnShakeListener listener;
    private long mShakeTimestamp;
    private int mShakeCount;

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

            // Ignore this shake if it's too close to the previous one
            if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                return;
            }

            // Reset count if enough time has passed without a shake
            if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                mShakeCount = 0;
            }

            mShakeTimestamp = now;
            mShakeCount++;

            if (mShakeCount >= REQUIRED_SHAKE_COUNT) {
                mShakeCount = 0;
                listener.onShake();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
