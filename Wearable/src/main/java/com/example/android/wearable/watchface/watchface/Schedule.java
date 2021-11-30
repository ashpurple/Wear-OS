package com.example.android.wearable.watchface.watchface;

public class Schedule {

    private final String company_name;
    private final String biz_name;
    private final String title;
    private final String location;
    private final String contents;
    private final String start_date;
    private final String end_date;
    private final String start_time;
    private final String end_time;

    public Schedule(String company_name, String biz_name, String title, String location, String contents,
                    String start_date, String end_date, String start_time, String end_time) {

        this.company_name = company_name;
        this.biz_name = biz_name;
        this.title = title;
        this.location = location;
        this.contents = contents;
        this.start_date = start_date;
        this.end_date = end_date;
        this.start_time = start_time;
        this.end_time = end_time;

    }

    public String getCompany_name() {
        return company_name;
    }
    public String getBiz_name() {
        return biz_name;
    }
    public String getTitle() {
        return title;
    }
    public String getLocation() {
        return location;
    }
    public String getContents() {
        return contents;
    }
    public String getStart_date() {
        return start_date;
    }
    public String getEnd_date() {
        return end_date;
    }
    public String getStart_time() {
        return start_time;
    }
    public String getEnd_time() {
        return end_time;
    }



}
