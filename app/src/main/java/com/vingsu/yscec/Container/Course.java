package com.vingsu.yscec.Container;

public class Course {

    private String name;
    private String id;

    public Course(String _name, String _id){
        this.name = _name;
        this.id = _id;
    }

    public void setName(String s){
        this.name = s;
    }
    public void setId(String s){
        this.id = s;
    }

    public String getName(){
        return this.name;
    }
    public String getId(){
        return this.id;
    }
}
