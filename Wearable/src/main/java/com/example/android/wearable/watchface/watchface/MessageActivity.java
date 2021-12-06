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
    Messenger mServiceMessenger;
    public boolean isPressed = true;
    public String[] user;
    final String[] answerList = {"안녕하세요","감사합니다","전화주세요","나중에 연락 드리겠습니다","사랑합니다"};
    public String toUser;
    String selectedAnswer;
    public boolean sendFlag = true;
    ArrayList<Sender> receivers;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        context=this;

        /* Set Receiver List */
        receivers=((NewMainActivity)NewMainActivity.context).messageList;
        final String userID=((NewMainActivity)NewMainActivity.context).userId;
        int n = receivers.size();
        user = new String[n];
        final Intent intent = new Intent(MessageActivity.this, MyMqttClient.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE); // ?

        int i = 0;
        for(Sender receiver : receivers){ // store receiver list
            user[i++] = String.valueOf(receiver.getUser_name());
        }

        setContentView(R.layout.messagetmp);

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
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

            }
        });

        /* Buttons */
        Button send =findViewById(R.id.send_btn);
        send.setOnClickListener(new View.OnClickListener(){ // send button
            @Override
            public void onClick(View view){
                if(isPressed){
                    final String[] args = {userID, toUser};
                    MyMqttClient.main(args);
                    isPressed = false;
                }
                if(sendFlag){
                    sendFlag = false;
                }
            }
        });
        Button view=findViewById(R.id.view_btn);
        view.setOnClickListener(new View.OnClickListener(){ // SCHEDULE
            @Override
            public void onClick(View view){
                Intent intent=new Intent(getApplicationContext(),messagelist.class);
                startActivity(intent);
            }});

    }

    public void displayMessage(final String msg){
        MessageActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT ).show();
            }
        });
    }

    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.getData().getString("messages") != null) {
                messageList.add(msg.getData().getString("messages"));
                Log.e("RRRRRRRR",String.valueOf(messageList));
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

