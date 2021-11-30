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
        System.out.println("Clear1\n");
        schedules = (ArrayList<Schedule>)getIntent().getSerializableExtra("schedule");
        System.out.println("Clear2\n");
        for(Schedule schedule : schedules){
            System.out.println(schedule.getContents());
        }
        scheduleAdapter = new ScheduleAdapter(schedules, getLayoutInflater());
        scheduleListView.setAdapter(scheduleAdapter);
        System.out.println("Clear3\n");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
