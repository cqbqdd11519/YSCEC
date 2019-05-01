package com.vingsu.yscec;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.vingsu.yscec.Container.Article;
import com.vingsu.yscec.Container.Course;
import com.vingsu.yscec.Container.ListContainer;
import com.vingsu.yscec.Container.ListContainerContainer;
import com.vingsu.yscec.Container.Notice;

import java.util.ArrayList;

public class DatabaseHelper {

    private static final String dBName = "notices_data";
    private static final String courseTableNamePrefix = "course_data_";
    private static final String articleTableNamePrefix = "article_data_";
    private static final int dbMode = Context.MODE_PRIVATE;

    private static String courseTableName = courseTableNamePrefix;
    private static String articleTableName = articleTableNamePrefix;
    private static SQLiteDatabase db;

    private Context context;

    public DatabaseHelper(Context _context){
        context = _context;
        db = context.openOrCreateDatabase(dBName,dbMode,null);
        SharedPreferences preferences = context.getSharedPreferences(CommonValues.PREF_LOGIN,Context.MODE_PRIVATE);
        String login_id = preferences.getString(CommonValues.PREF_KEY_LOGIN_ID,"");
        String login_hyhg = preferences.getString(CommonValues.PREF_KEY_HYHG_PREFIX+login_id,"");
        courseTableName = courseTableNamePrefix + login_id+"_"+login_hyhg;
        articleTableName = articleTableNamePrefix + login_id+"_"+login_hyhg;
    }

    public void makeDBTable(){
        String course_sql_make = "create table if not exists " + courseTableName + "(" +
                "idx int(32) primary key," +
                "title text not null," +
                "id text not null," +
                "url text not null);";
        db.execSQL(course_sql_make);

        String article_sql_make = "create table if not exists " + articleTableName + "(" +
                "idx int(32) primary key," +
                "title text not null," +
                "id text not null," +
                "course_id text not null," +
                "b_id text not null," +
                "type text not null," +
                "url text not null," +
                "read int(4) not null default 0)";
        db.execSQL(article_sql_make);
    }

    public ArrayList<String> getUserTables(){
        ArrayList<String> data = new ArrayList<>();
        SharedPreferences preferences = context.getSharedPreferences(CommonValues.PREF_LOGIN,Context.MODE_PRIVATE);
        String login_id = preferences.getString(CommonValues.PREF_KEY_LOGIN_ID,"");
        String sql = "select name from sqlite_master WHERE type='table' AND name LIKE '"
                +courseTableNamePrefix+login_id+"%';";
        Cursor results = db.rawQuery(sql, null);
        results.moveToFirst();

        while(!results.isAfterLast()) {
            String name = results.getString(0);
            data.add(name);
            results.moveToNext();
        }
        results.close();
        return data;
    }

    public void cleanCourseList(ArrayList<String> courses){
        ArrayList<Course> storedCourse = getCourseList();
        for(Course c : storedCourse){
            if(!courses.contains(c.getId())){
                deleteCourse(c.getId());
            }
        }
        for(String s : getUserTables()){
            Log.d("tagtag","hyhg : "+s);
        }
    }

    private void deleteCourse(String id){
        String course_sql = "DELETE FROM " + courseTableName + "" +
                " WHERE id=?";
        SQLiteStatement statement = db.compileStatement(course_sql);
        statement.bindString(1,id);
        statement.execute();
        deleteArticle(id);
    }

    private void deleteArticle(String id){
        String course_sql = "DELETE FROM " + articleTableName + "" +
                " WHERE id=?";
        SQLiteStatement statement = db.compileStatement(course_sql);
        statement.bindString(1,id);
        statement.execute();
    }

