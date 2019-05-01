package com.vingsu.yscec.Network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.YSCEC;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint("SetJavaScriptEnabled")
public class LoginProcess {

    public static final int SUCCESS_LOGIN = 100;
    public static final int FAILED_LOGIN = -100;

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0";

    private Context mContext;
    private Handler mHandler;
    private LoginNetwork mNetwork;

    private String id;
    private String pw;

    private String ssoChallenge;
    private String keyModulus;

    private HashMap<String,String> params;
    private CookieManager cookies;
    private String final_cookie = "";

    private static final String[] urls = {
            "https://yscec.yonsei.ac.kr/login/index.php",
            "https://yscec.yonsei.ac.kr/passni/sso/spLogin.php",
            "https://infra.yonsei.ac.kr/sso/PmSSOService",
            "https://yscec.yonsei.ac.kr/login/index.php",
            "https://infra.yonsei.ac.kr/sso/PmSSOAuthService",
            "https://yscec.yonsei.ac.kr/passni/sso/spLoginData.php",
            "https://yscec.yonsei.ac.kr/passni/spLoginProcess.php"
    };
    private String url_final = "";
    private String url_final2 = "";

    public LoginProcess(Context _context,Handler _handler){
        mContext = _context;
        mHandler = _handler;
        cookies = new CookieManager();
        params = new HashMap<>();
    }

    public void login(String login_id,String login_pw) throws Exception{
        id = login_id;
        pw = login_pw;

        prepareParams(0,null);

        mNetwork = new LoginNetwork();
        mNetwork.execute(0);
    }

