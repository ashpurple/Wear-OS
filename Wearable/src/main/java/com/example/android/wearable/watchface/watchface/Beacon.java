package com.example.android.wearable.watchface.watchface;

public class Beacon {
    private String address;
    private int rssi;
    private String now;
    private String name;

    public Beacon(String address, int rssi, String now, String name) {
        this.address = address;
        this.rssi = rssi;
        this.now = now;
        this.name= name;
    }

    public String getAddress() {
        return address;
    }

    public int getRssi() {
        return rssi;
    }

    public String getNow() {
        return now;
    }
    public String getName(){
        return name;
    }
}

