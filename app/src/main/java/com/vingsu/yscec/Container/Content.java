package com.vingsu.yscec.Container;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class Content {
    private String boardTitle;
    private String title;
    private String writer;
    private String others;
    private String read;
    private String html;
    private ArrayList<String> attachment;
    private ArrayList<String> attachment_url;

    public void setBoardTitle(String s){
        this.boardTitle = s;
    }
    public void setTitle(String s){
        this.title = s;
    }
    public void setWriter(String s){
        this.writer = s;
    }
    public void setOthers(String s){
        this.others = s;
    }
    public void setRead(String s){
        this.read = s;
    }
    public void setHtml(String s){
        this.html = s;
    }
    public void setAttachment(ArrayList<String> a){
        this.attachment = a;
    }
    public void setAttachment_url(ArrayList<String> a){
        this.attachment_url = a;
    }

    public String getBoardTitle(){
        return this.boardTitle;
    }
    public String getTitle(){
        return this.title;
    }
    public String getWriter(){
        return this.writer;
    }
    public String getOthers(){
        return this.others;
    }
    public String getRead(){
        return this.read;
    }
    public String getHtml(){
        return this.html;
    }
    public ArrayList<String> getAttachment(){
        return this.attachment;
    }
    public ArrayList<String> getAttachment_url(){
        return this.attachment_url;
    }
}
