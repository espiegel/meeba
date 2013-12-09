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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by Padi on 07/11/13.
 */


public class DashboardActivity extends SherlockActivity {
    private ListView mEventListView;
    private User mCurrentUser;
    private List<Event> list;
    private EventArrayAdapter mEventArrayAdapter;
    private List<User> ListOfAppContacts;
    private PullToRefreshLayout mPullToRefreshLayout;
    private boolean pullTorefresh = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.LOGD("Dashboard activity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        // Now find the PullToRefreshLayout to setup
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(this)
                // Mark All Children as pullable
                .allChildrenArePullable()
                        // Set the OnRefreshListener
                .listener(
                        new OnRefreshListener() {
                            @Override
                            public void onRefreshStarted(View view) {
                                pullTorefresh=true;
                                asyncRefresh();
                            }
                        }
                )
                        // Finally commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);

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
            ImageView noEvent;
            ProgressDialog progressDialog;
            boolean exceptionOccured = false;

            protected void onPreExecute() {
                Utils.LOGD("onPreExecute");
                noEvent = (ImageView) findViewById(R.id.noEvent);
                noEvent.setVisibility(View.GONE);
                super.onPreExecute();
                if (!pullTorefresh) {
                    progressDialog = ProgressDialog
                            .show(DashboardActivity.this, getString(R.string.refreshing_events), getString(R.string.please_wait), true);
                }
            }


            protected List<Event> doInBackground(Void... params) {
                Utils.LOGD("doInBackground");

                try {
                    list = UserFunctions.getEventsByUser(mCurrentUser.getUid());
                } catch (Exception e) {
                    exceptionOccured = true;
                }

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
                if (exceptionOccured)
                    Toast.makeText(DashboardActivity.this, "No Internet Connection\n connect  to internet and refresh ",
                            Toast.LENGTH_LONG).show();

                /*
                // add a dummy event  with eid=-1 when the list is empty ,to allow pull to refresh on an "empty" table
                //the EventArrayAdapter makes events with eid=-1 invisible
             if(events.size()==0) {
                   Event dummyEvent = new Event( -1,-1,"dummyEvent","dummyEvent","dummyEvent"  );
                    events.add(0,dummyEvent);
             }
            */

             //if you have no events this will show the logo and text "you have no events"
             if (events.isEmpty()){
                    noEvent.setVisibility(View.VISIBLE);

            }else{
                try {noEvent.setVisibility(View.GONE);
                } catch (Exception e) {/* nothing */ }
                //update the event list view
                mEventArrayAdapter = new EventArrayAdapter(dashboard, events);
                mEventListView.setAdapter(mEventArrayAdapter);
            }
                try {
                    progressDialog.dismiss();
                } catch (Exception e) {/* nothing */ }

                mPullToRefreshLayout.setRefreshComplete();  // Notify PullToRefreshLayout that  refresh has finished
                pullTorefresh=false; //initialize pullTorefresh
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
                Map<String, String> phoneMap = Utils.allPhoneNumbersAndName(getContentResolver());
                for (Map.Entry<String, String> entry : phoneMap.entrySet()) {
                    Utils.LOGD(entry.getKey() + ", " + entry.getValue());
                }

                List<String> phoneList = Utils.phoneList(phoneMap);
                ListOfAppContacts = UserFunctions.getUsersByPhones(phoneList);

                List<String> meebaUsersPhones = new ArrayList<String>();
                for(User user : ListOfAppContacts) {
                    meebaUsersPhones.add(user.getPhone_number());
                }

                // Create an 'ordered' map so that we can sort contacts alphabetically
                TreeMap<String, String> contactMap = new TreeMap<String, String>(new Comparator<String>() {
                    @Override
                    public int compare(String s, String s2) {
                        return s.toLowerCase().compareTo(s2.toLowerCase());
                    }
                });

                // Now swap around the key and the value so that the contacts will go in the tree map ordered by name
                for(Map.Entry<String, String> entry : phoneMap.entrySet()) {
                    contactMap.put(entry.getValue(), entry.getKey());
                }

                // Now add users that don't have meeba to the list
                for(Map.Entry<String, String> entry : contactMap.entrySet()) {
                    if(!meebaUsersPhones.contains(entry.getValue())) {
                        User user = new User(Utils.DUMMY_USER, "", entry.getKey(), entry.getValue(), "", "", "");
                        ListOfAppContacts.add(user);
                    }
                }

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