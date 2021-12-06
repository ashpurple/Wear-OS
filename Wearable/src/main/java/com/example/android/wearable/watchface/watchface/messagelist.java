package com.example.android.wearable.watchface.watchface;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.android.wearable.watchface.R;

import java.util.ArrayList;

public class messagelist extends Activity {

    @SuppressLint("ResourceType")
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.id.messagelist);
        TextView t=findViewById(R.id.messagelist);
        ArrayList b=((MessageActivity)MessageActivity.context).messageList;
        t.setText(String.valueOf(b));
    }

}

