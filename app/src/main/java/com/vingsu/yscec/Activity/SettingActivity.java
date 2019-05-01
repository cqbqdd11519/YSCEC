package com.vingsu.yscec.Activity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.Container.ListContainer;
import com.vingsu.yscec.Container.ListContainerContainer;
import com.vingsu.yscec.DatabaseHelper;
import com.vingsu.yscec.R;
import com.vingsu.yscec.Service.YSCECBackgroundService;
import com.vingsu.yscec.Widget.TouchBlackHoleProgress;

import java.security.acl.AclNotFoundException;
import java.util.ArrayList;

public class SettingActivity extends AppCompatActivity {

    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = SettingActivity.this;
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content,new SettingFragment());
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        Fragment list_setting = getFragmentManager().findFragmentByTag(CommonValues.TAG_LIST_SETTING);
        if(list_setting != null){
            getFragmentManager().beginTransaction()
                    .remove(list_setting)
                    .commit();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    public static class SettingFragment extends PreferenceFragment{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.setting_main);

            SharedPreferences preferences_time = mContext.getSharedPreferences(CommonValues.PREF_REFRESH, MODE_PRIVATE);
            final SharedPreferences.Editor ed_time = preferences_time.edit();

            SwitchPreference enable_refresh = (SwitchPreference) findPreference(CommonValues.PREF_KEY_ENABLE_REFRESH);
            enable_refresh.setChecked(preferences_time.getBoolean(CommonValues.PREF_KEY_ENABLE_REFRESH, false));

            final EditTextPreference interval_minute = (EditTextPreference) findPreference(CommonValues.PREF_KEY_REFRESH_INTERVAL);
            interval_minute.setText("" + preferences_time.getInt(CommonValues.PREF_KEY_REFRESH_INTERVAL, 30));
            EditText interval_text_field = interval_minute.getEditText();
            interval_text_field.setKeyListener(DigitsKeyListener.getInstance(false, false));

            Preference refresh_list = findPreference(CommonValues.PREF_KEY_REFRESH_LIST);

            interval_minute.setEnabled(preferences_time.getBoolean(CommonValues.PREF_KEY_ENABLE_REFRESH, false));
            enable_refresh.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean b = (boolean) o;
                    interval_minute.setEnabled(b);
                    if (b) {
                        getActivity().startService(new Intent(getActivity(), YSCECBackgroundService.class));
                    } else {
                        AlarmManager alarm = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
                        alarm.cancel(PendingIntent.getService(getActivity().getApplicationContext(), 0, new Intent(getActivity().getApplicationContext(), YSCECBackgroundService.class), 0));
                    }
                    ed_time.putBoolean(CommonValues.PREF_KEY_ENABLE_REFRESH, b);
                    ed_time.apply();
                    return true;
                }
            });
            interval_minute.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    int time = Integer.parseInt((String) o);
                    AlarmManager alarm = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
                    alarm.cancel(PendingIntent.getService(getActivity().getApplicationContext(), 0, new Intent(getActivity().getApplicationContext(), YSCECBackgroundService.class), 0));
                    alarm.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + time * 60 * 1000, PendingIntent.getService(mContext, 0, new Intent(mContext, YSCECBackgroundService.class), 0));
                    ed_time.putInt(CommonValues.PREF_KEY_REFRESH_INTERVAL, time);
                    ed_time.apply();
                    return true;
                }
            });
            refresh_list.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getFragmentManager().beginTransaction()
                            .add(android.R.id.content, new ListSettingFragment(), CommonValues.TAG_LIST_SETTING)
                            .commit();
                    return false;
                }
            });

            Preference info = findPreference(CommonValues.PREF_KEY_MADE_BY);
            info.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getText(R.string.vingsu_lab_url).toString()));
                        startActivity(intent);
                    }catch (ActivityNotFoundException e){
                        Toast.makeText(mContext,R.string.no_activity_to_url,Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }
    }

    public static class ListSettingFragment extends Fragment{

        private static Context mContext;
        private static ExpandableListView mListView;
        private static TouchBlackHoleProgress mProgress;
        private static ListAdapter mAdapter;
        private static ArrayList<ListContainerContainer> mData;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mContext = getActivity();
            mData = new ArrayList<>();
            mAdapter = new ListAdapter(mContext,R.layout.setting_list_row,mData);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main,container,false);
            mListView = (ExpandableListView) rootView.findViewById(R.id.notice_list);
            mListView.setAdapter(mAdapter);
            mListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                    return true;
                }
            });
            mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, final View view, final int ii, final int i1, long l) {
                    final TextView id_v = (TextView) view.findViewById(R.id.list_setting_id);
                    final TextView is_now_on = (TextView) view.findViewById(R.id.list_setting_is_now_on_t);
                    String m_title;
                    String m_message;
                    DialogInterface.OnClickListener clickListener;
                    if (is_now_on.getText().toString().equals("true")) {
                        m_title = getText(R.string.unsubscribe_title).toString();
                        m_message = getText(R.string.unsubscribe_message).toString();
                        clickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ImageView imageView = (ImageView) view.findViewById(R.id.list_setting_image);
                                imageView.setImageResource(R.drawable.ic_notifications_off_grey600_36dp);
                                mData.get(ii).getBoards().get(i1).setNowOn(false);
                                String false_string = "" + false;
                                is_now_on.setText(false_string);
                                SharedPreferences preferences = getActivity().getSharedPreferences(CommonValues.PREF_REFRESH, MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(CommonValues.PREF_KEY_UNSUBSCRIBE_LIST
                                        , preferences.getString(CommonValues.PREF_KEY_UNSUBSCRIBE_LIST, "") +
                                        "^@^" +
                                        mData.get(ii).getId() +
                                        "^&^" +
                                        id_v.getText() + ";");
                                editor.apply();
                                Toast.makeText(mContext, "'" + id_v.getText() + "'" + getText(R.string.toast_unsubscribed), Toast.LENGTH_SHORT).show();
                            }
                        };
                    } else {
                        m_title = getText(R.string.subscribe_title).toString();
                        m_message = getText(R.string.subscribe_message).toString();
                        clickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ImageView imageView = (ImageView) view.findViewById(R.id.list_setting_image);
                                imageView.setImageResource(R.drawable.ic_notifications_grey600_36dp);
                                mData.get(ii).getBoards().get(i1).setNowOn(true);
                                String true_string = "" + true;
                                is_now_on.setText(true_string);
                                SharedPreferences preferences = getActivity().getSharedPreferences(CommonValues.PREF_REFRESH, MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(CommonValues.PREF_KEY_UNSUBSCRIBE_LIST
                                        , preferences.getString(CommonValues.PREF_KEY_UNSUBSCRIBE_LIST, "").replace("" +
                                        "^@^" +
                                        mData.get(ii).getId() +
                                        "^&^" +
                                        id_v.getText() + ";", ""));
                                editor.apply();
                                Toast.makeText(mContext, "'" + id_v.getText() + "'" + getText(R.string.toast_subscribed), Toast.LENGTH_SHORT).show();
                            }
                        };
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(m_title)
                            .setMessage(m_message)
                            .setPositiveButton(R.string.ok_text, clickListener)
                            .setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();
                    return false;
                }
            });
            mProgress = (TouchBlackHoleProgress) rootView.findViewById(R.id.main_progress);
            mProgress.setVisibility(View.GONE);
            SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
            swipeRefreshLayout.setEnabled(false);
            TextView year_term = (TextView) rootView.findViewById(R.id.year_term);
            year_term.setVisibility(View.GONE);
            DatabaseHelper helper = new DatabaseHelper(mContext);
            helper.getAllBoards(mData);
            helper.closeDB();
            mAdapter.notifyDataSetChanged();
            return rootView;
        }

        private static class ListAdapter extends BaseExpandableListAdapter{

            private ArrayList<ListContainerContainer> list;
            private int textViewResourceId;
            private LayoutInflater inflater;

            public ListAdapter(Context context, int _textViewResourceId,ArrayList<ListContainerContainer> items){
                this.list = items;
                this.textViewResourceId = _textViewResourceId;
                this.inflater = LayoutInflater.from(context);
            }
            @Override
            public void notifyDataSetChanged() {
                super.notifyDataSetChanged();
                for(int i=0;i<getGroupCount();i++)
                    mListView.expandGroup(i);
            }
            @Override
            public Object getGroup(int i) {
                return list.get(i);
            }
            @Override
            public long getGroupId(int i) {
                return i;
            }
            @Override
            public int getGroupCount() {
                return list.size();
            }
            @Override
            public boolean hasStableIds() {
                return true;
            }
            @Override
            public Object getChild(int i, int i1) {
                return list.get(i).getBoards().get(i1);
            }
            @Override
            public long getChildId(int i, int i1) {
                return 0;
            }
            @Override
            public int getChildrenCount(int i) {
                return list.get(i).getBoards().size();
            }
            @Override
            public boolean isChildSelectable(int i, int i1) {
                return true;
            }

            @Override
            public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
                ListContainerContainer containerContainer = (ListContainerContainer) getGroup(i);
                View rootView = view;
                GroupHolder holder;
                if(rootView == null){
                    rootView = inflater.inflate(R.layout.notice_group_row,viewGroup,false);
                    holder = new GroupHolder();
                    holder.title = (TextView) rootView.findViewById(R.id.notice_group_title);
                    holder.id = (TextView) rootView.findViewById(R.id.notice_group_id);
                    rootView.setTag(holder);
                }else{
                    holder = (GroupHolder) rootView.getTag();
                }
                TextView title = holder.title;
                TextView id = holder.id;

                title.setText(containerContainer.getTitle());
                id.setText(containerContainer.getId());
                return rootView;
            }

            @Override
            public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
                ListContainer container = (ListContainer) getChild(i,i1);
                View rootView = view;
                ChildHolder holder;
                if(rootView == null){
                    rootView = inflater.inflate(this.textViewResourceId,viewGroup,false);
                    holder = new ChildHolder();
                    holder.title = (TextView) rootView.findViewById(R.id.list_setting_title);
                    holder.is_now_on = (TextView) rootView.findViewById(R.id.list_setting_is_now_on_t);
                    holder.id = (TextView) rootView.findViewById(R.id.list_setting_id);
                    holder.onOffSwitch = (ImageView) rootView.findViewById(R.id.list_setting_image);
                    rootView.setTag(holder);
                }else{
                    holder = (ChildHolder) rootView.getTag();
                }
                TextView title = holder.title;
                TextView is_now_on = holder.is_now_on;
                TextView id = holder.id;
                ImageView onOffSwitch = holder.onOffSwitch;

                title.setText(container.getTitle());
                id.setText(container.getId());
                String no_on_text = ""+container.getIsNowOn();
                is_now_on.setText(no_on_text);
                if(container.getIsNowOn()){
                    onOffSwitch.setImageResource(R.drawable.ic_notifications_grey600_36dp);
                }else{
                    onOffSwitch.setImageResource(R.drawable.ic_notifications_off_grey600_36dp);
                }
                return rootView;
            }
        }

        private static class GroupHolder{
            public TextView title;
            public TextView id;
        }

        private static class ChildHolder{
            public TextView title;
            public TextView is_now_on;
            public TextView id;
            public ImageView onOffSwitch;
        }
    }

}
