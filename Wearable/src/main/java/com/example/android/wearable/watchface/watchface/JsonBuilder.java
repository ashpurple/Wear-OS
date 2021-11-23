package com.example.android.wearable.watchface.watchface;

import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class JsonBuilder {
    private Timestamp timestamp;
    private SimpleDateFormat sdf;
    @SuppressLint("SimpleDateFormat")

    private void setTimestamp() {
        timestamp = new Timestamp(System.currentTimeMillis());
        sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    }

    public JSONObject getHRM(float heart) throws JSONException {
        setTimestamp();

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("isEncrypted","true");

        JSONObject data = new JSONObject();
        data.put("command", "SENSOR_HRM");
        JSONArray jsonArr = new JSONArray();
        JSONObject value = new JSONObject();
        value.put("value",heart);
        value.put("timeStamp",sdf.format(timestamp));
        jsonArr.put(value);
        data.put("values",jsonArr);

        String input = data.toString();
        input = encrypt(input);

        jsonObj.put("data",input);
        return jsonObj;
    }
    public JSONObject getPedometer(int step, int calorie, int distance) throws JSONException {
        setTimestamp();

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("isEncrypted","true");

        JSONObject data = new JSONObject();
        data.put("command", "SENSOR_PEDOMETER");
        JSONArray jsonArr = new JSONArray();
        JSONObject value = new JSONObject();
        value.put("value",step);
        value.put("calorie",calorie);
        value.put("distance",distance);
        value.put("timeStamp",sdf.format(timestamp));
        jsonArr.put(value);
        data.put("values",jsonArr);

        String input = data.toString();
        input = encrypt(input);

        jsonObj.put("data",input);
        return jsonObj;
    }
    public JSONObject getGPS(float latitude, float longitude) throws JSONException {
        setTimestamp();

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("isEncrypted","true");

        JSONObject data = new JSONObject();
        data.put("command", "GPS");
        JSONArray jsonArr = new JSONArray();
        JSONObject value = new JSONObject();
        value.put("latitude",latitude);
        value.put("longitude",longitude);
        value.put("timeStamp",sdf.format(timestamp));
        jsonArr.put(value);
        data.put("values",jsonArr);

        String input = data.toString();
        input = encrypt(input);

        jsonObj.put("data",input);
        return jsonObj;
    }

    public String encrypt(final String data) {
        String encrypted = "";
        //Log.e(MAIN_TAG, "SENSOR POST ENCRYPT");
        try {
            encrypted = AES256s.encrypt(data, "08:97:98:0E:E6:DA");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }

}