    public long saveCourseData(Notice n){
        String course_sql = "insert into " + courseTableName + " (title,id,url) " +
                "select ?,?,? " +
                "where not exists (select 1 from "+courseTableName+" where id=?);";
        String course_sql2 = "update "+ courseTableName + " set title = ? " +
                "where id=? and title != ?";
        SQLiteStatement course_statement = db.compileStatement(course_sql);

        String title = n.getTitle();
        String url = n.getCourseLink();
        String id = n.getCourseId();
        int index1 = 1;
        course_statement.bindString(index1++,title);
        course_statement.bindString(index1++, id);
        course_statement.bindString(index1++, url);
        course_statement.bindString(index1, id);
        long result = course_statement.executeInsert();
        course_statement.close();
        if(result <= 0) {
            SQLiteStatement course_statement2 = db.compileStatement(course_sql2);
            index1 = 1;
            course_statement2.bindString(index1++, title);
            course_statement2.bindString(index1++, id);
            course_statement2.bindString(index1, title);
            long result2 = course_statement2.executeInsert();
            course_statement2.close();
        }
        return result;
    }

    public long saveArticleData(String course_id, Article a){
        String article_sql = "insert into " + articleTableName + " (title,id,course_id,b_id,type,url) " +
                "select ?,?,?,?,?,? " +
                "where not exists (select 1 from "+articleTableName+" where id=? and course_id=? and b_id=?)";

        String article_sql2 = "update "+articleTableName+" set title = ? " +
                "where id=? and course_id=? and b_id=? and title !=?;";
        SQLiteStatement article_statement = db.compileStatement(article_sql);

        String a_title = a.getArticleName();
        String a_id = a.getArticleId();
        String a_b_id = a.getArticleBoardId();
        String a_url = a.getArticleURL();
        String a_type = a.getArticleType();
        int index1 = 1;
        article_statement.bindString(index1++,a_title);
        article_statement.bindString(index1++,a_id);
        article_statement.bindString(index1++,course_id);
        article_statement.bindString(index1++,a_b_id);
        article_statement.bindString(index1++,a_type);
        article_statement.bindString(index1++,a_url);
        article_statement.bindString(index1++, a_id);
        article_statement.bindString(index1++, course_id);
        article_statement.bindString(index1, a_b_id);
        long result = article_statement.executeInsert();
        article_statement.close();
        if(result <= 0) {
            SQLiteStatement article_statement2 = db.compileStatement(article_sql2);
            index1 = 1;
            article_statement2.bindString(index1++, a_title);
            article_statement2.bindString(index1++, a_id);
            article_statement2.bindString(index1++, course_id);
            article_statement2.bindString(index1++, a_b_id);
            article_statement2.bindString(index1, a_title);
            long result2 = article_statement2.executeInsert();
            article_statement2.close();
        }
        return result;
    }

    public ArrayList<Course> getCourseList(){
        ArrayList<Course> data = new ArrayList<>();
        String sql = "select title,id from " + courseTableName + " ORDER BY idx ASC;";
        Cursor results = db.rawQuery(sql, null);
        results.moveToFirst();

        while(!results.isAfterLast()) {
            String title = results.getString(0);
            String id = results.getString(1);
            data.add(new Course(title,id));
            results.moveToNext();
        }
        results.close();
        return data;
    }

    public void closeDB(){
        db.close();
    }

    public void setArticleRead(String b_id,String id){
        String sql = "update "+articleTableName+" set read=1 where b_id='"+b_id+"' and id='"+id+"';";
        db.execSQL(sql);
    }

