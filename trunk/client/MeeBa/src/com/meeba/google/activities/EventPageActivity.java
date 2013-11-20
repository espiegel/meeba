package com.meeba.google.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.meeba.google.R;
import com.meeba.google.adapters.GuestArrayAdapter;
import com.meeba.google.objects.User;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;

import java.util.List;

/**
 * Created by Eidan on 11/19/13.
 */
public class EventPageActivity extends Activity {
    private TextView mTxtHost;
    private TextView mTxtWhere;
    private TextView mTxtWhen;
    private ListView mListView;

    private int eid;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventpage_activity);

        mTxtHost = (TextView)findViewById(R.id.txtHost);
        mTxtWhere = (TextView)findViewById(R.id.txtWhere);
        mTxtWhen = (TextView)findViewById(R.id.txtWhen);
        mListView = (ListView)findViewById(R.id.listGuests);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        Utils.LOGD("bundle hostName=" + bundle.getString("hostName"));
        mTxtHost.setText(bundle.getString("hostName"));
        mTxtWhere.setText(bundle.getString("where"));
        mTxtWhen.setText(bundle.getString("when"));

        eid = bundle.getInt("eid");

        refreshGuests();
    }

    private void refreshGuests() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                List<User> guestList = UserFunctions.getUsersByEvent(eid);
                if(guestList == null) {
                    return null;
                }

                final GuestArrayAdapter adapter = new GuestArrayAdapter(EventPageActivity.this,  guestList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListView.setAdapter(adapter);
                    }
                });

                return null;
            }
        }.execute();
    }
}