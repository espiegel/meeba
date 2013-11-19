package com.meeba.google.activities;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.objects.User;
import com.meeba.google.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.meeba.google.util.UserFunctions.getUsersByPhones;


/**
 * Created by or malka on 11/11/13.
 */
public class ContactsActivity extends Activity {
    Button next;
    final Activity con = this;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_activity);

        //just checking with this button - i will change it to bring us to dayana's activity
        next = (Button) findViewById(R.id.invitebtn);

        //just for checking
        Toast.makeText(getApplicationContext(),"Loading your contacts list..." ,Toast.LENGTH_LONG).show();



        AsyncTask<Void, Void, List<User>> aTast = new AsyncTask<Void, Void, List<User>>() {

            protected void onPreExecute() {

            }

            protected List<User> doInBackground(Void... params) {
                HashMap<String, String> HashOfPhoneContacts = allPhoneNumbersAndName();
                List<String> ListOfPhoneContacts = phoneList(HashOfPhoneContacts);
                List<User> ListOfAppContacts = getUsersByPhones(ListOfPhoneContacts);
                return ListOfAppContacts;
            }


            protected void onPostExecute(List<User> appContacts){
            //create list of name string or number string
                 List<String> appNameOrPhone = null;
                for(User u: appContacts) {
                    if(u.getName() == null) {
                        appNameOrPhone.add(u.getPhone_number());
                    }
                    else{
                        appNameOrPhone.add(u.getName());
                    }
                }

                //build adapter

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        con,                    //Context for the activity
                        R.layout.listview_item, //layout to use(create)
                        appNameOrPhone);        //items to display

                //configure the list view
                ListView list = (ListView) findViewById(R.id.appContacts);
                list.setAdapter(adapter);

            }


        };

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

    public void stamFunc(){

    }

}



//