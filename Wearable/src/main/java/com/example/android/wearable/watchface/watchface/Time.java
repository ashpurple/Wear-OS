package com.example.android.wearable.watchface.watchface;

import java.util.Calendar;

public class Time {
    private int year;
    private int month;
    private int day;
    private int week;
    private int hour;
    private int minute;
    private int second;
    private int amPm;

    Time(){
        year = 0;
        month= 0;
        day = 0;
        week = 0;
        hour = 0;
        minute = 0;
        second = 0;
        amPm = 0;
    }

    public void setCalendar(){
        Calendar now = Calendar.getInstance();
        year = now.get(Calendar.YEAR); // 년
        month = now.get(Calendar.MONTH); // 월
        day = now.get(Calendar.DAY_OF_MONTH); // 일
        week = now.get(Calendar.DAY_OF_WEEK); // 요일
        hour = now.get(Calendar.HOUR); // 시
        minute = now.get(Calendar.MINUTE); // 분
        second = now.get(Calendar.SECOND); // 초
        amPm = now.get(Calendar.AM_PM); // 오전 오후
    }

    public String transformHour(){
        String newTime = String.valueOf(hour);
        if(hour < 10)
            newTime = "0"+newTime;
        return newTime;
    }
    public String transformMin(){
        String newTime = String.valueOf(minute);
        if(minute < 10)
            newTime = "0"+newTime;
        return newTime;
    }

    public String transformWeek(){
        String newWeek = "default";
        switch(week){
            case 1:
                newWeek = "Sun";
                break;
            case 2:
                newWeek = "Mon";
                break;
            case 3:
                newWeek = "Tue";
                break;
            case 4:
                newWeek = "Wed";
                break;
            case 5:
                newWeek = "Thu";
                break;
            case 6:
                newWeek = "Fri";
                break;
            case 7:
                newWeek = "Sat";
                break;
        }
        return newWeek;
    }

    public String transformMonth(){
        String newMonth = "default";
        switch(month){
            case 0:
                newMonth = "Jan";
                break;
            case 1:
                newMonth = "Feb";
                break;
            case 2:
                newMonth = "Mar";
                break;
            case 3:
                newMonth = "Apr";
                break;
            case 4:
                newMonth = "May";
                break;
            case 5:
                newMonth = "Jun";
                break;
            case 6:
                newMonth = "Jul";
                break;
            case 7:
                newMonth = "Aug";
                break;
            case 8:
                newMonth = "Sep";
                break;
            case 9:
                newMonth = "Oct";
                break;
            case 10:
                newMonth = "Nov";
                break;
            case 11:
                newMonth = "Dec";
                break;
        }
        return newMonth;
    }

    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public int getMonth() {
        return month;
    }
    public void setMonth(int month) {
        this.month = month;
    }
    public int getDay() {
        return day;
    }
    public void setDay(int day) {
        this.day = day;
    }
    public int getWeek() {
        return week;
    }
    public void setWeek(int week) {
        this.week = week;
    }
    public int getHour() {
        return hour;
    }
    public void setHour(int hour) {
        this.hour = hour;
    }
    public int getMinute() {
        return minute;
    }
    public void setMinute(int minute) {
        this.minute = minute;
    }
    public int getSecond() {
        return second;
    }
    public void setSecond(int second) {
        this.second = second;
    }
    public int getAmPm() {
        return amPm;
    }
    public void setAmPm(int amPm) {
        this.amPm = amPm;
    }
}
