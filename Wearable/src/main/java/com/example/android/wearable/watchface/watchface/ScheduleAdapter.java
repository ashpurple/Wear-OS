package com.example.android.wearable.watchface.watchface;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.android.wearable.watchface.R;

import java.util.Vector;

public class ScheduleAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private Schedule schedule;

    public ScheduleAdapter(Schedule schedule, LayoutInflater layoutInflater) {
        this.schedule = schedule;
        this.layoutInflater = layoutInflater;
    }

    @Override
    public int getCount() {
        return schedule.size();
    }

    @Override
    public Object getItem(int position) {
        return schedule.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ScheduleAdapter.ScheduleHolder scheduleHolder;
        if (convertView == null) {
            scheduleHolder = new ScheduleAdapter.ScheduleHolder();
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
            scheduleHolder.company_name.setText("회사 이름 : " + schedule.get(position).getCompany_name());
            scheduleHolder.biz_name.setText("업무 이름 : " + schedule.get(position).getBiz_name());
            scheduleHolder.title.setText("제목 :" + schedule.get(position).getTitle());
            scheduleHolder.location.setText("위치 : " + schedule.get(position).getLocation() + "dBm");
            scheduleHolder.contents.setText("내용 : " + schedule.get(position).getContents());
            scheduleHolder.start_date.setText("시작 일자 : " + schedule.get(position).getStart_date());
            scheduleHolder.end_date.setText("종료 일자 : " + schedule.get(position).getEnd_date());
            scheduleHolder.start_time.setText("시작 시간 : " + schedule.get(position).getStart_time());
            scheduleHolder.end_time.setText("종료 시간 : " + schedule.get(position).getEnd_time());
        }
        return convertView;
    }

    private class ScheduleHolder {
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
