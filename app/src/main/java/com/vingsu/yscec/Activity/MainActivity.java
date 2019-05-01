package com.vingsu.yscec.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.Fragment.BaseFragment;
import com.vingsu.yscec.Fragment.CourseMain;
import com.vingsu.yscec.Fragment.LoginFragment;
import com.vingsu.yscec.Fragment.NavigationDrawerFragment;
import com.vingsu.yscec.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    private static Context mContext;
    private static DrawerLayout mDrawerLayout;
    private static ActionBarDrawerToggle mDrawerToggle;
    private static Toolbar mToolbar;

    private static ArrayList<String> fragments;

    public static String noti_full_url = null;
    public static String noti_course_id = null;
    public static String noti_b_id = null;
    public static String noti_id = null;

    private long backPressedTime = 0;

    public static Toolbar getMToolbar(){
        return mToolbar;
    }

    public void lockDrawer(boolean b){
        if(b)
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
        else
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);
    }

    public void closeDrawer(){
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public void syncToggleState(){
        mDrawerToggle.syncState();
    }

    public void openCourseMain(Bundle bundle){
        Fragment article_list_fragment = new CourseMain();
        article_list_fragment.setArguments(bundle);

        addFragment(R.id.container, article_list_fragment, CommonValues.TAG_ARTICLE_LIST);
    }

    public static void addFragment(int id , Fragment fragment , String tag){
        /**
         *
         *  TAG :
         *  CommonValues.TAG_MAIN
         *  CommonValues.TAG_LOGIN
         *  CommonValues.TAG_TIMETABLE
         *  CommonValues.TAG_CALENDAR
         *  CommonValues.TAG_CONTENT
         *  CommonValues.TAG_ARTICLE_LIST
         *
         */
        if(mContext == null)
            return;
        FragmentTransaction transaction = ((MainActivity) mContext).getFragmentManager().beginTransaction();
        switch (tag){
            case CommonValues.TAG_MAIN:
            case CommonValues.TAG_LOGIN:
                for(String s : fragments){
                    Fragment f = ((MainActivity) mContext).getFragmentManager().findFragmentByTag(s);
                    transaction.remove(f);
                }
                fragments.clear();
                break;
            case CommonValues.TAG_ARTICLE_LIST:
                for(String s : fragments){
                    if(s.equals(CommonValues.TAG_MAIN))
                        continue;
                    Fragment f = ((MainActivity) mContext).getFragmentManager().findFragmentByTag(s);
                    transaction.remove(f);
                }
                fragments.clear();
                fragments.add(CommonValues.TAG_MAIN);
                break;
            case CommonValues.TAG_TIMETABLE:
                if(fragments.contains(CommonValues.TAG_TIMETABLE))
                    return;
                break;
            case CommonValues.TAG_CALENDAR:
                if(fragments.contains(CommonValues.TAG_CALENDAR))
                    return;
                break;
            default:
        }
        fragments.add(tag);
        transaction.add(id,fragment,tag);
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noti_course_id = null;
        noti_b_id = null;
        noti_id = null;

        mContext = this;

        fragments = new ArrayList<>();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mToolbar.setBackgroundColor(ContextCompat.getColor(mContext,R.color.theme_color));
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,mToolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        Intent intent = getIntent();
        if(intent != null
                && intent.hasExtra("type") && intent.getStringExtra("type").equals("content")
                && intent.hasExtra(CommonValues.PARAM_FULL_URL)
                && intent.hasExtra(CommonValues.PARAM_COURSE_ID)
                && intent.hasExtra(CommonValues.PARAM_B_ID)
                && intent.hasExtra(CommonValues.PARAM_ID)){
            noti_full_url = intent.getStringExtra(CommonValues.PARAM_FULL_URL);
            noti_course_id = intent.getStringExtra(CommonValues.PARAM_COURSE_ID);
            noti_b_id = intent.getStringExtra(CommonValues.PARAM_B_ID);
            noti_id = intent.getStringExtra(CommonValues.PARAM_ID);
        }

        getFragmentManager()
                .beginTransaction()
                .add(R.id.navigation_drawer, new NavigationDrawerFragment(), CommonValues.TAG_DRAWER)
                .commit();
        addFragment(R.id.container, new LoginFragment(), CommonValues.TAG_LOGIN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext = null;
    }

    @Override
    public FragmentManager getFragmentManager() {
        return super.getFragmentManager();
    }

    @Override
    public void onBackPressed() {
        long tempTime        = System.currentTimeMillis();
        long intervalTime    = tempTime - backPressedTime;
        final long	FINISH_INTERVAL_TIME    = 2000;
        if ( 0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime ) {
            finish();
        }else {
            if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
                closeDrawer();
            else if(fragments.size() > 1){
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentByTag(fragments.get(fragments.size()-1)))
                        .commit();
                fragments.remove(fragments.size()-1);
                ((BaseFragment)getFragmentManager().findFragmentByTag(fragments.get(fragments.size()-1))).invokeNowViewing();
            }else{
                backPressedTime = tempTime;
                Toast.makeText(MainActivity.this,R.string.on_backpress, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
