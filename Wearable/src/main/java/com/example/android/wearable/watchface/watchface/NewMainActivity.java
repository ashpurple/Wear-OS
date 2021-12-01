package com.example.android.wearable.watchface.watchface;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.android.wearable.watchface.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class NewMainActivity extends Activity {
    // permission
    private final static String[] permissions = new String[]
            {Manifest.permission.BODY_SENSORS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int REQUEST_RECORD_PERMISSION = 100;
    /* 세훈 */
 //   static final String DUID = "MDg6OTc6OTg6MEU6RTY6REE";
 //   static final String MAC = "08:97:98:0E:E6:DA";
  //  static final String userId = "2103";
    /* 찬빈 */
    static final String DUID = "OUM6NUE6NDQ6Qjc6Qjk6OEU";
    static final String MAC = "9C:5A:44:B7:B9:8E";
    static final String userId = "2106";
//    /* 건 */
//    static final String DUID = "MjA6MjA6MDg6Mjc6MDU6MDA";
//    static final String MAC = "20:20:08:27:05:00";
//    static final String userId = "2104";

    /* Info Text */
    String jsonInput = "";
    TextView userText;
    TextView groupText;
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
    Button Broadcasting;
    Button sos_button;
    Button schedule_button;
    Button message_button;
    Button schedule;
    ConstraintLayout layout;
    /* Get Info */
    String name;
    String group;
    String birthday;
    String skin;
    String protective;
    String maxHeartRate;
    ArrayList<String> ble;
    String MAIN_TAG = "NEW MAIN";
    /* Global variables */
    String scheduleInput;
    public float  stress = 0, fatigue = 0;
    public int heartTemp = 0, stepTemp = 0, fatigueTemp = 0, stressTemp = 0;
    public int previousStep = 0, currentStep =0;
    public boolean stepFlag = false;
    public double latitude = 0, longitude = 0;
    public ArrayList<String> blelist=new ArrayList<String>();
    public float distance, calorie;
    public boolean sosFlag = false;
    public boolean broadcastingFlag = false;
    public boolean scanningFlag = false;
    ArrayList<SensorValueInfo> heartList;
    ArrayList<SensorValueInfo> stepList;
    ArrayList<SensorValueInfo> gpsList;
    ArrayList<SensorValueInfo> fatigueList;
    ArrayList<SensorValueInfo> heartForFatigueList;
    ArrayList<SensorValueInfo> stressList;
    ArrayList<SensorValueInfo> heartForStressList;
    UserInfo userInfo;
    ArrayList<TimerInfo> timerList;
    JsonParser jsonParser;
    Time time;
    Context mContext;
    /* Timer variables */
    int upload_battery = 0;
    int upload_gps = 0;
    int collect_gps = 0;
    int upload_pedometer = 0;
    int collect_pedometer = 0;
    int upload_hrm = 0;
    int collect_hrm = 0;
    int upload_fatigue = 0;
    int collect_fatigue = 0;
    int upload_stress = 0;
    int collect_stress = 0;

    String onOff_battery;
    String onOff_gps;
    String onOff_pedometer;
    String onOff_hrm;
    String onOff_fatigue;
    String onOff_stress;
    String onOff_ble;
    Messenger mServiceMessenger;
    public static Context context;

    /* Service Binding */
    boolean isServiced = false;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mServiceMessenger = new Messenger(iBinder);
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

    public void sendMessageToService(String str){
        Log.d("in","inininin");
        if(isServiced){
            Log.d("in","inininin");
            if(mServiceMessenger!=null){
                Log.d("in","inininin");
                try{
                    Message msg=Message.obtain(null,BackService.MSG_SEND_TO_SERVICE,str);
                    Log.d("in","inininin");
                    msg.replyTo=mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // always on display
        mContext = getApplicationContext();
        context=this;
        Log.e(MAIN_TAG, "onCreate");
        setPermissions(); // Permission Check

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newmainpage);

        // Starts Service Binding
        final Intent intent = new Intent(NewMainActivity.this, BackService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        /* Value List */
        heartList = new ArrayList<>();
        gpsList = new ArrayList<>();
        stepList = new ArrayList<>();
        heartForFatigueList = new ArrayList<>();
        fatigueList = new ArrayList<>();
        heartForStressList = new ArrayList<>();
        stressList = new ArrayList<>();

        /* Text Views */
        userText = (TextView) findViewById(R.id.Name);
        hourMinuteText = (TextView) findViewById(R.id.HourMinute);
        secondText = (TextView) findViewById(R.id.Second);
        monthDayText = (TextView) findViewById(R.id.MonthDay);
        amPmText = (TextView) findViewById(R.id.AMPM);
        groupText = (TextView) findViewById(R.id.group);

        heartText =findViewById(R.id.HeartRateValue);
        stepText=findViewById(R.id.StepValue);
        distanceText=findViewById(R.id.DistanceValue);
        calorieText=findViewById(R.id.CaloriesValue);
        gpsText=findViewById(R.id.GPSValue);
        fatigueText=findViewById(R.id.FatigueValue);
        stressText=findViewById(R.id.StressValue);

        /* Button */
        scanning=(Button)findViewById(R.id.button);
        Broadcasting=(Button)findViewById(R.id.buttonbroad);
        sos_button =(Button)findViewById(R.id.buttonSos);
        layout = (ConstraintLayout) findViewById(R.id.const_layout);
        schedule_button = (Button)findViewById(R.id.schedule_btn);
        message_button = (Button)findViewById(R.id.message_btn);


        /* Threads */
        time = new Time();
        TimeThread timeThread = new TimeThread();
        timeThread.start();

        CollectSensorThread collectSensorThread = new CollectSensorThread();
        collectSensorThread.start();
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
        PostSensorThread postFatigue= new PostSensorThread("FATIGUE");
        postFatigue.start();
        PostSensorThread postStress = new PostSensorThread("STRESS");
        postStress.start();
        PostSensorThread postBLE= new PostSensorThread("BLE_SCAN");
        postBLE.start();
        /* input parsing */
        jsonParser = new JsonParser();
        userInfo = new UserInfo();
        // set default value -> it is not connected to network yet
        name = userInfo.getName();
        group = userInfo.getGroup();
        birthday = userInfo.getBirthday();
        skin = userInfo.getSkin();
        protective = userInfo.getProtective();
        maxHeartRate = userInfo.getMaxHeartRate();
        updateInfo();

        scanning.setOnClickListener(new View.OnClickListener(){ // BLE SCAN
            @Override
            public void onClick(View view){

                if(scanningFlag){
                    scanningFlag = false;
                    sendMessageToService("scanoff");
                    layout.setBackgroundColor(Color.BLACK);
                }
                else{
                    scanningFlag = true;
                    sendMessageToService("scanon");
                    layout.setBackgroundColor(Color.BLUE);
                }
            }

        });
        Broadcasting.setOnClickListener(new View.OnClickListener(){ // BLE Broadcasting
            @Override
            public void onClick(View view){

                if(broadcastingFlag){
                    broadcastingFlag = false;
                    sendMessageToService("advoff");
                    layout.setBackgroundColor(Color.BLACK);
                }
                else{
                    broadcastingFlag = true;
                    sendMessageToService("advon");
                    layout.setBackgroundColor(Color.BLUE);
                }
            }

        });

        sos_button.setOnClickListener(new View.OnClickListener(){ // SOS
            @Override
            public void onClick(View view){

                if(sosFlag) {
                    sosFlag = false;
                    layout.setBackgroundColor(Color.BLACK);
                }
                else{
                    sosFlag = true;
                    SosThread sosThread = new SosThread();
                    sosThread.start();
                    layout.setBackgroundColor(Color.RED);
                }

            }

        });

        schedule_button.setOnClickListener(new View.OnClickListener(){ // SCHEDULE
            @Override
            public void onClick(View view){
                ScheduleThread scheduleThread = new ScheduleThread();
                scheduleThread.start();
            }

        });

        message_button.setOnClickListener(new View.OnClickListener(){ // MESSAGE
            @Override
            public void onClick(View view){
                MessageThread messageThread = new MessageThread();
                messageThread.start();
                Intent intent=new Intent(getApplicationContext(),MessageActivity.class);
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
            if(msg.getData().getInt("HEART")!=0){
                heartTemp = msg.getData().getInt("HEART");
                if(!maxHeartRate.equals("None")) {
                    float max = Float.parseFloat(maxHeartRate);
                    if (heartTemp >= max) {
                        Toast.makeText(getApplicationContext(), "심박 경고 임계치(" + max + ") 초과", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            if(msg.getData().getInt("STEP")!=0){
                if(!stepFlag) {
                    stepTemp = msg.getData().getInt("STEP");
                    previousStep = stepTemp;
                } else{
                    stepTemp = msg.getData().getInt("STEP");
                }
                stepFlag = true;
            }
            if(msg.getData().getDouble("LATITUDE")!=0){
                latitude = msg.getData().getDouble("LATITUDE");}
            if(msg.getData().getDouble("LONGITUDE")!=0){
                longitude = msg.getData().getDouble("LONGITUDE");}
            if(msg.getData().getFloat("Advertise")!=0){
                }
            if(msg.getData().getStringArrayList("MAC")!=null){
                blelist = msg.getData().getStringArrayList("MAC");
                Log.e("BLE_SCAN:",String.valueOf(blelist));
            }
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
        groupText.setText(group);
    }

    public void updateTimer(){
        for(TimerInfo timer : timerList){
            String timerName = timer.getTmrNm();
            int uploadInterval = Integer.parseInt(timer.getIntervalSec());
            int collectInterval = Integer.parseInt(timer.getDurationSec());
            String onOff = timer.getOnOff();
            switch(timerName){
                case "BATTERY":
                    upload_battery = uploadInterval;
                    onOff_battery = onOff;
                    break;
                case "GPS":
                    upload_gps = uploadInterval;
                    collect_gps = collectInterval;
                    onOff_gps = onOff;
                    break;
                case "SENSOR_HRM":
                    upload_hrm = uploadInterval;
                    collect_hrm = collectInterval;
                    onOff_hrm = onOff;
                    break;
                case "SENSOR_PEDOMETER":
                    upload_pedometer = uploadInterval;
                    collect_pedometer = collectInterval;
                    onOff_pedometer = onOff;
                    break;
                case "FATIGUE":
                    upload_fatigue = uploadInterval;
                    collect_fatigue = collectInterval;
                    onOff_fatigue = onOff;
                    break;
                case "STRESS":
                    upload_stress = uploadInterval;
                    collect_stress = collectInterval;
                    onOff_stress = onOff;
                    break;
                case "BLE_SCAN":
                    Log.e("HI","HI");
                    onOff_ble = onOff;
                    break;
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String getTimestamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(timestamp);
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
                        heartText.setText(String.valueOf(heartTemp));
                        stepText.setText(String.valueOf(stepTemp));
                        distance = (float) (stepTemp * 0.5);
                        distanceText.setText((int)distance+"m");
                        calorie = Math.round((stepTemp *388/10000)*100)/100;
                        calorieText.setText(String.valueOf((int)calorie));
                        int intLatitude = (int)latitude;
                        int intLongitude = (int)longitude;
                        gpsText.setText(intLatitude+"."+intLongitude);
                        fatigueText.setText(String.valueOf(fatigueTemp));
                        stressText.setText(stressTemp+"%");
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
        public String urlStr = "http://15.164.45.229:8889/users/"+DUID+"=";

        //Handler handler = new Handler();
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
                    sleep(10000); // delay value
                    //handler.post(this);
                }
            } catch (Exception e) {
                Log.e(MAIN_TAG, "GET Request error");
                e.printStackTrace();
            }
        }
        public void decrypt(final String data) {
                    String temp = "";
                    int temp2 = 0;
                    try {
                        for (int i = 0; i < data.length(); i++) {
                            if (data.charAt(i) == ':')
                                temp2 = i;
                        }
                        temp = AES256s.decryptToString(data.substring(temp2 + 2, data.length() - 2), MAC);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    jsonInput = temp;
                    updateInfo();
        }
    }

    class PostWearThread extends Thread {
        @Override
        public void run() {
            try {
                while(true) {
                    sleep(60000); // upload delay 1 min
                    String urlStr = "http://15.164.45.229:8889/managers/"+DUID+"=/wear/";
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
        private String onOff;
        PostSensorThread(String sensorName){
            this.sensorType = sensorName;
        }

        String urlStr = "http://15.164.45.229:8889/managers/"+DUID+"=/sensorInfos";
        @Override
        public void run() {
            try {
                int initialDelay = 60000;
                while(true) {
                    sleep(initialDelay); // initial delay
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    if (conn != null) {
                        JsonBuilder jsonBuilder = new JsonBuilder(MAC);
                        JSONObject jsonObj = new JSONObject();
                        switch (sensorType){
                            case "BATTERY":
                                onOff = onOff_battery;
                                jsonObj = jsonBuilder.getBattery(getBatteryRemain(mContext), isBatteryCharging(mContext)); // battery
                                uploadInterval = upload_battery;
                                break;
                            case "GPS":
                                onOff = onOff_gps;
                                jsonObj = jsonBuilder.getGPS(gpsList); // gps
                                uploadInterval = upload_gps;
                                break;
                            case "SENSOR_HRM":
                                onOff = onOff_hrm;
                                jsonObj = jsonBuilder.getHRM(heartList); // heart
                                uploadInterval = upload_hrm;
                                break;
                            case "SENSOR_PEDOMETER":
                                onOff = onOff_pedometer;
                                jsonObj = jsonBuilder.getPedometer(stepList); // step
                                uploadInterval = upload_pedometer;
                                break;
                            case "FATIGUE":
                                onOff = onOff_fatigue;
                                jsonObj = jsonBuilder.getFatigue(fatigueList); // fatigue
                                uploadInterval = upload_fatigue;
                                break;
                            case "STRESS":
                                onOff = onOff_stress;
                                jsonObj = jsonBuilder.getStress(stressList); // fatigue
                                uploadInterval = upload_stress;
                                break;
                            case "BLE_SCAN":
                                onOff = onOff_ble;
                                jsonObj = jsonBuilder.getBLE(blelist);
                                break;
                        }
                        if(blelist.size()<3&&sensorType.equals("BLE_SCAN"))
                            continue;
                        if(!onOff.equals("O")) // if setting is off
                            continue;
                        Log.e(MAIN_TAG, sensorType+"| Upload Interval: "+uploadInterval);

                        conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                        conn.setRequestMethod("POST");
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Content-Type","application/json");
                        conn.setRequestProperty("Accept","application/json");

                        /* Body Write */
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                        bw.write(jsonObj.toString());
                        bw.flush();
                        bw.close();

                        /* Result Read */
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        JSONObject jsonObject = new JSONObject(in.readLine());
                        String returnMsg = jsonObject.getString("message");
                        if(returnMsg.equals("success")){ // if network access success
                            switch (sensorType){ // initialize lists
                                case "BATTERY":
                                    break;
                                case "GPS":
                                    gpsList.clear();
                                    break;
                                case "SENSOR_HRM":
                                    heartList.clear();
                                    break;
                                case "SENSOR_PEDOMETER":
                                    stepList.clear();
                                    break;
                                case "FATIGUE":
                                    fatigueList.clear();
                                    break;
                                case "STRESS":
                                    stressList.clear();
                                    break;
                                case "BLE_SCAN":
                                    blelist.clear();
                                    break;
                            }
                        }
                        Log.e(MAIN_TAG, sensorType+" "+returnMsg);

                        /* Response Code */
                        int resCode = conn.getResponseCode();
                        if (resCode == HttpURLConnection.HTTP_OK) {
                        } else { Log.e(MAIN_TAG, sensorType+" RESPONSE CODE ERROR");}

                        conn.disconnect();
                        if(sensorType.equals("BLE_SCAN"))
                            continue;
                        sleep(uploadInterval * 1000L - initialDelay); // upload delay
                    }
                }
            } catch (Exception e) {
                Log.e(MAIN_TAG, sensorType+" Request error");
                e.printStackTrace();
            }
        }
    }

    class CollectSensorThread extends Thread {
        @Override
        public void run() {
            try {
                int initialDelay = 10000; // wait for 10 sec
                sleep(initialDelay); // initial delay
                int sec = 0;
                while(true) {
                    sec += 1;
                    if(sec % 30 == 0){ // heart
                        heartList.add(new SensorValueInfo(heartTemp, getTimestamp()));
                    }
                    if(sec % 30 == 0){ // gps
                        gpsList.add(new SensorValueInfo(latitude, longitude, getTimestamp()));
                    }
                    if(sec % 30 == 0){ // step
                        currentStep = stepTemp;
                        stepList.add(new SensorValueInfo(currentStep - previousStep, getTimestamp()));
                        previousStep = currentStep;
                    }
                    if(sec % 2 == 0){ // heart for fatigue and stress
                        heartForFatigueList.add(new SensorValueInfo(heartTemp, getTimestamp()));
                        heartForStressList.add(new SensorValueInfo(heartTemp, getTimestamp()));
                    }
                    if(sec % 40 == 0){ // fatigue
                        CalculationFatigueStress calculationFatigueStress = new CalculationFatigueStress(heartForFatigueList);
                        int fatigueValue = calculationFatigueStress.calculateFatigue();
                        fatigueTemp = fatigueValue;
                        fatigueList.add(new SensorValueInfo(fatigueValue, getTimestamp()));
                        heartForFatigueList.clear();
                    }
                    if(sec % 40 == 0){ // stress
                        CalculationFatigueStress calculationFatigueStress = new CalculationFatigueStress(heartForStressList);
                        int stressValue = calculationFatigueStress.CalculateStress();
                        stressTemp = stressValue;
                        stressList.add(new SensorValueInfo(stressValue, getTimestamp()));
                        heartForStressList.clear();
                    }
                    sleep(1000);

                }
            } catch (Exception e) {
                Log.e(MAIN_TAG, "Collect Sensor Error");
                e.printStackTrace();
            }
        }
    }

    class SosThread extends Thread {
        @Override
        public void run() {
            try {
                String urlStr = "http://15.164.45.229:8889/managers/"+DUID+"=/sos";
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn != null) {
                    conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type","application/json");
                    conn.setRequestProperty("Accept","application/json");

                    /* Result Read */
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    JSONObject jsonObject = new JSONObject(in.readLine());
                    String returnMsg = jsonObject.getString("result");
                    if(returnMsg.equals("success")){
                        Log.e(MAIN_TAG, "SOS POST REQUEST");
                    }
                    int resCode = conn.getResponseCode();
                    conn.disconnect();
                }

            } catch (Exception e) {
                Log.e(MAIN_TAG, "SOS POST Request error");
                e.printStackTrace();
            }
        }
    }

    class ScheduleThread extends Thread {
        @Override
        public void run() {
            String schedule;
            try {
                String urlStr = "http://15.164.45.229:8889/managers/"+DUID+"=/schedules";
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn != null) {
                    conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type","application/json");
                    conn.setRequestProperty("Accept","application/json");

                    /* Result Read */
                    int resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        JSONObject jsonObject = new JSONObject(in.readLine());
                        String returnMsg = jsonObject.getString("data");
                        decrypt(returnMsg);
                    }
                    conn.disconnect();
                }

            } catch (Exception e) {
                Log.e(MAIN_TAG, "Schedule Request error");
                e.printStackTrace();
            }
        }
        public void decrypt(final String data) {
            String scheduleJson = "";
            try {
                scheduleJson = AES256s.decryptToString(data, MAC);
            } catch (Exception e) {
                e.printStackTrace();
            }
            scheduleInput = scheduleJson;

            ArrayList<Schedule> scheduleList = new ArrayList<>();
            try { // parsing
                scheduleList = jsonParser.getSchedule(scheduleJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println("SCHEDULE!");
            System.out.println(scheduleJson);
            for(Schedule schedule : scheduleList){
                System.out.println(schedule.getContents());
            }

            Intent intent=new Intent(getApplicationContext(),ScheduleActivity.class);
            intent.putExtra("schedule" , scheduleList );
            startActivity(intent);
        }
    }

    class MessageThread extends Thread {
        @Override
        public void run() {
            try {
                String urlStr = "http://15.164.45.229:8889/messages/"+DUID+"=/senders";
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn != null) {
                    conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type","application/json");
                    conn.setRequestProperty("Accept","application/json");

                    /* Result Read */
                    int resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        JSONObject jsonObject = new JSONObject(in.readLine());
                        String returnMsg = jsonObject.getString("data");
                        decrypt(returnMsg);
                    }
                    conn.disconnect();
                }

            } catch (Exception e) {
                Log.e(MAIN_TAG, "Message Request error");
                e.printStackTrace();
            }
        }
        public void decrypt(final String data) {
            String messageJson = "";
            try {
                messageJson = AES256s.decryptToString(data, MAC);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ArrayList<Sender> messageList = new ArrayList<>();
            try { // parsing
                messageList = jsonParser.getMessage(messageJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println("Message!");
            System.out.println(messageJson);
            for(Sender message : messageList){
                System.out.println(message.getUser_id());
            }
         //   MyMqttClient myMqttClient = new MyMqttClient();
         //   String[] args = {"2106","2103"};
         //   myMqttClient.main(args);
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
