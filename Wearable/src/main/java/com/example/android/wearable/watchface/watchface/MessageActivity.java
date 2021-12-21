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

import java.util.ArrayList;

public class MessageActivity extends Activity {

    public ArrayList<String> messageList;

    public static Context context;
    public boolean isPressed = true;
    public String[] user;
    final String[] answerList = {"안녕하세요","알겠습니다","다시 연락주세요","감사합니다","연락드리겠습니다"};
    public String toUser;
    String selectedAnswer;
    public boolean sendFlag = false;
    ArrayList<Sender> receivers;
    public boolean messageFlag = false;
    String touser="";


    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        context=this;

        /* Set Receiver List */
        receivers=((NewMainActivity)NewMainActivity.context).messageList;
        final String userID=((NewMainActivity)NewMainActivity.context).userId;
        ((NewMainActivity)NewMainActivity.context).endflag=false;
        int n = receivers.size();
        user = new String[n];

        int i = 0;
        for(Sender receiver : receivers){ // store receiver list
            user[i++] = String.valueOf(receiver.getUser_name());
        }

        setContentView(R.layout.messagetmp);

        final MyMqttClient myMqttClient = new MyMqttClient(this);
        final String[] args = {userID};
        myMqttClient.main(args);

        /* User Spinner */
        final Spinner name=(Spinner)findViewById(R.id.name_spinner);
        ArrayAdapter nameAdapter=new ArrayAdapter(
                getApplicationContext(),R.layout.spinner,user);
        nameAdapter.setDropDownViewResource(R.layout.spinner_down);
        name.setAdapter(nameAdapter);

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
                toUser = uid;
                touser=toUser;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                String userName = String.valueOf(name.getItemAtPosition(0));
                String uid = "";
                for(Sender receiver : receivers){
                    if(userName.equals(receiver.getUser_name())){
                        uid = String.valueOf(receiver.getUser_id());
                    }
                }
                toUser = uid;
                touser=toUser;
            }
        });

        /* Answer Spinner */
        final Spinner answer=(Spinner)findViewById(R.id.answer_spinner);
        ArrayAdapter answerAdapter=new ArrayAdapter( // answer spinner
                getApplicationContext(),R.layout.spinner, answerList);
        answerAdapter.setDropDownViewResource(R.layout.spinner_down);
        //nameAdapter.setDropDownViewResource(R.layout.spinner_down);
        answer.setAdapter(answerAdapter);
        answer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAnswer = String.valueOf(answer.getItemAtPosition(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedAnswer = String.valueOf(answer.getItemAtPosition(0));
            }
        });

        /* Buttons */
        Button send =findViewById(R.id.send_btn);
        send.setOnClickListener(new View.OnClickListener(){ // send button
            @Override
            public void onClick(View view){
                messageFlag = true;
                if(isPressed){
                    //final String[] args = {userID, toUser};
                    touser=toUser;
                    isPressed = false;
                }
                if(!sendFlag){
                    sendFlag = true;
                }
            }
        });
    }

    public void receiveMessage(String senderId, String msg){
        String senderName = "";
        final String myMsg;
        for(Sender receiver : receivers){ // set sender name
            if(Integer.parseInt(senderId) == receiver.getUser_id()){
                senderName = String.valueOf(receiver.getUser_name());
            }
        }

        if(msg.equals("OK")){
            myMsg = senderName+"님이 메시지를 수신하였습니다";
        } else{
            myMsg = senderName+": "+msg;
        }
        MessageActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, myMsg, Toast.LENGTH_SHORT ).show();
            }
        });
    }

    public void sendMessage(){
        final String myMsg;
        if(messageFlag) {
            myMsg = "메시지 전송 완료";

            MessageActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, myMsg, Toast.LENGTH_SHORT).show();
                }
            });
            messageFlag = false;
        }
    }
}

