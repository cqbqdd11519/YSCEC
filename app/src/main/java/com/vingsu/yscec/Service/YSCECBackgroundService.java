package com.vingsu.yscec.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.widget.Toast;

import com.vingsu.yscec.Container.Article;
import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.Network.LoginProcess;
import com.vingsu.yscec.Network.ParseMainProcess;
import com.vingsu.yscec.Noticer;
import com.vingsu.yscec.R;
import com.vingsu.yscec.YSCEC;

import java.util.ArrayList;

public class YSCECBackgroundService extends Service {

    private static LoginHandler mHandler;
    private static Context mContext;
    private static YSCECBackgroundService thisService;

    public YSCECBackgroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mContext = getApplicationContext();
        thisService = YSCECBackgroundService.this;

        int supers = super.onStartCommand(intent, flags, startId);
        mContext = getApplicationContext();
        mHandler = new LoginHandler();

        SharedPreferences preferences = getSharedPreferences(CommonValues.PREF_LOGIN, MODE_PRIVATE);
        String saved_id = preferences.getString(CommonValues.PREF_KEY_LOGIN_ID, "");
        String saved_pw = preferences.getString(CommonValues.PREF_KEY_LOGIN_PW, "");
        if (saved_id.equals("") || saved_pw.equals(""))
        return supers;
        LoginProcess loginProcess = new LoginProcess(mContext,mHandler);
        try{
            loginProcess.login(saved_id, saved_pw);
        }catch (Exception e){
            e.printStackTrace();
            mHandler.sendEmptyMessage(LoginProcess.FAILED_LOGIN);
        }

        return supers;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("unchecked")
    private static class LoginHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case LoginProcess.SUCCESS_LOGIN:
                    String cookie = (String) msg.obj;
                    YSCEC.setLogin_cookie(cookie);
                    if(cookie != null && !cookie.equals("")){
                        ParseMainProcess parser = new ParseMainProcess(mContext,mHandler);
                        try{
                            parser.parse();
                        }catch (Exception e){
                            e.printStackTrace();
                            mHandler.sendEmptyMessage(ParseMainProcess.FAILED_MAIN);
                        }
                    }
                    break;
                case LoginProcess.FAILED_LOGIN:
                    //Toast.makeText(mContext,R.string.cant_login,Toast.LENGTH_SHORT).show();
                    break;
                case ParseMainProcess.SUCCESS_MAIN:
                    Noticer noticer = new Noticer(mContext);
                    noticer.noticeNewArticle((ArrayList<Article>)msg.obj);
                    break;
                case ParseMainProcess.FAILED_MAIN:
                    //Toast.makeText(mContext,R.string.cant_load_notice,Toast.LENGTH_SHORT).show();
                    break;
                default:
            }
            SharedPreferences preferences_time = mContext.getSharedPreferences(CommonValues.PREF_REFRESH,MODE_PRIVATE);
            int minute_interval = preferences_time.getInt(CommonValues.PREF_KEY_REFRESH_INTERVAL,30);
            AlarmManager alarm = (AlarmManager)mContext.getSystemService(ALARM_SERVICE);
            alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + minute_interval*60*1000,PendingIntent.getService(mContext, 0, new Intent(mContext,YSCECBackgroundService.class), 0));

            thisService.stopSelf();
        }
    }
}
