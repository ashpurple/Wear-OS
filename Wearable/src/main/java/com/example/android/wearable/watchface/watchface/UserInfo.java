package com.example.android.wearable.watchface.watchface;

import java.util.ArrayList;

public class UserInfo {
    // User
    private String birthday;
    private String name;
    private String skin;
    private String protective;
    private String maxHeartRate;
    private String group;

    // TimerList
    private final ArrayList<TimerInfo> timerList;
    private String tmrSeq;
    private String bizId;
    private String schSeq;
    private String tmrNm;
    private String onOff;
    private String intervalSec;
    private String durationSec;
    private String timerMillis;
    private String loopCount;
    private String tmrGbn;
    private String memo;
    private String startDt;
    private String endDt;

    UserInfo(){
        // User
        this.name = "홍길동";
        this.group = "SB";
        this.birthday = "None";
        this.skin = "None";
        this.protective = "None";
        this.maxHeartRate = "None";
        timerList = new ArrayList<TimerInfo>();
    }
    /* Setter */
    public void setTimerList(TimerInfo timer){
        this.timerList.add(timer);
    }
    public void setBirthday(String birthday){
        this.birthday = birthday;
    }
    public void setName(String name){
        this.name = name.substring(5);
    }
    public void setSkin(String skin){
        this.skin = skin;
    }
    public void setProtective(String protective){
        this.protective = protective;
    }
    public void setMaxHeartRate(String maxHeartRate){
        this.maxHeartRate = maxHeartRate;
    }
    public void setGroup(String group){
        this.group = group;
    }

    /* Getter */
    // User Info List
    public ArrayList<TimerInfo> getTimerList(){
        return timerList;
    }
    public String getBirthday(){
        return birthday;
    }
    public String getName(){
        return name;
    }
    public String getSkin(){
        return skin;
    }
    public String getProtective(){
        return protective;
    }
    public String getMaxHeartRate(){
        return maxHeartRate;
    }
    public String getGroup(){
        return group;
    }
}
