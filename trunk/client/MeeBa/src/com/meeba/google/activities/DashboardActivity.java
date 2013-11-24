package com.meeba.google.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.content.Intent;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
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
public class DashboardActivity extends SherlockActivity {
    private ListView mEventListView;
    private Button mCreateEventBtn;
    private User mCurrentUser;
    private List<Event> list;
    private EventArrayAdapter mEventArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
            return;
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

                    int eid = event.getEid();
                    String where = event.getWhere();
                    String when = event.getWhen();
                    String hostName = event.getHost_name();

                    Intent intent = new Intent(DashboardActivity.this, EventPageActivity.class);
                    Bundle extras = new Bundle();

                    extras.putInt("eid", eid);
                    extras.putString("where", where);
                    extras.putString("when", when);
                    extras.putString("hostName", hostName);

                    intent.putExtras(extras);
                    startActivity(intent);

                }
            });
            asyncRefresh();
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
                        .show(DashboardActivity.this, "Getting your events ", "please wait !", true);
            }

            protected List<Event> doInBackground(Void... params) {
                Utils.LOGD("doInBackground");

                Utils.LOGD("diana:uid=" + mCurrentUser.getUid());
                list = UserFunctions.getEventsByUser(mCurrentUser.getUid());

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
                progressDialog.dismiss();
            }
        };
        task.execute();

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