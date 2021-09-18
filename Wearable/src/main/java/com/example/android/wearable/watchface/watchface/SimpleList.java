package com.example.android.wearable.watchface.watchface;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

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

public class SimpleList extends Activity {
    private static final String TAG = "SimpleList";
    public String datapath = "/data_path";
    RelativeLayout Scroll;
    ArrayList<Button> buttons = new ArrayList<Button>();
    String[] sample={"나중에 답장드리겠습니다.","알겠습니다.","나중에 답장드리겠습니다.","알겠습니다.","나중에 답장드리겠습니다.","알겠습니다."};

    //String sample="나중에 답장드리겠습니다.:알겠습니다.:알겠다.";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_answer_list);
        Scroll = (RelativeLayout) findViewById(R.id.scroll);

        setKeyboardCharacters(sample);

    }

        public void setKeyboardCharacters(String[] Characters){
        for (int i = 0; i<6;i++){
            Button b = new Button(getApplicationContext());
            b.setId(i);
            buttons.add(b);
            Button currentButton = buttons.get(i);
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, r.getDisplayMetrics());
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) px,(int) 100);
            if (i>0){
                lp.addRule(RelativeLayout.BELOW,buttons.get(i-1).getId());
                /**
                if (i%4!=0) {
                    if (i<4){
                        lp.addRule(RelativeLayout.BELOW, R.id.textedit);}
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
            }**/}
            else{
                lp.addRule(RelativeLayout.BELOW,R.id.textedit);
            }
            currentButton.setLayoutParams(lp);
            currentButton.setText(Characters[i]);
            currentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Vibrator vi = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
                    vi.vibrate(50);
                    Button button = (Button) v;
                    sendData(String.valueOf(button.getText()));
                    finish();
                }
            });
            Scroll.addView(currentButton);
        }
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
}
