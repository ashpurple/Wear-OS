package com.example.android.wearable.watchface.watchface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.android.wearable.watchface.R;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import java.util.ArrayList;

public class MessageActivity extends Activity {
    MqttClient myClient;
    MqttConnectOptions connOpt;
    static final int MAX_QUEUE_LEN = 10;
    static String BROKER_URL = "tcp://15.164.45.229:1883";
    //	static final String SBSYS_USERNAME = "";
//	static final String SBSYS_PASSWORD = "";
    int msgCount;
    String from_id;
    static String to_id;
    // send to other users
    String s_topic;
    // receive from other users
    String s_topic2;
    static String msg;
    Boolean subscriber;
    static ArrayList<String> p_topics;
    public static Context context;
    MqttTopic topic;
    public int presscheck=1;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messagetmp);
        Button send =findViewById(R.id.bu);
        context=this;
        MyMqttClient myMqttClient = new MyMqttClient();
        String[] args = {"2103","2106"};
        myMqttClient.main(args);

        send.setOnClickListener(new View.OnClickListener(){ // SCHEDULE
            @Override
            public void onClick(View view){
                if(presscheck==1)
                    presscheck=0;
            }

        });
    }
    public void myPublish(String p_topic, String pubMsg) {
        topic = myClient.getTopic(p_topic);
        int pubQoS = 0;
        MqttMessage message = new MqttMessage(pubMsg.getBytes());
        message.setQos(pubQoS);
        message.setRetained(false);

        // Publish the message
        System.out.println("Publishing to topic \"" + p_topic + "\" qos " + pubQoS);
        MqttDeliveryToken token = null;
        try {
            token = topic.publish(message);
        } catch (Exception e) {
            System.out.println("Error in onPublish()!");
            e.printStackTrace();
        }
    }
}
