package com.example.android.wearable.watchface.watchface;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.android.wearable.watchface.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

public class MyActivity extends Activity implements View.OnClickListener {
    /**
     * Wear-Keyboard, Activity-Based Keyboard for Android Wear.
     * Built by Ido Ideas, 2014.
     * This code is Open Source and free to use.
     */
    private static final String TAG = "MyActivity";
    public String datapath = "/data_path";

    private TextView mTextView;
    static EditText editText;
    static Button del,space, num, cap, OK;
    RelativeLayout Scroll;
    static String letters = "ㄱㄴㄷㄹㅁㅂㅅㅇㅈㅊㅋㅍㅌㅎ";
    static String capitalletters = "ㄲㄴㄸㄹㅁㅃㅆㅇㅉㅊㅋㅍㅌㅎ";
    static String letters2="ㅛㅕㅑㅐㅔㅗㅓㅏㅣㅠㅜㅡ";
    static String numbers = "1234567890";
    ArrayList<Button> buttons = new ArrayList<Button>();
    private boolean Capital = false;
    static String TextInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rect_activity_my);
        Scroll = (RelativeLayout) findViewById(R.id.scroll);
        editText = (EditText) findViewById(R.id.textedit);

        Button Simple= (Button) findViewById(R.id.simple);
        Simple.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent=new Intent(getApplicationContext(),SimpleList.class);
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }
        });
        OK = (Button) findViewById(R.id.send);
        OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextInput = editText.getText().toString();
                sendData(TextInput);
                finish();
            }
        });
setKeyboardCharacters(letters);
del = (Button) findViewById(R.id.backspace);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().length() > 0) {
                    editText.setText(editText.getText().toString().substring(0, editText.getText().length() - 1));
                }
            }
        });
        space = (Button) findViewById(R.id.space);
        space.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(editText.getText()+" ");
            }
        });
        num = (Button) findViewById(R.id.numbers);
        num.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Scroll.removeAllViews();
                if (num.getText().toString().equals("123")){
                num.setText("Eng");
                setKeyboardCharacters(numbers);}
                else{
                    num.setText("123");
                    setKeyboardCharacters(letters);
                }
            }
        });
        cap = (Button) findViewById(R.id.capital);
        cap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (num.getText().toString().equals("123")){
                    Scroll.removeAllViews();
                    if (!Capital){
                        Capital = true;
                    setKeyboardCharacters(capitalletters); }
                    else{
                        Capital = false;
                        setKeyboardCharacters(letters);
                    }
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        Vibrator vi = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
        vi.vibrate(50);
        Button button = (Button) v;
        editText.setText(editText.getText()+""+button.getText());
    }
    private void sendData(String message) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(datapath);
        dataMap.getDataMap().putString("message", message);
        Log.d("WEARABLE STUFF",dataMap.getDataMap().getString("message"));
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();

        Task<DataItem> dataItemTask = Wearable.getDataClient(getApplicationContext()).putDataItem(request);
        dataItemTask
                .addOnSuccessListener(new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Log.d(TAG, "Sending message was successful: " + dataItem);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Sending message failed: " + e);
                    }
                })
        ;
    }

    public void setKeyboardCharacters(String Characters){
    for (int i = 0; i<Characters.length();i++){
        Button b = new Button(getApplicationContext());
        b.setId(i);
        buttons.add(b);
        Button currentButton = buttons.get(i);
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) px,(int) px);
        if (i>0){
            if (i%4!=0) {
                if (i<4){
                    lp.addRule(RelativeLayout.BELOW,R.id.textedit);}
                else{
                    lp.addRule(RelativeLayout.BELOW,buttons.get(i-4).getId());
                }
                if (i==1){
                    lp.setMargins((int)  px,lp.topMargin,lp.rightMargin,lp.bottomMargin);}
                else{
                    lp.addRule(RelativeLayout.RIGHT_OF,buttons.get(i-1).getId());}
            }
            else{
                lp.addRule(RelativeLayout.BELOW,buttons.get(i-3).getId());
            }
        }
        else{
            lp.addRule(RelativeLayout.BELOW,R.id.textedit);
        }
        currentButton.setLayoutParams(lp);
        currentButton.setText(Characters.charAt(i)+"");
        currentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vi = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
                vi.vibrate(50);
                Button button = (Button) v;
                editText.setText(editText.getText()+""+button.getText());

            }
        });
            Scroll.addView(currentButton);
    }
}
}
