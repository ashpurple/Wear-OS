package com.example.android.wearable.watchface.watchface;

import android.content.Context;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ramij on 9/20/18.
 */

public class SharedPreference {
    private static final String PREF_NAME = "ANDROID_WEAR";

    public static final String SERVICE_HANDLER = "service_handler";

    public static void setPreference(Context context, String key, String value){
        try {
            android.content.SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
            editor.putString(key, value);
            editor.apply();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getPreference(Context context,String key,String default_value){
        String return_value = "";
        try {
            android.content.SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            return_value = prefs.getString(key, default_value);
        }catch (Exception e){
            e.printStackTrace();
        }
        return return_value;
    }
}
