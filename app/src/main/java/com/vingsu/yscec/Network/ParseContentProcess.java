package com.vingsu.yscec.Network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;

import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.Container.Content;
import com.vingsu.yscec.R;
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
public class ParseContentProcess {

    public final static int SUCCESS_CONTENT = 200;
    public final static int FAILED_CONTENT = -200;

    private static Context mContext;
    private static Handler mHandler;

    private static String nowUrl = "";

    public ParseContentProcess(Context c,Handler h){
        mContext = c;
        mHandler = h;
    }

    public void parse(String b_id,String id){
        nowUrl = "https://yscec.yonsei.ac.kr/mod/jinotechboard/content.php?contentId="+id+"&b="+b_id+"&boardform=1";
        String cookie = YSCEC.getLogin_cookie();
        ParseNetwork parseNetwork = new ParseNetwork();
        parseNetwork.execute(cookie);
    }

    private void parseFromHTML(String s) throws Exception{
        if(s == null){
            mHandler.sendEmptyMessage(FAILED_CONTENT);
            return;
        }
        Document doc = Jsoup.parse(s);
        Content content = new Content();

        String board_title = doc.getElementsByClass("board-title").first().text();
        Element board_details_v = doc.getElementsByClass("board-detail-area").first();

        String article_title = board_details_v.getElementsByClass("detail-title").first().text();

        Element detail_date = board_details_v.getElementsByClass("detail-date").first();
        String article_writer = detail_date.getElementsByTag("a").first().text();
        String others = detail_date.ownText().replaceAll("by[ ]?", "");

        String read = board_details_v.getElementsByClass("detail-viewinfo").first().ownText();
        String html = board_details_v.getElementsByClass("detail-contents").first().html();

        ArrayList<String> attachment = new ArrayList<>();
        ArrayList<String> attachment_url = new ArrayList<>();
        if(board_details_v.getElementsByClass("detail-attachment").size() > 0) {
            Element attaches = board_details_v.getElementsByClass("detail-attachment").first();
            Elements lis = attaches.getElementsByTag("li");
            for(Element element : lis){
                Element anchor = element.getElementsByTag("a").get(1);
                attachment.add(anchor.text());
                attachment_url.add(anchor.attr("href"));
            }
        }

        String color_string = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(mContext, R.color.background_color)));
        html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>294641: lab tour</title>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no\">\n" +
                "    <meta http-equiv=\"x-ua-compatible\" content=\"IE=10\">\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
                "<style type='text/css'>" +
                "   body {background-color:"+color_string+"}" +
                "</style>" +
                "</head>" +
                "<body>" +
                html +
                "</body>" +
                "</html>";

        content.setBoardTitle(board_title);
        content.setTitle(article_title);
        content.setWriter(article_writer);
        content.setOthers(others);
        content.setRead(read);
        content.setHtml(html);
        content.setAttachment(attachment);
        content.setAttachment_url(attachment_url);

        Message message = Message.obtain();
        message.what = SUCCESS_CONTENT;
        message.obj = content;
        mHandler.sendMessage(message);
    }

    private class ParseNetwork extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            String cookie = strings[0];
            String fin = "";
            HttpURLConnection connection = null;

            try{
                URL url = new URL(nowUrl);

                connection = (HttpURLConnection)url.openConnection();
                String method = CommonValues.STRING_POST;
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
            }
        }
    }
}
