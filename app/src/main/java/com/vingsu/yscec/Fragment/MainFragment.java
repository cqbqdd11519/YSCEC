package com.vingsu.yscec.Fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vingsu.yscec.Activity.MainActivity;
import com.vingsu.yscec.Activity.SettingActivity;
import com.vingsu.yscec.CommonFunctions;
import com.vingsu.yscec.Container.Article;
import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.DatabaseHelper;
import com.vingsu.yscec.Container.Notice;
import com.vingsu.yscec.R;
import com.vingsu.yscec.Widget.TouchBlackHoleProgress;
import com.vingsu.yscec.YSCEC;
import com.vingsu.yscec.Network.ParseMainProcess;

import java.util.ArrayList;
import java.util.TreeSet;

public class MainFragment extends BaseFragment {

    private static MainHandler mainHandler;
    private static ArrayList<Notice> notices;
    private static NoticeAdapter mAdapter;
    private static ExpandableListView listView;
    private static SwipeRefreshLayout refreshLayout;
    private static TouchBlackHoleProgress progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainHandler = new MainHandler();
        notices = new ArrayList<>();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        invokeNowViewing();

        if(YSCEC.getLogin_cookie() == null || YSCEC.getLogin_cookie().equals("")){
            Toast.makeText(mContext,R.string.error,Toast.LENGTH_SHORT).show();
            ((Activity)mContext).finish();
            return rootView;
        }

        ((MainActivity)mContext).lockDrawer(false);

