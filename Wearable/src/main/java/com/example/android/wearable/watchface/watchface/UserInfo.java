package com.example.android.wearable.watchface.watchface;

public class UserInfo {
    private String birthday;
    private String name;
    private String skin;
    private String protective;
    private String[] timerList;
    private String maxHeartRate;
    private String group;

    UserInfo(){
        this.name = "홍길동";
        this.group = "가천대";
    }

    public void setBirthday(String birthday){
        this.birthday = birthday;
    }
    public void setName(String name){
        this.name = name;
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
