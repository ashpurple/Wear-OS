package com.example.android.wearable.watchface.watchface;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class BackService extends Service implements SensorEventListener {
    public Context context = this;
    public Handler handler = null;
    public Runnable runnable = null;

    public static boolean start_handler = true;
    public static SpeechRecognizer speech = null;
    public Intent recognizerIntent;
    public String LOG_TAG = "VoiceRecognitionActivity";

    //SENSOR MANAGER STUFF
    private SensorManager sensorManager;
    public String SENSOR_TAG = "SensorEventListener";
    public Boolean hasCalledBecauseOfSensor = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Sensor Manager shit
        sensorManager = getSystemService(SensorManager.class);
        startSensors();

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {

                handler.postDelayed(runnable, 5000);
            }

        };

        handler.postDelayed(runnable, 1000);
        return START_STICKY;
    }

    @Override
    public boolean stopService(Intent name) {
        Log.e("Service", "Stop");
        speech.stopListening();
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(runnable);
        return super.stopService(name);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // SENSOR SHIT HERE

    /**
     * Starts gathering sensor data
     */
    private void startSensors() {
        if (sensorManager != null) {
            // Start heart rate sensor
            final Sensor heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);

            // Start step counter
            final Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(SENSOR_TAG, "Sensor !");
        } else {
            Log.e(SENSOR_TAG, "SensorManager is null");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.e(SENSOR_TAG, "onSensorChanged");
        // Check if a value is attached, if not we can ignore it
        if (sensorEvent.values.length > 0) {
            final float value = sensorEvent.values[0];

            if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                Log.e(SENSOR_TAG, " heartrate Detected. Value: " + value);

                if (value > 90){
                    String text = "CRITICAL HEART RATE. Heart Rate: " + value;
                    Log.d(SENSOR_TAG, text);
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

                    if (hasCalledBecauseOfSensor == false){
                       
                    }
                }
            }
            else if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                Log.e(SENSOR_TAG, "Received new step counter value: " + value);

            }
            }
        else {
            Log.e(SENSOR_TAG, " No heartrate Detected. Value: ");
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
