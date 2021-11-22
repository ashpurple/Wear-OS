package com.example.android.wearable.watchface.watchface;


import android.annotation.SuppressLint;
import android.app.Service;
import android.location.LocationListener;
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
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
/* gps */
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


public class BackService extends Service implements SensorEventListener, LocationListener {
    public Context context = this;
    public Handler handler = null;
    public Runnable runnable = null;

    public static boolean start_handler = true;
    public static SpeechRecognizer speech = null;

    private final String[] permissions = new String[]{Manifest.permission.BODY_SENSORS};
    //SENSOR MANAGER STUFF
    private Messenger mClient=null;
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_SEND_TO_SERVICE = 3;
    public static final int MSG_SEND_TO_ACTIVITY = 4;
    private SensorManager sensorManager;
    public String SENSOR_TAG = "SensorEventListener";
    public Boolean hasCalledBecauseOfSensor = false;
    //LOCATION MANAGER
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onCreate() {
        Log.e(SENSOR_TAG, "onCreate");
        Log.d(SENSOR_TAG,"Permission "+ permissionsGranted());
        //Sensor Manager shit
        sensorManager = getSystemService(SensorManager.class); // sensor (heart rate, step)
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); // location (gps)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        if (!hasGps()) {
//            Log.e(LOCATION_TAG, "This hardware doesn't have GPS.");
//            // Fall back to functionality that does not use location or
//            // warn the user that location function is not available.
//        }
        startSensors();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOCATION_TAG, "Location Permission Fail");
        } else{
            Log.e(LOCATION_TAG, "Location Permission Success");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        }
        //getLocation();
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, 5000);
            }
        };
        handler.postDelayed(runnable, 1000);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(SENSOR_TAG, "onStartCommand");
        Log.d(SENSOR_TAG,"Permission "+ permissionsGranted());
        //Sensor Manager shit
        sensorManager = getSystemService(SensorManager.class); // sensor (heart rate, step)
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); // location (gps)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        if (!hasGps()) {
//            Log.e(LOCATION_TAG, "This hardware doesn't have GPS.");
//            // Fall back to functionality that does not use location or
//            // warn the user that location function is not available.
//        }
        startSensors();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOCATION_TAG, "Location Permission Fail");
        } else{
            Log.e(LOCATION_TAG, "Location Permission Success");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        }
        //getLocation();
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, 5000);
            }
        };
        handler.postDelayed(runnable, 1000);

        return START_STICKY;
    }

    private boolean permissionsGranted() {
        boolean result = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                result = false;

            }
        }
        return result;
    }

    @Override
    public boolean stopService(Intent name) {
        Log.e(SENSOR_TAG, "Stop Service");
        speech.stopListening();
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(runnable);
        stopUsingGPS(); // gps
        return super.stopService(name);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }


    // SENSOR SHIT HERE

    /**
     * Starts gathering sensor data
     */
    private void startSensors() {
        if (sensorManager != null) {
            Log.e(SENSOR_TAG, "startSensors");
            // Start heart rate sensor
            final Sensor heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);

            // Start step counter
            final Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    float lat = Math.round(location.getLatitude()*100)/100;
                    float lang = Math.round(location.getLongitude()*100)/100;
                    sendMsgToActivity(lat ,"LATI");
                    sendMsgToActivity(lang ,"LANGI");
                    Log.e(LOCATION_TAG,lat + " " + lang);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
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
                if(mClient!=null) {
                    sendMsgToActivity(value,"HEART");
                }
                Log.e(SENSOR_TAG, "heart Rate : " + value + "bpm");
                //getLocation();
                /**
                if (value > 90) {
                    String text = "CRITICAL HEART RATE. Heart Rate: " + value;
                    Log.e(SENSOR_TAG, text);
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                }**/
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                Log.e(SENSOR_TAG, "Step Count : " + value + "step");
                if(mClient!=null) {
                    sendMsgToActivity(value,"STEP");
                }
            }
        } else {
            Log.e(SENSOR_TAG, "No Sensor detected");
        }
    }

    /* gps location manager */
    private boolean hasGps() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }


    protected LocationManager locationManager;
    Location location;
    private double latitude;
    private double longitude;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // meter
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1 * 1; // milli * sec * min
    public String LOCATION_TAG = "LocationManager";
    private LocationListener locationListener;
//    public BackService(Context context){ // constructor
//        this.context = context;
//        getLocation();
//    }

    public void printGPS() {
        Log.e(LOCATION_TAG, "Latitude: " + latitude + " | Longitude: " + longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

        /**
    public Location getLocation() {
        try {
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.e(LOCATION_TAG, "Location can not be provided");
            } else {
                int hasFineLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
                if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Log.e(LOCATION_TAG, "Location Permission is not allowed");
                    return null;
                }
//                if (isNetworkEnabled){
//                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//                    if (locationManager != null) {
//                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                        if (location != null){
//                            latitude = location.getLatitude();
//                            longitude = location.getLongitude();
//                            Log.e(LOCATION_TAG, "Location Updated by network");
//                        } else {
//                            Log.e(LOCATION_TAG, "Location  is null");
//                        }
//                    } else{
//                        Log.e(LOCATION_TAG, "Location manager is null");
//                    }
//                }
//                else{
//                    Log.e(LOCATION_TAG, "Network is not connected");
//                }
                if (isGPSEnabled) {
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                        } else {
                            Log.e(LOCATION_TAG, "Location is null");
                        }
                    }
                } else {
                    Log.e(LOCATION_TAG, "Gps is not connected");
                }
            }
        } catch (Exception e) {
            Log.e(LOCATION_TAG, "" + e.toString());
        }
        printGPS();
        return location;
    }
**/
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(BackService.this);
        }
    }
    private void sendMsgToActivity(float sendValue,String type){
        try{
            Bundle bundle= new Bundle();
            bundle.putFloat(type,sendValue);
            Message msg=Message.obtain(null,MSG_SEND_TO_ACTIVITY);
            msg.setData(bundle);
            mClient.send(msg);
        }
        catch (RemoteException e){

        }
    }
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.w("test","ControlService - message what : "+msg.what +" , msg.obj "+ msg.obj);
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClient = msg.replyTo;

                    break;
            }
            return false;
        }
    }));

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        printGPS();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {

    }
    @Override
    public void onProviderDisabled(String s) {
    }
}
