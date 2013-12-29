package com.meeba.google.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.meeba.google.R;
import com.meeba.google.adapters.ContactsArrayAdapter;
import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.dialogs.ContactDetailsDialog;
import com.meeba.google.objects.Event;
import com.meeba.google.objects.User;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;
import com.twotoasters.jazzylistview.JazzyHelper;
import com.twotoasters.jazzylistview.JazzyListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


/**
 * Created by or malka on 11/11/13.
 */
public class ContactsActivity extends SherlockFragmentActivity {
    private JazzyListView mUserListView;
    private ContactsArrayAdapter mContactsAdapter;
    private String mWhen;
    private String mTitle;
    private String mWhere;
    private int mHostUid;
    private List<String> mListUid;
    private List<User> mDummies;
    private List<User> mPositiveDummies;
    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mPrefsEditor;
    private Event mEvent;
    private Bitmap mPicture;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_activity);

        mSharedPrefs = this.getSharedPreferences("waitingList", 0); // 0 - for private mode
        mPrefsEditor = mSharedPrefs.edit();
        // mPrefsEditor.clear();//just for debugging

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Choose Contacts");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        mUserListView = (JazzyListView) findViewById(R.id.appContacts);
        mUserListView.setTransitionEffect(JazzyHelper.SLIDE_IN);

        Bundle bundle = getIntent().getExtras();
        mWhen = bundle.getString("when");
        mTitle = bundle.getString("title");
        mWhere = bundle.getString("where");

        byte[] byteArray = bundle.getByteArray("event_picture");
        mPicture = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        mHostUid = DatabaseFunctions.getUserDetails(getApplicationContext()).getUid();

        mUserListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                User user = (User)adapterView.getItemAtPosition(position);

                ContactDetailsDialog dialog = new ContactDetailsDialog(user);
                dialog.show(getSupportFragmentManager(), ContactDetailsDialog.TAG);
                return false;
            }
        });
        asyncRefresh();
    }

    private void asyncRefresh() {
        new AsyncTask<Void, Void, List<User>>() {
            ProgressDialog progressDialog;
            boolean canceled=false;

            protected void onPreExecute() {
                Utils.LOGD("onPreExecute");
                super.onPreExecute();
                progressDialog = ProgressDialog
                        .show(ContactsActivity.this, "", "Loading  contacts...", true,true);

                progressDialog.setCanceledOnTouchOutside(false);

                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        canceled = true;
                    }
                });
            }

            protected List<User> doInBackground(Void... params) {

                if(canceled)
                    return null;

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
                for (User user : userList) {
                    Utils.LOGD("asyncRefresh user:" + user);
                    if (user.getUid() != Utils.DUMMY_USER && user.getIs_dummy() != 1) {
                        sortedUserList.add(user);
                    }
                }

                // Then add the rest of the contact list
                for (User user : userList) {
                    if (user.getUid() == Utils.DUMMY_USER || user.getIs_dummy() == 1)
                        sortedUserList.add(user);
                }
                return sortedUserList;
            }

            protected void onPostExecute(List<User> list) {

                if(progressDialog!=null)
                    progressDialog.dismiss();

                if(canceled)
                    finish();

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

            }
        }.execute();
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
        mDummies = new ArrayList<User>();

        for (User user : mContactsAdapter.getList()) {
            Utils.LOGD("user= " + user.toString());
            if (user.isSelected()) {
                if (user.getUid() != Utils.DUMMY_USER && user.getIs_dummy() == 0) {
                    mListUid.add(String.valueOf(user.getUid()));
                } else {
                    mDummies.add(user);
                }
            }
        }

        if (mListUid.isEmpty()) {
            if (mDummies.isEmpty()) {
                Utils.showToast(ContactsActivity.this, "You must select users to invite");
                return;
            }
            // sendSms(mDummies);
            //return;
        }

        //populates mPositiveDummies with dummies with uid>0 and isDummy=true;
        //and then calls asyncCreateEvent;

        asyncCreateDummies(mDummies);
        // asyncCreateEvent();


