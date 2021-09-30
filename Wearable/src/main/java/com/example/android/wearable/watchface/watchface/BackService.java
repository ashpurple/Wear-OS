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

public class BackService extends Service implements RecognitionListener, SensorEventListener {
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

    @Override
    public void onBeginningOfSpeech() {
        Log.e(LOG_TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.e(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.e(LOG_TAG, "onEndOfSpeech");
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        if (!errorMessage.equals("")){
            Log.e(LOG_TAG, "FAILED " + errorMessage);
        }
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.e(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.e(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.e(LOG_TAG, "onReadyForSpeech");
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResults(Bundle results) {
        try {
            Log.e(LOG_TAG, "onResults");
            ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String text = "";
            if (matches != null) {
                for (String result : matches) {
                    text = result;
                    break;
                }
            }
            Log.e("Result", text);

            if (text.toLowerCase().trim().contains("call to")) {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                String number = getPhoneNumber(text.toLowerCase().trim().replace("call to","").trim().toLowerCase().replace(".",""),context);
                Log.e("SearchName","=>"+text.toLowerCase().trim().replace("call to","").trim().toLowerCase().replace(".",""));
                Log.e("Number","=>"+number);
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("tel:" + number));
                context.startActivity(intent);
            }
            speech.stopListening();
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 1000);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRmsChanged(float rmsdB) { }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (speech != null) {
            speech.destroy();
            Log.e(LOG_TAG, "destroy");
        }
        super.onTaskRemoved(rootIntent);
    }

    public String getPhoneNumber(String name, Context context) {
        String ret = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + name +"%'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);
        if (c != null && c.moveToFirst()) {
            ret = c.getString(0);
        }
        if (c != null) {
            c.close();
        }
        if(ret==null)
            ret = "Unsaved";
        return ret;
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
                        hasCalledBecauseOfSensor = true;
                        //Place call
                        String number = getPhoneNumber("david",context);
                        Log.e("Number","=>"+number);
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse("tel:" + number));
                        context.startActivity(intent);
                    }
                }
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