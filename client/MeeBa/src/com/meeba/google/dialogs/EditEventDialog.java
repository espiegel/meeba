package com.meeba.google.dialogs;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.meeba.google.R;
import com.meeba.google.adapters.WhereAutoCompleteAdapter;
import com.meeba.google.objects.Event;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;
import com.meeba.google.view.AutoCompleteClearableEditText;
import com.meeba.google.view.ClearableEditText;

import org.joda.time.DateTime;

import java.util.Calendar;

/**
 * Created by Eidan on 12/22/13.
 */
public class EditEventDialog extends SherlockDialogFragment {

    public static String TAG = "EditEventDialog";
    private final EventUpdateCallback mCallback;
    private Event mEvent;
    private ClearableEditText mEditTitle;
    private ClearableEditText mEditWhen;
    private AutoCompleteClearableEditText mEditWhere;
    private String mDate;
    private ProgressBar mProgressBar;
    private LinearLayout mFields;
    private String mFormmatedDate;
    private int mYear;
    private int mMonth;
    private int mDay;
    private DateTime dt;

    public EditEventDialog(Event event, EventUpdateCallback callback) {
        mEvent = event;
        mCallback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialog = inflater.inflate(R.layout.dialog_edit_event, null);

        mEditTitle = (ClearableEditText) dialog.findViewById(R.id.titleTxtUser);
        mEditWhen = (ClearableEditText) dialog.findViewById(R.id.whenTxtUser);
        mEditWhere = (AutoCompleteClearableEditText) dialog.findViewById(R.id.whereTxtUser);
        Button btnUpdate = (Button) dialog.findViewById(R.id.btnUpdate);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        mFields = (LinearLayout) dialog.findViewById(R.id.dialog_edit_event_layout);
        mProgressBar = (ProgressBar) dialog.findViewById(R.id.progressBar);

        mProgressBar.setVisibility(View.GONE);

        mEditTitle.setText(mEvent.getTitle());
        mEditWhen.setText(mEvent.getWhen());
        mEditWhere.setText(mEvent.getWhere());

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String title = mEditTitle.getText().toString();
                //final String when = mEditWhen.getText().toString();
                final String when = mFormmatedDate;
                final String where = mEditWhere.getText().toString();

                if (TextUtils.isEmpty(where) || TextUtils.isEmpty(when) || TextUtils.isEmpty(title)) {
                    Toast.makeText(getActivity(), getString(R.string.bad_event_details_input), Toast.LENGTH_SHORT).show();
                    return;
                }

                Utils.hideSoftKeyboard(getActivity());
                updateDetails(title, when, where);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                Utils.hideSoftKeyboard(getActivity());
            }
        });

        WhereAutoCompleteAdapter autoCompleteAdapter = new WhereAutoCompleteAdapter(getActivity(), R.layout.dropdown_autocomplete,
                R.id.txtViewSearch, new WhereAutoCompleteAdapter.SearchAutoComplete() {
            @Override
            public void autoCompleteItemClicked(String query) {
                mEditWhere.setText(query);
            }
        });
        mEditWhere.setDropDownAnchor(R.id.titleTxtUser);
        mEditWhere.setThreshold(2);
        mEditWhere.setAdapter(autoCompleteAdapter);

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
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
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

        final AlertDialog alertDialog = builder.create();
        alertDialog.setView(dialog, 0, 0, 0, 0);

        return alertDialog;
    }

    private void updateDetails(final String title, final String when, final String where) {
        new AsyncTask<Void, Void, Event>() {
            @Override
            protected void onPreExecute() {
                mProgressBar.setVisibility(View.VISIBLE);
                mFields.setVisibility(View.GONE);
            }

            @Override
            protected Event doInBackground(Void... voids) {
                Event event = UserFunctions.updateEvent(mEvent.getEid(), title, when, where);
                return event;
            }

            @Override
            protected void onPostExecute(Event event) {
                if (event == null) {
                    Utils.showToast(getActivity(), "Update event failed");
                    mProgressBar.setVisibility(View.GONE);
                    mFields.setVisibility(View.VISIBLE);
                } else {
                    mCallback.onEventDetailsUpdate(event);
                    dismiss();
                    Utils.hideSoftKeyboard(getActivity());
                }
            }
        }.execute();
    }

    private void showTimePicker() {
        Calendar currentTime = Calendar.getInstance();
        int year = currentTime.get(Calendar.YEAR);
        int month = currentTime.get(Calendar.MONTH);
        int day = currentTime.get(Calendar.DAY_OF_MONTH);
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        final TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
            //    String minutes;
             //   if (selectedMinute < 10) {
          //          minutes = "0" + selectedMinute;
         //       } else {
          //          minutes = String.valueOf(selectedMinute);
         //       }
                // mEditWhen.setText(selectedHour + ":" + minutes + " " + mDate);
                dt = new DateTime(mYear, mMonth, mDay, selectedHour, selectedMinute);

                mFormmatedDate = dt.toString( "h:mm dd/MM/yyyy ") ;
                mDate=Utils.makePrettyDate(mFormmatedDate);

                mEditWhen.setText(mDate);
                mEditWhere.requestFocus();
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");

        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                mEditWhen.setText("");
               // mDate = day + "/" + (month + 1) + "/" + year;
                //mEditWhen.setText(mDate);
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

    public interface EventUpdateCallback {
        public void onEventDetailsUpdate(Event newEvent);
    }
}