    public static String getCourseNameById(Context context,String _id){
        String courseName = "";
        String sql = "select title from " + courseTableName + " WHERE id='"+_id+"' LIMIT 1;";
        try {
            SQLiteDatabase db = context.openOrCreateDatabase(dBName, dbMode, null);
            Cursor results = db.rawQuery(sql, null);
            results.moveToFirst();
            courseName = results.getString(0);
            results.close();
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return courseName;
    }
    public static String getCourseLinkById(Context context,String _id){
        String courseLink = "";
        String sql = "select url from " + courseTableName + " WHERE id='"+_id+"' LIMIT 1;";
        try {
            SQLiteDatabase db = context.openOrCreateDatabase(dBName, dbMode, null);
            Cursor results = db.rawQuery(sql, null);
            results.moveToFirst();
            courseLink = results.getString(0);
            results.close();
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return courseLink;
    }

    public ArrayList<Notice> getNoticeData(){
        ArrayList<Notice> data = new ArrayList<>();
        String sql = "select id from " + courseTableName + " ORDER BY idx ASC;";
        Cursor results = db.rawQuery(sql, null);
        results.moveToFirst();

        data.clear();
        while(!results.isAfterLast()){
            String id = results.getString(0);
            Notice tmp_notice = getCourseNoticeData(id,true);
            if(tmp_notice.getArticles().size() > 0)
                data.add(tmp_notice);
            results.moveToNext();
        }
        results.close();
        return data;
    }

    public Notice getCourseNoticeData(String id , boolean cut){
        ArrayList<Article> articles = new ArrayList<>();
        String a_sql = "select title,id,course_id,b_id,url,type,read from " + articleTableName + " where course_id="+id+" ORDER BY idx DESC";
        if(cut){
            a_sql += " LIMIT 10";
        }
        a_sql += ";";
        Cursor a_results = db.rawQuery(a_sql, null);
        a_results.moveToFirst();

        articles.clear();
        while(!a_results.isAfterLast()){
            String a_title = a_results.getString(0);
            String a_id = a_results.getString(1);
            String a_course_id = a_results.getString(2);
            String a_b_id = a_results.getString(3);
            String a_url = a_results.getString(4);
            String a_type = a_results.getString(5);
            int a_read = a_results.getInt(6);

            Article article = new Article();
            article.setArticleName(a_title);
            article.setArticleId(a_id);
            article.setArticleBoardId(a_b_id);
            article.setArticleCourseId(a_course_id);
            article.setArticleURL(a_url);
            article.setArticleType(a_type);
            article.setArticleRead(a_read);

            articles.add(article);
            a_results.moveToNext();
        }
        a_results.close();

        Notice notice = new Notice();
        notice.setTitle(getCourseNameById(context,id));
        notice.setCourseId(id);
        notice.setCourseLink(getCourseLinkById(context, id));
        notice.setArticles(articles);
        return notice;
    }

    public void getAllBoards(ArrayList<ListContainerContainer> l){
        ArrayList<Course> courses = getCourseList();
        SharedPreferences preferences = context.getSharedPreferences(CommonValues.PREF_REFRESH, Context.MODE_PRIVATE);
        String[] unsubscribe_list = preferences.getString(CommonValues.PREF_KEY_UNSUBSCRIBE_LIST, "").split(";");
        ArrayList<String> list__ = new ArrayList<>();
        for(String s : unsubscribe_list){
            if(s.length() > 4)
                list__.add(s.substring(3));
        }

        for(Course c : courses){
            String a_sql = "select DISTINCT b_id,type,course_id from " + articleTableName + " where course_id="+c.getId()+" ORDER BY type COLLATE LOCALIZED ASC;";
            Cursor a_results = db.rawQuery(a_sql, null);
            a_results.moveToFirst();

            ArrayList<ListContainer> containers = new ArrayList<>();
            while(!a_results.isAfterLast()){
                String a_b_id = a_results.getString(0);
                String a_type = a_results.getString(1);
                String a_course_id = a_results.getString(2);
                boolean is_now_on = true;

                if(list__.contains(a_course_id+"^&^"+a_b_id))
                    is_now_on = false;

                containers.add(new ListContainer(a_type,a_b_id,is_now_on));

                a_results.moveToNext();
            }
            if(containers.size() > 0)
                l.add(new ListContainerContainer(c.getName(),c.getId(),containers));
            a_results.close();
        }
    }
}
