package com.meeba.google.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.content.Intent;

import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.objects.Event;
import com.meeba.google.objects.User;
import com.meeba.google.R;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;
import com.meeba.google.adapters.EventArrayAdapter;

import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Padi on 07/11/13.
 */
public class DashboardActivity extends Activity {
    private ListView mEventListView;
    private List<String> eventsItems;
    private Button mCreateEventBtn;
    private User mCurrentUser;
    private List<Event> list;
    private EventArrayAdapter mEventArrayAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        mCurrentUser = DatabaseFunctions.getUserDetails(getApplicationContext());
          if( mCurrentUser == null ){
                Utils.LOGD("diana : user was null !!!!!!!");
                Toast.makeText(getApplicationContext(), "user  is null !", Toast.LENGTH_LONG).show();
                return;
          }

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

        mEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Event event = ((Event)mEventListView.getAdapter().getItem(position));

                if(event == null) {
                    Utils.LOGD("event is null!");
                    return;
                }

                int eid = event.getEid();
                String where = event.getWhere();
                String when = event.getWhen();
                String hostName = event.getHost_name();

                Intent intent = new Intent(DashboardActivity.this, EventPageActivity.class);
                Bundle extras = new Bundle();

                extras.putInt("eid", eid);
                extras.putString("where",where);
                extras.putString("when", when);
                extras.putString("hostName", hostName);

                intent.putExtras(extras);
                startActivity(intent);

            }
        });
        asyncRefresh();
    }

    private void asyncRefresh() {
        final Activity dashboard = this;
        AsyncTask<Void, Void, List<Event>> task = new AsyncTask<Void, Void, List<Event>>() {
            ProgressDialog progressDialog;
            protected void onPreExecute() {
                Utils.LOGD("onPreExecute");
                super.onPreExecute();
                progressDialog = ProgressDialog
                        .show(DashboardActivity.this, "Getting your events ", "please wait !", true);
            }

            protected List<Event> doInBackground(Void... params) {
                Utils.LOGD("doInBackground");

                Utils.LOGD("diana:uid=" + mCurrentUser.getUid());
                list = UserFunctions.getEventsByUser(mCurrentUser.getUid());

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
                mEventListView.setAdapter(mEventArrayAdapter);
                progressDialog.dismiss();
            }
        };
        task.execute();

    }
}