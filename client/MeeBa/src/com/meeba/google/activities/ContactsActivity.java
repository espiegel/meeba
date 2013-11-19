package com.meeba.google.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.meeba.google.adapters.ContactsArrayAdapter;
import com.meeba.google.adapters.EventArrayAdapter;
import com.meeba.google.database.DatabaseHandler;
import com.meeba.google.objects.User;
import com.meeba.google.R;
import com.meeba.google.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.meeba.google.util.UserFunctions.getUsersByPhones;


/**
 * Created by or malka on 11/11/13.
 */
public class ContactsActivity extends Activity {
    Button next;
    //listview for choose contacts from list
    ListView listview;
    ContactsArrayAdapter mContactsArrayAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_activity);
        final ContactsActivity contactsActivity = this;

        //just checking with this button - i will change it to bring us to dayana's activity
        next = (Button) findViewById(R.id.invitebtn);

        //just for checking
        Toast.makeText(getApplicationContext(),"Loading your contacts list..." ,Toast.LENGTH_LONG).show();


        final Activity conAct = this;

        AsyncTask<Void, Void, List<User>> aTast = new AsyncTask<Void, Void, List<User>>() {
         //   final ProgressDialog progress = new ProgressDialog(contactsActivity);
         ProgressDialog progressDialog;

            protected void onPreExecute() {
                Log.d("load","Loading your contacts list...");
                //progress.setMessage("Sending Invitations...");
                //progress.show();
                progressDialog = ProgressDialog
                        .show(ContactsActivity.this, "Loading your contacts list ", "please wait !", true);
            }

            protected List<User> doInBackground(Void... params) {
                listview = (ListView) findViewById(R.id.appContacts);


                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                Log.d("a", "getting contacts...");
                List<String> allPhoneNumbers = phoneList(allPhoneNumbersAndName());
                // get only app contacts so the user can invite them to his event
                List<User> appContacts = getUsersByPhones(allPhoneNumbers);

           //     mContactsArrayAdapter = new ContactsArrayAdapter(conAct,appContacts);
          //      listview.setAdapter(mContactsArrayAdapter);
            //    listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);



                //return list of app users
              //  return listview;
                return appContacts;
            }


            protected void onPostExecute(List<User> appContacts){
                Utils.LOGD("onPostExecute");


                // update the event list view
                mContactsArrayAdapter = new ContactsArrayAdapter(conAct, appContacts);
                listview.setAdapter(mContactsArrayAdapter);

                progressDialog.dismiss();

                //    for(ListView u:appContacts){
              //      System.out.println(u.getAdapter());
              //  }


            //    Toast.makeText(getApplicationContext(),appContacts. ,Toast.LENGTH_LONG).show();
            }
        };
        //  HashMap<String,String> allPhoneNumbersAndName = getAllPhoneNumbers();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //here should be:
                //send details to server/dayana, kill activity
                //when we click on this button (now that i'm checking) he will show us the user's input from the previous activity
                // with getExtras we get the details from the previous activity
                Bundle bundle = getIntent().getExtras();

                //show details as toast
                //  Toast.makeText(getApplicationContext(),"when? "+bundle.getString("when")+ "  " +"where? "+bundle.getString("where"), Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Returns all people from contact list
     * @return HashMap<phoneNumber,Name> : {"0545356070":"eidan", ... etc }
     */

    private HashMap<String,String> allPhoneNumbersAndName(){
        HashMap<String,String> phonesMap = new HashMap<String,String>();

        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts._ID));
            //Log.d("loop", "contactId="+contactId);
            String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            //Log.d("loop", "hasphone="+hasPhone);

            if (Integer.parseInt(hasPhone) == 1) {
                int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                String contact = cursor.getString(nameFieldColumnIndex);

                // You know it has a number so now query it like this
                Cursor phones = getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId, null, null);
                while (phones.moveToNext()) {
                    String phoneNumber = phones.getString(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));

                    phonesMap.put(phoneNumber, contact);
                }
                phones.close();
            }
        }
        cursor.close();

        return phonesMap;
    }

    /**
     *
     * @param hashlist
     * @return List of phone numbers from HashMap so we can use UserFunction function "getUsersByPhones"
     */
    private List<String> phoneList(HashMap<String,String> hashlist){
        List<String> phoneList = new ArrayList<String>();
        for(String s : hashlist.keySet()) {
            phoneList.add(s);
        }
        return phoneList;
    }

}



//