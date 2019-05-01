package com.vingsu.yscec.Network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.YSCEC;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseTimeTableProcess {

    public static final int SUCCESS_TIMETABLE = 501;
    public static final int FAILED_TIMETABLE = 502;

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0";

    private String[] urls = {
            "https://underwood1.yonsei.ac.kr:8443/haksa/main.jsp", //YSCEC.getLogin_cookie()
            "", //첫번째 Request 의 Location
            "https://infra.yonsei.ac.kr/sso/PmSSOService",
            "https://underwood1.yonsei.ac.kr:8443/haksa/SSOLegacy.do?pname=spLoginData",
            "", //네번째 Request 의 Location
            //"https://ysrollbook.yonsei.ac.kr/eams/webView/studTimeTable"    //Timetable JSON GET
            "https://underwood1.yonsei.ac.kr:8443/haksa/main.jsp",
            "https://underwood1.yonsei.ac.kr:8443/haksa/act/KK/sg/SG_S020M.jsp"    //Timetable JSON GET
    };

    private static Handler mHandler;

    private static String stu_no = "";
    private static String hyhg = "20152";

    private HashMap<String,String> params;
    private CookieManager cookies;

    private static final String[] methods = {
            CommonValues.STRING_GET,
            CommonValues.STRING_GET,
            CommonValues.STRING_POST,
            CommonValues.STRING_POST,
            CommonValues.STRING_GET,
            CommonValues.STRING_GET,
            CommonValues.STRING_POST
    };

    public ParseTimeTableProcess(Context _context, Handler _handler){
        mHandler = _handler;
        params = new HashMap<>();
        cookies = new CookieManager();
        SharedPreferences preferences = _context.getSharedPreferences(CommonValues.PREF_LOGIN,Context.MODE_PRIVATE);
        stu_no = preferences.getString(CommonValues.PREF_KEY_LOGIN_ID,"");
    }

    public void parse(){
        if(urls[0] == null || urls[0].equals("")){
            mHandler.sendEmptyMessage(FAILED_TIMETABLE);
            return;
        }
        Networks networks = new Networks();
        networks.execute(0);
    }

    private class Networks extends AsyncTask<Integer,String,String>{

        private Integer nowStep = -1;
        private String nowUrl = "";
        private String nowMethod = "";

        @Override
        protected String doInBackground(Integer... integers) {
            String fin = "";

            this.nowStep = integers[0];
            this.nowUrl = urls[this.nowStep];
            this.nowMethod = methods[this.nowStep];

            HttpURLConnection connection = null;

            try{
                URL url = new URL(this.nowUrl);

                connection = (HttpURLConnection) url.openConnection();

                connection.setReadTimeout(5000);
                connection.setConnectTimeout(5000);
                connection.setRequestMethod(this.nowMethod);
                connection.setInstanceFollowRedirects(false);
                connection.setDoInput(true);
                if(this.nowMethod.equals(CommonValues.STRING_POST))
                    connection.setDoOutput(true);
                connection.setRequestProperty(CommonValues.STRING_USER_AGENT,USER_AGENT);

                if(this.nowStep > 0)
                    connection.setRequestProperty(CommonValues.STRING_REFERER,urls[this.nowStep-1]);
                else if(this.nowStep == 0)
                    connection.setRequestProperty(CommonValues.STRING_REFERER,"http://yscec.yonsei.ac.kr/my/");

                String cookieString = "";
                if(cookies.getCookieStore().getCookies().size()>0 && !this.nowUrl.contains("infra.yonsei.ac.kr"))
                    cookieString = TextUtils.join(";", cookies.getCookieStore().getCookies());
                else if(this.nowUrl.contains("infra.yonsei.ac.kr")){
                    cookieString = CommonValues.STRING_JSESSIONID_SSO +"="+YSCEC.getInfra_JSESSIONID();
                }

                connection.setRequestProperty("Cookie",cookieString);

                String paramString = getPostParamString(params);

                if(this.nowStep == 6){
                    paramString = "{\"dc_req\":{\"lang\":\"0\",\"flag\":\"schedule\",\"fnddomain\":\"\",\"fndhakbun\":\""+stu_no+"\",\"fndhyhg\":\""+hyhg+"\",\"week\":\"3\"}}";
                    connection.setRequestProperty("Content-Type","application/json; charset=\"UTF-8\"");
                    connection.setRequestProperty("submissionid","sbm_sgs020m_schedule");
                }

                if(this.nowMethod.equals(CommonValues.STRING_POST)) {
                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, CommonValues.STRING_UTF_8));
                    writer.write(paramString);
                    writer.flush();
                    writer.close();
                    os.close();
                }

                InputStreamReader is = new InputStreamReader(connection.getInputStream());
                StringBuilder sb=new StringBuilder();
                BufferedReader br = new BufferedReader(is);
                String read = br.readLine();
                while(read != null) {
                    sb.append(read);
                    read =br.readLine();
                }
                fin = sb.toString();

                Map<String, List<String>> iMap = connection.getHeaderFields( ) ;
                if( iMap.containsKey( CommonValues.STRING_SET_COOKIE ) ){
                    List<String> lString = iMap.get( CommonValues.STRING_SET_COOKIE ) ;
                    if(!this.nowUrl.contains("infra.yonsei.ac.kr")){
                        for(String s : lString) {
                            HttpCookie httpCookie = HttpCookie.parse(s).get(0);
                            cookies.getCookieStore().add(null, httpCookie);
                        }
                    }
                }
                if(iMap.containsKey(CommonValues.STRING_LOCATION) && this.nowStep != 4 && this.nowStep != 5){
                    urls[this.nowStep + 1] = fin = iMap.get(CommonValues.STRING_LOCATION).get(0);
                }else if(this.nowStep == 4){
                    fin = iMap.get(CommonValues.STRING_LOCATION).get(0);
                }

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
            if(s == null){
                mHandler.sendEmptyMessage(FAILED_TIMETABLE);
                return;
            }

            try{
                switch (this.nowStep){
                    case 0:
                        Networks networks0 = new Networks();
                        networks0.execute(1);
                        break;
                    case 1:
                        prepareParams(this.nowStep,s);
                        Networks networks1 = new Networks();
                        networks1.execute(2);
                        break;
                    case 2:
                        prepareParams(this.nowStep,s);
                        Networks networks2 = new Networks();
                        networks2.execute(3);
                        break;
                    case 3:
                        Networks networks3 = new Networks();
                        networks3.execute(4);
                        break;
                    case 4:
                        if(s.equals(urls[0])){
                            params.clear();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(System.currentTimeMillis()));
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH) + 1;

                            if(month >= 8)
                                hyhg = year+"2";
                            else
                                hyhg = year+"1";
                            Networks networks4 = new Networks();
                            networks4.execute(5);
                        }else{
                            mHandler.sendEmptyMessage(FAILED_TIMETABLE);
                        }
                        break;
                    case 5:
                        Networks networks55 = new Networks();
                        networks55.execute(6);
                        break;
                    case 6:
                        JSONObject object = new JSONObject(s);
                        Message message = Message.obtain();
                        message.what = SUCCESS_TIMETABLE;
                        message.obj = object;
                        mHandler.sendMessage(message);
                        break;
                    default:
                        mHandler.sendEmptyMessage(FAILED_TIMETABLE);
                }
            }catch (Exception e){
                e.printStackTrace();
                mHandler.sendEmptyMessage(FAILED_TIMETABLE);
            }
        }

        public String getPostParamString(HashMap<String, String> params) throws Exception{
            if(params.size() == 0)
                return "";

            StringBuilder buf = new StringBuilder();
            for(Map.Entry<String,String> entry : params.entrySet()) {
                buf.append(buf.length() == 0 ? "" : "&");
                buf.append(entry.getKey()).append("=").append(entry.getValue());
            }
            return buf.toString();
        }

        private void prepareParams(int index,String s) throws Exception{
            if(s == null)
                return;

            if(params == null){
                params = new HashMap<>();
            }else{
                params.clear();
            }

            String formName;
            switch (index){
                case 1:
                    formName = "frmSSO";
                    break;
                case 2:
                    formName = "ssoLoginForm";
                    break;
                default:
                    return;
            }

            try {
                Document doc = Jsoup.parse(s);
                Element form = doc.getElementById(formName);
                Elements inputs = form.getElementsByTag("input");
                for(Element input : inputs){
                    String name = input.attr("name");
                    String value = input.attr("value");
                    params.put(name,value);
                }
            }catch (Exception e){
                e.printStackTrace();
                mHandler.sendEmptyMessage(FAILED_TIMETABLE);
            }
        }
    }
}
