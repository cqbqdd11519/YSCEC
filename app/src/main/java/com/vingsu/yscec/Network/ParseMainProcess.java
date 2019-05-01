package com.vingsu.yscec.Network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.vingsu.yscec.Container.Article;
import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.Container.Course;
import com.vingsu.yscec.DatabaseHelper;
import com.vingsu.yscec.Container.Notice;
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

public class ParseMainProcess {

    public final static int SUCCESS_MAIN = 200;
    public final static int FAILED_MAIN = -200;

    private Context mContext;
    private Handler mHandler;

    private String nowUrl = "";
    private ArrayList<Article> alarms;

    public ParseMainProcess(Context c,Handler h){
        mContext = c;
        mHandler = h;
        nowUrl = "https://yscec.yonsei.ac.kr/local/courselist/courseoverview.php";
    }

    public void parse()throws Exception{
        alarms = new ArrayList<>();
        String cookie = YSCEC.getLogin_cookie();
        ParseNetwork parseNetwork = new ParseNetwork();
        parseNetwork.execute(cookie);
    }

    private void parseFromHTML(String s) throws Exception{
        Log.d("tagtag",s);
        Document doc = Jsoup.parse(s);
        Element courseList = doc.getElementsByClass("course_list").get(0);
        Elements courses = courseList.getElementsByClass("coursebox");

        DatabaseHelper helper = new DatabaseHelper(mContext);
        helper.makeDBTable();
        ArrayList<String> courses_arr = new ArrayList<>();

        for(Element c : courses){
            Notice notice = new Notice();
            Element titleElement = c.getElementsByClass("title").get(0);
            String title = titleElement.text();
            String courseLink = titleElement.getElementsByTag("a").get(0).attr("href");
            String[] courseId_tmp = courseLink.split("\\?")[1].split("&");
            String courseId = "";
            for(String ss : courseId_tmp){
                String name = ss.split("=")[0];
                String value = ss.split("=")[1];
                if(name.equals("id")){
                    courseId = value;
                    break;
                }
            }
            if(!courses_arr.contains(courseId)){
                courses_arr.add(courseId);
            }
            Elements articleElements = c.getElementsByClass("overview");
            ArrayList<Article> articles = new ArrayList<>();
            for(int j = articleElements.size()-1;j>=0;j--){
                Element e = articleElements.get(j);
                Element name = e.getElementsByClass("name").get(0);
                Article article = new Article();
                Element a = name.getElementsByTag("a").get(0);
                String articleLink = a.attr("href");
                String[] article_link_split = articleLink.split("\\?")[1].split("&");
                String articleId = "";
                String articleBoardId = "";

                int type = 0;
                if(articleLink.contains("contentId"))
                    type = 1;
                String[] link_split = articleLink.split("/");
                for(String ss : article_link_split){
                    String _name = ss.split("=")[0];
                    String _value = ss.split("=")[1];
                    if(type == 1) {
                        if (_name.equals("contentId"))
                            articleId = _value;
                        else if (_name.equals("b"))
                            articleBoardId = _value;
                    }else{
                        if(_name.equals("id") || _name.equals("f")){
                            articleId = _value;
                            articleBoardId = "@"+CommonValues.STRING_DUMMY+"_"+link_split[link_split.length-2];
                        }
                    }
                }
                String articleTitle = a.text();
                String articleType = name.ownText().trim();
                if(articleType.endsWith(":"))
                    articleType = articleType.substring(0,articleType.length()-1);

                article.setArticleName(articleTitle);
                article.setArticleURL(articleLink);
                article.setArticleId(articleId);
                article.setArticleBoardId(articleBoardId);
                article.setArticleType(articleType);
                article.setArticleCourseId(courseId);
                if(articles.size() < 10)
                    articles.add(article);

                long nowRow = helper.saveArticleData(courseId,article);
                if(nowRow > 0){
                    alarms.add(article);
                }
            }
            notice.setTitle(title);
            notice.setCourseLink(courseLink);
            notice.setCourseId(courseId);
            notice.setArticles(articles);

            helper.saveCourseData(notice);
        }
        helper.cleanCourseList(courses_arr);
        helper.closeDB();

        Message message = Message.obtain();
        message.what = SUCCESS_MAIN;
        message.obj = alarms;
        mHandler.sendMessage(message);
    }

    private class ParseNetwork extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... strings) {
            String cookie = strings[0];
            String fin = "";
            HttpURLConnection connection = null;

            try{
                URL url = new URL(nowUrl);

                connection = (HttpURLConnection)url.openConnection();
                String method = CommonValues.STRING_POST;
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod(method);
                connection.setInstanceFollowRedirects(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                connection.setRequestProperty(CommonValues.STRING_REFERER,nowUrl);

                String cookieString = CommonValues.STRING_COOKIE_PREFIX+"="+cookie+";";
                connection.setRequestProperty(CommonValues.STRING_COOKIE,cookieString);

                InputStreamReader is = new InputStreamReader(connection.getInputStream());
                StringBuilder sb=new StringBuilder();
                BufferedReader br = new BufferedReader(is);
                String read = br.readLine();
                while(read != null) {
                    sb.append(read);
                    read =br.readLine();
                }
                fin = sb.toString();

            }catch (Exception e){
                e.printStackTrace();
                mHandler.sendEmptyMessage(FAILED_MAIN);
                return null;
            }finally {
                if(connection != null)
                    connection.disconnect();
            }
            return fin;
        }

        @Override
        protected void onPostExecute(String s) {
            try{
                parseFromHTML(s);
            }catch (Exception e){
                e.printStackTrace();
                mHandler.sendEmptyMessage(FAILED_MAIN);
            }
        }
    }
}
