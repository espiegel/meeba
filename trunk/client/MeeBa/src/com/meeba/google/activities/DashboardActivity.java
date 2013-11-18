package com.meeba.google.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.widget.ListView;
import android.content.Intent;

import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.objects.Event;
import com.meeba.google.objects.User;
import com.meeba.google.R;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;
import com.meeba.google.adapters.EventArrayAdapter;

import android.widget.ArrayAdapter;
import android.widget.Button;


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
   // private ArrayAdapter mEventArrayAdapter;
    private EventArrayAdapter mEventArrayAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        mCurrentUser = DatabaseFunctions.getUserDetails(getApplicationContext());

        mEventListView = (ListView)findViewById(R.id.listViewDashboard);

        mCreateEventBtn = (Button) findViewById(R.id.createEvent);
       // eventsItems = new ArrayList<String>();
        //mEventArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, eventsItems);






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
            ProgressDialog progressDialog;
            protected void onPreExecute() {
                Utils.LOGD("onPreExecute");
                super.onPreExecute();
                progressDialog = ProgressDialog
                        .show(DashboardActivity.this, "Getting your events ", "please wait !", true);
            }

            protected List<Event> doInBackground(Void... params) {
                Utils.LOGD("doInBackground");
                List<String> invites = new ArrayList<String>();
                invites.add(0,"1");
                invites.add(1,"7");


                UserFunctions.createEvent(mCurrentUser.getUid(),"Salat Aroma", "14:00",invites);


                Utils.LOGD("uid=" + mCurrentUser.getUid());
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



               // for (Event element : list) {
              //      String eventInfo= "where:" + element.getWhere() + " when:" + element.getWhen();
              //      eventsItems.add(0,eventInfo);
            //    }


                mEventListView.setAdapter(mEventArrayAdapter);
                progressDialog.dismiss();

            }

        };
        task.execute();

    }
}