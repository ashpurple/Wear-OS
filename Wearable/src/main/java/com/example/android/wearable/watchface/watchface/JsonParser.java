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
            JSONObject obj = jsonArray.getJSONObject(1); // index 1
            userInfo.setTmrSeq(obj.getString("tmrSeq"));
            userInfo.setBizId(obj.getString("bizId"));
            userInfo.setSchSeq(obj.getString("schSeq"));
            userInfo.setTmrNm(obj.getString("tmrNm"));
            userInfo.setOnOff(obj.getString("onOff"));
            userInfo.setIntervalSec(obj.getString("intervalSec"));
            userInfo.setDurationSec(obj.getString("durationSec"));
            userInfo.setTimerMillis(obj.getString("timeMillis")); // 값 변경 이슈
            userInfo.setLoopCount(obj.getString("loopCount"));
            userInfo.setTmrGbn(obj.getString("tmrGbn"));
            //userInfo.setMemo(obj.getString("memo")); // 값 변경 이슈
            userInfo.setStartDt(obj.getString("startDt"));
            userInfo.setEndDt(obj.getString("endDt"));
            //Log.d("Parser",userInfo.getTimerMillis());
            return userInfo; // 여기서 return 안하면 error

        } catch (JSONException e){
            e.printStackTrace();
        }

        return userInfo;

    }

}
