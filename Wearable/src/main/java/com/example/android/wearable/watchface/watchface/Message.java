package com.example.android.wearable.watchface.watchface;

public class Message {

    private int user_id;
    private String user_name;
    private int group_code;
    private String group_name;

    public Message(int user_id, String user_name, int group_code, String group_name) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.group_code = group_code;
        this.group_name= group_name;
    }

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

}
