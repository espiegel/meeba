package com.meeba.google.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.meeba.google.R;


/**
 * Created by or malka on 11/11/13.
 */
public class ContactsActivity extends Activity {
    Button next;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_activity);
        //just checking with this button - i will change it to bring us to dayana's activity
        next = (Button) findViewById(R.id.invitebtn);
        Toast.makeText(getApplicationContext(),"Loading your contacts list..." ,Toast.LENGTH_LONG).show();

/*
        //getting list of phone numbers
        ContentResolver contResv = getContentResolver();
        Cursor cursor =  contResv.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if(cursor.moveToFirst())
        {
            List<String> alContacts = new ArrayList<String>();
            do
            {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                if(Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    Cursor pCur = contResv.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ id }, null);
                    while (pCur.moveToNext())
                    {
                        String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        alContacts.add(contactNumber);
                        break;
                    }
                    pCur.close();
                }

            } while (cursor.moveToNext()) ;

        }

*/
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //when we click on this button (now that i'm checking) he will show us the user's input from the previous activity
              // with getExtras we get the details from the previous activity
                Bundle bundle = getIntent().getExtras();

                //show details as toast
              //  Toast.makeText(getApplicationContext(),"when? "+bundle.getString("when")+ "  " +"where? "+bundle.getString("where"), Toast.LENGTH_SHORT).show();
            }
        });
    }



    }



