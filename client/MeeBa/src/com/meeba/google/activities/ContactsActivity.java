package com.meeba.google.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.meeba.google.R;
import com.meeba.google.adapters.ContactsArrayAdapter;
import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.database.DatabaseHandler;
import com.meeba.google.objects.Event;
import com.meeba.google.objects.User;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by or malka on 11/11/13.
 */
public class ContactsActivity extends SherlockActivity {
    private Button next;
    private List<User> ListOfAppContacts;
    private Cursor cursor;
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

        //just checking with this button - i will change it to bring us to dayana's activity
        next = (Button) findViewById(R.id.btnInvite);

        Bundle bundle = getIntent().getExtras();
        mWhen = bundle.getString("when");
        mWhere = bundle.getString("where");

        mHostUid = DatabaseFunctions.getUserDetails (getApplicationContext()  , DatabaseHandler.TABLE_USER).getUid();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //here should be:
                //send details to server/dayana, kill activity
                //when we click on this button (now that i'm checking) he will show us the user's input from the previous activity
                // with getExtras we get the details from the previous activity
                mListUid = new ArrayList<String>();

                mContactsAdapter = (ContactsArrayAdapter) mUserListView.getAdapter();

                for(User user : mContactsAdapter.getList()) {
                    Utils.LOGD("user= "+user.toString());
                    if(user.isSelected()) {
                        mListUid.add(String.valueOf(user.getUid()));
                    }
                }

                if(mListUid.isEmpty()) {
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

                        if(event == null) {
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
                Utils.LOGD("doInBackground");
                HashMap<String, String> phoneMap = allPhoneNumbersAndName();
                for(Map.Entry<String, String> entry : phoneMap.entrySet()) {
                    Utils.LOGD(entry.getKey()+", "+entry.getValue());
                }
                ListOfAppContacts = UserFunctions.getUsersByPhones(phoneList(phoneMap));
                return ListOfAppContacts;
            }

            protected void onPostExecute(List<User> list) {
                if(list == null) {
                    Utils.LOGD("getUsersByPhones returned null!");
                    progressDialog.dismiss();
                    return;
                }
                Utils.LOGD("onPostExecute");

                Utils.LOGD("list=...");
                for(User u : list) {
                    Utils.LOGD(u.toString());
                }
                mContactsAdapter = new ContactsArrayAdapter(ContactsActivity.this, list);
                mUserListView.setAdapter(mContactsAdapter);
                progressDialog.dismiss();

            }
        }.execute();
    }


    /**
     * Returns all people from contact list
     *
     * @return HashMap<phoneNumber,Name> : {"0545356070":"eidan", ... etc }
     */

    private HashMap<String, String> allPhoneNumbersAndName() {
        HashMap<String, String> phonesMap = new HashMap<String, String>();

        cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts._ID));
            String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if (Integer.parseInt(hasPhone) == 1) {
                int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                String contact = cursor.getString(nameFieldColumnIndex);

                // You know it has a number so now query it like this
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                while (phones.moveToNext()) {
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    // Filter out all the "-"'s and "*"'s from the phone number
                    phoneNumber = phoneNumber.replaceAll("\\+972","05").replaceAll(" ", "").replaceAll("-","").replaceAll("\\*","").replaceAll("[)(]]","");

                    phonesMap.put(phoneNumber, contact);
                }
                phones.close();
            }
        }
        cursor.close();

        return phonesMap;
    }

    /**
     * @param hashlist
     * @return List of phone numbers from HashMap so we can use UserFunction function "getUsersByPhones"
     */

    private List<String> phoneList(HashMap<String, String> hashlist) {
        List<String> phoneList = new ArrayList<String>();
        for (String s : hashlist.keySet()) {
            phoneList.add(s);
        }
        return phoneList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        if(mContactsAdapter != null) {
            mContactsAdapter.onDestroy();
        }
        super.onDestroy();
    }
}