package com.meeba.google.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.meeba.google.R;
import com.meeba.google.adapters.EventArrayAdapter;
import com.meeba.google.adapters.NavDrawerListAdapter;
import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.objects.Event;
import com.meeba.google.objects.NavDrawerItem;
import com.meeba.google.objects.User;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;
import com.twotoasters.jazzylistview.JazzyHelper;
import com.twotoasters.jazzylistview.JazzyListView;

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
    private JazzyListView mEventListView;
    private User mCurrentUser;
    private List<Event> mAllEventsList;
    private List<Event> mRejectedEventsList;
    private List<Event> mAcceptedEventsList;
    private List<Event> mUnknownEventsList;
    private EventArrayAdapter mEventArrayAdapter;
    private List<User> ListOfAppContacts;
    private PullToRefreshLayout mPullToRefreshLayout;

    private NavDrawerListAdapter adapter;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private final Activity dashboard = this;
    private ActionBarDrawerToggle mDrawerToggle;

    private final int FILTER_ALL_EVENTS = 0;
    private final int FILTER_CREATED_BY_ME = 1;
    private final int FILTER_GOING = 2;
    private final int FILTER_NOT_GOING = 3;
    private int appliedFilter = FILTER_ALL_EVENTS;

    private ImageView mNoEvent;
    private final Event mDummyEvent = new Event(-1, "", "", "", new User(-1, "", "", "", "", "", ""));

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Utils.LOGD("Dashboard activity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        // No event placeholder
        mNoEvent = (ImageView) findViewById(R.id.noEvent);

        /**initialize  event lists**/
        mRejectedEventsList = new ArrayList<Event>();
        mAcceptedEventsList = new ArrayList<Event>();
        mUnknownEventsList = new ArrayList<Event>();
        mAllEventsList = new ArrayList<Event>();

        /** Navigation drawer setup:*/
        ArrayList<NavDrawerItem> navDrawerItems = new ArrayList<NavDrawerItem>();
        String[] navMenuTitles = getResources().getStringArray(R.array.drawer_event_filters);
        TypedArray navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // adding nav drawer items to array
        // All Events
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[FILTER_ALL_EVENTS], navMenuIcons.getResourceId(FILTER_ALL_EVENTS, -1), false, "0"));
        //  I created
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[FILTER_CREATED_BY_ME], navMenuIcons.getResourceId(FILTER_CREATED_BY_ME, -1), false, "0"));
        // Going
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[FILTER_GOING], navMenuIcons.getResourceId(FILTER_GOING, -1), false, "0"));
        //Not  Going
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[FILTER_NOT_GOING], navMenuIcons.getResourceId(FILTER_NOT_GOING, -1), false, "0"));

        // Recycle the typed array
        navMenuIcons.recycle();

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);
        //make the first row selected as default
        mDrawerList.setItemChecked(0, true);

        // Adding nav drawer icon
        ActionBar ab = getSupportActionBar();
        ab.setTitle("Events");
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_navigation_drawer, //nav menu toggle icon - wont be showed, requires API level >10
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                // calling onPrepareOptionsMenu()
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                // calling onPrepareOptionsMenu()
                invalidateOptionsMenu();
            }
        };

        /**setup a ClickListener on  an Event Drawer Row**/
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                mNoEvent.setVisibility(View.GONE);
                NavDrawerItem selectedRow = (NavDrawerItem) adapter.getItem(position);
                selectedRow.getTitle();

                //create and show the filtered list,  and close the drawer

                appliedFilter = position; //filter type is the selected row's number
                List<Event> filteredList = filterEventList(mAllEventsList);

                if (filteredList.isEmpty()) {
                    mNoEvent.setVisibility(View.VISIBLE); // Show this also if the filter is empty
                    filteredList.add(mDummyEvent);
                }

                if (filteredList.get(0).getEid() == -1) {
                    mNoEvent.setVisibility(View.VISIBLE);
                }

                EventArrayAdapter filteredAdapter = new EventArrayAdapter(dashboard, filteredList);
                mEventListView.setAdapter(filteredAdapter);
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

        /** Pull to refresh setup:*/
        // find the PullToRefreshLayout to setup
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
                                asyncRefresh();
                            }
                        }
                )
                        // Finally commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);

        /** rest of onCreate :*/

        mCurrentUser = DatabaseFunctions.getUserDetails(getApplicationContext());
        if (mCurrentUser == null) {
            // We aren't registered so go back to login screen
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            mEventListView = (JazzyListView) findViewById(R.id.listViewDashboard);
            mEventListView.setTransitionEffect(JazzyHelper.HELIX);
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

    /**
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * filters the events list by the specified filter type
     */
    private List<Event> filterEventList(List<Event> eventsTofilter) {
        List<Event> filtered = new ArrayList<Event>();
        ActionBar ab = getSupportActionBar();

        switch (appliedFilter) {
            case FILTER_ALL_EVENTS:
                filtered = eventsTofilter;
                ab.setTitle("All Events");
                break;

            case FILTER_CREATED_BY_ME: {
                for (Event e : eventsTofilter)
                    if (e.getHost().getUid() == mCurrentUser.getUid())
                        filtered.add(e);
            }
            ab.setTitle("My Events");
            break;
            case FILTER_GOING:
                //mAcceptedEventsList is updated on asyncSortEvents
                filtered = mAcceptedEventsList;
                ab.setTitle("Accepted Events");
                break;

            case FILTER_NOT_GOING:
                //mRejectedEventsList is updated on asyncSortEvents
                filtered = mRejectedEventsList;
                ab.setTitle("Rejected  Events");
                break;

            default:
                Utils.LOGD(" not a valid filter :" + appliedFilter);
        }
        if (filtered.isEmpty()) filtered.add(mDummyEvent);
        return filtered;
    }

    private void asyncRefresh() {
        AsyncTask<Void, Void, List<Event>> task = new AsyncTask<Void, Void, List<Event>>() {
            // ProgressDialog progressDialog; using action bar animation instead
            boolean exceptionOccured = false;

            protected void onPreExecute() {
                Utils.LOGD("asyncRefresh onPreExecute");
                mNoEvent.setVisibility(View.GONE);
                super.onPreExecute();

                //animate action bar :
                mPullToRefreshLayout.setRefreshing(true);
            }

            protected List<Event> doInBackground(Void... params) {
                Utils.LOGD("asyncRefresh doInBackground");

                try {
                    mAllEventsList = UserFunctions.getEventsByUser(mCurrentUser.getUid());
                } catch (Exception e) {
                    exceptionOccured = true;
                }

                if (mAllEventsList == null) {
                    mAllEventsList = new ArrayList<Event>();
                }
                return mAllEventsList;
            }

            protected void onPostExecute(List<Event> allEvents) {
                Utils.LOGD("asyncRefresh onPostExecute");

                List<Event> filterdEvents;
                asyncSortEvents();  /** sort Events by status */
                if (exceptionOccured)
                    Toast.makeText(DashboardActivity.this,
                            "An Error occured when trying to update events ", Toast.LENGTH_LONG).show();

                //apply the chosen filter to the updated event list
                filterdEvents = filterEventList(mAllEventsList);

                if (filterdEvents.isEmpty() || filterdEvents.get(0).getEid() == -1) {
                    Utils.LOGD("maxEebug " + filterdEvents.size() + "first= " + filterdEvents.get(0).getEid());
                    mNoEvent.setVisibility(View.VISIBLE);
                } else {
                    mNoEvent.setVisibility(View.GONE);
                }

                mEventArrayAdapter = new EventArrayAdapter(dashboard, filterdEvents);
                mEventListView.setAdapter(mEventArrayAdapter);
                Utils.LOGD("list count =  " + mEventArrayAdapter.getCount());
            }
        };
        task.execute();
    }

    /**
     * this method fills the lists :  mAcceptedEventsList ,mRejectedEventsList ,mUnknownEventsList
     */
    private void asyncSortEvents() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            boolean exceptionOccured = false;

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {

                    List<Event> temp;
                    temp = UserFunctions.getEventsByUser(mCurrentUser.getUid(), 1);
                    mAcceptedEventsList = temp != null ? temp : new ArrayList<Event>();

                    temp = UserFunctions.getEventsByUser(mCurrentUser.getUid(), -1);
                    mRejectedEventsList = temp != null ? temp : new ArrayList<Event>();

                    temp = UserFunctions.getEventsByUser(mCurrentUser.getUid(), 0);
                    mUnknownEventsList = temp != null ? temp : new ArrayList<Event>();

                } catch (Exception e) {
                   /* Stack Trace is already printed in  UserFunctions*/
                    exceptionOccured = true;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                if (exceptionOccured)
                    Toast.makeText(DashboardActivity.this,
                            "An Error occured when trying to update events ", Toast.LENGTH_LONG).show();

                mPullToRefreshLayout.setRefreshing(false);
                mPullToRefreshLayout.setRefreshComplete();

                Utils.LOGD("list =  " + mAllEventsList + "\ngoing= " + mAcceptedEventsList +
                        "\nnotGoing= " + mRejectedEventsList + "\nunknown=" + mUnknownEventsList);
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

                if (ListOfAppContacts == null) {
                    return null;
                }

                List<String> meebaUsersPhones = new ArrayList<String>();
                for (User user : ListOfAppContacts) {
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
                for (Map.Entry<String, String> entry : phoneMap.entrySet()) {
                    contactMap.put(entry.getValue(), entry.getKey());
                }

                // Now add users that don't have meeba to the list
                for (Map.Entry<String, String> entry : contactMap.entrySet()) {
                    if (!meebaUsersPhones.contains(entry.getValue())) {
                        User user = new User(Utils.DUMMY_USER, "", entry.getKey(), entry.getValue(), "", "", "");
                        ListOfAppContacts.add(user);
                    }
                }

                if (ListOfAppContacts != null) {
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

            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                } else {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
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

    @Override
    public void onResume() {
        super.onResume();
        asyncRefresh();

    }

}