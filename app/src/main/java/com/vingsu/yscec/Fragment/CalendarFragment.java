package com.vingsu.yscec.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vingsu.yscec.Activity.MainActivity;
import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.Network.ParseCalendarProcess;
import com.vingsu.yscec.R;
import com.vingsu.yscec.TermCalendar;
import com.vingsu.yscec.Widget.SchoolCalendar;
import com.vingsu.yscec.Widget.TouchBlackHoleProgress;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalendarFragment extends BaseFragment {

    private static TouchBlackHoleProgress progress;
    private static CalendarHandler mHandler;

    private static SchoolCalendar calendarView;
    private static LinearLayout calendarHead;
    private static Spinner calendarMonth;

    private static int nowYear = 2016;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new CalendarHandler();
    }

    @Override
    public void invokeNowViewing() {
        ActionBar actionBar = ((MainActivity) mContext).getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        this.setTitle(getString(R.string.calendar_text));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);
        invokeNowViewing();

        ActionBar actionBar = ((MainActivity) mContext).getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.calendar_text));
        }
        SharedPreferences pref_login = mContext.getSharedPreferences(CommonValues.PREF_LOGIN, Context.MODE_PRIVATE);
        String login_id = pref_login.getString(CommonValues.PREF_KEY_LOGIN_ID, "");
        String hyhg = pref_login.getString(CommonValues.PREF_KEY_HYHG_PREFIX + login_id, "");
        String year = hyhg.substring(0, 4);
        String term = hyhg.substring(4);

        nowYear = Integer.parseInt(year);

        calendarHead = (LinearLayout) rootView.findViewById(R.id.calendar_head);

        calendarView = (SchoolCalendar) rootView.findViewById(R.id.school_calendar);
        Spinner calendarHyHg = (Spinner) rootView.findViewById(R.id.calendar_hyhg);
        ArrayList<String> hyhgs = new ArrayList<>();
        for(int i = Integer.parseInt(year) ; i >= 2014 ; i--){
            hyhgs.add(i+"년도 2학기");
            hyhgs.add(i+"년도 1학기");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,android.R.layout.simple_dropdown_item_1line,hyhgs);
        calendarHyHg.setAdapter(adapter);
        calendarHyHg.setSelection(hyhgs.indexOf(year + "년도 " + term + "학기"));
        calendarHyHg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = ((TextView)view.findViewById(android.R.id.text1)).getText().toString();
                Pattern pattern = Pattern.compile("([0-9]*)년도 ([0-9]*)학기");
                Matcher matcher = pattern.matcher(text);
                if(matcher.matches()){
                    int year = Integer.parseInt(matcher.group(1));
                    int term = Integer.parseInt(matcher.group(2));
                    progress.setVisibility(View.VISIBLE);
                    ParseCalendarProcess parseCalendarProcess = new ParseCalendarProcess(getActivity(),mHandler);
                    parseCalendarProcess.parse(year, term);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        calendarMonth = (Spinner) rootView.findViewById(R.id.calendar_month);
        calendarMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = ((TextView)view.findViewById(android.R.id.text1)).getText().toString();
                Pattern pattern = Pattern.compile("([0-9]*)월");
                Matcher matcher = pattern.matcher(text.trim());
                if(matcher.matches()){
                    int month = Integer.parseInt(matcher.group(1));
                    calendarView.setMonth(nowYear,month);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        calendarView.setSpinner(calendarMonth);

        progress = (TouchBlackHoleProgress) rootView.findViewById(R.id.cd_progress);
        progress.setVisibility(View.VISIBLE);

        ParseCalendarProcess parseCalendarProcess = new ParseCalendarProcess(getActivity(),mHandler);
        parseCalendarProcess.parse(Integer.parseInt(year), Integer.parseInt(term));

        return rootView;
    }

    private static class CalendarHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ParseCalendarProcess.SUCCESS_CALENDAR:
                    TermCalendar calendar_data = (TermCalendar) msg.obj;
                    ArrayList<String> months = new ArrayList<>();
                    for(Integer i : calendar_data.getMonths()){
                        months.add(i+"월");
                    }
                    Calendar cal = Calendar.getInstance();
                    int now_month = cal.get(Calendar.MONTH)+1;
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,android.R.layout.simple_dropdown_item_1line,months);
                    calendarMonth.setAdapter(adapter);
                    calendarHead.setVisibility(View.VISIBLE);
                    calendarView.setData(calendar_data);
                    if(nowYear == calendar_data.getYear() && months.contains(now_month + "월"))
                        calendarMonth.setSelection(months.indexOf(now_month + "월"));
                    break;
                case ParseCalendarProcess.FAILED_CALENDAR:
                    Toast.makeText(mContext,R.string.cant_load_calendar,Toast.LENGTH_SHORT).show();
                    break;
            }
            progress.setVisibility(View.GONE);
        }
    }

}
