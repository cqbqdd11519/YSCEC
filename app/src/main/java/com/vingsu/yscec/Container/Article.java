package com.vingsu.yscec.Container;

public class Article {
    private String articleName;
    private String articleURL;
    private String articleId;
    private String articleBoardId;
    private String articleType;
    private String articleCourseId;
    private int articleRead;

    public void setArticleName(String s){
        this.articleName = s;
    }
    public void setArticleURL(String s){
        this.articleURL = s;
    }
    public void setArticleId(String s){
        this.articleId = s;
    }
    public void setArticleBoardId(String s){
        this.articleBoardId = s;
    }
    public void setArticleCourseId(String s){
        this.articleCourseId = s;
    }
    public void setArticleType(String s){
        this.articleType = s;
    }
    public void setArticleRead(int i){
        this.articleRead = i;
    }

    public String getArticleName(){
        return this.articleName;
    }
    public String getArticleURL(){
        return this.articleURL;
    }
    public String getArticleId(){
        return this.articleId;
    }
    public String getArticleBoardId(){
        return this.articleBoardId;
    }
    public String getArticleCourseId(){
        return this.articleCourseId;
    }
    public String getArticleType(){
        return this.articleType;
    }
    public int getArticleRead(){
        return this.articleRead;
    }
}
