package com.example.android.wearable.watchface.watchface;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.android.wearable.watchface.watchface.SharedPreference.SERVICE_HANDLER;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends WearableActivity {
    private static final int REQUEST_RECORD_PERMISSION = 100;
    private final static String TAG = "MAIN";

    private final static String[] permissions = new String[]
            {Manifest.permission.BODY_SENSORS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "MAIN Create");

        // If we already have all the permissions start immediately, otherwise request permissions
        if (permissionsGranted()) {
            Log.e(TAG, "All Permissions OK");
            //init();
        } else {
            Log.e(TAG, "Permission needed upadate");
            Log.d(TAG, "Requesting permissions");
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_PERMISSION);
        }
        finish();
    }

    /**
     * Checks if all necessary permissions have been granted
     *
     * @return True if all necessary permissions have been granted, false otherwise
     */
    private boolean permissionsGranted() {
        Boolean result = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                result = false;
            }
        }
        return result;
    }

    public void init(){
        try {
            Log.d(TAG,"INIT");
            SharedPreference.setPreference(getApplicationContext(),SERVICE_HANDLER,"y");
            BackService.start_handler = true;
            startService(new Intent(MainActivity.this, BackService.class));
        }catch (Exception e){
            Log.d(TAG,"INIT Fail");
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast
                            .LENGTH_SHORT).show();
                    finish();
                }
        }
    }
}