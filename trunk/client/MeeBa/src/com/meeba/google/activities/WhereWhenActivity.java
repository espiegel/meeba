package com.meeba.google.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.meeba.google.R;
import com.meeba.google.util.Utils;

import java.util.Calendar;

/**
 * Created by or malka on 09/11/13.
 */
public class WhereWhenActivity extends SherlockActivity {

    EditText editWhere;
    EditText editWhen;

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

        editWhere.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        editWhen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar currentTime = Calendar.getInstance();
                int year = currentTime.get(Calendar.YEAR);
                int month = currentTime.get(Calendar.MONTH);
                int day = currentTime.get(Calendar.DAY_OF_MONTH);
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);

                final TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(WhereWhenActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String minutes;
                        if (selectedMinute < 10) {
                            minutes = "0" + selectedMinute;
                        } else {
                            minutes = String.valueOf(selectedMinute);
                        }
                        String date = editWhen.getText().toString();
                        editWhen.setText(selectedHour + ":" + minutes + " " + date);
                        editWhere.requestFocus();
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");

                DatePickerDialog mDatePicker;
                mDatePicker = new DatePickerDialog(WhereWhenActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        editWhen.setText("");
                        editWhen.setText(day + "/" + (month+1) + "/" + year);
                        mTimePicker.show();
                    }
                }, year, month, day);
                mDatePicker.setTitle("Select date");
                mDatePicker.show();
            }
        });
    }

    private void nextButton() {
        String where = editWhere.getText().toString();
        String when = editWhen.getText().toString();
        if (TextUtils.isEmpty(where) || TextUtils.isEmpty(when)) {
            Toast.makeText(getApplicationContext(), "You must input a location and a time", Toast.LENGTH_SHORT).show();
            return;
        }

        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editWhere.getWindowToken(), 0);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.where_when, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_next:
                nextButton();
                return true;

            default:
                break;
        }
        onBackPressed();
        return true;
    }
}


