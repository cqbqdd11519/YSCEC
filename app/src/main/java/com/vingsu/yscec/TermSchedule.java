package com.vingsu.yscec;

public class TermSchedule {

    private int start_month;
    private int end_month;
    private int start_date;
    private int end_date;
    private String title;

    public TermSchedule(int _start_month, int _end_month, int _start_data, int _end_date, String _title){
        this.start_month = _start_month;
        this.end_month = _end_month;
        this.start_date = _start_data;
        this.end_date = _end_date;
        this.title = _title;
    }
    public int getStartMonth(){
        return this.start_month;
    }
    public int getEndMonth(){
        return this.end_month;
    }
    public int getStartDate(){
        return this.start_date;
    }
    public int getEndDate(){
        return this.end_date;
    }
    public String getTitle(){
        return this.title;
    }

    public boolean equals(TermSchedule schedule){
        return ( this.start_month == schedule.start_month
                && this.end_month == schedule.end_month
                && this.start_date == schedule.start_date
                && this.end_date == schedule.end_date
                && this.title.equals(schedule.title) );
    }
}
