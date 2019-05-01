package com.vingsu.yscec.Widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.vingsu.yscec.CommonFunctions;
import com.vingsu.yscec.R;
import com.vingsu.yscec.TermCalendar;
import com.vingsu.yscec.TermSchedule;

import java.util.ArrayList;
import java.util.Calendar;

public class SchoolCalendar extends RelativeLayout {

    private GridLayout gridCalendar;
    private static Context mContext;
    private TermCalendar data;
    private Spinner monthSpinner;

    private static ArrayList<Integer> isDrawn;
    private static int max_date = 0;

    private float y1;
    static final int MIN_DISTANCE = 150;

    private static int year = 0;
    private static int month = 0;

    private TextView[] dates = new TextView[31];
    private ArrayList<TextView> events = new ArrayList<>();
    private SparseArray<SparseArray<String>> event_list;
    private ArrayList<String> selectedEvents;

    private DrawHandler handler;

    private Dialog eventDialog;
    private ArrayAdapter<String> arrayAdapter;

    private OnClickListener onDateClickListener;

    public SchoolCalendar(Context context) {
        super(context);
        mContext = context;
        this.init();
    }

    public SchoolCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        this.init();
    }

    public void setSpinner(Spinner spinner){
        this.monthSpinner = spinner;
    }

    private void init() {
        selectedEvents = new ArrayList<>();
        isDrawn = new ArrayList<>();
        event_list = new SparseArray<>();
        handler = new DrawHandler();
        gridCalendar = new GridLayout(mContext);
        RelativeLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.setMargins(0, 0, 0, 0);
        this.addView(gridCalendar, lp);
        gridCalendar.setUseDefaultMargins(false);
        gridCalendar.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        gridCalendar.setColumnCount(7);
        eventDialog = new Dialog(mContext);
        ListView eventListView = new ListView(mContext);
        eventDialog.setContentView(eventListView);
        arrayAdapter = new ArrayAdapter<>(mContext,android.R.layout.simple_list_item_1,android.R.id.text1,selectedEvents);
        eventListView.setAdapter(arrayAdapter);
        onDateClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                String date_t = ((TextView)v).getText().toString();
                if(date_t.equals(""))
                    return;
                int date = Integer.parseInt(date_t);
                if(event_list.get(date) != null){
                    selectedEvents.clear();
                    for(int i = 0 ; i < 3 ; i++){
                        if(event_list.get(date).get(i) != null)
                            selectedEvents.add(event_list.get(date).get(i));
                    }
                    arrayAdapter.notifyDataSetChanged();
                    eventDialog.setTitle(month+"월 "+date+"일");
                    eventDialog.show();
                }
            }
        };
    }

    public void setData(TermCalendar calendar) {
        this.data = calendar;
    }

    public void setMonth(int _year,int _month) {
        for(TextView tv : events){
            this.removeView(tv);
        }
        events.clear();
        year = _year;
        month = _month;
        isDrawn.clear();
        gridCalendar.removeAllViews();
        Calendar calendar = Calendar.getInstance();
        int now_year = calendar.get(Calendar.YEAR);
        int now_month = calendar.get(Calendar.MONTH)+1;
        int now_date = calendar.get(Calendar.DATE);
        calendar.set(year, month - 1, 1);
        max_date = calendar.getActualMaximum(Calendar.DATE);
        int maxWeekNumber = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH);
        gridCalendar.setRowCount(maxWeekNumber + 1);

        int px_1dp =  (int) CommonFunctions.dpToPixel(mContext, 1);

        float cellWidth = (gridCalendar.getWidth() - px_1dp) / 7;
        float headHeight = (gridCalendar.getHeight() - px_1dp ) / 15;
        float cellHeight = (gridCalendar.getHeight() - px_1dp -headHeight) / maxWeekNumber;

        for (int i = 0; i < 7; i++) {
            TextView tv = new TextView(mContext);
            String day = "";
            switch (i) {
                case 0:
                    day = mContext.getString(R.string.day_sun_short);
                    break;
                case 1:
                    day = mContext.getString(R.string.day_mon_short);
                    break;
                case 2:
                    day = mContext.getString(R.string.day_tues_short);
                    break;
                case 3:
                    day = mContext.getString(R.string.day_wednes_short);
                    break;
                case 4:
                    day = mContext.getString(R.string.day_thurs_short);
                    break;
                case 5:
                    day = mContext.getString(R.string.day_fri_short);
                    break;
                case 6:
                    day = mContext.getString(R.string.day_sat_short);
                    break;
                default:
            }
            tv.setBackgroundColor(getResources().getColor(R.color.background_color));
            tv.setText(day);
            if(i == 0)
                tv.setTextColor(Color.RED);
            tv.setGravity(Gravity.CENTER);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = (int)cellWidth - px_1dp;
            lp.height = (int)headHeight - px_1dp;
            lp.leftMargin = px_1dp;
            lp.topMargin = px_1dp;
            lp.setGravity(Gravity.CENTER);
            lp.rowSpec = GridLayout.spec(0);
            lp.columnSpec = GridLayout.spec(i);
            gridCalendar.addView(tv, lp);
        }

        int first_day = calendar.get(Calendar.DAY_OF_WEEK)-1;
        int row = 1;
        for(int i=0;i<7*maxWeekNumber ; i++){
            final int date = i-first_day+1;
            if( i > 1 && i % 7 == 0 )
                row++;
            TextView tv = new TextView(mContext){
                @Override
                protected void onDraw(Canvas canvas) {
                    super.onDraw(canvas);
                    String text = this.getText().toString();
                    if(!text.equals(""))
                        handler.sendEmptyMessage(Integer.parseInt(text));
                }
            };
            tv.setOnClickListener(onDateClickListener);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
            tv.setBackgroundColor(getResources().getColor(R.color.background_color));
            String text = "";
            if(i%7 == 0)
                tv.setTextColor(Color.RED);
            tv.setGravity(Gravity.START | Gravity.TOP);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = (int)cellWidth - px_1dp;
            lp.height = (int)cellHeight - px_1dp;
            lp.leftMargin = px_1dp;
            lp.topMargin = px_1dp;
            lp.setGravity(Gravity.CENTER);
            lp.rowSpec = GridLayout.spec(row);
            lp.columnSpec = GridLayout.spec(i%7);
            if(i >= first_day && i < first_day+max_date ){
                text = ""+date;
                dates[date-1] = tv;
            }
            if(now_year == _year && now_month == _month && date == now_date){
                tv.setTypeface(null, Typeface.BOLD);
                tv.setPaintFlags(tv.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
            }
            tv.setText(text);
            gridCalendar.addView(tv, lp);
        }
        gridCalendar.setBackgroundColor(getResources().getColor(R.color.border_color));
    }

    private void setEventData(){
        event_list.clear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year,month-1,1);
        int max_date = calendar.getActualMaximum(Calendar.DATE);
        ArrayList<TermSchedule> data_month = data.getSchedulesOfMonth(month);
        if(data_month == null){
            return;
        }
        for(TermSchedule schedule : data_month){
            String title = schedule.getTitle();
            int start_month = schedule.getStartMonth();
            int end_month = schedule.getEndMonth();
            int start_date = schedule.getStartDate();
            int end_date = schedule.getEndDate();
            if(start_month != month){
                start_date = 1;
            }
            if(end_month != month){
                end_date = max_date;
            }

            calendar.set(year, month - 1, start_date);
            int start_week = calendar.get(Calendar.WEEK_OF_YEAR);
            calendar.set(year, month - 1, end_date);
            int end_week = calendar.get(Calendar.WEEK_OF_YEAR);

            for(int week = start_week ; week <= end_week ; week++) {
                calendar.clear();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.WEEK_OF_YEAR, week);
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                int start_date_tmp = calendar.get(Calendar.DATE);
                if (calendar.get(Calendar.MONTH) + 1 != month)
                    start_date_tmp = 1;
                calendar.add(Calendar.DATE, 6);
                int end_date_tmp = calendar.get(Calendar.DATE);
                if (calendar.get(Calendar.MONTH) + 1 != month)
                    end_date_tmp = max_date;

                int this_start = Math.max(start_date, start_date_tmp);
                int this_end = Math.min(end_date, end_date_tmp);

                int height = (int)CommonFunctions.dpToPixel(mContext,15);
                int gap = height / 5;
                int top_position = height;
                boolean[] position_arr = {true,true,true};
                int index = -1;
                for (int i = this_start; i <= this_end; i++) {
                    if (event_list.get(i) == null) {
                        event_list.put(i, new SparseArray<String>());
                    }
                    for (int j = 0; j < 3; j++) {
                        if (event_list.get(i).get(j) != null) {
                            position_arr[j] = false;
                        }
                    }
                }
                for (int j = 0; j < 3; j++) {
                    if(position_arr[j]){
                        index = j;
                        break;
                    }
                }
                for (int i = this_start; i <= this_end; i++) {
                    event_list.get(i).put(index, title);
                }
                top_position = Math.max(top_position, (height+gap) * (index + 1));

                int left = dates[this_start - 1].getLeft();
                int right = dates[this_end - 1].getRight();
                int top = dates[this_start - 1].getTop() + top_position;


                if(top_position > 0){
                    TextView tv = new TextView(mContext);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                    tv.setBackgroundColor(getResources().getColor(R.color.timetable_1));
                    LayoutParams params = new LayoutParams(Math.abs(right - left), height);
                    params.leftMargin = left;
                    params.topMargin = top;
                    tv.setText(title);
                    tv.setSingleLine();
                    tv.setLines(1);
                    tv.setEllipsize(TextUtils.TruncateAt.END);
                    events.add(tv);
                    this.addView(tv, params);
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float y2 = event.getY();
                float deltaX = y2 - y1;

                if (Math.abs(deltaX) > MIN_DISTANCE){
                    int cnt = monthSpinner.getAdapter().getCount();
                    int index = -1;
                    for(int i = 0 ; i < cnt ; i++){
                        if( monthSpinner.getAdapter().getItem(i).equals(month+"월") ){
                            index = i;
                        }
                    }
                    if(index >= 0){
                        if (y2 > y1){
                            if(index > 0){
                                monthSpinner.setSelection(index - 1);
                            }
                        }else{
                            if(index < cnt-1){
                                monthSpinner.setSelection(index+1);
                            }
                        }
                    }

                }
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    private class DrawHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            if(!isDrawn.contains(msg.what))
                isDrawn.add(msg.what);
            if(isDrawn.size() == max_date)
                setEventData();
        }
    }
}