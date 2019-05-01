package com.vingsu.yscec.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.vingsu.yscec.Activity.MainActivity;
import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.Container.Content;
import com.vingsu.yscec.DatabaseHelper;
import com.vingsu.yscec.Network.ParseContentProcess;
import com.vingsu.yscec.R;
import com.vingsu.yscec.Widget.TouchBlackHoleProgress;
import com.vingsu.yscec.YSCEC;

import java.util.ArrayList;

@SuppressLint("SetJavaScriptEnabled")
public class ContentFragment extends BaseFragment {

    private static final int READ_REQUEST_CODE = 18;

    private static TextView failed_v;
    private static TextView title_v;
    private static TextView others_v;
    private static WebView html_v;
    private static FrameLayout webViewTarget;
    private static LinearLayout attach_v;
    private static ScrollView content_v;
    private static TouchBlackHoleProgress progress_v;

    private static String full_url;
    private static String course_id;
    private static String b_id;
    private static String id;

    private static ArticleHandler handler = new ArticleHandler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_content, container, false);

        Bundle bundle = getArguments();
        full_url = bundle.getString(CommonValues.PARAM_FULL_URL);
        course_id = bundle.getString(CommonValues.PARAM_COURSE_ID);
        b_id = bundle.getString(CommonValues.PARAM_B_ID);
        id = bundle.getString(CommonValues.PARAM_ID);

        DatabaseHelper helper = new DatabaseHelper(mContext);
        helper.setArticleRead(b_id, id);
        helper.closeDB();

        invokeNowViewing();

        failed_v = (TextView) rootView.findViewById(R.id.content_failed_text);
        title_v = (TextView) rootView.findViewById(R.id.article_title);
        others_v = (TextView) rootView.findViewById(R.id.article_others);
        webViewTarget = (FrameLayout) rootView.findViewById(R.id.article_web_view_target);
        html_v = new WebView(mContext);
        html_v.setInitialScale(0);
        html_v.setHorizontalScrollBarEnabled(false);
        html_v.setVerticalScrollBarEnabled(false);
        html_v.setWebChromeClient(new WebChromeClient());
        html_v.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }catch (ActivityNotFoundException e){
                    Toast.makeText(mContext,R.string.no_activity_to_url,Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        html_v.getSettings().setSupportZoom(false);
        html_v.getSettings().setJavaScriptEnabled(true);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, 0);
        webViewTarget.addView(html_v,layoutParams);
        attach_v = (LinearLayout) rootView.findViewById(R.id.article_attach_area);
        content_v = (ScrollView) rootView.findViewById(R.id.content_container);
        content_v.setVisibility(View.GONE);

        progress_v = (TouchBlackHoleProgress) rootView.findViewById(R.id.content_progress);
        progress_v.setVisibility(View.VISIBLE);

        if(b_id.contains(CommonValues.STRING_DUMMY)){
            failed_v.setText(Html.fromHtml(getText(R.string.only_in_web)+"\n<br/>\n<br/><a href='"+full_url+"'>브라우저로 보기</a>"));
            failed_v.setMovementMethod(LinkMovementMethod.getInstance());
            failed_v.setVisibility(View.VISIBLE);
            progress_v.setVisibility(View.GONE);
        }else {
            ParseContentProcess parser = new ParseContentProcess(mContext, handler);
            parser.parse(b_id, id);
        }
        return rootView;
    }

    @Override
    public void invokeNowViewing() {
        ActionBar actionBar = ((MainActivity) mContext).getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        this.setTitle(DatabaseHelper.getCourseNameById(mContext, course_id));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        html_v.loadUrl("about:blank;");
        html_v.clearCache(true);
        html_v.clearFormData();
        html_v.clearHistory();
        html_v.clearMatches();
        html_v.clearSslPreferences();
        webViewTarget.removeAllViews();
        html_v.destroy();
    }

    private static void downloadAttachment(String title,String url){
        Toast.makeText(mContext, "'" + title + "' "+mContext.getText(R.string.start_download), Toast.LENGTH_SHORT).show();
        String cookie = YSCEC.getLogin_cookie();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.addRequestHeader(CommonValues.STRING_COOKIE,CommonValues.STRING_COOKIE_PREFIX+"="+cookie);
        request.addRequestHeader(CommonValues.STRING_REFERER, "https://yscec.yonsei.ac.kr/my/");
        request.setTitle(title);
        request.setDescription(url);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        String[] split_string = url.split("/");
        String fileName = split_string[split_string.length-1];

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        long id = manager.enqueue(request);
    }

    private static class ArticleHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ParseContentProcess.SUCCESS_CONTENT:
                    Content content = (Content) msg.obj;
                    title_v.setText(content.getTitle());
                    String others_t = content.getWriter()+" "+content.getOthers()+ " Read : "+content.getRead();
                    others_v.setText(others_t);
                    html_v.loadDataWithBaseURL("https://yscec.yonsei.ac.kr/my",content.getHtml(),"text/html","utf-8",null);
                    ArrayList<String> attachment = content.getAttachment();
                    ArrayList<String> attachment_url = content.getAttachment_url();
                    for(int i=0;i<attachment.size();i++){
                        final String atc_url = attachment_url.get(i);
                        final String atc_txt = attachment.get(i);
                        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        Button button = (Button) inflater.inflate(R.layout.styled_button2,null,false);
                        button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        button.setText(attachment.get(i));
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                                    ActivityCompat.requestPermissions((Activity)mContext, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_REQUEST_CODE);
                                }else{
                                    downloadAttachment(atc_txt,atc_url);
                                }
                            }
                        });
                        attach_v.addView(button);
                    }
                    content_v.setVisibility(View.VISIBLE);
                    progress_v.setVisibility(View.GONE);
                    break;
                case ParseContentProcess.FAILED_CONTENT:
                    failed_v.setText(mContext.getText(R.string.cant_load_content));
                    failed_v.setVisibility(View.VISIBLE);
                    progress_v.setVisibility(View.GONE);
                    break;
                default:
            }
        }
    }
}
