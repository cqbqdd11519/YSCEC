package com.vingsu.yscec.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vingsu.yscec.Activity.MainActivity;
import com.vingsu.yscec.Activity.WebViewActivity;
import com.vingsu.yscec.CommonFunctions;
import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.Network.ParseTimeTableProcess;
import com.vingsu.yscec.R;
import com.vingsu.yscec.Container.TimeTable;
import com.vingsu.yscec.Container.TimeTableCourse;
import com.vingsu.yscec.Widget.TimeTableItem;
import com.vingsu.yscec.Widget.TimeTableText;
import com.vingsu.yscec.Widget.TouchBlackHoleProgress;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class TimeTableFragment extends BaseFragment {

    private static Handler mHandler;

    private static GridLayout timeTableContainer;
    private static View.OnClickListener buttonClickListener;
    private static TouchBlackHoleProgress progress;

    private static int[] tt_colors;
    private static HashMap<String,Integer> colorHashMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new TimeTableHandler();
        buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimeTableCourse course = (TimeTableCourse) view.getTag();
                SharedPreferences preferences = mContext.getSharedPreferences(CommonValues.PREF_LOGIN, Context.MODE_PRIVATE);
                String login_id = preferences.getString(CommonValues.PREF_KEY_LOGIN_ID,"");
                String login_hyhg = preferences.getString(CommonValues.PREF_KEY_HYHG_PREFIX + login_id, "");
                String hy = login_hyhg.substring(0,4);
                String hg = login_hyhg.substring(4);
                String url = "https://ysweb.yonsei.ac.kr:8888/curri120601/curri_pop2.jsp?&hakno="+ course.getCourseId1()+
                        "&bb="+ course.getCourseId2() +
                        "&sbb="+ course.getCourseId3() +
                        "&domain=H1" +
                        "&startyy="+ hy +
                        "&hakgi=" + hg;
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(CommonValues.PARAM_URL,url);
                intent.putExtra(CommonValues.PARAM_TITLE,course.getCourseName());
                getActivity().startActivity(intent);
            }
        };
        tt_colors = new int[]{
                ContextCompat.getColor(mContext,R.color.timetable_1),
                ContextCompat.getColor(mContext,R.color.timetable_2),
                ContextCompat.getColor(mContext,R.color.timetable_3),
                ContextCompat.getColor(mContext,R.color.timetable_4),
                ContextCompat.getColor(mContext,R.color.timetable_5),
                ContextCompat.getColor(mContext,R.color.timetable_6),
                ContextCompat.getColor(mContext,R.color.timetable_7),
                ContextCompat.getColor(mContext,R.color.timetable_8)
        };
        colorHashMap = new HashMap<>();
    }

    @Override
    public void invokeNowViewing() {
        ActionBar actionBar = ((MainActivity) mContext).getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        this.setTitle(getString(R.string.timetable_text));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_time_table, container, false);
        invokeNowViewing();

        ActionBar actionBar = ((MainActivity) mContext).getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.timetable_text));
        }

        timeTableContainer = (GridLayout) rootView.findViewById(R.id.timetable_container);
        timeTableContainer.setUseDefaultMargins(false);
        timeTableContainer.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        progress = (TouchBlackHoleProgress) rootView.findViewById(R.id.tt_progress);
        progress.setVisibility(View.VISIBLE);
        ParseTimeTableProcess process = new ParseTimeTableProcess(mContext,mHandler);
        process.parse();

        return rootView;
    }

    private static void updateTimeTable(TimeTable timeTable){
        timeTableContainer.removeAllViews();
        int px_1dp =  (int) CommonFunctions.dpToPixel(mContext, 1);
        int columnCnt = 6;
        int headHeight = (timeTableContainer.getHeight() - px_1dp) / 15;
        int headWidth = (timeTableContainer.getWidth() - px_1dp) / 15;
        int rowCnt = timeTable.maxTime + 1;
        int rowHeight = (timeTableContainer.getHeight() - px_1dp - headHeight) / (rowCnt-1);
        int columnWidth = (timeTableContainer.getWidth() - px_1dp - headWidth) / (columnCnt - 1);

        timeTableContainer.setColumnCount(columnCnt);
        timeTableContainer.setRowCount(rowCnt);

        TextView tv1 = new TextView(mContext);
        tv1.setText("");
        tv1.setBackgroundColor(mContext.getResources().getColor(R.color.background_color));
        tv1.setGravity(Gravity.CENTER);
        GridLayout.LayoutParams lp1 = new GridLayout.LayoutParams();
        lp1.width = headWidth;
        lp1.height = headHeight;
        lp1.rowSpec = GridLayout.spec(0);
        lp1.columnSpec = GridLayout.spec(0);
        timeTableContainer.addView(tv1, lp1);
        int index = 0;
        for(int i=0;i<=4;i++){
            TextView tv = new TextView(mContext);
            tv.setBackgroundColor(mContext.getResources().getColor(R.color.background_color));
            String day = "";
            switch (i){
                case 0:
                    day = mContext.getString(R.string.day_mon);
                    break;
                case 1:
                    day = mContext.getString(R.string.day_tues);
                    break;
                case 2:
                    day = mContext.getString(R.string.day_wednes);
                    break;
                case 3:
                    day = mContext.getString(R.string.day_thurs);
                    break;
                case 4:
                    day = mContext.getString(R.string.day_fri);
                    break;
                default:
            }
            tv.setText(day);
            tv.setGravity(Gravity.CENTER);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = columnWidth - px_1dp;
            lp.height = headHeight;
            lp.leftMargin = px_1dp;
            lp.rowSpec = GridLayout.spec(0);
            lp.columnSpec = GridLayout.spec(++index);
            timeTableContainer.addView(tv,lp);
        }

        int row = 1;
        for(int time = 2;time <= timeTable.maxTime+1;time++,row++){
            TimeTableText textView =new TimeTableText(mContext);
            textView.setBackgroundColor(mContext.getResources().getColor(R.color.background_color));
            String info_txt = ""+(time+7);
            textView.setText(info_txt);
            textView.setGravity(Gravity.RIGHT|Gravity.TOP);
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = headWidth;
            layoutParams.height = rowHeight - px_1dp;
            layoutParams.topMargin = px_1dp;
            layoutParams.rowSpec = GridLayout.spec(row);
            layoutParams.columnSpec = GridLayout.spec(0);
            timeTableContainer.addView(textView, layoutParams);
            for(int col = 0; col <= 4; col++){
                TimeTableCourse course = timeTable.getCourse(col,time);
                if(course == null || course.getCourseName().equals("")){
                    TextView tv = new TextView(mContext);
                    tv.setText("");
                    tv.setBackgroundColor(mContext.getResources().getColor(R.color.background_color));
                    GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                    lp.width = columnWidth - px_1dp;
                    lp.height = rowHeight - px_1dp;
                    lp.leftMargin = px_1dp;
                    lp.topMargin = px_1dp;
                    lp.rowSpec = GridLayout.spec(row,GridLayout.FILL);
                    lp.columnSpec = GridLayout.spec(col+1);
                    timeTableContainer.addView(tv, lp);
                    continue;
                }
                if(time >= 1){
                    TimeTableCourse prev = timeTable.getCourse(col,time-1);
                    if(prev != null && prev.equals(course))
                        continue;
                }

                int cnt = 1;
                if(time < timeTable.maxTime){
                    for(int tmp = time+1;tmp <= TimeTable.lastTime;tmp++){
                        TimeTableCourse temp = timeTable.getCourse(col,tmp);
                        if(temp != null && temp.equals(course)){
                            cnt++;
                        }else
                            break;
                    }
                }
                String fullCourseId = course.getCourseId1()+"-"+course.getCourseId2()+"-"+course.getCourseId3();
                TextView tv = new TextView(mContext);
                tv.setTextColor(Color.WHITE);
                tv.setGravity(Gravity.TOP | Gravity.LEFT);
                tv.setTag(course);
                if(colorHashMap.containsKey(fullCourseId)){
                    tv.setBackground(new ColorDrawable(colorHashMap.get(fullCourseId)));
                }else{
                    int color_index = colorHashMap.size() % tt_colors.length;
                    tv.setBackground(new ColorDrawable(tt_colors[color_index]));
                    colorHashMap.put(fullCourseId,tt_colors[color_index]);
                }
                tv.setOnClickListener(buttonClickListener);
                int padding = (int) CommonFunctions.dpToPixel(mContext,3);
                tv.setPadding(padding, padding, padding, padding);
                String btn_text = "<b>"+course.getCourseName() + "</b>\n<br/>" + course.getCoursePlace();
                if(course.getCourseExtra() != null && !course.getCourseExtra().equals(""))
                    btn_text += "\n<br/><i>" + course.getCourseExtra()+"</i>";
                tv.setText(Html.fromHtml(btn_text));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.width = columnWidth - px_1dp;
                lp.height = cnt*rowHeight - px_1dp;
                lp.leftMargin = px_1dp;
                lp.topMargin = px_1dp;
                lp.rowSpec = GridLayout.spec(row,cnt,GridLayout.FILL);
                lp.columnSpec = GridLayout.spec(col+1);
                timeTableContainer.addView(tv, lp);
            }
        }
        timeTableContainer.setBackgroundColor(mContext.getResources().getColor(R.color.border_color));
        progress.setVisibility(View.GONE);
    }

    private static class TimeTableHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ParseTimeTableProcess.SUCCESS_TIMETABLE:
                    try {
                        JSONObject jsonObject = (JSONObject) msg.obj;

                        if(!jsonObject.has("result") || !jsonObject.has("data")){
                            this.sendEmptyMessage(ParseTimeTableProcess.FAILED_TIMETABLE);
                            return;
                        }

                        JSONObject result_object = jsonObject.getJSONObject("result");
                        if(result_object.has("status") && result_object.getString("status").equals("success")){
                            TimeTable timeTable = new TimeTable();
                            JSONArray data = jsonObject.getJSONArray("data");
                            for(int index = 0; index<data.length();index++){
                                JSONObject time_object = data.getJSONObject(index);
                                for(int inner=1;inner<=5;inner++){
                                    String raw = time_object.getString("haknonm" + inner);
                                    if(raw != null && !raw.equals("null")){
                                        String[] tmp = raw.split("<br/>");
                                        String[] courseIds = tmp[0].split("\\-");
                                        String name = tmp[1].replaceAll("<[^>]*>", "");
                                        String place = tmp[2];
                                        String extra = "";
                                        if(tmp.length > 3)
                                            extra = tmp[3].replaceAll("<[^>]*>", "");
                                        timeTable.setCourse(inner-1,index,new TimeTableCourse(name,place,courseIds[0],courseIds[1],courseIds[2],extra));
                                    }
                                }
                            }
                            updateTimeTable(timeTable);
                        }else{
                            this.sendEmptyMessage(ParseTimeTableProcess.FAILED_TIMETABLE);
                            return;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case ParseTimeTableProcess.FAILED_TIMETABLE:
                    Toast.makeText(mContext,R.string.cant_load_timetable,Toast.LENGTH_SHORT).show();
                    break;
                default:
            }
        }
    }

}
