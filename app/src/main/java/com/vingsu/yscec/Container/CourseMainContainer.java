package com.vingsu.yscec.Container;

import java.util.ArrayList;

public class CourseMainContainer {
    private String title;
    private String plan_url;
    private ArrayList<String> prof_info;
    private ArrayList<String> prof_info_mail;

    public CourseMainContainer(String _title, String _plan_url, ArrayList<String> _prof_info, ArrayList<String> _prof_info_mail){
        this.title = _title;
        this.plan_url = _plan_url;
        this.prof_info = _prof_info;
        this.prof_info_mail = _prof_info_mail;
    }

    public String getTitle(){
        return this.title;
    }
    public String getPlanUrl(){
        return this.plan_url;
    }
    public ArrayList<String> getProfInfo(){
        return this.prof_info;
    }
    public ArrayList<String> getProfInfoMail(){
        return this.prof_info_mail;
    }
}
