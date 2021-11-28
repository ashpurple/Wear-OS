package com.example.android.wearable.watchface.watchface;


import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.location.LocationListener;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.SpeechRecognizer;
import android.util.Log;

/* gps */
import android.location.Location;
import android.location.LocationManager;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.List;


public class BackService extends Service implements SensorEventListener, LocationListener {
    public Context context = this;
    public Handler handler = null;
    public Runnable runnable = null;

    public static boolean start_handler = true;
    public static SpeechRecognizer speech = null;

    private final String[] permissions = new String[]{Manifest.permission.BODY_SENSORS};
    //SENSOR MANAGER STUFF
    private Messenger mClient = null;
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_SEND_TO_SERVICE = 3;
    public static final int MSG_SEND_TO_ACTIVITY = 4;
    private SensorManager sensorManager;
    public String SENSOR_TAG = "SensorEventListener";
    public Boolean hasCalledBecauseOfSensor = false;
    //LOCATION MANAGER STUFF
    protected LocationManager locationManager;
    private double latitude;
    private double longitude;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // meter
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1 * 1; // milli * sec * min
    public String LOCATION_TAG = "LocationManager";
    private FusedLocationProviderClient fusedLocationClient;
    /* Sensor Values */
    private float heart;
    private float step;
    /** BLE Declaration **/
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    /** layout Variables Declaration **/
    private EditText mEdit;
    private Button Start_Adv;
    private Button Stop_Adv;
    private boolean BLE_status = FALSE;

    @Override
    public void onCreate() {
        Log.e(SENSOR_TAG, "onCreate");

        //Sensor Manager shit
        sensorManager = getSystemService(SensorManager.class); // sensor (heart rate, step)

        if (!hasGps()) {
            Log.e(LOCATION_TAG, "This hardware doesn't have GPS.");
        } else {
            Log.e(LOCATION_TAG, "This hardware have GPS.");
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOCATION_TAG, "Location Permission Fail");
        } else {
            Log.e(LOCATION_TAG, "Location Permission Success");
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        }
        //getLocation();
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, locationListener);
        getLastKnownLocation();
        startSensors();
        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, 5000);
            }
        };
        handler.postDelayed(runnable, 10000);

        super.onCreate();
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
        } else {
            Log.e(SENSOR_TAG, "SensorManager is null");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Check if a value is attached, if not we can ignore it
        if (sensorEvent.values.length > 0) {
            final float value = sensorEvent.values[0];
            if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                heart = value;
                if (mClient != null) {
                    sendMsgToActivity(heart, "HEART");
                    sendMsgToActivity(step, "STEP");
                    sendGPSToActivity(latitude ,"LATITUDE");
                    sendGPSToActivity(longitude ,"LONGITUDE");
                }
                Log.e(SENSOR_TAG, "heart Rate : " + heart + "bpm");
                printGPS();
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                step = value;
                Log.e(SENSOR_TAG, "Step Count : " + step + "step");
                if (mClient != null) {
                    sendMsgToActivity(step, "STEP");
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
    public void printGPS() {
        Log.e(LOCATION_TAG, "Latitude: " + latitude + " | Longitude: " + longitude);
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLongitude();
            longitude = location.getLatitude();
            Log.e("LOCATION_CHANGE",latitude + " : " + longitude);
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

    private void getLastKnownLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(LOCATION_TAG,"Permission Error");
            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
                latitude = bestLocation.getLatitude();
                longitude = bestLocation.getLongitude();
            }
        }
    }
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(BackService.this);
        }
    }

    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.obj=="advon"){
                startAdvertising();
                Log.e("ININNIN","IININI");
                ((NewMainActivity) NewMainActivity.context).Broadcastingcheck=0;

            }
            if(msg.obj=="advoff"){

                stopAdvertising();

                Log.e("ININNIN","IININIasfsafas");
                ((NewMainActivity) NewMainActivity.context).Broadcastingcheck=1;

            }
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClient = msg.replyTo;
                    Log.e(SENSOR_TAG,"Binding Complete");
                    break;

            }
            return false;
        }
    }));
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
    private void sendGPSToActivity(double sendValue,String type){
        try{
            Bundle bundle= new Bundle();
            bundle.putDouble(type,sendValue);
            Message msg=Message.obtain(null,MSG_SEND_TO_ACTIVITY);
            msg.setData(bundle);
            mClient.send(msg);
        }
        catch (RemoteException e){

        }
    }

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
    /** BLE Advertising **/
    public void startAdvertising(){
        /** BLE Settings **/
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothLeAdvertiser == null) return;

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) //3 modes: LOW_POWER, BALANCED, LOW_LATENCY
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) // ULTRA_LOW, LOW, MEDIUM, HIGH
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .build();

        mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);

        ((NewMainActivity) NewMainActivity.context).Broadcastingcheck=0;

        Log.i("ADSTART", "LE Advertise Start."+ ((NewMainActivity) NewMainActivity.context).Broadcastingcheck);
    }


    public void stopAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        Log.i("ADSTOP", "LE Advertise Stopped.");
        BLE_status = FALSE;
        Toast.makeText(getApplicationContext(),"Restart advertising with new UserID..",Toast.LENGTH_SHORT).show();
        ((NewMainActivity) NewMainActivity.context).Broadcastingcheck=1;
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d("hihi:",settingsInEffect.toString());
            Log.i("ADSTART", "LE Advertise Started.");
            BLE_status = TRUE;
        }
        @Override
        public void onStartFailure(int errorCode) {
            Log.w("ADFAIL", "LE Advertise Failed: " + errorCode);
            BLE_status = FALSE;
        }
    };

}
