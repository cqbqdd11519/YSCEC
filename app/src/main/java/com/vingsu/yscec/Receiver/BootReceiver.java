package com.vingsu.yscec.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.vingsu.yscec.CommonValues;
import com.vingsu.yscec.Service.YSCECBackgroundService;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() { }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)){
            SharedPreferences preferences_time = context.getSharedPreferences(CommonValues.PREF_REFRESH,Context.MODE_PRIVATE);
            boolean enable_refresh = preferences_time.getBoolean(CommonValues.PREF_KEY_ENABLE_REFRESH,false);
            if(enable_refresh)
                context.startService(new Intent(context,YSCECBackgroundService.class));
        }
    }
}
