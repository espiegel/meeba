package com.meeba.google;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.support.v4.content.WakefulBroadcastReceiver;

import com.meeba.google.util.Utils;

/**
 * Created by Max on 24/12/13.
 */
public class SmsListener extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Utils.LOGD("SmsListener: inside onReceive!");
        // Explicitly specify that SmsIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                com.meeba.google.SmsIntentService.class.getName());

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);

    }
}

