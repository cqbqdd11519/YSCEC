package com.vingsu.yscec.Network;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.vingsu.yscec.TermCalendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseCalendarProcess {
    public static final int SUCCESS_CALENDAR = 601;
    public static final int FAILED_CALENDAR = 602;

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0";

    private static Handler mHandler;

    private static int year;
    private static int term;

    public ParseCalendarProcess(Context context, Handler handler){
        mHandler = handler;
    }

    public void parse(int hy,int hg){
        year = hy;
        term = hg;
        String url = "https://www.yonsei.ac.kr/sc/support/calendar.jsp?cYear="+year+"&hakGi="+term;
        CalendarNetwork network = new CalendarNetwork();
        network.execute(url);
    }

    private class CalendarNetwork extends AsyncTask<String,String,TermCalendar> {
        @Override
        protected TermCalendar doInBackground(String... params) {
            if(params.length < 1)
                return null;
            TermCalendar calendar = new TermCalendar(year,term);
            try{
                Document doc = Jsoup.connect(params[0]).userAgent(USER_AGENT).get();
                Element table = doc.getElementsByClass("jw_vertical").get(0).getElementsByTag("tbody").get(0);
                Elements trs = table.getElementsByTag("tr");
                int now_month = 0;
                for(Element tr : trs){
                    Elements ths = tr.getElementsByTag("th");
                    if(ths.size() > 0){
                        String className = ths.get(0).getElementsByTag("span").get(0).className().trim();
                        Pattern pattern = Pattern.compile("month([0-9]*)");
                        Matcher matcher = pattern.matcher(className);
                        if(matcher.matches()){
                            now_month = Integer.parseInt(matcher.group(1));
                        }
                    }
                    Elements tds = tr.getElementsByTag("td");
                    String title = tds.get(1).text().trim();
                    int start = 0;
                    int end = 0;
                    String range = tds.get(0).text().trim();
                    Pattern pattern0 = Pattern.compile("([0-9]*(\\.[0-9]*)?)\\([^\\)]*\\)~([0-9]*(\\.[0-9]*)?)\\([^\\)]*\\)");
                    Matcher matcher0 = pattern0.matcher(range);
                    Pattern pattern1 = Pattern.compile("([0-9]*)\\([^\\)]*\\)");
                    Matcher matcher1 = pattern1.matcher(range);

                    int start_month = now_month;
                    int end_month = now_month;
                    if(matcher0.matches()){
                        if(matcher0.group(1).contains(".")){
                            start_month = Integer.parseInt(matcher0.group(1).split("\\.")[0]);
                            start = Integer.parseInt(matcher0.group(1).split("\\.")[1]);
                        }else{
                            start =Integer.parseInt(matcher0.group(1));
                        }

                        if(matcher0.group(3).contains(".")){
                            end_month = Integer.parseInt(matcher0.group(3).split("\\.")[0]);
                            end = Integer.parseInt(matcher0.group(3).split("\\.")[1]);
                        }else{
                            end = Integer.parseInt(matcher0.group(3));
                        }
                    }else if(matcher1.matches()){
                        start = end = Integer.parseInt(matcher1.group(1));
                    }
                    calendar.addSchedule(start_month,end_month,start,end,title);
                }
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
            return calendar;
        }

        @Override
        protected void onPostExecute(TermCalendar termCalendar) {
            if(termCalendar == null){
                mHandler.sendEmptyMessage(FAILED_CALENDAR);
                return;
            }
            Message message = Message.obtain();
            message.what = SUCCESS_CALENDAR;
            message.obj = termCalendar;

            mHandler.sendMessage(message);
        }
    }
}
