package com.example.android.wearable.watchface.watchface;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.example.android.wearable.watchface.R;

public class DisplayInfo extends Activity {
    private static final String TAG = "DisplayInfoActivity";
    public String datapath = "/data_path";
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_info);

    }
}