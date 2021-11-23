package com.example.android.wearable.watchface.watchface;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.android.wearable.watchface.R;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NewMainActivity extends Activity {
    // permission
    private final static String[] permissions = new String[]
            {Manifest.permission.BODY_SENSORS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int REQUEST_RECORD_PERMISSION = 100;

    String jsonInput = "";
    TextView userText;
    TextView hourMinuteText;
    TextView secondText;
    TextView monthDayText;
    TextView amPmText;

    TextView distanceText;
    TextView heartText;
    TextView stepText;
    TextView calorieText;
    TextView gpsText;
    TextView fatigueText;
    TextView stressText;

    public float heartTemp =0, stepTemp =0 , latitude = 0, longitude = 0, stress = 0, fatigue = 0;
    UserInfo userInfo;
    JsonParser jsonParser;
    String name;
    String group;
    String birthday;
    String skin;
    String protective;
    String maxHeartRate;
    String MAIN_TAG = "NEW MAIN";

    private LocationListener locationListener;
    private LocationManager locationManager;
    Time time;

    /* Service Binding */
    boolean isServiced = false;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Messenger mServiceMessenger = new Messenger(iBinder);
            try {
                Message msg = Message.obtain(null, BackService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mServiceMessenger.send(msg);
                isServiced = true;
                Log.d(MAIN_TAG,"onServiceConnected Success");
            }
            catch (RemoteException e) {
                Log.e(MAIN_TAG,"onServiceConnected Fail");
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiced= false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(MAIN_TAG, "onCreate");
        setPermissions(); // Permission Check

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newmainpage);

        // Starts Service Binding
        Intent intent = new Intent(NewMainActivity.this, BackService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        /* Text Views */
        userText = (TextView) findViewById(R.id.Name);
        hourMinuteText = (TextView) findViewById(R.id.HourMinute);
        secondText = (TextView) findViewById(R.id.Second);
        monthDayText = (TextView) findViewById(R.id.MonthDay);
        amPmText = (TextView) findViewById(R.id.AMPM);

        heartText =findViewById(R.id.HeartRateValue);
        stepText=findViewById(R.id.StepValue);
        distanceText=findViewById(R.id.DistanceValue);
        calorieText=findViewById(R.id.CaloriesValue);
        gpsText=findViewById(R.id.GPSValue);
        fatigueText=findViewById(R.id.FatigueValue);
        stressText=findViewById(R.id.StressValue);

        /* Threads */
        time = new Time();
        RequestThread requestThread = new RequestThread();
        requestThread.start();
        PostWearThread postWearThread = new PostWearThread();
        postWearThread.start();
        TimeThread timeThread = new TimeThread();
        timeThread.start();

        /* input parsing */
        jsonParser = new JsonParser();
        userInfo = new UserInfo();
        // default value -- can't be connected network
        name = userInfo.getName();
        group = userInfo.getGroup();
        birthday = userInfo.getBirthday();
        skin = userInfo.getSkin();
        protective = userInfo.getProtective();
        maxHeartRate = userInfo.getMaxHeartRate();
        updateInfo();

    }


    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d(MAIN_TAG,"Messenger handler");
            if(msg.getData().getFloat("HEART")!=0){
                heartTemp = msg.getData().getFloat("HEART");}
            if(msg.getData().getFloat("STEP")!=0){
                stepTemp = msg.getData().getFloat("STEP");}
            if(msg.getData().getFloat("LATITUDE")!=0){
                latitude = msg.getData().getFloat("LATITUDE");}
            if(msg.getData().getFloat("LONGITUDE")!=0){
                longitude = msg.getData().getFloat("LONGITUDE");}
            return false;
        }
    }));

    @Override
    protected void onStart() {
            super.onStart();
            updateInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void updateInfo(){
        try { // Parsing and Store
            userInfo = jsonParser.getUserInfo(jsonInput); // 유저 정보 객체
            // update
            name = userInfo.getName();
            group = userInfo.getGroup();
            birthday = userInfo.getBirthday();
            skin = userInfo.getSkin();
            protective = userInfo.getProtective();
            maxHeartRate = userInfo.getMaxHeartRate();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        userText.setText(name);
    }

    @SuppressLint("SetTextI18n")
    public void setTime(){
        time.setCalendar();
        String newMonth = time.transformMonth();
        String newWeek = time.transformWeek();
        String newHour = time.transformHour();
        String newMin = time.transformMin();
        monthDayText.setText(newMonth + " " + time.getDay() + " " + newWeek);
        hourMinuteText.setText(newHour + ":" + newMin);
        secondText.setText(String.valueOf(time.getSecond()));
        if(time.getAmPm() == 0)
            amPmText.setText("AM");
        else
            amPmText.setText("PM");
    }

    /* Threads */
    class TimeThread extends Thread{
        @Override
        public void run() {
            while(!isInterrupted()){
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        setTime();
                        heartText.setText(String.valueOf((int) heartTemp));
                        stepText.setText(String.valueOf((int) stepTemp));
                        double distance= stepTemp *0.5;
                        distanceText.setText((int)distance+"m");
                        double calorie=Math.round((stepTemp *388/10000)*100)/100;
                        calorieText.setText(String.valueOf((int)calorie));
                        int intLatitude = (int)latitude;
                        int intLongitude = (int)longitude;
                        gpsText.setText(intLatitude+"."+intLongitude);
                        //secondText.setText(second);
                    }
                });
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    class RequestThread extends Thread {
        public String urlStr = "http://15.164.45.229:8889/users/MDg6OTc6OTg6MEU6RTY6REE=";
        Handler handler = new Handler();
        @Override
        public void run() {
            Log.e(MAIN_TAG, "RUN");
            try {
                while(true) {
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    if (conn != null) {
                        conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                        conn.setRequestMethod("GET");
                        conn.setDoInput(true);
                        int resCode = conn.getResponseCode();
                        if (resCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String line = null;
                            while (true) {
                                line = reader.readLine();
                                if (line == null)
                                    break;
                                decrypt(line);
                            }
                            reader.close();
                        }
                        conn.disconnect();

                    }
                    sleep(5000); // delay value
                    handler.post(this);
                }
            } catch (Exception e) {
                Log.e(MAIN_TAG, "HTTP Request error");
                e.printStackTrace();
            }
        }
        public void decrypt(final String data) {
            Log.e(MAIN_TAG, "DECRYPT");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String temp = "";
                    int temp2 = 0;
                    try {
                        for (int i = 0; i < data.length(); i++) {
                            if (data.charAt(i) == ':')
                                temp2 = i;
                        }
                        temp = AES256s.decryptToString(data.substring(temp2 + 2, data.length() - 2), "08:97:98:0E:E6:DA");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    jsonInput = temp;
                    updateInfo();
                    Log.e(MAIN_TAG, jsonInput);
                }
            });
        }
    }

    class PostWearThread extends Thread {
        Handler handler = new Handler();
        @Override
        public void run() {
            Log.e(MAIN_TAG, "WEAR POST RUN");
            try {
                while(true) {
                    String urlStr = "http://15.164.45.229:8889/managers/MDg6OTc6OTg6MEU6RTY6REE=/wear/";
                    if(heartTemp == 0){
                        Log.e(MAIN_TAG, "OFF");
                        urlStr += "off";
                    } else{
                        Log.e(MAIN_TAG, "ON");
                        urlStr += "on";
                    }
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    if (conn != null) {
                        conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                        conn.setRequestMethod("POST");
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Content-Type","application/json");
                        conn.setRequestProperty("Accept","application/json");
                        Log.e(MAIN_TAG, "WEAR POST REQUEST");
                        int resCode = conn.getResponseCode();
                        conn.disconnect();
                    }
                    sleep(5000); // delay value
                    handler.post(this);
                }
            } catch (Exception e) {
                Log.e(MAIN_TAG, "WEAR POST Request error");
                e.printStackTrace();
            }
        }

    }

    class PostSensorThread extends Thread {
        Handler handler = new Handler();
        @Override
        public void run() {
            Log.e(MAIN_TAG, "SENSOR POST RUN");
            try {
                while(true) {
                    String urlStr = "http://15.164.45.229:8889/managers/MDg6OTc6OTg6MEU6RTY6REE=/sensorInfos/";
                    String json = "";
                    if(heartTemp == 0){
                        Log.e(MAIN_TAG, "OFF");
                        urlStr += "off";
                    } else{
                        Log.e(MAIN_TAG, "ON");
                        urlStr += "on";
                    }
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    if (conn != null) {
                        conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                        conn.setRequestMethod("POST");
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Content-Type","application/json");
                        conn.setRequestProperty("Accept","application/json");

                        OutputStream os = conn.getOutputStream();
                        os.write(json.getBytes("euc-kr"));
                        os.flush();

                        /* Read */
                        int resCode = conn.getResponseCode();
                        if (resCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String line = null;
                            while (true) {
                                line = reader.readLine();
                                if (line == null)
                                    break;
                                decrypt(line);
                            }
                            reader.close();
                        }

                        Log.e(MAIN_TAG, "SENSOR POST REQUEST");

                        conn.disconnect();
                    }
                    sleep(5000); // delay value
                    handler.post(this);
                }
            } catch (Exception e) {
                Log.e(MAIN_TAG, "SENSOR POST Request error");
                e.printStackTrace();
            }
        }
        public void decrypt(final String data) {
            Log.e(MAIN_TAG, "SENSOR POST DECRYPT");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String temp = "";
                    int temp2 = 0;
                    try {
                        for (int i = 0; i < data.length(); i++) {
                            if (data.charAt(i) == ':')
                                temp2 = i;
                        }
                        temp = AES256s.decryptToString(data.substring(temp2 + 2, data.length() - 2), "08:97:98:0E:E6:DA");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e(MAIN_TAG, "Request Result:" + temp);
                }
            });
        }
        public String encrypt(final String data) {
            final String[] code = {""};
            Log.e(MAIN_TAG, "SENSOR POST ENCRYPT");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    int temp2 = 0;
                    try {
                        code[0] = AES256s.encrypt(data.substring(temp2 + 2, data.length() - 2), "08:97:98:0E:E6:DA");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e(MAIN_TAG, "Encrypt Result:" + code[0]);
                }
            });
            return code[0];
        }
    }

    /* Permissions */
    private void setPermissions(){
        // If we already have all the permissions start immediately, otherwise request permissions
        if (permissionsGranted()) {
            Log.e(MAIN_TAG, "All Permissions OK");
            //init();
        } else {
            Log.e(MAIN_TAG, "Permission needed upadate");
            Log.d(MAIN_TAG, "Requesting permissions");
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_PERMISSION);
        }
    }
    private boolean permissionsGranted() {
        boolean result = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                result = false;
            }
        }
        return result;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e(MAIN_TAG, "Permission Granted");
            } else {
                Log.e(MAIN_TAG, "Permission Denied");
            }
        }
    }

}