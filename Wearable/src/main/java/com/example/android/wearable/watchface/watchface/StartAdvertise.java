package com.example.android.wearable.watchface.watchface;

import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import com.example.android.wearable.watchface.R;

public class StartAdvertise extends AppCompatActivity {
    /** BLE Declaration **/
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    /** layout Variables Declaration **/
    private EditText mEdit;
    private Button Start_Adv;
    private Button Stop_Adv;
    public static Context context;
    private boolean BLE_status = FALSE;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mBluetoothLeAdvertiser == null) return;
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        Log.i("STOP", "LE Advertise Stopped.");
        BLE_status = FALSE;
        Toast.makeText(getApplicationContext(),"Restart advertising with new UserID..",Toast.LENGTH_SHORT).show();
        ((NewMainActivity)NewMainActivity.context).Broadcastingcheck=0;
   
        finish();
    }
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
      
            Log.i("StartAdvertise", "LE Advertise Started.");
            BLE_status = TRUE;
        }
        @Override
        public void onStartFailure(int errorCode) {
            Log.w("False advertise", "LE Advertise Failed: " + errorCode);
            BLE_status = FALSE;
        }
    };
}
