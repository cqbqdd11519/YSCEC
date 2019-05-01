package com.vingsu.yscec.Container;

@SuppressWarnings("unused")
public class TimeTableCourse {
    private String courseName = "";
    private String coursePlace = "";

    //학정번호 : courseId1-courseId2-courseId3 형식
    private String courseId1 = "";
    private String courseId2 = "";
    private String courseId3 = "";

    private String courseExtra = "";

    public TimeTableCourse(){}

    public TimeTableCourse(String _courseName, String _coursePlace, String _courseId1, String _courseId2, String _courseId3,String _extra){
        this.courseName = _courseName;
        this.coursePlace = _coursePlace;
        this.courseId1 = _courseId1;
        this.courseId2 = _courseId2;
        this.courseId3 = _courseId3;
        this.courseExtra = _extra;
    }

    public void setCourseName(String s){
        this.courseName = s;
    }
    public void setCoursePlace(String s){
        this.coursePlace = s;
    }
    public void setCourseId1(String s){
        this.courseId1 = s;
    }
    public void setCourseId2(String s){
        this.courseId2 = s;
    }
    public void setCourseId3(String s){
        this.courseId3 = s;
    }
    public void setCourseExtra(String s){
        this.courseExtra = s;
    }

    public String getCourseName(){
        return this.courseName;
    }
    public String getCoursePlace(){
        return this.coursePlace;
    }
    public String getCourseId1(){
        return this.courseId1;
    }
    public String getCourseId2(){
        return this.courseId2;
    }
    public String getCourseId3(){
        return this.courseId3;
    }
    public String getCourseExtra(){
        return this.courseExtra;
    }

    public boolean equals(TimeTableCourse t){
        return t.getCourseId1().equals(this.getCourseId1()) &&
                t.getCourseId2().equals(this.getCourseId2()) &&
                t.getCourseId3().equals(this.getCourseId3()) &&
                t.getCourseName().equals(this.getCourseName()) &&
                t.getCoursePlace().equals(this.getCoursePlace()) &&
                t.getCourseExtra().equals(this.getCourseExtra());
    }
}
