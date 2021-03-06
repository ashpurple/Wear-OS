package com.example.android.wearable.watchface.watchface;

import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class JsonBuilder {
    private Timestamp timestamp;
    private SimpleDateFormat sdf;
    private String MAC;
    @SuppressLint("SimpleDateFormat")

    JsonBuilder(String MAC){
        this.MAC = MAC;
    }

    private void setTimestamp() {
        timestamp = new Timestamp(System.currentTimeMillis());
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
    public JSONObject getBLE(ArrayList<String> ble) throws JSONException {
        setTimestamp();

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("isEncrypted","true");

        JSONObject data = new JSONObject();
        data.put("command", "BLE");
        JSONArray jsonArr = new JSONArray();
        for(int i=1; i<ble.size(); i=i+4) {
            JSONObject value = new JSONObject();
            value.put("remoteAddress",ble.get(i));
            value.put("deviceName",ble.get(i+1));
            value.put("rssi",ble.get(i+2));
            value.put("timeStamp",ble.get(i+3));
            jsonArr.put(value);
        }
        data.put("values",jsonArr);

        String input = data.toString();
        input = encrypt(input);

        jsonObj.put("data",input);
        return jsonObj;
    }
    public JSONObject getHRM(ArrayList<SensorValueInfo> heart) throws JSONException {
        setTimestamp();

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("isEncrypted","true");

        JSONObject data = new JSONObject();
        data.put("command", "SENSOR_HRM");
        JSONArray jsonArr = new JSONArray();
        for(SensorValueInfo sensorValue: heart) {
            JSONObject value = new JSONObject();
            value.put("value", sensorValue.getValue());
            value.put("timeStamp", sensorValue.getTime());
            jsonArr.put(value);
        }
        data.put("values",jsonArr);

        String input = data.toString();
        input = encrypt(input);

        jsonObj.put("data",input);
        return jsonObj;
    }
    public JSONObject getPedometer(ArrayList<SensorValueInfo> step) throws JSONException {
        setTimestamp();

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("isEncrypted","true");

        JSONObject data = new JSONObject();
        data.put("command", "SENSOR_PEDOMETER");
        JSONArray jsonArr = new JSONArray();
        for(SensorValueInfo sensorValue: step) {
            JSONObject value = new JSONObject();
            float stepValue = sensorValue.getValue();
            value.put("value", stepValue);
            float calorie = Math.round((stepValue *388/10000)*100)/100;
            value.put("calorie", calorie);
            float distance = (float) (stepValue * 0.5);
            value.put("distance", distance);
            value.put("timeStamp", sensorValue.getTime());
            jsonArr.put(value);
        }
        data.put("values",jsonArr);

        String input = data.toString();
        input = encrypt(input);

        jsonObj.put("data",input);
        return jsonObj;
    }
    public JSONObject getGPS(ArrayList<SensorValueInfo> gps) throws JSONException {
        setTimestamp();

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("isEncrypted","true");

        JSONObject data = new JSONObject();
        data.put("command", "GPS");
        JSONArray jsonArr = new JSONArray();
        for(SensorValueInfo sensorValue: gps) {
            JSONObject value = new JSONObject();
            value.put("latitude", sensorValue.getLatitude());
            value.put("longitude", sensorValue.getLongitude());
            value.put("timeStamp", sensorValue.getTime());
            jsonArr.put(value);
        }
        data.put("values",jsonArr);

        String input = data.toString();
        input = encrypt(input);

        jsonObj.put("data",input);
        return jsonObj;
    }
    public JSONObject getStress(ArrayList<SensorValueInfo> stress) throws JSONException {
        setTimestamp();

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("isEncrypted","true");

        JSONObject data = new JSONObject();
        data.put("command", "STRESS");
        JSONArray jsonArr = new JSONArray();
        for(SensorValueInfo sensorValue: stress) {
            JSONObject value = new JSONObject();
            value.put("value", sensorValue.getValue());
            value.put("timeStamp", sensorValue.getTime());
            jsonArr.put(value);
        }
        data.put("values",jsonArr);
        String input = data.toString();
        input = encrypt(input);

        jsonObj.put("data",input);
        return jsonObj;
    }
    public JSONObject getFatigue(ArrayList<SensorValueInfo> fatigue) throws JSONException {
        setTimestamp();

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("isEncrypted","true");

        JSONObject data = new JSONObject();
        data.put("command", "FATIGUE");
        JSONArray jsonArr = new JSONArray();
        for(SensorValueInfo sensorValue: fatigue) {
            JSONObject value = new JSONObject();
            value.put("value", sensorValue.getValue());
            value.put("timeStamp", sensorValue.getTime());
            jsonArr.put(value);
        }
        data.put("values", jsonArr);
        String input = data.toString();
        input = encrypt(input);

        jsonObj.put("data",input);
        return jsonObj;
    }
    public JSONObject getBLE(String address, String name, int rssi) throws JSONException {
        setTimestamp();

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("isEncrypted","true");

        JSONObject data = new JSONObject();
        data.put("command", "BLE_SCAN");
        JSONArray jsonArr = new JSONArray();
        JSONObject value = new JSONObject();
        value.put("remoteAddress",address);
        value.put("deviceName",name);
        value.put("rssi",rssi);
        value.put("timeStamp",sdf.format(timestamp));
        jsonArr.put(value);
        data.put("values",jsonArr);

        String input = data.toString();
        input = encrypt(input);

        jsonObj.put("data",input);
        return jsonObj;
    }
    public JSONObject getBattery(int battery, String chargeState) throws JSONException {
        setTimestamp();

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("isEncrypted","true");

        JSONObject data = new JSONObject();
        data.put("command", "BATTERY");
        JSONArray jsonArr = new JSONArray();
        JSONObject value = new JSONObject();
        value.put("value",battery);
        value.put("chargeState",chargeState);
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
            encrypted = AES256s.encrypt(data, MAC);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }

}
