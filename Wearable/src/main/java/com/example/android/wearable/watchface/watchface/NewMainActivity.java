package com.example.android.wearable.watchface.watchface;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.android.wearable.watchface.R;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class NewMainActivity extends Activity {
    String jsonInput = "";
    TextView userText;
    TextView hourMinuteText;
    TextView secondText;
    TextView monthDayText;
    TextView amPmText;

    UserInfo userInfo;
    JsonParser jsonParser;
    String name;
    String group;
    String birthday;
    String skin;
    String protective;
    String maxHeartRate;

    int year;
    int month;
    int day;
    int week;
    int hour;
    int minute;
    int second;
    int amPm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newmainpage);

        /* Text Views */
        userText = (TextView) findViewById(R.id.Name);
        hourMinuteText = (TextView) findViewById(R.id.HourMinute);
        secondText = (TextView) findViewById(R.id.Second);
        monthDayText = (TextView) findViewById(R.id.MonthDay);
        amPmText = (TextView) findViewById(R.id.AMPM);

        /* Threads */
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

    public void getTime(){
        Calendar now = Calendar.getInstance();
        year = now.get(Calendar.YEAR); // 년
        month = now.get(Calendar.MONTH); // 월
        day = now.get(Calendar.DAY_OF_MONTH); // 일
        week = now.get(Calendar.DAY_OF_WEEK); // 요일
        hour = now.get(Calendar.HOUR); // 시
        minute = now.get(Calendar.MINUTE); // 분
        second = now.get(Calendar.SECOND); // 초
        amPm = now.get(Calendar.AM_PM); // 오전 오후
    }

    public void setTime(){
        String newMonth = getMonth(month);
        String newWeek = getWeek(week);
        String newHour = getTime(hour);
        String newMin = getTime(minute);
        monthDayText.setText(newMonth + " " + day + " " + newWeek);
        hourMinuteText.setText(newHour + ":" + newMin);
        secondText.setText(String.valueOf(second));
        if(amPm == 0)
            amPmText.setText("AM");
        else
            amPmText.setText("PM");

    }
    public String getTime(int time){
        String newTime = String.valueOf(time);
        if(time < 10)
            newTime = "0"+newTime;
        return newTime;
    }

    public String getWeek(int week){
        String newWeek = "default";
        switch(week){
            case 1:
                newWeek = "Sun";
                break;
            case 2:
                newWeek = "Mon";
                break;
            case 3:
                newWeek = "Tue";
                break;
            case 4:
                newWeek = "Wed";
                break;
            case 5:
                newWeek = "Thu";
                break;
            case 6:
                newWeek = "Fri";
                break;
            case 7:
                newWeek = "Sat";
                break;
        }
        return newWeek;
    }


    public String getMonth(int month){
        String newMonth = "default";
        switch(month){
            case 0:
                newMonth = "Jan";
                break;
            case 1:
                newMonth = "Feb";
                break;
            case 2:
                newMonth = "Mar";
                break;
            case 3:
                newMonth = "Apr";
                break;
            case 4:
                newMonth = "May";
                break;
            case 5:
                newMonth = "Jun";
                break;
            case 6:
                newMonth = "Jul";
                break;
            case 7:
                newMonth = "Aug";
                break;
            case 8:
                newMonth = "Sep";
                break;
            case 9:
                newMonth = "Oct";
                break;
            case 10:
                newMonth = "Nov";
                break;
            case 11:
                newMonth = "Dec";
                break;
        }
        return newMonth;
    }


    class TimeThread extends Thread{
        @Override
        public void run() {
            while(!isInterrupted()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getTime();
                        setTime();
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
        public String urlStr = "http://15.164.45.229:8888/users/MDg6OTc6OTg6MEU6RTY6REE=";
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
                }
            });
        }
    }
}