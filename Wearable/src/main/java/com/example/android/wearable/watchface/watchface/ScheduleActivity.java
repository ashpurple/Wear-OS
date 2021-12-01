package com.example.android.wearable.watchface.watchface;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.wearable.watchface.R;

import java.util.ArrayList;

public class ScheduleActivity extends AppCompatActivity {

    ScheduleAdapter scheduleAdapter;
    ListView scheduleListView;
    ArrayList<Schedule> schedules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_page);
        scheduleListView = (ListView) findViewById(R.id.ScheduleListView);
        schedules = (ArrayList<Schedule>)getIntent().getSerializableExtra("schedule");
        for(Schedule schedule : schedules){
            System.out.println(schedule.getContents());
        }
        scheduleAdapter = new ScheduleAdapter(schedules, getLayoutInflater());
        scheduleListView.setAdapter(scheduleAdapter);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
