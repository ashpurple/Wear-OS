package com.example.android.wearable.watchface.watchface;

public class Sender {

    private int user_id;
    private String user_name;
    private int group_code;
    private String group_name;

    public int getUser_id() {
        return user_id;
    }
    public String getUser_name() {
        return user_name;
    }
    public int getGroup_code() {
        return group_code;
    }
    public String getGroup_name(){
        return group_name;
    }

    public void setUser_id(int user_id){this.user_id = user_id;}
    public void setUser_name(String user_name){this.user_name = user_name;}
    public void setGroup_code(int group_code){this.group_code = group_code;}
    public void setGroup_name(String group_name){this.group_name = group_name;}

}
