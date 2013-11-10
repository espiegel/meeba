package com.meeba.google.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.*;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.plus.PlusClient;
import com.meeba.google.Objects.Event;
import com.meeba.google.Objects.User;
import com.meeba.google.R;
import com.meeba.google.Util.UserFunctions;
import com.meeba.google.Util.Utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class LoginActivity extends Activity implements OnClickListener,
        ConnectionCallbacks, OnConnectionFailedListener, PlusClient.OnAccessRevokedListener {

    /**
     * project number =   1023637778529
     * API KEY= AIzaSyCRftoO8hmXEoEDBF75SapiefJ8xh3_Up4
     */

    private static final int DIALOG_GET_GOOGLE_PLAY_SERVICES = 1;
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 2;

    GoogleCloudMessaging gcm;
    private PlusClient mPlusClient;

    private ConnectionResult mConnectionResult;
    private SignInButton mSignInButton;
    private TextView mSignInStatus;
    private View mSignOutButton;
    private View mRevokeAccessButton;

    String email;
    String phoneNumber;
    String name;
    String rid;
    String SENDER_ID = "1023637778529";
    Boolean isRegistered;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Testing the web services. Output is in the android log
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            protected void onPreExecute() {
                Utils.LOGD("onPreExecute");
            }

            protected Void doInBackground(Void... params) {
                Utils.LOGD("doInBackground");

                User user = UserFunctions.getUserByEmail("a@a.com");

                if (user != null)
                    Utils.LOGD(user.toString());

                Utils.LOGD("CREATING A NEW USER!!!");
                user = UserFunctions.createUser("my@email.com", "silly billy", "0234987", "987asdflkj");

                if (user != null)
                    Utils.LOGD(user.toString());

                Utils.LOGD("GETTING EVENTS OF USER!");
                List<Event> events = UserFunctions.getEventsByUser(2);

                Utils.LOGD("GETTING USERS BY PHONE NUMBERS");
                String[] phones = {"345345", "1", "12", "123", "467845673", "234"};
                List<User> users = UserFunctions.getUsersByPhones(Arrays.asList(phones));

                if (users != null) {
                    for (User u : users) {
                        Utils.LOGD(u.toString());
                    }
                }

                Utils.LOGD("TESTING CREATE EVENT");
                String[] uids = {"2", "7", "8"};
                Event event = UserFunctions.createEvent(1, "thailand", "summer 2014", Arrays.asList(uids));

                if (event != null) {
                    Utils.LOGD(event.toString());
                }

                Utils.LOGD("testing accept / decline");
                UserFunctions.acceptInvite(7, 4);
                UserFunctions.declineInvite(8, 4);
                return null;
            }

            protected void onPostExecute(Void v) {
                Utils.LOGD("onPostExecute");

            }
        };
        task.execute();

        mPlusClient = new PlusClient.Builder(this, this, this)
                .setActions("http://schemas.google.com/AddActivity").build();

        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(this);
        mSignInStatus = (TextView) findViewById(R.id.tvSignInStatus);
        mSignOutButton = findViewById(R.id.sign_out_button);
        mSignOutButton.setOnClickListener(this);
        mRevokeAccessButton = findViewById(R.id.revoke_access_button);
        mRevokeAccessButton.setOnClickListener(this);

        /*check if google play services are available*/
        checkPlayServices();
        context = getApplicationContext();
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        //TODO  show a progress bar /make button unavalible  until sign in progress completes
        //other wise user can  click sign in while sing in is in progress

        /* greet the user */
        String currentPersonName = mPlusClient.getCurrentPerson() != null
                ? mPlusClient.getCurrentPerson().getDisplayName()
                : getString(R.string.unknown_person);
        mSignInStatus.setText(getString(R.string.signed_in_status, currentPersonName));

        /**
         now :
         1) check if the user's google account is registered in the DB (in background) , then:
         2)if not ,get a regid from google(in background), then:
         3)ask his phone number, then:
         4) register him(in background)
         **/

        /** this method starts the above chain of events  **/
        backgroundCheckIfRegistered();
    }

    private void backgroundCheckIfRegistered() {
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog
                        .show(LoginActivity.this, "counting", "until ten  million!", true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                for (int i = 0; i < 10000000; i++) ;
                isRegistered = false; //for now;
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
                progressDialog.dismiss();
                if (isRegistered == false) {
                    backgroundGetRegid();
                }
            }
        }.execute(null, null, null);
    }


    private void backgroundGetRegid() {
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                progressDialog = ProgressDialog
                        .show(LoginActivity.this, "getting  regID from google", "wait!", true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    rid = gcm.register(SENDER_ID);
                } catch (IOException ex) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), rid, Toast.LENGTH_LONG).show();
                askUserPhoneNumber();
            }
        }.execute(null, null, null);
    }

    private void askUserPhoneNumber() {

//TODO check if  user entered  a legal phone number
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Please Enter Your Phone Number");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setBackgroundColor(0); // 0 == Transparent . looks better
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                phoneNumber = input.getText().toString();
                backgroundRegisterUser();
            }

        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sign_in_button) {
            if (mPlusClient.isConnected()) {
                Toast.makeText(this, "you are already signed in!", Toast.LENGTH_LONG).show();
            } else {

                try {
                    mConnectionResult.startResolutionForResult(this, REQUEST_CODE_SIGN_IN);
                } catch (IntentSender.SendIntentException e) {
                    mPlusClient.connect();
                }
            }

        } else if (view.getId() == R.id.sign_out_button) {
            if (mPlusClient.isConnected()) {
                mPlusClient.clearDefaultAccount();
                mPlusClient.disconnect();
                mPlusClient.connect();
                mSignInStatus.setText("signed out");
            }
        } else { //(view.getId()==R.id.revoke_access_button
            if (mPlusClient.isConnected()) {
                mPlusClient.revokeAccessAndDisconnect(this);
            }
        }
    }


    private void backgroundRegisterUser() {
        Toast.makeText(getApplicationContext(), "time to register", Toast.LENGTH_LONG).show();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_SIGN_IN
                || requestCode == REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES) {

            if (resultCode == RESULT_OK && !mPlusClient.isConnected()
                    && !mPlusClient.isConnecting()) {
                // This time, connect should succeed.
                mPlusClient.connect();
            }
        }
    }

    @Override
    public void onAccessRevoked(ConnectionResult status) {
        if (status.isSuccess()) {
            mSignInStatus.setText("access revoked");
        } else {
            mSignInStatus.setText("unable to revoke access");
            mPlusClient.disconnect();
        }
        mPlusClient.connect();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mPlusClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlusClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Save the intent so that we can start an activity when the user clicks
        // the sign-in button.
        mConnectionResult = result;
    }

    @Override
    public void onDisconnected() {
        mPlusClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        DIALOG_GET_GOOGLE_PLAY_SERVICES).show();
            } else {
                Log.i("MeeBa", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
