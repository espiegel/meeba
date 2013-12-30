package com.meeba.google.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.meeba.google.R;
import com.meeba.google.adapters.WhereAutoCompleteAdapter;
import com.meeba.google.util.Utils;
import com.meeba.google.view.AutoCompleteClearableEditText;
import com.meeba.google.view.ClearableEditText;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

/**
 * Created by or malka on 09/11/13.
 */
public class WhereWhenActivity extends SherlockActivity {

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_CAMERA_IMAGE = 3;
    private static final float BASE_WIDTH = 500;
    private static final float BASE_HEIGHT = 250;

    AutoCompleteClearableEditText mEditWhere;
    ClearableEditText mEditTitle;
    ClearableEditText mEditWhen;

    private String mDate = "";
    private ImageView mEditPicture;
    private ImageView mPicturePlusButton;

    private View.OnClickListener mOnPictureClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(WhereWhenActivity.this);
            builder.setTitle("Event Picture")
                    .setIcon(R.drawable.ic_launcher)
                    .setMessage("Where to take the photo from?")
                    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, RESULT_CAMERA_IMAGE);
                        }
                    })
                    .setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, RESULT_LOAD_IMAGE);
                        }
                    });
            builder.create().show();

        }
    };
    private int mDay;
    private int mMonth;
    private int mYear;
    private DateTime dt;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.where_when_activity);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Event Details");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        Utils.setupUI(findViewById(R.id.wherewhenLayout), this);
        mEditWhen = (ClearableEditText) findViewById(R.id.whenTxtUser);
        mEditTitle = (ClearableEditText) findViewById(R.id.titleTxtUser);
        mEditWhere = (AutoCompleteClearableEditText) findViewById(R.id.whereTxtUser);
        mEditPicture = (ImageView) findViewById(R.id.eventPicture);
        mPicturePlusButton = (ImageView) findViewById(R.id.plusButton);

        mEditPicture.setOnClickListener(mOnPictureClick);
        mPicturePlusButton.setOnClickListener(mOnPictureClick);

        WhereAutoCompleteAdapter autoCompleteAdapter = new WhereAutoCompleteAdapter(WhereWhenActivity.this, R.layout.dropdown_autocomplete,
                R.id.txtViewSearch, new WhereAutoCompleteAdapter.SearchAutoComplete() {
            @Override
            public void autoCompleteItemClicked(String query) {
                mEditWhere.setText(query);
            }
        });
        mEditWhere.setThreshold(2);
        mEditWhere.setAdapter(autoCompleteAdapter);

        mEditTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        mEditTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE) {
                    mEditWhere.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mEditWhere.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        mEditWhere.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE) {
                    nextButton();
                    return true;
                }
                return false;
            }
        });

        mEditWhen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });

        mEditWhen.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (focus) {
                    showTimePicker();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null) {
            if(requestCode == RESULT_CAMERA_IMAGE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                photo = Bitmap.createScaledBitmap(photo, (int)BASE_WIDTH, (int)BASE_HEIGHT, true);
                mEditPicture.setImageBitmap(photo);
            } else if(requestCode == RESULT_LOAD_IMAGE) {
                try {
                    Uri selectedImage = data.getData();
                    Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    photo = Bitmap.createScaledBitmap(photo, (int)BASE_WIDTH, (int)BASE_HEIGHT, true);
                    mEditPicture.setImageBitmap(photo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showTimePicker() {
        Calendar currentTime = Calendar.getInstance();
        int year = currentTime.get(Calendar.YEAR);
        int month = currentTime.get(Calendar.MONTH);
        int day = currentTime.get(Calendar.DAY_OF_MONTH);
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        final TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                dt = new DateTime(mYear, mMonth, mDay, selectedHour, selectedMinute);
                long date_mili = dt.getMillis();
                mDate = dt.dayOfWeek().getAsShortText() + ", " + dt.monthOfYear().getAsShortText() + " " + dt.dayOfMonth().getAsShortText() + ", " +
                        DateTimeFormat.forPattern("HH:mm").print(dt);
                mEditWhen.setText(mDate);
                mEditWhere.requestFocus();
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");

        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(WhereWhenActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                mEditWhen.setText("");
                mYear = year;
                mMonth = month+1;
                mDay = day;

                mEditWhen.setText(mDate);
                mTimePicker.show();
            }
        }, year, month, day);
        mDatePicker.setTitle("Select date");
        mDatePicker.show();
    }

    private void nextButton() {
        String title =  mEditTitle.getText().toString();
        String where = mEditWhere.getText().toString();
        String when = mEditWhen.getText().toString();
        if (TextUtils.isEmpty(where) || TextUtils.isEmpty(when) || TextUtils.isEmpty(title))  {
            Toast.makeText(getApplicationContext(), getString(R.string.bad_event_details_input), Toast.LENGTH_SHORT).show();
            return;
        }

        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditWhere.getWindowToken(), 0);

        //when we click on the button it will bring us to the contacts activity
        Intent i = new Intent(getApplicationContext(),
                ContactsActivity.class);
        //build this so i can use user's input in the post activity
        Bundle bundle = new Bundle();
        bundle.putString("when", when);
        bundle.putString("title", title);
        bundle.putString("where", where);

        // Compress the bitmap
        Bitmap bitmap = ((BitmapDrawable)mEditPicture.getDrawable()).getBitmap();
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);

        bundle.putByteArray("event_picture", bs.toByteArray());
        i.putExtras(bundle);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.wherewhen_activity, menu);
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


