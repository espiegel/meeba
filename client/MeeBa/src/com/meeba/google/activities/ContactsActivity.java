package com.meeba.google.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.Comparator;
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
                selectedUser.setSelected(! selectedUser.isSelected()); // invert the selection state
            }
        });

        mUserListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                User user = (User)adapterView.getItemAtPosition(position);

                final Dialog dialog = new Dialog(ContactsActivity.this, R.style.MeebaStyle_MeebaDialog);
                dialog.setContentView(R.layout.dialog_contact_details);
                ImageView contactPicture = (ImageView) dialog.findViewById(R.id.contactPicture);
                TextView contactName = (TextView) dialog.findViewById(R.id.contactName);
                TextView contactPhone = (TextView) dialog.findViewById(R.id.contactPhone);
                Button button = (Button) dialog.findViewById(R.id.button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                if(!TextUtils.isEmpty(user.getPicture_url())) {
                    Utils.getImageLoader(ContactsActivity.this).displayImage(user.getPicture_url(), contactPicture);
                }
                contactName.setText(user.getName());
                contactPhone.setText(user.getPhone_number());

                dialog.show();
                return false;
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
                List<User> userList = DatabaseFunctions.loadContacts(getApplicationContext());
                List<User> sortedUserList = new ArrayList<User>();

                // Sort the user list by alphabetical order
                Collections.sort(userList, new Comparator<User>() {
                    @Override
                    public int compare(User user, User user2) {
                        return user.getName().toLowerCase().compareTo(user2.getName().toLowerCase());
                    }
                });

                // First add meeba users
                for(User user : userList) {
                    if(user.getUid() != Utils.DUMMY_USER) {
                        sortedUserList.add(user);
                    }
                }

                // Then add the rest of the contact list
                for(User user : userList) {
                    if(user.getUid() == Utils.DUMMY_USER)
                    sortedUserList.add(user);
                }

                return sortedUserList;

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

        // Lets get all dummy users (ones without meeba)
        List<User> dummies = new ArrayList<User>();

        for (User user : mContactsAdapter.getList()) {
            Utils.LOGD("user= " + user.toString());
            if (user.isSelected()) {
                if(user.getUid() != Utils.DUMMY_USER) {
                    mListUid.add(String.valueOf(user.getUid()));
                } else {
                    dummies.add(user);
                }
            }
        }


        // Open a dialog asking the user whether he wants to send an sms
        if(!dummies.isEmpty()) {
            String separator = "; ";

            // Silly Samsung...
            if(android.os.Build.MANUFACTURER.equalsIgnoreCase("Samsung")){
                separator = ", ";
            }

            String address = "";
            for(User dummy : dummies) {
                if(TextUtils.isEmpty(address)) {
                    address = address.concat(dummy.getPhone_number());
                } else {
                    address = address.concat(separator+dummy.getPhone_number());
                }
            }

            try {
                Utils.LOGD("address="+address);
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.putExtra("address", address);
                sendIntent.putExtra("sms_body", "Hey, you're invited to "+mWhere+" at "+mWhen+"! Generated by MeeBa for Android: https://code.google.com/p/meeba/");
                sendIntent.setType("vnd.android-dir/mms-sms");
                startActivity(sendIntent);

            } catch (Exception e) {
                Toast.makeText(ContactsActivity.this,
                        "SMS sending failed, oops!",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

        if (mListUid.isEmpty()) {
            if(dummies.isEmpty()) {
                Utils.showToast(ContactsActivity.this, "You must select users to invite");
            }
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