    private void prepareParams(int index,String s) throws Exception{

        String formName = "";
        if(params == null){
            params = new HashMap<>();
        }else{
            params.clear();
        }

        switch (index){
            case 0:
                break;
            case 1:
                params.put("ssoGubun","Login");
                return;
            case 2:
                formName = "frmSSO";
                break;
            case 3:
                formName = "ssoLoginForm";
                break;
            case 4:
                formName = "ssoLoginForm";
                break;
            case 5:
                formName = "ssoLoginForm";
                break;
            case 6:
                return;
            default:
                return;
        }

        if(s == null)
            return;

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
        }
    }

    private void processStep0(String s) throws Exception{
        prepareParams(1,s);
        mNetwork = new LoginNetwork();
        mNetwork.execute(1);
    }
    private void processStep1(String s) throws Exception{
        prepareParams(2,s);
        mNetwork = new LoginNetwork();
        mNetwork.execute(2);
    }
    private void processStep2(String s) throws Exception{
        prepareParams(3, s);
        ssoChallenge = params.get("ssoChallenge");
        keyModulus = params.get("keyModulus");
        mNetwork = new LoginNetwork();
        mNetwork.execute(3);
    }

    private void processStep3(String s) throws Exception{
        prepareParams(4, s);
        String user_id = id;
        String user_pw = pw;
        String PUB = keyModulus;
        String PVK = ssoChallenge;

        String html;
        html = "<html><head>" +
                "<script src='jsbn.js' type='text/javascript'></script>" +
                "<script src='prng4.js' type='text/javascript'></script>" +
                "<script src='prototype.js' type='text/javascript'></script>" +
                "<script src='rng.js' type='text/javascript'></script>" +
                "<script src='rsa.js' type='text/javascript'></script>" +
                "<script src='sha256.js' type='text/javascript'></script>" +
                "</head><body>" +
                "<script type='text/javascript'>" +
                "var username = '"+ user_id +"';" +
                "var password = '"+ user_pw +"';" +
                "var ssoChallenge = '"+ PVK +"';" +
                "var jsonObj = {'userid':username, 'userpw':password, 'ssoChallenge':ssoChallenge};\n" +
                "var jsonStr = Object.toJSON( jsonObj );" +
                "var rsa = new RSAKey();\n" +
                "rsa.setPublic( '"+PUB+"', '10001' );\n" +
                "window.YSCEC.sendNext(rsa.encrypt(jsonStr));" +
                "</script>" +
                "</body></html>";

        WebView webView = new WebView(mContext);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new AndroidBridge(), "YSCEC");
        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);

    }
    private void processStep4(String s) throws Exception{
        prepareParams(5, s);
        mNetwork = new LoginNetwork();
        mNetwork.execute(5);
    }
    private void processStep5(String s) throws Exception{
        prepareParams(6,s);
        mNetwork = new LoginNetwork();
        mNetwork.execute(6);
    }
    private void processStep6(String s) throws Exception{
        if(!s.startsWith("http")){
            mHandler.sendEmptyMessage(FAILED_LOGIN);
            return;
        }
        url_final = s;
        mNetwork = new LoginNetwork();
        mNetwork.execute(7);
    }
    private void processStep7(String s) throws Exception{
        if(!s.startsWith("http") || !s.contains("/my")){
            mHandler.sendEmptyMessage(FAILED_LOGIN);
            return;
        }
        url_final2 = url_final;
        url_final = s;
        mNetwork = new LoginNetwork();
        mNetwork.execute(8);
    }
    private void processStep8(String s) throws Exception{

        Document doc = Jsoup.parse(s);
        Element names = doc.getElementsByClass("mymenu").first();
        String name = names.children().first().ownText();
        YSCEC.setLogin_name(name);

        Message message = Message.obtain();
        message.what = SUCCESS_LOGIN;
        if(final_cookie.equals(""))
            message.what = FAILED_LOGIN;

        if(message.what == SUCCESS_LOGIN){
            if(!setHyHg(doc,id)){
                message.what = FAILED_LOGIN;
            }
        }

        message.obj = final_cookie;
        mHandler.sendMessage(message);
    }

    private boolean setHyHg(Document doc,String login_id) throws Exception{
        SharedPreferences preferences = mContext.getSharedPreferences(CommonValues.PREF_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String hy = doc.select("#year option[selected]").text();
        String hg_tmp = doc.select("#term option[selected]").text();
        Pattern pattern = Pattern.compile("[0-9]");
        Matcher matcher = pattern.matcher(hg_tmp);
        String hg = "";
        if(matcher.find()){
            hg = matcher.group();
        }

        String finalString = hy+hg;
        if(finalString.equals("") || finalString.length() != 5){
            return false;
        }
        editor.putString(CommonValues.PREF_KEY_HYHG_PREFIX + login_id, finalString);
        editor.apply();
        return true;
    }

    public static String getPostParamString(HashMap<String, String> params) throws Exception{
        if(params.size() == 0)
            return "";

        StringBuilder buf = new StringBuilder();
        for(Map.Entry<String,String> entry : params.entrySet()) {
            buf.append(buf.length() == 0 ? "" : "&");
            buf.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return buf.toString();
    }

    private class LoginNetwork extends AsyncTask<Integer,String,String>{

        private Integer nowStep = -1;
        private String nowURL = null;

        @Override
        protected String doInBackground(Integer... indexes) {

            this.nowStep = indexes[0];
            if(nowStep >= 7)
                this.nowURL = url_final;
            else
                this.nowURL = urls[this.nowStep];

            String fin = "";
            HttpURLConnection connection = null;

            try{
                URL url = new URL(nowURL);

                connection = (HttpURLConnection)url.openConnection();
                String method = CommonValues.STRING_POST;
                if(this.nowStep >= 6)
                    method = CommonValues.STRING_GET;
                connection.setReadTimeout(5000);
                connection.setConnectTimeout(5000);
                connection.setRequestMethod(method);
                connection.setInstanceFollowRedirects(false);
                connection.setDoInput(true);
                if(this.nowStep < 6)
                    connection.setDoOutput(true);
                connection.setRequestProperty(CommonValues.STRING_USER_AGENT,USER_AGENT);

                if(this.nowStep > 0 && this.nowStep != 8)
                    connection.setRequestProperty(CommonValues.STRING_REFERER,urls[this.nowStep-1]);
                else if(this.nowStep == 8)
                    connection.setRequestProperty(CommonValues.STRING_REFERER,url_final2);

                String cookieString = "";
                if(cookies.getCookieStore().getCookies().size()>0 && nowStep != 6)
                    cookieString = TextUtils.join(";",cookies.getCookieStore().getCookies());
                else if(nowStep >= 6){
                    for(HttpCookie c : cookies.getCookieStore().getCookies()){
                        if(c.getName().equals(CommonValues.STRING_COOKIE_PREFIX)){
                            cookieString = c.toString();
                            break;
                        }
                    }
                }
                connection.setRequestProperty(CommonValues.STRING_COOKIE,cookieString);

                if(params == null)
                    return null;

                String paramString = getPostParamString(params);

                if(this.nowStep < 6) {
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
                    for(String s : lString) {
                        HttpCookie httpCookie = HttpCookie.parse(s).get(0);
                        cookies.getCookieStore().add(null, httpCookie);
                        if(this.nowStep == 2 && httpCookie.getName().equals(CommonValues.STRING_JSESSIONID_SSO)){
                            YSCEC.setInfra_JSESSIONID(httpCookie.getValue());
                        }
                        if(httpCookie.getName().equals(CommonValues.STRING_COOKIE_PREFIX))
                            final_cookie = httpCookie.getValue();
                    }
                }
                if(this.nowStep >= 6 && iMap.containsKey(CommonValues.STRING_LOCATION)){
                    fin = iMap.get(CommonValues.STRING_LOCATION).get(0);
                }

            }catch (Exception e){
                e.printStackTrace();
                mHandler.sendEmptyMessage(FAILED_LOGIN);
                return null;
            }finally {
                if(connection != null)
                    connection.disconnect();
            }
            return fin;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s == null || s.equals("")){
                mHandler.sendEmptyMessage(FAILED_LOGIN);
                return;
            }
            try{
                switch (this.nowStep){
                    case 0:
                        processStep0(s);
                        break;
                    case 1:
                        processStep1(s);
                        break;
                    case 2:
                        processStep2(s);
                        break;
                    case 3:
                        processStep3(s);
                        break;
                    case 4:
                        processStep4(s);
                        break;
                    case 5:
                        processStep5(s);
                        break;
                    case 6:
                        processStep6(s);
                        break;
                    case 7:
                        processStep7(s);
                        break;
                    case 8:
                        processStep8(s);
                    default:
                }
            }catch (Exception e){
                e.printStackTrace();
                mHandler.sendEmptyMessage(FAILED_LOGIN);
            }
        }
    }

    private class AndroidBridge {
        public AndroidBridge(){

        }
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void sendNext(final String arg) { // must be final
            new Handler().post(new Runnable() {
                public void run() {
                    params.put("username","");
                    params.put("password","");
                    params.put("E2",arg);
                    mNetwork = new LoginNetwork();
                    mNetwork.execute(4);
                }
            });
        }
    }
}
