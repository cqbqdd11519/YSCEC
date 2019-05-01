package com.vingsu.yscec.Fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.vingsu.yscec.Activity.MainActivity;
import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.Container.Course;
import com.vingsu.yscec.DatabaseHelper;
import com.vingsu.yscec.R;
import com.vingsu.yscec.YSCEC;

import java.util.ArrayList;

public class NavigationDrawerFragment extends Fragment {

    private static Context mContext;

    private static TextView mName;

    private static ArrayList<Course> mClasses;
    private static DrawerListAdapter mAdapter;

    public NavigationDrawerFragment(){}

    public static void setContent(){
        SharedPreferences pref_login = mContext.getSharedPreferences(CommonValues.PREF_LOGIN,Context.MODE_PRIVATE);
        String login_id = pref_login.getString(CommonValues.PREF_KEY_LOGIN_ID, "");
        String hyhg = pref_login.getString(CommonValues.PREF_KEY_HYHG_PREFIX + login_id, "");
        String year = hyhg.substring(0, 4);
        String term = hyhg.substring(4);
        String name_id = YSCEC.getLogin_name() + "("+login_id+")\n" +
                year+mContext.getText(R.string.year_text)+" "+term+mContext.getText(R.string.term_text);
        mName.setGravity(Gravity.CENTER);
        mName.setText(name_id);

        mClasses.clear();

        DatabaseHelper helper = new DatabaseHelper(mContext);
        ArrayList<Course> courses = helper.getCourseList();
        helper.closeDB();
        for(Course c : courses){
            mClasses.add(c);
        }

        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mClasses = new ArrayList<>();
        mAdapter = new DrawerListAdapter(mContext,0,mClasses);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_navigation_drawer,container,false);

        mName = (TextView) rootView.findViewById(R.id.drawer_login_name);
        ListView mList = (ListView) rootView.findViewById(R.id.drawer_list);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String id = ((TextView) view.findViewById(R.id.drawer_list_row_id)).getText().toString();
                ((MainActivity) getActivity()).closeDrawer();
                Bundle bundle = new Bundle();
                bundle.putString(CommonValues.PARAM_COURSE_ID, id);
                ((MainActivity) getActivity()).openCourseMain(bundle);
            }
        });

        TextView button_calendar = (TextView) rootView.findViewById(R.id.button_calendar);
        TextView button_timetable = (TextView) rootView.findViewById(R.id.button_timetable);

        button_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).closeDrawer();
                if (getFragmentManager().findFragmentByTag(CommonValues.TAG_CALENDAR) != null) {
                    return;
                }
                Fragment timetable_fragment = new CalendarFragment();
                MainActivity.addFragment(R.id.container, timetable_fragment, CommonValues.TAG_CALENDAR);
            }
        });

        button_timetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).closeDrawer();
                if (getFragmentManager().findFragmentByTag(CommonValues.TAG_TIMETABLE) != null) {
                    return;
                }
                Fragment timetable_fragment = new TimeTableFragment();
                MainActivity.addFragment(R.id.container, timetable_fragment, CommonValues.TAG_TIMETABLE);
            }
        });

        return rootView;
    }

    private class DrawerListAdapter extends ArrayAdapter<Course>{
        private ArrayList<Course> list;

        public DrawerListAdapter(Context c,int textViewResourceId,ArrayList<Course> items){
            super(c,textViewResourceId,items);
            this.list = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null){
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.drawer_list_row,parent,false);
            }
            Course c = list.get(position);

            TextView name = (TextView) v.findViewById(R.id.drawer_list_row_name);
            TextView id = (TextView) v.findViewById(R.id.drawer_list_row_id);

            name.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            name.setText(c.getName());
            id.setText(c.getId());
            return v;
        }
    }
}
