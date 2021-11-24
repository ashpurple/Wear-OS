package com.example.android.wearable.watchface.watchface;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
            userInfo.setSkin(jsonObject.getString("maxHeartRate"));
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

}
