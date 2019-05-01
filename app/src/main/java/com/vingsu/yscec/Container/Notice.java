package com.vingsu.yscec.Container;

import java.util.ArrayList;

public class Notice {
    private String title;
    private String courseLink;
    private String courseId;
    private ArrayList<Article> articles;

    public void setTitle(String s){
        this.title = s;
    }
    public void setCourseLink(String s){
        this.courseLink = s;
    }
    public void setCourseId(String s){
        this.courseId = s;
    }
    public void setArticles(ArrayList<Article> s){
        this.articles = s;
    }

    public String getTitle(){
        return this.title;
    }
    public String getCourseLink(){
        return this.courseLink;
    }
    public String getCourseId(){
        return this.courseId;
    }
    public ArrayList<Article> getArticles(){
        return this.articles;
    }
}
