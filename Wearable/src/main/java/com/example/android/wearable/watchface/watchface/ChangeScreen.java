package com.example.android.wearable.watchface.watchface;

import android.app.Activity;
import android.os.Bundle;

import com.example.android.wearable.watchface.R;

public class ChangeScreen extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_screen);
        finishActivity(0);

    }
}
