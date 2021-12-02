package com.example.android.wearable.watchface.watchface;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

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
    public ArrayList messagelist;
    public int messagelistnum=0;
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
    Messenger mServiceMessenger;
    public int presscheck=1;
    public String[] user;
    final String[] answerlist={"안녕하세요","감사합니다","전화주세요","나중에 연락 드리겠습니다","사랑합니다"};
    public String touser;
    String selectedanswer;
    public int sendcheck=1;
    ArrayList<Sender> receivers;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        receivers=((NewMainActivity)NewMainActivity.context).messageList;
        final String userID=((NewMainActivity)NewMainActivity.context).userId;
        int n = receivers.size();
        user = new String[n];
        final Intent intent = new Intent(MessageActivity.this, MyMqttClient.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        int i = 0;
        for(Sender receiver : receivers){
            user[i++] = String.valueOf(receiver.getUser_name());
            System.out.println(receiver.getUser_id());
        }

        setContentView(R.layout.messagetmp);
        Button send =findViewById(R.id.bu);
        context=this;
        final MyMqttClient myMqttClient = new MyMqttClient();
        final MyMqttClient myMqttClient2 = new MyMqttClient(getApplicationContext());

        Button see=findViewById(R.id.list);
        see.setOnClickListener(new View.OnClickListener(){ // SCHEDULE
            @Override
            public void onClick(View view){
                Intent intent=new Intent(getApplicationContext(),messagelist.class);
                startActivity(intent);


                }});
        final Spinner name=(Spinner)findViewById(R.id.spinner);
        ArrayAdapter adapter=new ArrayAdapter(
                getApplicationContext(),R.layout.spinner,user);
        adapter.setDropDownViewResource(R.layout.spinner_down);
        name.setAdapter(adapter);
//
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
      //  String[] tmp={userID,"0000"};
      //  myMqttClient.main(tmp);
        send.setOnClickListener(new View.OnClickListener(){ // SCHEDULE
            @Override
            public void onClick(View view){
                if(presscheck==1){
                    final String[] args = {userID,touser};
                    myMqttClient.main(args);
                    presscheck=0;
                }
                if(sendcheck==1){
                    sendcheck=0;
                }
            }

        });
    }
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.getData().getString("messages") != null) {
                messagelist.add(msg.getData().getString("messages"));
                Log.e("RRRRRRRR",String.valueOf(messagelist));
            }
            return false;


        }

    }));
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mServiceMessenger = new Messenger(iBinder);
            try {
                Message msg = Message.obtain(null, BackService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mServiceMessenger.send(msg);
            }
            catch (RemoteException e) {
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
        }

