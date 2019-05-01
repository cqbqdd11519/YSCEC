package com.vingsu.yscec.Fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.vingsu.yscec.Activity.MainActivity;
import com.vingsu.yscec.R;

@SuppressWarnings("unused")
public abstract class BaseFragment extends Fragment {

    public static Context mContext;
    private static Fragment mThis;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mThis = this;
    }

    public void setTitle(String s){
        Toolbar toolbar = MainActivity.getMToolbar();
        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(s);
        title.setSelected(true);
    }

    public abstract void invokeNowViewing();
}
