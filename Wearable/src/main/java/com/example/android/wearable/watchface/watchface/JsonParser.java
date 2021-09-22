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
            return userInfo; // 여기서 return 안하면 error

        } catch (JSONException e){
            e.printStackTrace();
        }

        return userInfo;

    }

}
