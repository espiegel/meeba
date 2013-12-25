package com.meeba.google;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;

import com.meeba.google.util.Utils;

import java.util.Map;

/**
 * Created by Max on 25/12/13.
 */
public class SmsIntentService extends IntentService {
    private String mMessageFrom;
    private String mMessageBody;
    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mPrefsEditor;

    public SmsIntentService() {
        super("SmsIntentService");
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Utils.LOGD("SmsListener: inside onHandleIntent!");
        mSharedPrefs = this.getSharedPreferences("waitingList", 0); // 0 - for private mode
        mPrefsEditor = mSharedPrefs.edit();

        for (Map.Entry<String, ?> entry : mSharedPrefs.getAll().entrySet() ) {
            Utils.LOGD("SmsListener: waiting " + entry );
        }

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            if (bundle != null) {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        mMessageFrom = msgs[i].getOriginatingAddress();
                        mMessageBody = msgs[i].getMessageBody();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //replace +XXX with 0 in  the phone number:
        String properPhoneNumber = "0" + mMessageFrom.substring(4);

        //check if the sender's phone number is in the waiting list
        if (mSharedPrefs.getAll().keySet().contains(properPhoneNumber)) {
            //TODO change the invite status of the user with this phone number, in the  corresponding event
            //for this we need:
            //1.getEventByEid(int eid) user function
            //2.be able to create events with non-registered users

            //now we can remove this number  from the  waiting list :
            mPrefsEditor.remove(properPhoneNumber);
            mPrefsEditor.commit();
        }

        Utils.LOGD("SmsListener:msg_body=" + mMessageBody + " :: msg_from=" + mMessageFrom);

        // WARNING!!!
        // If you uncomment the next line then received SMS will not be put to incoming.
        // Be careful!
        // this.abortBroadcast();

    }
}
