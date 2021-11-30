package com.example.android.wearable.watchface.watchface;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.android.wearable.watchface.R;

import java.util.ArrayList;
import java.util.Vector;

public class ScheduleAdapter extends BaseAdapter {
    // private Vector<Beacon> beacons; Schedule 받아오는 객체
    private LayoutInflater layoutInflater;
    private ArrayList<Schedule> schedules;

    public ScheduleAdapter(ArrayList<Schedule> schedules, LayoutInflater layoutInflater) {
        this.schedules = schedules;
        this.layoutInflater = layoutInflater;
    }
    private Schedule schedule;

    @Override
    public int getCount() {
        return schedules.size();
    }

    @Override
    public Object getItem(int position) {
        return schedules.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ScheduleAdapter.ScheduleHolder scheduleHolder;
        if (convertView == null) {
            scheduleHolder = new ScheduleHolder();
            convertView = layoutInflater.inflate(R.layout.item_schedule, parent, false);
            scheduleHolder.company_name= convertView.findViewById(R.id.company_name);
            scheduleHolder.biz_name = convertView.findViewById(R.id.biz_name);
            scheduleHolder.title = convertView.findViewById(R.id.title);
            scheduleHolder.location=convertView.findViewById(R.id.schedule_location);
            scheduleHolder.contents= convertView.findViewById(R.id.contents);
            scheduleHolder.start_date = convertView.findViewById(R.id.start_date);
            scheduleHolder.end_date = convertView.findViewById(R.id.end_date);
            scheduleHolder.start_time=convertView.findViewById(R.id.start_time);
            scheduleHolder.end_time=convertView.findViewById(R.id.end_time);
            convertView.setTag(scheduleHolder);
        } else {
            scheduleHolder = (ScheduleAdapter.ScheduleHolder)convertView.getTag();
        }
        if(scheduleHolder.company_name!=null) {
            scheduleHolder.company_name.setText("회사 이름 : " + schedules.get(position).getCompany_name());
            scheduleHolder.biz_name.setText("업무 이름 : " + schedules.get(position).getBiz_name());
            scheduleHolder.title.setText("제목 :" + schedules.get(position).getTitle());
            scheduleHolder.location.setText("위치 : " + schedules.get(position).getLocation() + "dBm");
            scheduleHolder.contents.setText("내용 : " + schedules.get(position).getContents());
            scheduleHolder.start_date.setText("시작 일자 : " + schedules.get(position).getStart_date());
            scheduleHolder.end_date.setText("종료 일자 : " + schedules.get(position).getEnd_date());
            scheduleHolder.start_time.setText("시작 시간 : " + schedules.get(position).getStart_time());
            scheduleHolder.end_time.setText("종료 시간 : " + schedules.get(position).getEnd_time());
        }
        return convertView;
    }

    private static class ScheduleHolder {
        TextView company_name;
        TextView biz_name;
        TextView title;
        TextView location;
        TextView contents;
        TextView start_date;
        TextView end_date;
        TextView start_time;
        TextView end_time;
    }
}
