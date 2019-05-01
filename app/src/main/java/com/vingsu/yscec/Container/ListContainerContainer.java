package com.vingsu.yscec.Container;

import java.util.ArrayList;

public class ListContainerContainer{
    private String title;
    private String id;
    private ArrayList<ListContainer> boards;

    public ListContainerContainer(String _title,String _id, ArrayList<ListContainer> _boards){
        this.title = _title;
        this.id = _id;
        this.boards = _boards;
    }

    public String getTitle(){
        return this.title;
    }
    public String getId(){
        return this.id;
    }
    public ArrayList<ListContainer> getBoards(){
        return this.boards;
    }
}
