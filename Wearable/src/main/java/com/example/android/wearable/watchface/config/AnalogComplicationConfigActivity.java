/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.watchface.config;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.support.wearable.complications.ProviderChooserIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableRecyclerView;

import com.example.android.wearable.watchface.R;
import com.example.android.wearable.watchface.model.AnalogComplicationConfigData;
import com.example.android.wearable.watchface.watchface.AnalogComplicationWatchFaceService;

import java.util.ArrayList;

/**
 * The watch-side config activity for {@link AnalogComplicationWatchFaceService}, which
 * allows for setting the left and right complications of watch face along with the second's marker
 * color, background color, unread notifications toggle, and background complication image.
 */
public class AnalogComplicationConfigActivity extends Activity {

    private static final String TAG = AnalogComplicationConfigActivity.class.getSimpleName();
    static final String EXTRA_SHARED_PREF =
            "com.example.android.wearable.watchface.config.extra.EXTRA_SHARED_PREF";
    static final int COMPLICATION_CONFIG_REQUEST_CODE = 1001;
    static final int UPDATE_COLORS_CONFIG_REQUEST_CODE = 1002;
    public ComplicationProviderInfo left;
    public ComplicationProviderInfo right;
    public ComplicationProviderInfo complicationProviderInfo;
    private WearableRecyclerView mWearableRecyclerView;
    private AnalogComplicationConfigRecyclerViewAdapter mAdapter;
    public SharedPreferences.Editor editor;
    String sharedPrefString;
    private String mSharedPrefString = sharedPrefString;
    public String color;
    public String color2;
    public String view2;
    public int Rightbuttonchecking=0;
    public int Leftbuttonchecking=0;
    public static Context context;
    public String userinfo="";
    public Activity testactivity;
    Intent launchIntent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_analog_complication_config);

        mWearableRecyclerView =
                (WearableRecyclerView) findViewById(R.id.wearable_recycler_view);


        mAdapter = new AnalogComplicationConfigRecyclerViewAdapter(
                getApplicationContext(),
                AnalogComplicationConfigData.getWatchFaceServiceClass(),
                AnalogComplicationConfigData.getDataToPopulateAdapter(this));


        // Aligns the first and last items on the list vertically centered on the screen.
        mWearableRecyclerView.setEdgeItemsCenteringEnabled(true);

        mWearableRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Improves performance because we know changes in content do not change the layout size of
        // the RecyclerView.
        mWearableRecyclerView.setHasFixedSize(true);

        mWearableRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE
                && resultCode == RESULT_OK) {

            // Retrieves information for selected Complication provider.
            complicationProviderInfo =
                    data.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO);
            Log.d(TAG, "Provider: " + complicationProviderInfo);

            mAdapter.updateSelectedComplication(complicationProviderInfo);

            // Updates preview with new complication information for selected complication id.
            // Note: complication id is saved and tracked in the adapter class.

        } else if (requestCode == UPDATE_COLORS_CONFIG_REQUEST_CODE
                && resultCode == RESULT_OK) {
            Log.d("this is th spot for this","background change");
            if(data.getStringExtra("result")!=null){
                color=data.getStringExtra("result");
                Log.d("COLOR 1 :",color);
            }
            if(data.getStringExtra("result2")!=null){
                color2=data.getStringExtra("result2");
                Log.d("COLOR 2 :",color2);
            }

            // Updates highlight and background colors based on the user preference.
            mAdapter.updatePreviewColors();
        }
    }
}
/**
public class ComplicationButton extends RecyclerView.ViewHolder implements View.OnClickListener{
    Button mAppearanceButton;
    public ComplicationButton(View view) {
        super(view);
        mAppearanceButton = (Button) view.findViewById(R.id.a);
        Log.d("AnalogComplicationConfigRecyclerViewAdapter","ColorPickerViewHolder");
        view.setOnClickListener(this);
    }
}**/