package com.meeba.google.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.meeba.google.R;
import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;

import org.w3c.dom.Text;

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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invitation_activity);

        mTxtHost = (TextView)findViewById(R.id.txtHost);
        mTxtWhere = (TextView)findViewById(R.id.txtWhere);
        mTxtWhen = (TextView)findViewById(R.id.txtWhen);

        mBtnAccept = (Button)findViewById(R.id.btnAccept);
        mBtnDecline = (Button)findViewById(R.id.btnDecline);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        Utils.LOGD("bundle hostName="+bundle.getString("hostName"));
        mTxtHost.setText(bundle.getString("hostName"));
        mTxtWhere.setText(bundle.getString("where"));
        mTxtWhen.setText(bundle.getString("when"));

        uid = DatabaseFunctions.getUserDetails(getApplicationContext()).getUid();
        eid = bundle.getInt("eid");

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
                    }
                }.execute();
            }
        });
    }

}