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
import android.os.Parcelable;
import android.support.wearable.view.CircledImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.android.wearable.watchface.R;

import java.util.ArrayList;

/**
 * Provides a binding from color selection data set to views that are displayed within
 * {@link ColorSelectionActivity}.
 * Color options change appearance for the item specified on the watch face. Value is saved to a
 * {@link SharedPreferences} value passed to the class.
 */

public class ColorSelectionRecyclerViewAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = ColorSelectionRecyclerViewAdapter.class.getSimpleName();
    public SharedPreferences left;
    public SharedPreferences right;
    public int leftthing=0;
    public int rightthing=0;
    private ArrayList<Integer> mColorOptionsDataSet;
    public Context context;
    private String mSharedPrefString;
    public SharedPreferences.Editor editor;
    public ColorSelectionRecyclerViewAdapter(
            String sharedPrefString,
            ArrayList<Integer> colorSettingsDataSet) {
        mSharedPrefString = sharedPrefString;
        mColorOptionsDataSet = colorSettingsDataSet;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder(): viewType: " + viewType);

        RecyclerView.ViewHolder viewHolder =
                new ColorViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.color_config_list_item, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Log.d(TAG, "Element " + position + " set.");

        Integer color = mColorOptionsDataSet.get(position);
        ColorViewHolder colorViewHolder = (ColorViewHolder) viewHolder;
        colorViewHolder.setColor(color);
    }

    @Override
    public int getItemCount() {
        return mColorOptionsDataSet.size();
    }

    /**
     * Displays color options for an item on the watch face and saves value to the
     * SharedPreference associated with it.
     */
    public class ColorViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private CircledImageView mColorCircleImageView;

        public ColorViewHolder(final View view) {
            super(view);
            mColorCircleImageView = (CircledImageView) view.findViewById(R.id.color);
            view.setOnClickListener(this);
        }

        public void setColor(int color) {
            mColorCircleImageView.setCircleColor(color);
        }

        @Override
        public void onClick (View view) {
            int position = getAdapterPosition();
            Integer color = mColorOptionsDataSet.get(position);

            Log.d(TAG, "Color: " + color + " onClick() position: " + position);
            Log.d("view", String.valueOf(view));
            Activity activity = (Activity) view.getContext();

            if (mSharedPrefString != null && !mSharedPrefString.isEmpty()) {
                SharedPreferences sharedPref = activity.getSharedPreferences(
                        activity.getString(R.string.analog_complication_preference_file_key),
                        Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPref.edit();

                editor.putInt(mSharedPrefString, color);
                //여기가 그 sharedmesage 그러니깐 전달받은 메세지가 어디에 사용되는건지 묻는거거든? 이거를 옮겨서 그거에 따라서 바꿔주면됄듯 한ㄷ데
                Log.d("this is shared message",mSharedPrefString);
                //editor.commit();
                Log.d("color change determine","color change determine");
                // Let's Complication Config Activity know there was an update to colors.
                Intent intent=new Intent();
                if (mSharedPrefString.compareTo("saved_markers_color")==0) {
                    intent.putExtra("result2",String.valueOf(color));

                    Log.d("ColorSElectionRecyclerViewAdapter : ","Marker received");
                }
                if (mSharedPrefString.compareTo("saved_background_color")==0) {
                    intent.putExtra("result",String.valueOf(color));
                    Log.d("ColorSElectionRecyclerViewAdapter : ","Background received");
                }
                Log.d("color change determine",String.valueOf(color));

                activity.setResult(Activity.RESULT_OK,intent);
            }
            activity.finish();
        }
    }
}