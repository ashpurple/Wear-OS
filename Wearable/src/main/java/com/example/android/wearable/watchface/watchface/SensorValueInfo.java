package com.example.android.wearable.watchface.watchface;

public class SensorValueInfo {
    private float value;
    private String time;
    private double latitude;
    private double longitude;

    SensorValueInfo(double latitude, double longitude, String time){
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }
    SensorValueInfo(float value, String time){
        this.value = value;
        this.time = time;
    }

    public double getLatitude(){
        return this.latitude;
    }
    public double getLongitude(){
        return this.longitude;
    }

    public float getValue() {
        return this.value;
    }
    public String getTime() {
        return this.time;
    }

}
