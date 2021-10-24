package com.example.android.wearable.watchface.watchface;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.android.wearable.watchface.R;
import org.json.JSONException;

public class DisplayInfo extends Activity {
    private static final String TAG = "DisplayInfoActivity";
    public static Context context;
    public String json;
    TextView textView;
    UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_info);

        textView = (TextView) findViewById(R.id.text_userInfo);
        Button load_button = (Button) findViewById(R.id.btn_load);
        load_button.setOnClickListener(mClickListener);

        Intent intent = getIntent();
        json = intent.getStringExtra("json");

        JsonParser jsonParser = new JsonParser();

        userInfo = new UserInfo();
        try {
            userInfo = jsonParser.getUserInfo(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
    Button.OnClickListener mClickListener = new View.OnClickListener(){
        public void onClick(View v){
            String name = userInfo.getName();
            String group = userInfo.getGroup();
            String birthday = userInfo.getBirthday();
            String skin = userInfo.getSkin();
            String protective = userInfo.getProtective();
            String maxHeartRate = userInfo.getMaxHeartRate();

            String user_string = "\nName: "+name+"\nGroup: "+group+"\nBirthDay: "+birthday+"\nSkin: "+skin+"\nProtective: "+protective+"\nMaxHeartRate: "+maxHeartRate;

            textView.setText(json + "\n\n<Parsing Data>" + user_string);
        }
    };


}