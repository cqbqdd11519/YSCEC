package com.vingsu.yscec;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.vingsu.yscec.Activity.MainActivity;
import com.vingsu.yscec.Container.Article;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class Noticer {

    private static Context mContext;

    public Noticer(Context c){
        mContext = c;
    }

    public void noticeNewArticle(ArrayList<Article> articles){
        if(articles == null || articles.size() == 0)
            return;

        SharedPreferences preferences_noti = mContext.getSharedPreferences(CommonValues.PREF_NOTIFICATION,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences_noti.edit();
        Set<String> unread_string = preferences_noti.getStringSet(CommonValues.PREF_KEY_UNREAD_NOTIFICATION,new TreeSet<String>());

        SharedPreferences preferences = mContext.getSharedPreferences(CommonValues.PREF_REFRESH, Context.MODE_PRIVATE);
        String[] unsubscribe_list = preferences.getString(CommonValues.PREF_KEY_UNSUBSCRIBE_LIST, "").split(";");
        ArrayList<String> list__ = new ArrayList<>();
        for(String s : unsubscribe_list){
            if(s.length() > 4)
                list__.add(s.substring(3));
        }

        //SimpleDateFormat format = new SimpleDateFormat("MMddHHmm",Locale.KOREA);
        //int notify_id = Integer.parseInt(format.format(new Date(System.currentTimeMillis()))) * 10;
        int notify_id = 0;

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder mBuilder = new Notification.Builder(mContext);
        Intent intent = new Intent(mContext, MainActivity.class);

        if(Build.VERSION.SDK_INT >= 21)
            mBuilder.setColor(ContextCompat.getColor(mContext,R.color.theme_color));
        mBuilder.setSmallIcon(R.drawable.ic_launcher_noti);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        mBuilder.setLights(Color.YELLOW, 500, 5000);
        mBuilder.setAutoCancel(true);
        mBuilder.setPriority(Notification.PRIORITY_DEFAULT);

        if(unread_string.size() == 0 && articles.size() == 1){
            Article a = articles.get(0);
            String title = a.getArticleName();
            String url = a.getArticleURL();
            String id = a.getArticleId();
            String b_id = a.getArticleBoardId();
            String course_id = a.getArticleCourseId();
            String type = a.getArticleType();

            if(list__.contains(course_id+"^&^"+b_id)){
                return;
            }

            String summary_text = DatabaseHelper.getCourseNameById(mContext,course_id) + " | "+type;
            mBuilder.setTicker(title);
            mBuilder.setContentTitle(title);
            mBuilder.setContentText(summary_text);

            mBuilder.setStyle(new Notification.BigTextStyle()
                    .bigText(title)
                    .setBigContentTitle(type)
                    .setSummaryText(summary_text));

            unread_string = new TreeSet<>();
            unread_string.add(type + " | " + title);
            intent.putExtra("type", "content");
            intent.putExtra(CommonValues.PARAM_FULL_URL, url);
            intent.putExtra(CommonValues.PARAM_COURSE_ID, course_id);
            intent.putExtra(CommonValues.PARAM_B_ID, b_id);
            intent.putExtra(CommonValues.PARAM_ID, id);
        }else{
            int message_cnt = articles.size();
            message_cnt += unread_string.size();
            mBuilder.setTicker(message_cnt + mContext.getString(R.string.number_of_noti_text));
            mBuilder.setContentTitle(mContext.getString(R.string.app_name)+mContext.getString(R.string.noti_text));
            mBuilder.setContentText(message_cnt + mContext.getString(R.string.number_of_noti_text));
            Notification.InboxStyle style = new Notification.InboxStyle();
            style.setBigContentTitle(message_cnt + mContext.getString(R.string.number_of_noti_text));

            for(Article a : articles){
                String title = a.getArticleName();
                String type = a.getArticleType();
                unread_string.add(type + " | " + title);
            }
            ArrayList<String> tmp_list = new ArrayList<>(unread_string);
            for(int i = 4 ; i >= 0 ; i--){
                if(unread_string.size() <= i)
                    continue;
                style.addLine(tmp_list.get(i));
            }

            mBuilder.setNumber(unread_string.size());
            mBuilder.setStyle(style);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notify_id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        notificationManager.notify(notify_id, mBuilder.build());

        editor.putStringSet(CommonValues.PREF_KEY_UNREAD_NOTIFICATION, unread_string);
        editor.apply();
        CommonFunctions.setBadge(mContext, unread_string.size());
    }
}
