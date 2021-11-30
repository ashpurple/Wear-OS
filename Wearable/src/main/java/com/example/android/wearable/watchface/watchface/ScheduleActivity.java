package com.example.android.wearable.watchface.watchface;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.wearable.watchface.R;

import java.util.ArrayList;

public class ScheduleActivity extends AppCompatActivity {

    ScheduleAdapter scheduleAdapter;
    ListView scheduleListView;
    private ArrayList<Schedule> schedules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_page);
        scheduleListView = (ListView) findViewById(R.id.ScheduleListView);
        scheduleAdapter = new ScheduleAdapter(schedules, getLayoutInflater());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
