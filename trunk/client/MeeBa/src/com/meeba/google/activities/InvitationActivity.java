package com.meeba.google.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.meeba.google.R;
import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.objects.Event;
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
    private TextView mTxtTitle;
    private Button mBtnAccept;
    private Button mBtnDecline;

    private int mUid;
    private int mEid;
    private String mWhere;
    private String mWhen;
    private String mHostName;
    private String mTitle;

    private Event mEvent;
    private ImageLoader mImageLoader;
    private ImageView mImageHost;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invitation_activity);

        mTxtHost = (TextView) findViewById(R.id.txtHost);
        mTxtWhere = (TextView) findViewById(R.id.txtWhere);
        mTxtWhen = (TextView) findViewById(R.id.txtWhen);
        mTxtTitle = (TextView) findViewById(R.id.txtTitle);
        mImageHost = (ImageView) findViewById(R.id.hostPicture);

        mBtnAccept = (Button) findViewById(R.id.btnAccept);
        mBtnDecline = (Button) findViewById(R.id.btnDecline);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mEvent = (Event) bundle.getSerializable(Utils.BUNDLE_EVENT);
        Utils.LOGD("mEvent=" + mEvent);
        mHostName = mEvent.getHost().getName();
        mWhere = mEvent.getWhere();
        mWhen = mEvent.getWhen();
        mTitle = mEvent.getTitle();

        mTxtHost.setText(mHostName);
        mTxtWhere.setText(mWhere);
        mTxtWhen.setText(mWhen);
        mTxtTitle.setText(mTitle);
        mEid = mEvent.getEid();

        mUid = DatabaseFunctions.getUserDetails(getApplicationContext()).getUid();

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
                        UserFunctions.acceptInvite(mUid, mEid);

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
                        UserFunctions.declineInvite(mUid, mEid);

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

        mImageLoader = Utils.getImageLoader(this);
        if(mEvent != null && mEvent.getHost() != null && !TextUtils.isEmpty(mEvent.getHost().getPicture_url())) {
            mImageLoader.displayImage(mEvent.getHost().getPicture_url(), mImageHost);
        }
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
}