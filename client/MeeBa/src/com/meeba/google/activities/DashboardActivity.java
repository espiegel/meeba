package com.meeba.google.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.meeba.google.R;
import com.meeba.google.adapters.EventArrayAdapter;
import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.objects.Event;
import com.meeba.google.objects.User;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Padi on 07/11/13.
 */


public class DashboardActivity extends SherlockActivity {
    private ListView mEventListView;
    private User mCurrentUser;
    private List<Event> list;
    private EventArrayAdapter mEventArrayAdapter;
    private List<User> ListOfAppContacts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.LOGD("Dashboard activity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Events");

        mCurrentUser = DatabaseFunctions.getUserDetails(getApplicationContext());
        if (mCurrentUser == null) {
            // We aren't registered so go back to login screen
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            mEventListView = (ListView) findViewById(R.id.listViewDashboard);
            mEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Event event = ((Event) mEventListView.getAdapter().getItem(position));

                    if (event == null) {
                        Utils.LOGD("event is null!");
                        return;
                    }

                    Intent intent = new Intent(DashboardActivity.this, EventPageActivity.class);
                    Bundle extras = new Bundle();

                    extras.putSerializable(Utils.BUNDLE_EVENT, event);

                    intent.putExtras(extras);
                    startActivity(intent);
                }
            });
            asyncRefresh();
            asyncUpdateContacts();
        }
    }


    private void asyncRefresh() {
        final Activity dashboard = this;
        AsyncTask<Void, Void, List<Event>> task = new AsyncTask<Void, Void, List<Event>>() {
            ProgressDialog progressDialog;

            protected void onPreExecute() {
                Utils.LOGD("onPreExecute");
                super.onPreExecute();
                progressDialog = ProgressDialog
                        .show(DashboardActivity.this, getString(R.string.refreshing_events), getString(R.string.please_wait), true);
            }

            protected List<Event> doInBackground(Void... params) {
                Utils.LOGD("doInBackground");
                list = UserFunctions.getEventsByUser(mCurrentUser.getUid());

                Utils.LOGD("list =  " + list);

                if (list == null) {
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

                try {
                    progressDialog.dismiss();
                } catch (Exception e) {/* nothing */ }

            }
        };
        task.execute();
    }

    /**
     * get user contacts who have meeba and store the on phone DB
     */
    private void asyncUpdateContacts() {
        new AsyncTask<Void, Void, Void>() {

            protected void onPreExecute() {
                Utils.LOGD("onPreExecute");
            }

            protected Void doInBackground(Void... params) {
                Utils.LOGD("asyncUpdateContacts  doInBackground");
                HashMap<String, String> phoneMap = Utils.allPhoneNumbersAndName(getContentResolver());
                for (Map.Entry<String, String> entry : phoneMap.entrySet()) {
                    Utils.LOGD(entry.getKey() + ", " + entry.getValue());
                }

                ListOfAppContacts = UserFunctions.getUsersByPhones(Utils.phoneList(phoneMap));
                if(ListOfAppContacts != null) {
                    DatabaseFunctions.storeContacts(getApplicationContext(), ListOfAppContacts);
                }

                return null;
            }

            protected void onPostExecute(Void v) {
                for (User user : DatabaseFunctions.loadContacts(getApplicationContext()))
                    Utils.LOGD("contact loaded :  " + user);
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_create_event:
                createEvent();
                break;

            default:
                break;
        }

        return true;
    }

    private void createEvent() {
        Intent i = new Intent(getApplicationContext(),
                WhereWhenActivity.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DashboardActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}