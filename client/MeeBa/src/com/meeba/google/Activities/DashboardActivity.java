package com.meeba.google.Activities;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.widget.ListView;
import android.content.Intent;

import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.Objects.Event;
import com.meeba.google.Objects.User;
import com.meeba.google.R;
import com.meeba.google.Util.UserFunctions;
import com.meeba.google.Util.Utils;
import com.meeba.google.adapters.EventArrayAdapter;

import android.widget.Button;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by Padi on 07/11/13.
 */
public class DashboardActivity extends Activity {
    private ListView mEventListView;
    private EventArrayAdapter mEventArrayAdapter;
    private Button mCreateEventBtn;
    private User mCurrentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        mCurrentUser = DatabaseFunctions.getUserDetails(getApplicationContext());

        mEventListView = (ListView)findViewById(R.id.listViewDashboard);

        mCreateEventBtn = (Button) findViewById(R.id.createEvent);

        mCreateEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.LOGD("on click");
                //when we click on the button it will bring us to the  WhereWhenActivity activity
                Intent i = new Intent(getApplicationContext(),
                        WhereWhenActivity.class);
                startActivity(i);

            }
        });

        asyncRefresh();
    }

    private void asyncRefresh() {
        final Activity dashboard = this;
        AsyncTask<Void, Void, List<Event>> task = new AsyncTask<Void, Void, List<Event>>() {
            protected void onPreExecute() {
                Utils.LOGD("onPreExecute");
            }

            protected List<Event> doInBackground(Void... params) {
                Utils.LOGD("doInBackground");

                Utils.LOGD("uid="+mCurrentUser.getUid());
                List<Event> list = UserFunctions.getEventsByUser(mCurrentUser.getUid());

                if(list == null) {
                    return new ArrayList<Event>();
                } else {
                    return list;
                }

            }

            protected void onPostExecute(List<Event> events) {
                Utils.LOGD("onPostExecute");

                // update the event list view
                mEventArrayAdapter = new EventArrayAdapter(dashboard, events);
                Utils.LOGD(mEventListView.toString());
                mEventListView.setAdapter(mEventArrayAdapter);

            }

        };
        task.execute();

    }
}