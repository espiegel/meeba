package com.meeba.google.activities;

import android.app.ProgressDialog;
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
import com.meeba.google.adapters.ContactsArrayAdapter;
import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.objects.Event;
import com.meeba.google.objects.User;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by or malka on 11/11/13.
 */
public class ContactsActivity extends SherlockActivity {
    private ListView mUserListView;
    private ContactsArrayAdapter mContactsAdapter;
    private String mWhen;
    private String mWhere;
    private int mHostUid;
    private List<String> mListUid;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_activity);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Choose Contacts");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        mUserListView = (ListView) findViewById(R.id.appContacts);

        Bundle bundle = getIntent().getExtras();
        mWhen = bundle.getString("when");
        mWhere = bundle.getString("where");

        mHostUid = DatabaseFunctions.getUserDetails(getApplicationContext()).getUid();

        mUserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User selectedUser = (User) adapterView.getAdapter().getItem(i);
                selectedUser.setSelected(true);
            }
        });

        asyncRefresh();
    }

    private void asyncRefresh() {
        new AsyncTask<Void, Void, List<User>>() {
            ProgressDialog progressDialog;

            protected void onPreExecute() {
                Utils.LOGD("onPreExecute");
                super.onPreExecute();
                progressDialog = ProgressDialog
                        .show(ContactsActivity.this, "Getting your contact list ", "please wait !", true);
            }

            protected List<User> doInBackground(Void... params) {
                return DatabaseFunctions.loadContacts(getApplicationContext());
            }

            protected void onPostExecute(List<User> list) {
                if (list == null) {
                    Utils.LOGD("getUsersByPhones returned null!");
                    progressDialog.dismiss();
                    return;
                }
                Utils.LOGD("onPostExecute");

                Utils.LOGD("list=...");
                for (User u : list) {
                    Utils.LOGD(u.toString());
                }
                mContactsAdapter = new ContactsArrayAdapter(ContactsActivity.this, list);
                mUserListView.setAdapter(mContactsAdapter);
                progressDialog.dismiss();

            }
        }.execute();
    }

    @Override
    protected void onDestroy() {
        if (mContactsAdapter != null) {
            mContactsAdapter.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.contacts_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_invite:
                invitePressed();
                return true;

            default:
                break;
        }
        onBackPressed();
        return true;
    }


    private void invitePressed() {
        mListUid = new ArrayList<String>();

        mContactsAdapter = (ContactsArrayAdapter) mUserListView.getAdapter();

        for (User user : mContactsAdapter.getList()) {
            Utils.LOGD("user= " + user.toString());
            if (user.isSelected()) {
                mListUid.add(String.valueOf(user.getUid()));
            }
        }

        if (mListUid.isEmpty()) {
            Utils.showToast(ContactsActivity.this, "You must select users to invite");
            return;
        }

        new AsyncTask<Void, Void, Event>() {
            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                Utils.LOGD("onPreExecute");
                super.onPreExecute();
                progressDialog = ProgressDialog
                        .show(ContactsActivity.this, "Inviting your friends", "please wait !", true);
            }

            @Override
            protected Event doInBackground(Void... voids) {
                return UserFunctions.createEvent(mHostUid, mWhere, mWhen, mListUid);
            }

            @Override
            protected void onPostExecute(Event event) {
                progressDialog.dismiss();

                if (event == null) {
                    Utils.showToast(ContactsActivity.this, "Failed to create event");
                } else {
                    Utils.showToast(ContactsActivity.this, "Created event successfully!");
                    Intent i = new Intent(ContactsActivity.this, EventPageActivity.class);
                    Bundle extras = new Bundle();

                    extras.putSerializable(Utils.BUNDLE_EVENT, event);

                    i.putExtras(extras);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
            }
        }.execute();
    }
}