/*
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                Utils.LOGD("onPreExecute");
                super.onPreExecute();
                progressDialog = ProgressDialog
                        .show(ContactsActivity.this, "Inviting your friends", "please wait !", true);
            }
            
            @Override
            protected Void doInBackground(Void... voids) {
                mEvent= UserFunctions.createEvent(mHostUid,mTitle, mWhere, mWhen, mListUid);
                if(mPicture != null) {
                    UserFunctions.uploadImage(mEvent.getEid(), mPicture);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                progressDialog.dismiss();

                Utils.LOGD("newly created event ="+mEvent);
                if (mEvent == null) {
                    Utils.showToast(ContactsActivity.this, "Failed to create event");
                } else {
                    Utils.showToast(ContactsActivity.this, "Created event successfully!");
                    Intent i = new Intent(ContactsActivity.this, EventPageActivity.class);
                    Bundle extras = new Bundle();

                    extras.putSerializable(Utils.BUNDLE_EVENT, mEvent);

                    i.putExtras(extras);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);

                    if(!mDummies.isEmpty()) {
                        sendSms(mDummies);
                    }
                    finish();
                }
            }
        }.execute();
        */
    }

    private void asyncCreateDummies(final List<User> negativeDummies) {
        final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            ProgressDialog progressDlg;
            boolean canceled = false;

            @Override
            protected void onPreExecute() {
                Utils.LOGD("asyncCreateDummies onPreExecute");

                progressDlg = ProgressDialog
                        .show(ContactsActivity.this, "", "inviting...", true, true);

                progressDlg.setCanceledOnTouchOutside(false);

                progressDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        canceled = true;
                    }
                });
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                Utils.LOGD("asyncCreateDummies doInBackground");

                if (canceled)
                    return null;

                mPositiveDummies = UserFunctions.createDummyUsers(negativeDummies, ContactsActivity.this);

                // add all dummy uids to mListUid : this will make the event to be created including the dummies
                for (User dummy : mPositiveDummies) {
                    mListUid.add(String.valueOf(dummy.getUid()));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                if (progressDlg != null)
                    progressDlg.dismiss();
                if (!canceled)
                    asyncCreateEvent();
            }
        };
        task.execute();
    }

    private void asyncCreateEvent() {
        AsyncTask<Void, Void, Event> task = new AsyncTask<Void, Void, Event>() {
            ProgressDialog progressDialog;
            boolean canceled = false;

            @Override
            protected void onPreExecute() {
                Utils.LOGD("onPreExecute");
                super.onPreExecute();
                progressDialog = ProgressDialog
                        .show(ContactsActivity.this, "", "Creating Event...", true, true);

                progressDialog.setCanceledOnTouchOutside(false);

                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        canceled = true;
                    }
                });
            }

            @Override
            protected Event doInBackground(Void... params) {
                if(canceled)
                    return null;

                Event event = UserFunctions.createEvent(mHostUid, mTitle, mWhere, mWhen, mListUid);
                if(event == null) {
                    return null;
                }
            //   mEvent = UserFunctions.createEvent(mHostUid, mTitle, mWhere, mWhen, mListUid);
           
                if (mPicture != null) {
                    String url = UserFunctions.uploadImage(event.getEid(), mPicture);
                    event.setEvent_picture(url);
                }
                return event;
            }

            @Override
            protected void onPostExecute(Event event) {

                if (progressDialog != null)
                    progressDialog.dismiss();

                if(canceled)
                    finish();

                mEvent = event;
                Utils.LOGD("newly created event =" + event);
                if (event == null) {
                    Utils.showToast(ContactsActivity.this, "Failed to create event");
                } else {
                    Toast.makeText(ContactsActivity.this, "Invite contacts without MeeBa by sms !", Toast.LENGTH_LONG);
                    Intent i = new Intent(ContactsActivity.this, EventPageActivity.class);
                    Bundle extras = new Bundle();

                    extras.putSerializable(Utils.BUNDLE_EVENT, mEvent);

                    i.putExtras(extras);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);

                    if (!mPositiveDummies.isEmpty() && !canceled) {
                        sendSms(mPositiveDummies);
                    }
                    finish();
                }
            }
        };
        task.execute();
    }


    private void sendSms(List<User> dummies) {
        List<String> stillWaiting = new ArrayList<String>();
        // Open a dialog asking the user whether he wants to send an sms
        if (!dummies.isEmpty()) {
            String separator = "; ";

            // Silly Samsung...
            if (android.os.Build.MANUFACTURER.equalsIgnoreCase("Samsung")) {
                separator = ", ";
            }

            String address = "";
            for (User dummy : dummies) {
                if (TextUtils.isEmpty(address)) {
                    address = address.concat(dummy.getPhone_number());
                } else {
                    address = address.concat(separator + dummy.getPhone_number());
                }
                for (String s : mSharedPrefs.getAll().keySet()) {
                    Utils.LOGD("waiting :: " + s);
                }
                //put phone 'phone-number->eid+uid'  entry  in the waiting list .
                mPrefsEditor.putString(dummy.getPhone_number(), "" + mEvent.getEid() + "," + dummy.getUid());
            }

            try {
                mPrefsEditor.commit();
                for (Map.Entry<String, ?> s : mSharedPrefs.getAll().entrySet()) {
                    String eid = ((String) s.getValue()).split(",")[0];
                    String uid = ((String) s.getValue()).split(",")[1];
                    Utils.LOGD(":: uid =" + uid + " ::  eid = " + eid);
                }

                Utils.LOGD("address=" + address);
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.putExtra("address", address);
                sendIntent.putExtra("sms_body", "Hey, you're invited to " + mTitle + " at " + mWhere + " at " + mWhen + "!\n" +
                        "to attend reply: 1\n" +
                        "to decline reply: 2\n" +
                        "Generated by MeeBa for Android: http://goo.gl/gWGoKx");
                sendIntent.setType("vnd.android-dir/mms-sms");

                startActivity(sendIntent);

            } catch (Exception e) {
                Toast.makeText(ContactsActivity.this,
                        "SMS sending failed, oops!",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
    protected void onResume() {
        super.onResume();
        mSharedPrefs = this.getSharedPreferences("waitingList", 0); // 0 - for private mode
        mPrefsEditor = mSharedPrefs.edit();
        //mPrefsEditor.clear();//just for debugging
    }

}