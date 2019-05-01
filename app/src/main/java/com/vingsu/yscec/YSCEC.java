package com.vingsu.yscec;

import android.app.Application;


public class YSCEC extends Application {

    private static String login_cookie = "";
    private static String infra_JSESSIONID = "";
    private static String login_name = "";

    public static void setInfra_JSESSIONID(String s){
        infra_JSESSIONID = s;
    }
    public static String getInfra_JSESSIONID(){
        return infra_JSESSIONID;
    }

    public static void setLogin_cookie(String s){
        login_cookie = s;
    }
    public static String getLogin_cookie(){
        return login_cookie;
    }

    public static void setLogin_name(String s){
        login_name = s;
    }
    public static String getLogin_name(){
        return login_name;
    }

}
