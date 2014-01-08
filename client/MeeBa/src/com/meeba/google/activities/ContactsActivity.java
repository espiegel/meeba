package com.meeba.google.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.meeba.google.R;
import com.meeba.google.adapters.ContactsArrayAdapter;
import com.meeba.google.adapters.ContactsAutoCompleteAdapter;
import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.dialogs.ContactDetailsDialog;
import com.meeba.google.objects.Event;
import com.meeba.google.objects.User;
import com.meeba.google.util.JsonEventsStack;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;
import com.meeba.google.view.AutoCompleteClearableEditText;
import com.twotoasters.jazzylistview.JazzyHelper;
import com.twotoasters.jazzylistview.JazzyListView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * Created by or malka on 11/11/13.
 */
public class ContactsActivity extends SherlockFragmentActivity {


    public final static String EVENT = "event";
    public final static String GUESTS = "guests";

    private static JazzyListView mUserListView;
    private static ContactsArrayAdapter mContactsAdapter;

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

    // private EditText mFilterText;
    private ArrayList<User> mGuests;
    private boolean isAddingGuests = false;

    AutoCompleteClearableEditText mEditFilterContacts;
    private static List<User> forFilterList;
    private static List<User> inviteList;


    public void onCreate(Bundle savedInstanceState) {

        /**
         * max's test , will remove later.
         */


        Stack<String> test = new Stack<String>();
        test.push("123");
        test.push("124");
        test.push("456");
        test.push("678");

        String jsonArrString = null;
        try {
            jsonArrString = JsonEventsStack.toJson("6969", test);

            Utils.LOGD("jsonStack = " + jsonArrString);

            Utils.LOGD("jsonStackConverted to  = " + JsonEventsStack.getEventsStackfromJson(jsonArrString));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_activity);

        mSharedPrefs = this.getSharedPreferences("waitingList", MODE_PRIVATE);
        mPrefsEditor = mSharedPrefs.edit();
        // mPrefsEditor.clear();//just for debugging

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Choose Contacts");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        // mFilterText = (EditText) findViewById(R.id.filterContacts);
        mUserListView = (JazzyListView) findViewById(R.id.appContacts);
        mUserListView.setTransitionEffect(JazzyHelper.SLIDE_IN);

        mEditFilterContacts = (AutoCompleteClearableEditText) findViewById(R.id.filterContactsNew);
        mEditFilterContacts.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        /*mEditFilterContacts.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE) {

                    return true;
                }
                return false;
            }
        });*/

        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey(EVENT)) {
            mEvent = (Event) bundle.getSerializable(EVENT);
            mGuests = (ArrayList<User>) bundle.getSerializable(GUESTS);
            isAddingGuests = true;
            mWhen = mEvent.getWhen();
            mTitle = mEvent.getTitle();
            mWhere = mEvent.getWhere();
        } else {
            mEvent = null;
            mGuests = null;
            isAddingGuests = false;
            mWhen = bundle.getString("when");
            mTitle = bundle.getString("title");
            mWhere = bundle.getString("where");

            byte[] byteArray = bundle.getByteArray("event_picture");
            mPicture = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        }
        mHostUid = DatabaseFunctions.getUserDetails(getApplicationContext()).getUid();


        mUserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                User user = (User) adapterView.getItemAtPosition(position);
                removeFromInviteList(user);

            }
        });

        mUserListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                User user = (User) adapterView.getItemAtPosition(position);

                ContactDetailsDialog dialog = new ContactDetailsDialog(user);
                dialog.show(getSupportFragmentManager(), ContactDetailsDialog.TAG);
                return false;
            }
        });
        asyncRefresh();
    }

    private void asyncRefresh() {
        ProgressDialog progressDialog;

        Utils.LOGD("onPreExecute");
        progressDialog = ProgressDialog
                .show(ContactsActivity.this, "", "Loading contacts...", true, true);

        List<User> userList = DatabaseFunctions.loadContacts(getApplicationContext());
        List<User> sortedUserList = new ArrayList<User>();

        // Sort the user list by alphabetical order
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User user, User user2) {
                return user.getName().toLowerCase().compareTo(user2.getName().toLowerCase());
            }
        });

        if (mGuests != null) {
            for (User user : mGuests) {
                Utils.LOGD("mGuests user =" + user);
            }
        }
        // First add meeba users
        for (User user : userList) {
            Utils.LOGD("asyncRefresh user:" + user);
            if (user.getUid() != Utils.DUMMY_USER && user.getIs_dummy() != 1) {
                if (!isAddingGuests || (mGuests != null && !mGuests.contains(user))) {
                    sortedUserList.add(user);
                }
            }
        }

        // Then add the rest of the contact list
        for (User user : userList) {
            if (user.getUid() == Utils.DUMMY_USER || user.getIs_dummy() == 1) {
                if (!isAddingGuests || (mGuests != null && !mGuests.contains(user))) {
                    sortedUserList.add(user);
                }
            }
        }

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        Utils.LOGD("list=...");
        for (User u : sortedUserList) {
            Utils.LOGD(u.toString());
        }

        forFilterList = sortedUserList;
        inviteList = new ArrayList<User>();

        ContactsAutoCompleteAdapter autoCompleteAdapter = new ContactsAutoCompleteAdapter(ContactsActivity.this, R.layout.contacts_dropdown,
                R.id.personName, forFilterList, new ContactsAutoCompleteAdapter.SearchAutoComplete() {
            @Override
            public void autoCompleteItemClicked(User user) {
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditFilterContacts.getWindowToken(), 0);
                addToInviteList(user);
                mEditFilterContacts.setText("");
            }
        });
        mEditFilterContacts.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mEditFilterContacts.setThreshold(0);
        mEditFilterContacts.setAdapter(autoCompleteAdapter);

        mContactsAdapter = new ContactsArrayAdapter(ContactsActivity.this, inviteList);
        mUserListView.setAdapter(mContactsAdapter);
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

            if (user.getUid() != Utils.DUMMY_USER && user.getIs_dummy() == 0) {
                mListUid.add(String.valueOf(user.getUid()));
            } else {
                mDummies.add(user);
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

                //update the database after creating the dummies
                DatabaseFunctions.storeContacts(ContactsActivity.this, mPositiveDummies);

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
                String message = isAddingGuests ? "Adding Guests..." : "Creating Event...";
                progressDialog = ProgressDialog
                        .show(ContactsActivity.this, "", message, true, true);

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
                if (canceled) {
                    return null;
                }

                // Adding guests to an existing event
                if (isAddingGuests) {
                    // invite everyone in mListUid
                    for (String uid : mListUid) {
                        UserFunctions.addUserToEvent(mEvent.getEid(), Integer.valueOf(uid));
                    }
                    return mEvent;
                }
                // Otherwise we are creating a new event
                else {
                    Event event = UserFunctions.createEvent(mHostUid, mTitle, mWhere, mWhen, mListUid);
                    if (event == null) {
                        return null;
                    }

                    if (mPicture != null) {
                        String url = UserFunctions.uploadImage(event.getEid(), mPicture);
                        event.setEvent_picture(url);
                    }
                    return event;
                }
            }

            @Override
            protected void onPostExecute(Event event) {

                if (progressDialog != null)
                    progressDialog.dismiss();

                if (canceled)
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
        Utils.LOGD("in sendSms dumies = " + dummies);
        // Open a dialog asking the user whether he wants to send an sms
        if (!dummies.isEmpty()) {
            String separator = "; ";

            // Silly Samsung...
            if (android.os.Build.MANUFACTURER.equalsIgnoreCase("Samsung")) {
                separator = ", ";
            }

            /**for denugging :*/
            for (Map.Entry<String, ?> s : mSharedPrefs.getAll().entrySet()) {
                Utils.LOGD("waiting :: " + s);
            }

            String address = "";
            try {
                for (User dummy : dummies) {
                    if (TextUtils.isEmpty(address)) {
                        address = address.concat(dummy.getPhone_number());
                    } else {
                        address = address.concat(separator + dummy.getPhone_number());
                    }

                    /**put phone 'phone-number->eid+uid'  entry  in the waiting list :*/
                    Utils.pushToWaitingList(dummy.getPhone_number(), dummy.getUid(), mEvent.getEid(), mSharedPrefs);
                    //mPrefsEditor.putString(dummy.getPhone_number(), "" + mEvent.getEid() + "," + dummy.getUid());
                    //  mPrefsEditor.commit();
                }

                /**call sms intent :*/
                Utils.LOGD("sending sms to address=" + address);
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                String date = Utils.makePrettyDate(mEvent.getFormmatedWhen());
                Utils.LOGD("new Date = " + date);
                sendIntent.putExtra("address", address);
                sendIntent.putExtra("sms_body", "Hey, you're invited to " + mTitle + " at " + mWhere + " at " + date + "!\n" +
                        "to attend reply: 1\n" +
                        "to decline reply: 2\n" +
                        getString(R.string.generated));
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
        mSharedPrefs = this.getSharedPreferences("waitingList", MODE_PRIVATE);
        mPrefsEditor = mSharedPrefs.edit();
        //mPrefsEditor.clear();//just for debugging
    }

    private void addToInviteList(User user) {
        if (!inviteList.contains(user)) {
            mContactsAdapter = (ContactsArrayAdapter) mUserListView.getAdapter();
            mContactsAdapter.getList().add(user);
            mContactsAdapter.notifyDataSetChanged();
        }
        forFilterList.remove(user);
    }

    private void removeFromInviteList(User user) {
        mContactsAdapter = (ContactsArrayAdapter) mUserListView.getAdapter();
        mContactsAdapter.getList().remove(user);
        mContactsAdapter.notifyDataSetChanged();
        if (forFilterList.contains(user)) {
        } else {
            forFilterList.add(user);
        }
    }
}