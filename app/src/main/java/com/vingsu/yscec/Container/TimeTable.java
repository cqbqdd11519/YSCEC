package com.vingsu.yscec.Container;

@SuppressWarnings("unused")
public class TimeTable {

    public static final int MONDAY = 0;
    public static final int TUESDAY = 1;
    public static final int WEDNESDAY = 2;
    public static final int THURSDAY = 3;
    public static final int FRIDAY = 4;

    public static final int lastTime = 16;
    public static final int lastDay = 4;

    public int maxTime = -1;
    public int minTime = lastTime+1;

    public int maxDay = -1;
    public int minDay = lastDay+1;

    TimeTableCourse[][] timeTableCourses;

    public TimeTable(){
        this.timeTableCourses = new TimeTableCourse[lastDay+1][lastTime+1];

        for(int i = 0; i < lastDay+1 ; i ++){
            this.timeTableCourses[i] = new TimeTableCourse[lastTime+1];
        }
    }

    public TimeTableCourse[] getTimeTableAtDay(int day){
        if(day < 0 || day > lastDay){
            return null;
        }
        return this.timeTableCourses[day];
    }

    public void setCourse(int day, int time,TimeTableCourse course){
        if(day < 0 || day > lastDay || time < 0 || time > lastTime){
            return;
        }
        this.timeTableCourses[day][time] = course;

        maxDay = max(maxDay,day);
        minDay = min(minDay,day);

        maxTime = max(maxTime,time);
        minTime = min(minTime,time);
    }

    public TimeTableCourse getCourse(int day, int time){
        if(day < 0 || day > lastDay || time < 0 || time > lastTime){
            return null;
        }
        return this.timeTableCourses[day][time];
    }

    private int max(int i,int j){
        if(i>j)
            return i;
        else
            return j;
    }
    private int min(int i,int j){
        if(i>j)
            return j;
        else
            return i;
    }

}
