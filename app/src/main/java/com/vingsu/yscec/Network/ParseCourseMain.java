package com.vingsu.yscec.Network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.Container.CourseMainContainer;
import com.vingsu.yscec.YSCEC;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class ParseCourseMain {

    public static final int SUCCESS_COURSE_MAIN = 487;
    public static final int FAILED_COURSE_MAIN = 486;

    private Context mContext;
    private Handler mHandler;

    public ParseCourseMain(Context context, Handler handler){
        this.mContext = context;
        this.mHandler = handler;
    }

    public void parse(String course_id){
        new ParseCourseClass().execute(course_id);
    }

    private class ParseCourseClass extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... strings) {
            String courseId = strings[0];

            HttpURLConnection connection = null;
            String fin = null;
            try {
                connection = (HttpURLConnection) new URL("http://yscec.yonsei.ac.kr/course/view.php?id="+courseId).openConnection();
                String method = CommonValues.STRING_GET;
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod(method);
                connection.setInstanceFollowRedirects(false);
                connection.setDoInput(true);

                connection.setRequestProperty(CommonValues.STRING_REFERER, "http://yscec.yonsei.ac.kr/my/");

                String cookieString = CommonValues.STRING_COOKIE_PREFIX+"="+ YSCEC.getLogin_cookie()+";";
                connection.setRequestProperty(CommonValues.STRING_COOKIE, cookieString);

                InputStreamReader is = new InputStreamReader(connection.getInputStream());
                StringBuilder sb=new StringBuilder();
                BufferedReader br = new BufferedReader(is);
                String read = br.readLine();
                while(read != null) {
                    if(!read.contains("\uFEFF"))
                        sb.append(read);
                    read =br.readLine();
                }
                fin = sb.toString();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(connection != null)
                    connection.disconnect();
            }
            return fin;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s == null || s.equals("")){
                mHandler.sendEmptyMessage(FAILED_COURSE_MAIN);
            }else{
                try{
                    ArrayList<String> info_prof = new ArrayList<>();
                    ArrayList<String> info_prof_mail = new ArrayList<>();
                    Document doc = Jsoup.parse(s);
                    Element page_title = doc.getElementsByClass("page-title").first();
                    String title = page_title.ownText();
                    Element syllabus = doc.getElementsByClass("syllabus").first();
                    String plan_url = "";
                    if( syllabus != null){
                        plan_url = syllabus.getElementsByTag("a").first().attr("href");
                    }
                    Element instructor_info = doc.getElementsByClass("instructor-info").first();
                    Elements lis = instructor_info.getElementsByTag("li");
                    for(Element e : lis){
                        Elements spans = e.getElementsByTag("span");
                        info_prof.add(spans.get(0).ownText());
                        info_prof_mail.add(spans.get(1).ownText());
                    }

                    CourseMainContainer container = new CourseMainContainer(title,plan_url,info_prof,info_prof_mail);
                    Message message = Message.obtain();
                    message.what = SUCCESS_COURSE_MAIN;
                    message.obj = container;
                    mHandler.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(FAILED_COURSE_MAIN);
                }
            }
        }
    }
}
