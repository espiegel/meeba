package com.meeba.google;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;

import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;

import java.util.Map;
import java.util.Stack;

/**
 * Created by Max on 25/12/13.
 */
public class SmsIntentService extends IntentService {
    private String mMessageFrom;
    private String mMessageBody;

    public SmsIntentService() {
        super("SmsIntentService");
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Utils.LOGD("SmsListener: inside onHandleIntent!");
        SharedPreferences mSharedPrefs = this.getSharedPreferences("waitingList", 0);
        SharedPreferences.Editor mPrefsEditor = mSharedPrefs.edit();

        for (Map.Entry<String, ?> entry : mSharedPrefs.getAll().entrySet()) {
            Utils.LOGD("SmsListener: waiting " + entry);
        }


        //parse the sms and store in  mMessageBody &  mMessageFrom
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
        mMessageFrom = Utils.sanitizePhoneNumber(mMessageFrom);

        //for debugging:
        Utils.LOGD("SmsListener:msg_body=" + mMessageBody + " :: msg_from=" + mMessageFrom);
        for (Map.Entry<String, ?> s : mSharedPrefs.getAll().entrySet()) {
            String eid = ((String) s.getValue()).split(",")[0];
            String uid = ((String) s.getValue()).split(",")[1];
            Utils.LOGD("SmsListener: in waiting list : uid =" + uid + " ::  eid = " + eid);
        }

        //check if the sender's phone number is in the waiting list
        if (mSharedPrefs.getAll().keySet().contains(mMessageFrom)) {

            String uidAndEid = mSharedPrefs.getString(mMessageFrom, null);

            if (uidAndEid == null) {
                Utils.LOGD("SmsListener: a very  weired error happened");
                return;
            }

            int eid = Integer.valueOf(uidAndEid.split(",")[0]);
            int uid = Integer.valueOf(uidAndEid.split(",")[1]);

            //accept invite
            if (mMessageBody.contains("1")) {
                UserFunctions.acceptInvite(uid, eid);
                mPrefsEditor.remove(mMessageFrom);
                mPrefsEditor.commit();
            }

            //decline invite
            else if (mMessageBody.contains("2")) {
                UserFunctions.declineInvite(uid, eid);
                mPrefsEditor.remove(mMessageFrom);
                mPrefsEditor.commit();
            } else {
                /* Do Noting */
                Utils.LOGD("SmsListener:sms body is not of the right form");
            }
        }


        // WARNING!!!
        // If you uncomment the next line then received SMS will not be put to incoming.
        // Be careful!
        // this.abortBroadcast();

    }
}
