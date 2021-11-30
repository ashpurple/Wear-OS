package com.example.android.wearable.watchface.watchface;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class JsonParser {

    public UserInfo getUserInfo(String input) throws JSONException {
        UserInfo userInfo = new UserInfo();

        try {
            JSONObject jsonObject = new JSONObject(input);
            userInfo.setBirthday(jsonObject.getString("birthday"));
            userInfo.setName(jsonObject.getString("name"));
            userInfo.setProtective(jsonObject.getString("protective"));
            userInfo.setSkin(jsonObject.getString("skin"));
            userInfo.setMaxHeartRate(jsonObject.getString("maxHeartRate"));
            userInfo.setGroup(jsonObject.getString("group"));

            // timerList Array
            JSONArray jsonArray = jsonObject.getJSONArray("timerList");
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                TimerInfo timerInfo = new TimerInfo();
                timerInfo.setTmrSeq(obj.getString("tmrSeq"));
                timerInfo.setBizId(obj.getString("bizId"));
                timerInfo.setSchSeq(obj.getString("schSeq"));
                timerInfo.setTmrNm(obj.getString("tmrNm"));
                timerInfo.setOnOff(obj.getString("onOff"));
                timerInfo.setIntervalSec(obj.getString("intervalSec"));
                timerInfo.setDurationSec(obj.getString("durationSec"));
                timerInfo.setTimerMillis(obj.getString("timeMillis")); // 값 변경 이슈
                timerInfo.setLoopCount(obj.getString("loopCount"));
                timerInfo.setTmrGbn(obj.getString("tmrGbn"));
                //userInfo.setMemo(obj.getString("memo")); // 값 변경 이슈
                timerInfo.setStartDt(obj.getString("startDt"));
                timerInfo.setEndDt(obj.getString("endDt"));
                userInfo.setTimerList(timerInfo);
            }
            return userInfo; // return 안하면 error

        } catch (JSONException e){
            e.printStackTrace();
        }
        return userInfo;
    }

    public ArrayList <Schedule> getSchedule(String input) throws JSONException {
        JSONArray jsonArray = new JSONArray(input);
        ArrayList<Schedule> schedules = new ArrayList<>();
        try {
            for(int i = 0; i < jsonArray.length(); i++) {
                Schedule schedule = new Schedule();
                JSONObject obj = jsonArray.getJSONObject(i);
                schedule.setBiz_name(obj.getString("bizNm"));
                schedule.setCompany_name(obj.getString("companyNm"));
                schedule.setStart_date(obj.getString("startDt"));
                schedule.setEnd_date(obj.getString("endDt"));
                schedule.setLocation(obj.getString("location"));
                schedule.setStart_time(obj.getString("startTime"));
                schedule.setEnd_time(obj.getString("endTime"));
                schedule.setTitle(obj.getString("title"));
                schedule.setDept_name(obj.getString("deptNm"));
                schedule.setContents(obj.getString("content"));
                schedules.add(schedule);
            }
            return schedules; // return 안하면 error

        } catch (JSONException e){
            e.printStackTrace();
        }
        return schedules;
    }


}
