package com.meeba.google.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.meeba.google.R;
import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.objects.Event;
import com.meeba.google.objects.User;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by Eidan on 11/19/13.
 */
public class InvitationActivity extends Activity {
    private TextView mTxtHost;
    private TextView mTxtWhere;
    private TextView mTxtWhen;
    private Button mBtnAccept;
    private Button mBtnDecline;

    private int uid;
    private int eid;
    private String mWhere;
    private String mWhen;
    private String mHostName;

    private Event mEvent;
    private AsyncTask<Void, Void, Void> refreshHostPicture;
    private ImageLoader mImageLoader;
    private ImageView mImageHost;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invitation_activity);

        mTxtHost = (TextView) findViewById(R.id.txtHost);
        mTxtWhere = (TextView) findViewById(R.id.txtWhere);
        mTxtWhen = (TextView) findViewById(R.id.txtWhen);
        mImageHost = (ImageView) findViewById(R.id.hostPicture);

        mBtnAccept = (Button) findViewById(R.id.btnAccept);
        mBtnDecline = (Button) findViewById(R.id.btnDecline);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mEvent = (Event) bundle.getSerializable(Utils.BUNDLE_EVENT);
        Utils.LOGD("mEvent=" + mEvent);
        mHostName = mEvent.getHost_name();
        mWhere = mEvent.getWhere();
        mWhen = mEvent.getWhen();

        mTxtHost.setText(mHostName);
        mTxtWhere.setText(mWhere);
        mTxtWhen.setText(mWhen);
        eid = mEvent.getEid();

        uid = DatabaseFunctions.getUserDetails(getApplicationContext()).getUid();

        mBtnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Void, Void, Void>() {
                    ProgressDialog dialog;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        dialog = ProgressDialog.show(InvitationActivity.this, "Accepting...", "wait", true);

                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        UserFunctions.acceptInvite(uid, eid);

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void v) {
                        super.onPostExecute(v);
                        dialog.dismiss();

                        startEventPage();
                    }
                }.execute();
            }
        });

        mBtnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Void, Void, Void>() {
                    ProgressDialog dialog;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        dialog = ProgressDialog.show(InvitationActivity.this, "Declining...", "wait", true);

                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        UserFunctions.declineInvite(uid, eid);

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void v) {
                        super.onPostExecute(v);
                        dialog.dismiss();

                        startEventPage();
                    }
                }.execute();
            }
        });

        refreshHostPicture();
    }

    private void refreshHostPicture() {
        refreshHostPicture = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final User host = UserFunctions.getUserByUid(mEvent.getHost_uid());

                if (host == null) {
                    return null;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageLoader = ImageLoader.getInstance();
                        if(!mImageLoader.isInited()) {
                            mImageLoader.init(Utils.getImageLoaderConfig(InvitationActivity.this));
                        }
                        mImageLoader.displayImage(host.getPicture_url(), mImageHost);
                    }
                });
                return null;
            }
        };
        refreshHostPicture.execute();
    }

    private void startEventPage() {
        finish();
        Intent intent = new Intent(getApplicationContext(), EventPageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Utils.BUNDLE_EVENT, mEvent);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (refreshHostPicture != null) {
            refreshHostPicture.cancel(true);
        }

        if (mImageLoader != null) {
            mImageLoader.stop();
            mImageLoader.destroy();
        }

        super.onDestroy();
    }

}