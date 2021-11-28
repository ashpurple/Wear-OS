package com.example.android.wearable.watchface.watchface;

public class SensorValueInfo {
    private int value;
    private String time;
    private double latitude;
    private double longitude;

    SensorValueInfo(double latitude, double longitude, String time){
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }
    SensorValueInfo(int value, String time){
        this.value = value;
        this.time = time;
    }

    public double getLatitude(){
        return this.latitude;
    }
    public double getLongitude(){
        return this.longitude;
    }

    public int getValue() {
        return this.value;
    }
    public String getTime() {
        return this.time;
    }

}
