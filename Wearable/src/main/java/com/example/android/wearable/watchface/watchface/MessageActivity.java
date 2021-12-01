package com.example.android.wearable.watchface.watchface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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
    public String[] user;
    final String[] answerlist={"안녕하세요","감사합니다","전화주세요","나중에 연락 드리겠습니다","사랑합니다"};
    String touser;
    String selectedanswer;
    public int sendcheck=1;
    ArrayList<Sender> receivers;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        receivers=((NewMainActivity)NewMainActivity.context).messageList;
        final String userID=((NewMainActivity)NewMainActivity.context).userId;
        int n = receivers.size();
        user = new String[n];
        int i = 0;
        for(Sender receiver : receivers){
            user[i++] = String.valueOf(receiver.getUser_name());
            System.out.println(receiver.getUser_id());
        }

        setContentView(R.layout.messagetmp);
        Button send =findViewById(R.id.bu);
        context=this;
        final MyMqttClient myMqttClient = new MyMqttClient();

        final Spinner name=(Spinner)findViewById(R.id.spinner);
        ArrayAdapter adapter=new ArrayAdapter(
                getApplicationContext(),R.layout.spinner,user);
        adapter.setDropDownViewResource(R.layout.spinner_down);
        name.setAdapter(adapter);

        final Spinner answer=(Spinner)findViewById(R.id.spinner2);
        ArrayAdapter adapter2=new ArrayAdapter(
                getApplicationContext(),R.layout.spinner,answerlist);
        adapter.setDropDownViewResource(R.layout.spinner_down);
        answer.setAdapter(adapter2);
        answer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedanswer = String.valueOf(answer.getItemAtPosition(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String userName = String.valueOf(name.getItemAtPosition(position));
                String uid = "";
                for(Sender receiver : receivers){
                    if(userName.equals(receiver.getUser_name())){
                        uid = String.valueOf(receiver.getUser_id());
                    }
                }
                touser= uid;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        final String[] args = {userID,touser};
        myMqttClient.main(args);
        send.setOnClickListener(new View.OnClickListener(){ // SCHEDULE
            @Override
            public void onClick(View view){
                if(presscheck==1){

                    presscheck=0;
            }
                if(sendcheck==1){
                sendcheck=0;
            }
            }

        });
    }
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

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
