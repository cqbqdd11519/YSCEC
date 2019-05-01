package com.vingsu.yscec;

import android.util.SparseArray;

import java.util.ArrayList;

public class TermCalendar {

    private int year = -1;
    private int term = -1;

    ArrayList<Integer> months = new ArrayList<>();

    private SparseArray<ArrayList<TermSchedule>> data_all;

    public TermCalendar(int year,int term){
        this.year = year;
        this.term = term;
        this.data_all = new SparseArray<>();
    }

    public int getYear(){
        return this.year;
    }

    public int getTerm(){
        return this.term;
    }

    public ArrayList<Integer> getMonths(){
        return this.months;
    }

    public void addSchedule(int start_month, int end_month, int start_date, int end_date, String title){
        if(! months.contains(start_month))
            months.add(start_month);
        if(! months.contains(end_month))
            months.add(end_month);

        if(data_all.get(start_month) == null)
            data_all.put(start_month,new ArrayList<TermSchedule>());
        if(data_all.get(end_month) == null)
            data_all.put(end_month,new ArrayList<TermSchedule>());

        TermSchedule schedule = new TermSchedule(start_month,end_month,start_date,end_date,title);

        if(!data_all.get(start_month).contains(schedule))
            data_all.get(start_month).add(schedule);
        if(start_month != end_month && !data_all.get(end_month).contains(schedule))
            data_all.get(end_month).add(schedule);
    }

    public SparseArray<ArrayList<TermSchedule>> getSchedules(){
        return data_all;
    }

    public ArrayList<TermSchedule> getSchedulesOfMonth(int month){
        return data_all.get(month);
    }
}
