package com.vingsu.yscec.Container;

public class ListContainer{
    private String title;
    private String id;
    private boolean isNowOn;

    public ListContainer(String _title , String _id , boolean _isNowOn){
        this.title = _title;
        this.id = _id;
        this.isNowOn = _isNowOn;
    }

    public String getTitle(){
        return this.title;
    }
    public String getId(){
        return this.id;
    }
    public boolean getIsNowOn(){
        return this.isNowOn;
    }

    public void setNowOn(boolean b){
        this.isNowOn = b;
    }
}