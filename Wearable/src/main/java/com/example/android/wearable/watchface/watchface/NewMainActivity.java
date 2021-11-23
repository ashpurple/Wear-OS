package com.example.android.wearable.watchface.watchface;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
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

import androidx.core.content.ContextCompat;

import com.example.android.wearable.watchface.R;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NewMainActivity extends Activity {
    String jsonInput = "";
    TextView userText;
    TextView hourMinuteText;
    TextView secondText;
    TextView monthDayText;
    TextView amPmText;

    TextView heartRate;
    public float heartTemp =0,locationtemp=0, stepTemp =0;
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
        // ContextCompat.checkSelfPermission(); // ->권한 확인
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newmainpage);
        // Starts Service Binding
        Intent intent = new Intent(NewMainActivity.this, BackService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //getApplicationContext().bindService(new Intent(NewMainActivity.this, BackService.class), mConnection, Context.BIND_AUTO_CREATE);

        /* Text Views */
        userText = (TextView) findViewById(R.id.Name);
        hourMinuteText = (TextView) findViewById(R.id.HourMinute);
        secondText = (TextView) findViewById(R.id.Second);
        monthDayText = (TextView) findViewById(R.id.MonthDay);
        amPmText = (TextView) findViewById(R.id.AMPM);

        /* Threads */
        time = new Time();
        RequestThread requestThread = new RequestThread();
        requestThread.start();
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
//        public void LocationFind(){
//            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//            locationListener = new LocationListener() {
//
//                @Override
//                public void onLocationChanged(Location location) {
//
//                    Log.d("dd",String.valueOf(Math.round(location.getLatitude()*100)/100));
//                    //sendMsgToActivity(Math.round(location.getLongitude()*100)/100 ,"LANGI");
//                }
//
//                @Override
//                public void onStatusChanged(String provider, int status, Bundle extras) {
//
//                }
//
//                @Override
//                public void onProviderEnabled(String provider) {
//
//                }
//
//                @Override
//                public void onProviderDisabled(String provider) {
//
//                }
//            };
//            configureButton();
//
//        }
//    public void configureButton() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
//
//    }


    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d(MAIN_TAG,"Messenger handler");
            if(msg.getData().getFloat("HEART")!=0){
                heartTemp = msg.getData().getFloat("HEART");}

            if(msg.getData().getFloat("STEP")!=0){
                stepTemp = msg.getData().getFloat("STEP");}
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

    class TimeThread extends Thread{
        @Override
        public void run() {
            while(!isInterrupted()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTime();
                        heartRate =findViewById(R.id.HeartRateValue);
                        heartRate.setText(String.valueOf((int) heartTemp));
                        TextView step=findViewById(R.id.StepValue);
                        step.setText(String.valueOf((int) stepTemp));
                        double distancetemp= stepTemp *0.5;
                        TextView distance=findViewById(R.id.DistanceValue);
                        distance.setText((int)distancetemp+"m");
                        double calorytemp=Math.round((stepTemp *388/10000)*100)/100;
                        TextView calory=findViewById(R.id.CaloriesValue);
                        calory.setText(String.valueOf((int)calorytemp));
                        //LocationFind();
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

    // Fatigue And Stress Calculation -------------------------------------------------
    ArrayList HRF = new ArrayList<Double>();
    ArrayList HRL = new ArrayList<Double>();
    ArrayList RR_interval = new ArrayList<Double>();


    public double slope(final double x1, final double y1, final double x2, final double y2) {
        double m = 0;
        double b = x2 - x1;
        double d = y2 - y1;
        if(b!=0){
            m = d/b;
        }
        return m;
    }

    public void getHRF(){
        HRF.add(78);
        HRF.add(79);
        HRF.add(78);
        HRF.add(80);
        HRF.add(81);
        HRF.add(82);
        HRF.add(82);
        HRF.add(82);
        HRF.add(83);
        HRF.add(84);
    }

    public void getHRL(){
        HRL.add(64);
        HRL.add(63);
        HRL.add(63);
        HRL.add(62);
        HRL.add(62);
        HRL.add(62);
        HRL.add(61);
        HRL.add(61);
        HRL.add(60);
        HRL.add(60);
    }

    public double getMean(ArrayList list){
        double avg;
        int sum = 0;
        for(int i = 0; i < list.size(); i++){
            sum += (int)list.get(i);
        }
        avg = sum/list.size();
        return avg;
    }

    public double getStdev(ArrayList list){
        double avg;
        int sum = 0;
        for(int i = 0; i < list.size(); i++){
            sum += (int)list.get(i);
        }
        avg = sum/list.size();
        sum = 0;
        for(int i =0; i < list.size(); i++) {
            sum += Math.pow((int)list.get(i) - avg, 2);
        }
        double var = (double)sum / list.size();
        double Stdev = Math.sqrt(var);
        return Stdev;
    }

    public int CalculateFatigue() {
        int MinTiredUp = 10;
        int MaxTiredUp = 50;
        int MinTiredDn = 10;
        int MaxTiredDn = 50;
        int Timeline = 300;
        int Tired = 0;
        int LastTired = 0;
        int CurrentTired;

        CurrentTired = (int)slope(0, getMean(HRF), Timeline, getMean(HRL)*100);
        if(CurrentTired > 0){
            if(CurrentTired > MaxTiredUp){
                MaxTiredUp = (int)CurrentTired;
            }
            if(CurrentTired < MinTiredUp){
                MinTiredUp = (int)CurrentTired;
            }
        }

        if(CurrentTired < 0){
            if(CurrentTired > MaxTiredDn){
                MaxTiredDn = (int)CurrentTired;
            }
            if(CurrentTired < MinTiredDn){
                MinTiredDn = (int)CurrentTired;
            }
        }
        // 약간 이상한데?
        if(CurrentTired > 0){
            Tired = (int)((CurrentTired/(MaxTiredUp - MinTiredUp)) * 100);
            if (Tired > LastTired) {
                System.out.println("현재 피로도는" + Tired + "이며, 휴식이 필요합니다.");
            }
            else {
                System.out.println("현재 피로도는" + Tired + "이며, 회복중입니다.");
            }
        }

        if(CurrentTired < 0){
            Tired = (int)((CurrentTired/(MaxTiredDn - MinTiredDn)) * 100);
            if (Tired < LastTired) {
                System.out.println("현재 피로도는" + Tired + "이며, 휴식이 필요합니다.");
            }
            else {
                System.out.println("현재 피로도는" + Tired + "이며, 회복중입니다.");
            }
        }

        if(CurrentTired == 0){
            System.out.println("현재 피로도는" + Tired + "이며, 안정된 상태입니다.");
        }

        LastTired = Tired;
        return LastTired;
    }

    public void getRRInterval(){
        RR_interval.add((int)1772.89);
        RR_interval.add((int)1770.01);
        RR_interval.add((int)1760.22);
        RR_interval.add((int)1810.88);
        RR_interval.add((int)1690.02);
        RR_interval.add((int)1700.28);
    }

    public void CalculateStress(){
        double MinStress = 25.0;
        double MaxStress = 75.0;

        double Measure = getStdev(RR_interval);
        if(MinStress > Measure){
            MinStress = Measure;
        }
        if(MaxStress < Measure){
            MaxStress = Measure;
        }
        double CurrentStress = ((Measure-MinStress)/(MaxStress - MinStress))*100;
        System.out.println("측정 RR-Interval 표준편차:" + Measure);
        System.out.println("개인 최대 스트레스(%)" + MaxStress + ", 개인 최소 스트레스(%)" + MinStress);
        System.out.println("현재 스트레스" + (int)CurrentStress + "%");
    }


    // ----------------------------------------------------------------------------

    class RequestThread extends Thread {
        public String urlStr = "http://15.164.45.229:8889/users/MDg6OTc6OTg6MEU6RTY6REE=";
        Handler handler = new Handler();

        @Override
        public void run() {
            try {
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
                            println(line);
                        }
                        reader.close();

                    }
                    conn.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public void println(final String data) {
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
                    Log.e(MAIN_TAG, jsonInput);
                }
            });
        }
    }
}