        TextView textView = (TextView) rootView.findViewById(R.id.year_term);
        SharedPreferences pref_login = mContext.getSharedPreferences(CommonValues.PREF_LOGIN, Context.MODE_PRIVATE);
        String login_id = pref_login.getString(CommonValues.PREF_KEY_LOGIN_ID, "");
        String hyhg = pref_login.getString(CommonValues.PREF_KEY_HYHG_PREFIX + login_id, "");
        String hyhg_final = hyhg;
        if(hyhg.length() == 5){
            String year = hyhg.substring(0,4);
            String term = hyhg.substring(4);
            hyhg_final = year+mContext.getText(R.string.year_text)+" "+term+mContext.getText(R.string.term_text);
        }
        textView.setText(hyhg_final);

        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
        refreshLayout.setRefreshing(false);
        refreshLayout.setEnabled(false);
        refreshLayout.setColorSchemeResources(R.color.theme_color);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ParseMainProcess parser = new ParseMainProcess(mContext,mainHandler);
                try{
                    parser.parse();
                }catch (Exception e){
                    e.printStackTrace();
                    mainHandler.sendEmptyMessage(ParseMainProcess.FAILED_MAIN);
                }
            }
        });

        listView = (ExpandableListView) rootView.findViewById(R.id.notice_list);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int topRowVerticalPosition = (absListView == null || absListView.getChildCount() == 0) ? 0 : absListView.getChildAt(0).getTop();
                refreshLayout.setEnabled(i == 0 && topRowVerticalPosition >= 0);
            }
        });
        mAdapter = new NoticeAdapter(mContext,notices);
        listView.setAdapter(mAdapter);
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                String courseId = ((TextView)view.findViewById(R.id.notice_group_id)).getText().toString();
                Bundle bundle = new Bundle();
                bundle.putString(CommonValues.PARAM_COURSE_ID,courseId);
                ((MainActivity)getActivity()).openCourseMain(bundle);
                return true;
            }
        });
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                TextView full_url_v = (TextView) view.findViewById(R.id.notice_full_url_hidden);
                TextView course_id_v = (TextView) view.findViewById(R.id.notice_course_id_hidden);
                TextView b_id_v = (TextView) view.findViewById(R.id.notice_b_id_hidden);
                TextView id_v = (TextView) view.findViewById(R.id.notice_id_hidden);
                notices.get(i).getArticles().get(i1).setArticleRead(1);
                mAdapter.notifyDataSetChanged();
                Bundle bundle = new Bundle();
                bundle.putString(CommonValues.PARAM_FULL_URL, full_url_v.getText().toString());
                bundle.putString(CommonValues.PARAM_COURSE_ID, course_id_v.getText().toString());
                bundle.putString(CommonValues.PARAM_B_ID, b_id_v.getText().toString());
                bundle.putString(CommonValues.PARAM_ID, id_v.getText().toString());
                Fragment contentFragment = new ContentFragment();
                contentFragment.setArguments(bundle);
                MainActivity.addFragment(R.id.container, contentFragment, CommonValues.TAG_CONTENT);
                return false;
            }
        });

        String noti_full_url = MainActivity.noti_full_url;
        String noti_course_id = MainActivity.noti_course_id;
        String noti_b_id = MainActivity.noti_b_id;
        String noti_id = MainActivity.noti_id;
        if(noti_course_id != null && noti_b_id != null && noti_id != null){
            Bundle bundle = new Bundle();
            bundle.putString(CommonValues.PARAM_FULL_URL, noti_full_url);
            bundle.putString(CommonValues.PARAM_COURSE_ID, noti_course_id);
            bundle.putString(CommonValues.PARAM_B_ID, noti_b_id);
            bundle.putString(CommonValues.PARAM_ID, noti_id);
            Fragment contentFragment = new ContentFragment();
            contentFragment.setArguments(bundle);
            MainActivity.addFragment(R.id.container, contentFragment, CommonValues.TAG_CONTENT);
        }

        progressBar = (TouchBlackHoleProgress) rootView.findViewById(R.id.main_progress);
        progressBar.setVisibility(View.VISIBLE);
        ParseMainProcess parser = new ParseMainProcess(mContext,mainHandler);
        try{
            parser.parse();
        }catch (Exception e){
            e.printStackTrace();
            mainHandler.sendEmptyMessage(ParseMainProcess.FAILED_MAIN);
        }

        return rootView;
    }

    @Override
    public void invokeNowViewing() {
        ActionBar actionBar = ((MainActivity)mContext).getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            ((MainActivity)mContext).syncToggleState();
        }
        this.setTitle(getString(R.string.course_notice));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent go_setting = new Intent(mContext, SettingActivity.class);
                mContext.startActivity(go_setting);
                break;
            case R.id.action_logout:
                YSCEC.setLogin_cookie(null);
                SharedPreferences preferences = mContext.getSharedPreferences(CommonValues.PREF_LOGIN, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(CommonValues.PREF_KEY_AUTO_LOGIN,false);
                editor.apply();
                MainActivity.addFragment(R.id.container, new LoginFragment(), CommonValues.TAG_LOGIN);
                break;
            default:
        }
        return false;
    }

    private class NoticeAdapter extends BaseExpandableListAdapter {

        private ArrayList<Notice> notices;
        private LayoutInflater inflater;
        public NoticeAdapter(Context c,ArrayList<Notice> _notices){
            notices = _notices;
            inflater = LayoutInflater.from(c);
        }
        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            for(int i=0;i<getGroupCount();i++)
                listView.expandGroup(i);
        }
        @Override
        public Object getGroup(int i) {
            return notices.get(i);
        }
        @Override
        public long getGroupId(int i) {
            return i;
        }
        @Override
        public int getGroupCount() {
            return notices.size();
        }
        @Override
        public boolean hasStableIds() {
            return true;
        }
        @Override
        public Object getChild(int i, int i1) {
            return notices.get(i).getArticles().get(i1);
        }
        @Override
        public long getChildId(int i, int i1) {
            return 0;
        }
        @Override
        public int getChildrenCount(int i) {
            return notices.get(i).getArticles().size();
        }
        @Override
        public boolean isChildSelectable(int i, int i1) {
            return true;
        }
        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
            Notice notice = (Notice) getGroup(i);
            GroupViewHolder viewHolder;
            if(view == null){
                view = inflater.inflate(R.layout.notice_group_row,viewGroup,false);
                viewHolder = new GroupViewHolder();
                viewHolder.container = view;
                viewHolder.groupTitle = (TextView)view.findViewById(R.id.notice_group_title);
                viewHolder.groupId = (TextView)view.findViewById(R.id.notice_group_id);

                view.setTag(viewHolder);
            }else{
                viewHolder = (GroupViewHolder) view.getTag();
            }

            TextView groupTitle = viewHolder.groupTitle;
            groupTitle.setText(notice.getTitle());
            TextView groupId = viewHolder.groupId;
            groupId.setText(notice.getCourseId());
            return view;
        }
        @Override
        public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
            Article article = (Article)getChild(i,i1);
            ChildViewHolder viewHolder;
            if(view == null){
                view = inflater.inflate(R.layout.notice_row,viewGroup,false);
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

    private static class GroupViewHolder{
        public View container;
        public TextView groupTitle;
        public TextView groupId;
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

    private static class MainHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ParseMainProcess.SUCCESS_MAIN:
                    DatabaseHelper helper = new DatabaseHelper(mContext);
                    ArrayList<Notice> __notices = helper.getNoticeData();
                    notices.clear();
                    for(Notice n : __notices){
                        notices.add(n);
                    }
                    mAdapter.notifyDataSetChanged();
                    NavigationDrawerFragment.setContent();
                    progressBar.setVisibility(View.GONE);
                    SharedPreferences preferences = mContext.getSharedPreferences(CommonValues.PREF_NOTIFICATION,Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putStringSet(CommonValues.PREF_KEY_UNREAD_NOTIFICATION,new TreeSet<String>());
                    editor.apply();
                    CommonFunctions.setBadge(mContext, 0);
                    break;
                case ParseMainProcess.FAILED_MAIN:
                    break;
                default:
            }
            refreshLayout.setRefreshing(false);
        }
    }

}
