package com.example.android.wearable.watchface.watchface;

public class SensorValueInfo {
    private float value;
    private String time;
    private float latitude;
    private float longitude;

    SensorValueInfo(float latitude, float longitude, String time){
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }
    SensorValueInfo(float value, String time){
        this.value = value;
        this.time = time;
    }

    public float getLatitude(){
        return this.latitude;
    }
    public float getLongitude(){
        return this.longitude;
    }

    public float getValue() {
        return this.value;
    }
    public String getTime() {
        return this.time;
    }

}
