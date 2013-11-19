package com.meeba.google;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.meeba.google.activities.InvitationActivity;
import com.meeba.google.util.Utils;


public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;

    private static final String TAG_INVITE = "invite";
    private static final String TITLE_INVITE = "MeeBa Invitation from ";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Utils.LOGD("GcmIntentService: inside onHandleIntent!");
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            String tag = extras.getString("tag");
            String when = extras.getString("when");
            String where = extras.getString("where");
            String hostName = extras.getString("hostName");

            Utils.LOGD("hostName="+hostName);
            int eid = Integer.valueOf(extras.getString("eid"));

            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Utils.LOGD("GCM: Error sending a GCM message");
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                if(tag.equals(TAG_INVITE)) {
                    sendNotification(tag, hostName, when, where, eid);
                    Utils.LOGD("GCM: Received a message " + extras.toString());
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String tag, String hostName, String when, String where, int eid) {
        if(tag.equals(TAG_INVITE)) {
            mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

            Intent intent = new Intent(this, InvitationActivity.class);
            Bundle bundle = new Bundle();

            bundle.putString("hostName", hostName);
            bundle.putString("when", when);
            bundle.putString("where", where);
            bundle.putInt("eid", eid);

            intent.putExtras(bundle);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    intent, 0);

            String msg = where + " at " + when;
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(TITLE_INVITE+hostName)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(msg))
                            .setContentText(msg);

            mBuilder.setContentIntent(contentIntent);
            Notification notif = mBuilder.build();

            notif.flags |= Notification.FLAG_AUTO_CANCEL;
            notif.defaults |= Notification.DEFAULT_ALL;

            mNotificationManager.notify(NOTIFICATION_ID, notif);
        }
    }
}