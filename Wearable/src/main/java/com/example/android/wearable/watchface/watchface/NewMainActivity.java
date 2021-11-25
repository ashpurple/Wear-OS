package com.example.android.wearable.watchface.watchface;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.android.wearable.watchface.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class NewMainActivity extends Activity {
    // permission
    private final static String[] permissions = new String[]
            {Manifest.permission.BODY_SENSORS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int REQUEST_RECORD_PERMISSION = 100;

    /* Info Text */
    String jsonInput = "";
    TextView userText;
    TextView hourMinuteText;
    TextView secondText;
    TextView monthDayText;
    TextView amPmText;
    /* Sensor Text */
    TextView distanceText;
    TextView heartText;
    TextView stepText;
    TextView calorieText;
    TextView gpsText;
    TextView fatigueText;
    TextView stressText;
    /* Buttons */
    Button scanning;
    /* Get Info */
    String name;
    String group;
    String birthday;
    String skin;
    String protective;
    String maxHeartRate;
    String MAIN_TAG = "NEW MAIN";
    /* Global variables */
    public float heartTemp = 0, stepTemp = 0 , latitude = 0, longitude = 0, stress = 0, fatigue = 0;
    public float distance, calorie;
    UserInfo userInfo;
    ArrayList<TimerInfo> timerList;
    JsonParser jsonParser;
    Time time;
    Context mContext;
    /* Interval variables */
    int upload_battery = 0;
    int sensor_battery = 0;
    int upload_gps = 0;
    int sensor_gps = 0;
    int upload_pedometer = 0;
    int sensor_pedometer = 0;
    int upload_hrm = 0;
    int sensor_hrm = 0;

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // always on display
        mContext = getApplicationContext();
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
        scanning=(Button)findViewById(R.id.button);
        heartText =findViewById(R.id.HeartRateValue);
        stepText=findViewById(R.id.StepValue);
        distanceText=findViewById(R.id.DistanceValue);
        calorieText=findViewById(R.id.CaloriesValue);
        gpsText=findViewById(R.id.GPSValue);
        fatigueText=findViewById(R.id.FatigueValue);
        stressText=findViewById(R.id.StressValue);

        /* Threads */
        time = new Time();
        TimeThread timeThread = new TimeThread();
        timeThread.start();

        GetInfoThread getInfoThread = new GetInfoThread();
        getInfoThread.start();
        PostWearThread postWearThread = new PostWearThread();
        postWearThread.start();
        PostSensorThread postBatteryThread= new PostSensorThread("BATTERY");
        postBatteryThread.start();
        PostSensorThread postHrmThread= new PostSensorThread("SENSOR_HRM");
        postHrmThread.start();
        PostSensorThread postPedometer= new PostSensorThread("SENSOR_PEDOMETER");
        postPedometer.start();
        PostSensorThread postGPS= new PostSensorThread("GPS");
        postGPS.start();

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

        scanning.setOnClickListener(new View.OnClickListener(){ // BEL SCAN
            @Override
            public void onClick(View view){
             Intent intent=new Intent(getApplicationContext(),BeaconActivity.class);
             startActivity(intent);
            }

        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            timerList = userInfo.getTimerList();
            updateTimer();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        userText.setText(name);
    }

    public void updateTimer(){
        for(TimerInfo timer : timerList){
            String timerName = timer.getTmrNm();
            int uploadInterval = Integer.parseInt(timer.getIntervalSec());
            int sensorInterval = Integer.parseInt(timer.getDurationSec());
            switch(timerName){
                case "BATTERY":
                    upload_battery = uploadInterval;
                    sensor_battery = sensorInterval;
                    break;
                case "GPS":
                    upload_gps = uploadInterval;
                    sensor_gps = sensorInterval;
                    break;
                case "SENSOR_HRM":
                    upload_hrm = uploadInterval;
                    sensor_hrm = sensorInterval;
                    break;
                case "SENSOR_PEDOMETER":
                    upload_pedometer = uploadInterval;
                    sensor_pedometer = sensorInterval;
                    break;
            }
        }
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
                        distance = (float) (stepTemp * 0.5);
                        distanceText.setText((int)distance+"m");
                        calorie = Math.round((stepTemp *388/10000)*100)/100;
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

    class GetInfoThread extends Thread {
        public String urlStr = "http://15.164.45.229:8889/users/MDg6OTc6OTg6MEU6RTY6REE=";
        Handler handler = new Handler();
        @Override
        public void run() {
            Log.e(MAIN_TAG, "GET");
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
                    sleep(30000); // delay value
                    handler.post(this);
                }
            } catch (Exception e) {
                Log.e(MAIN_TAG, "GET Request error");
                e.printStackTrace();
            }
        }
        public void decrypt(final String data) {
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
                    //Log.e(MAIN_TAG, jsonInput);
                }
            });
        }
    }

    class PostWearThread extends Thread {
        Handler handler = new Handler();
        @Override
        public void run() {
            try {
                while(true) {
                    sleep(60000); // delay value
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
                    handler.post(this);
                }
            } catch (Exception e) {
                Log.e(MAIN_TAG, "WEAR POST Request error");
                e.printStackTrace();
            }
        }
    }

    class PostSensorThread extends Thread {
        private final String sensorType;
        private int uploadInterval;
        PostSensorThread(String sensorName){
            this.sensorType = sensorName;
        }

        String urlStr = "http://15.164.45.229:8889/managers/MDg6OTc6OTg6MEU6RTY6REE=/sensorInfos";
        Handler handler = new Handler();
        @Override
        public void run() {
            try {
                while(true) {
                    sleep(30000); // initial delay
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    if (conn != null) {
                        conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                        conn.setRequestMethod("POST");
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Content-Type","application/json");
                        conn.setRequestProperty("Accept","application/json");

                        JsonBuilder jsonBuilder = new JsonBuilder();
                        JSONObject jsonObj = new JSONObject();
                        switch (sensorType){
                            case "BATTERY":
                                jsonObj = jsonBuilder.getBattery(getBatteryRemain(mContext), isBatteryCharging(mContext)); // battery
                                uploadInterval = upload_battery;
                                break;
                            case "GPS":
                                jsonObj = jsonBuilder.getGPS(latitude, longitude); // gps
                                uploadInterval = upload_gps;
                                break;
                            case "SENSOR_HRM":
                                jsonObj = jsonBuilder.getHRM(heartTemp); // heart
                                uploadInterval = upload_hrm;
                                break;
                            case "SENSOR_PEDOMETER":
                                jsonObj = jsonBuilder.getPedometer((int)stepTemp, (int)calorie, (int)distance); // step
                                uploadInterval = upload_pedometer;
                                break;

                        }
                        Log.e(MAIN_TAG, sensorType+"&"+uploadInterval);
                        /* Body Write */
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                        bw.write(jsonObj.toString());
                        bw.flush();
                        bw.close();

                        /* Result Read */
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String returnMsg = in.readLine();
                        Log.e(MAIN_TAG, sensorType+" "+returnMsg);

                        /* Response Code */
                        int resCode = conn.getResponseCode();
                        if (resCode == HttpURLConnection.HTTP_OK) {
                        } else { Log.e(MAIN_TAG, sensorType+" RESPONSE CODE ERROR");}

                        conn.disconnect();
                        Log.e("UPLOAD_"+sensorType,String.valueOf(uploadInterval * 1000L));
                        sleep(uploadInterval * 1000L); // delay value
                    }
                    handler.post(this);
                }
            } catch (Exception e) {
                Log.e(MAIN_TAG, sensorType+" Request error");
                e.printStackTrace();
            }
        }
    }

    public static int getBatteryRemain(Context context) {
        Intent intentBattery = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = intentBattery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intentBattery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale;

        return (int)(batteryPct * 100);
    }
    public static String isBatteryCharging(Context context){
        Intent intentBattery = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = intentBattery.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        String result = "";
        if(status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL){
            result = "Y";
        }else if(status == BatteryManager.BATTERY_STATUS_NOT_CHARGING || status == BatteryManager.BATTERY_STATUS_DISCHARGING){
            result = "N";
        }
        return result;
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