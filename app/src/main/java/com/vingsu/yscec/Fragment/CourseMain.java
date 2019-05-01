package com.vingsu.yscec.Fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vingsu.yscec.Activity.MainActivity;
import com.vingsu.yscec.Activity.WebViewActivity;
import com.vingsu.yscec.Container.Article;
import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.Container.CourseMainContainer;
import com.vingsu.yscec.DatabaseHelper;
import com.vingsu.yscec.Container.Notice;
import com.vingsu.yscec.Network.ParseCourseMain;
import com.vingsu.yscec.R;
import com.vingsu.yscec.Widget.TouchBlackHoleProgress;

import java.util.ArrayList;

public class CourseMain extends BaseFragment {

    private static CourseListAdapter mAdapter;
    private static ArrayList<Article> mData;

    private static ListView mListView;
    private static TextView noArticle;
    private static Button headerPlan;
    private static LinearLayout headerTarget;
    private static TouchBlackHoleProgress mProgress;

    private String courseId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mData = new ArrayList<>();
        mAdapter = new CourseListAdapter(mContext,R.layout.notice_row,mData);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_article_list, container, false);

        Bundle bundle = getArguments();
        courseId = bundle.getString(CommonValues.PARAM_COURSE_ID);

        invokeNowViewing();

        mListView = (ListView) rootView.findViewById(R.id.course_article_list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView full_url_v = (TextView) view.findViewById(R.id.notice_full_url_hidden);
                TextView course_id_v = (TextView) view.findViewById(R.id.notice_course_id_hidden);
                TextView b_id_v = (TextView) view.findViewById(R.id.notice_b_id_hidden);
                TextView id_v = (TextView) view.findViewById(R.id.notice_id_hidden);
                mData.get(i-1).setArticleRead(1);
                mAdapter.notifyDataSetChanged();
                Bundle bundle = new Bundle();
                bundle.putString(CommonValues.PARAM_FULL_URL, full_url_v.getText().toString());
                bundle.putString(CommonValues.PARAM_COURSE_ID, course_id_v.getText().toString());
                bundle.putString(CommonValues.PARAM_B_ID, b_id_v.getText().toString());
                bundle.putString(CommonValues.PARAM_ID, id_v.getText().toString());
                Fragment contentFragment = new ContentFragment();
                contentFragment.setArguments(bundle);
                MainActivity.addFragment(R.id.container, contentFragment, CommonValues.TAG_CONTENT);
            }
        });
        View headerView = inflater.inflate(R.layout.course_header,null,false);
        headerPlan = (Button) headerView.findViewById(R.id.course_header_plan);
        headerTarget = (LinearLayout) headerView.findViewById(R.id.course_header_prof_target);
        mProgress = (TouchBlackHoleProgress) rootView.findViewById(R.id.course_main_progress);
        mListView.addHeaderView(headerView);
        mListView.setAdapter(mAdapter);

        ParseCourseMain parser = new ParseCourseMain(mContext,new CourseMainHandler());
        parser.parse(courseId);

        DatabaseHelper helper = new DatabaseHelper(mContext);
        Notice notice = helper.getCourseNoticeData(courseId,false);
        helper.closeDB();
        mData.clear();
        if(notice.getArticles().size() > 0) {
            for (Article a : notice.getArticles()) {
                mData.add(a);
            }
            mAdapter.notifyDataSetChanged();
        }
        noArticle = (TextView) rootView.findViewById(R.id.no_list_tv);
        return rootView;
    }

    @Override
    public void invokeNowViewing() {
        ActionBar actionBar = ((MainActivity) mContext).getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        this.setTitle(DatabaseHelper.getCourseNameById(mContext, courseId));
    }

    private class CourseListAdapter extends ArrayAdapter<Article>{
        ArrayList<Article> list;
        public CourseListAdapter(Context context, int textViewResourceId,ArrayList<Article> items){
            super(context,textViewResourceId,items);
            this.list = items;
        }
        @Override
        public Article getItem(int position) {
            return list.get(position);
        }
        @Override
        public int getCount() {
            return list.size();
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            Article article = getItem(position);
            ChildViewHolder viewHolder;
            if(view == null){
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.notice_row,parent,false);
                viewHolder = new ChildViewHolder();

                viewHolder.container = view;
                viewHolder.articleTitle = (TextView)view.findViewById(R.id.notice_row_title);
                viewHolder.articleType = (TextView)view.findViewById(R.id.notice_row_type);
                viewHolder.articleId = (TextView)view.findViewById(R.id.notice_id_hidden);
                viewHolder.articleBId = (TextView)view.findViewById(R.id.notice_b_id_hidden);
                viewHolder.articleCourseId = (TextView)view.findViewById(R.id.notice_course_id_hidden);
                viewHolder.articleFullUrl = (TextView)view.findViewById(R.id.notice_full_url_hidden);

                view.setTag(viewHolder);
            }else{
                viewHolder = (ChildViewHolder) view.getTag();
            }
            TextView articleTitle = viewHolder.articleTitle;
            articleTitle.setText(article.getArticleName());
            TextView articleType = viewHolder.articleType;
            articleType.setText(article.getArticleType());

            TextView articleId = viewHolder.articleId;
            TextView articleBId = viewHolder.articleBId;
            TextView articleCourseId = viewHolder.articleCourseId;
            TextView articleFullUrl = viewHolder.articleFullUrl;

            articleId.setText(article.getArticleId());
            articleBId.setText(article.getArticleBoardId());
            articleCourseId.setText(article.getArticleCourseId());
            articleFullUrl.setText(article.getArticleURL());

            if(article.getArticleRead() == 0)
                view.setAlpha(1f);
            else
                view.setAlpha(0.3f);
            return view;
        }
    }

    private static class ChildViewHolder{
        public View container;
        public TextView articleTitle;
        public TextView articleType;
        public TextView articleId;
        public TextView articleBId;
        public TextView articleCourseId;
        public TextView articleFullUrl;
    }

    private static class CourseMainHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ParseCourseMain.SUCCESS_COURSE_MAIN:
                    final CourseMainContainer container = (CourseMainContainer) msg.obj;
                    if(container.getPlanUrl().equals("")){
                        headerPlan.setVisibility(View.GONE);
                    }
                    headerPlan.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, WebViewActivity.class);
                            intent.putExtra(CommonValues.PARAM_URL, container.getPlanUrl());
                            intent.putExtra(CommonValues.PARAM_TITLE, container.getTitle());
                            mContext.startActivity(intent);
                        }
                    });
                    for(int i=0;i<container.getProfInfo().size();i++){
                        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        Button button = (Button) inflater.inflate(R.layout.styled_button,null,false);
                        button.setTransformationMethod(null);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        button.setText(container.getProfInfo().get(i));
                        final int ii = i;
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String mail = container.getProfInfoMail().get(ii);
                                if(mail.trim().equals("")){
                                    Toast.makeText(mContext,R.string.no_mail_address,Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                try {
                                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + mail));
                                    mContext.startActivity(intent);
                                }catch (ActivityNotFoundException e){
                                    Toast.makeText(mContext,R.string.no_activity_to_mail,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        params.setMargins(0, 0, 0, 0);
                        headerTarget.addView(button,params);
                    }
                    mListView.setVisibility(View.VISIBLE);
                    break;
                case ParseCourseMain.FAILED_COURSE_MAIN:
                    break;
                default:
            }
            mProgress.setVisibility(View.GONE);
            if(mData.size() < 1){
                noArticle.setVisibility(View.VISIBLE);
            }
        }
    }
}
