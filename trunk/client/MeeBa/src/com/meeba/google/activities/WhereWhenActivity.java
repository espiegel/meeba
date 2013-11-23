package com.meeba.google.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.meeba.google.R;
import com.meeba.google.util.Utils;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by or malka on 09/11/13.
 */
public class WhereWhenActivity extends SherlockActivity {

    EditText editWhere;
    EditText editWhen;
    Button next;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.where_when_activity);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Event Details");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        Utils.setupUI(findViewById(R.id.wherewhenLayout), this);
        editWhen = (EditText) findViewById(R.id.whenTxtUser);
        editWhere = (EditText) findViewById(R.id.whereTxtUser);

        next = (Button) findViewById(R.id.NextBtn);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String where = editWhere.getText().toString();
                final String when = editWhen.getText().toString();
                if(TextUtils.isEmpty(where) || TextUtils.isEmpty(when)) {
                    Toast.makeText(getApplicationContext(),"You must input a location and a time" ,Toast.LENGTH_SHORT).show();
                    return;
                }
                //when we click on the button it will bring us to the contacts activity
                Intent i = new Intent(getApplicationContext(),
                        ContactsActivity.class);
                //build this so i can use user's input in the post activity
                Bundle bundle = new Bundle();
                bundle.putString("when", when);
                bundle.putString("where", where);
                i.putExtras(bundle);
                startActivity(i);

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        onBackPressed();
        return true;
    }
}


