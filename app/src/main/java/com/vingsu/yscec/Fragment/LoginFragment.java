package com.vingsu.yscec.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.vingsu.yscec.Activity.MainActivity;
import com.vingsu.yscec.CommonFunctions;
import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.Network.LoginProcess;
import com.vingsu.yscec.R;
import com.vingsu.yscec.Service.YSCECBackgroundService;
import com.vingsu.yscec.Widget.TouchBlackHoleProgress;
import com.vingsu.yscec.YSCEC;

public class LoginFragment extends BaseFragment {

    private static LoginHandler mHandler;

    private static EditText login_id_v;
    private static EditText login_pw_v;
    private static TouchBlackHoleProgress progressBar;

    private static boolean isProcessing = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new LoginHandler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login,container,false);
        invokeNowViewing();
        isProcessing = false;

        ((MainActivity)mContext).lockDrawer(true);

        login_id_v = (EditText) rootView.findViewById(R.id.login_id);
        login_pw_v = (EditText) rootView.findViewById(R.id.login_pw);
        progressBar = (TouchBlackHoleProgress) rootView.findViewById(R.id.login_progress);
        Button login_btn = (Button) rootView.findViewById(R.id.login_btn);

        SharedPreferences preferences = mContext.getSharedPreferences(CommonValues.PREF_LOGIN, Context.MODE_PRIVATE);
        String saved_id = preferences.getString(CommonValues.PREF_KEY_LOGIN_ID, "");
        String saved_pw = preferences.getString(CommonValues.PREF_KEY_LOGIN_PW, "");
        boolean auto_login = preferences.getBoolean(CommonValues.PREF_KEY_AUTO_LOGIN, true);

        if(!saved_id.equals("") && !saved_pw.equals("")){
            login_id_v.setText(saved_id);
            login_pw_v.setText(saved_pw);
            if(auto_login){
                login();
            }
        }

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
        return rootView;
    }

    @Override
    public void invokeNowViewing() {
        ((MainActivity)mContext).lockDrawer(true);
        ActionBar actionBar = ((MainActivity)mContext).getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        this.setTitle(getString(R.string.app_name));
    }

    private void login(){
        if(isProcessing)
            return;
        String login_id = login_id_v.getText().toString();
        String login_pw = login_pw_v.getText().toString();
        CommonFunctions.hideKeyPad((MainActivity)mContext);

        progressBar.setVisibility(View.VISIBLE);
        LoginProcess loginProcess = new LoginProcess(mContext, mHandler);
        try {
            loginProcess.login(login_id, login_pw);
            isProcessing = true;
        } catch (Exception e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(LoginProcess.FAILED_LOGIN);
        }
    }

    private static class LoginHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case LoginProcess.SUCCESS_LOGIN:
                    progressBar.setVisibility(View.GONE);
                    YSCEC.setLogin_cookie((String) msg.obj);
                    SharedPreferences preferences = mContext.getSharedPreferences(CommonValues.PREF_LOGIN, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(CommonValues.PREF_KEY_LOGIN_ID,login_id_v.getText().toString());
                    editor.putString(CommonValues.PREF_KEY_LOGIN_PW, login_pw_v.getText().toString());
                    editor.putBoolean(CommonValues.PREF_KEY_AUTO_LOGIN, true);
                    editor.apply();
                    SharedPreferences preferences0 = mContext.getSharedPreferences(CommonValues.PREF_REFRESH, Context.MODE_PRIVATE);
                    if(preferences0.getBoolean(CommonValues.PREF_KEY_FIRST_LOGIN_SUCCESS,true)) {
                        SharedPreferences.Editor editor0 = preferences0.edit();
                        editor0.putBoolean(CommonValues.PREF_KEY_ENABLE_REFRESH, true);
                        editor0.putBoolean(CommonValues.PREF_KEY_FIRST_LOGIN_SUCCESS, false);
                        editor0.apply();
                        mContext.startService(new Intent(mContext, YSCECBackgroundService.class));
                    }
                    MainActivity.addFragment(R.id.container,new MainFragment(),CommonValues.TAG_MAIN);
                    break;
                case LoginProcess.FAILED_LOGIN:
                    isProcessing = false;
                    progressBar.setVisibility(View.GONE);
                    break;
                default:
            }
        }
    }
}
