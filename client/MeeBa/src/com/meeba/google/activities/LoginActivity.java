package com.meeba.google.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.plus.PlusClient;
import com.meeba.google.R;
import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.database.DatabaseHandler;
import com.meeba.google.objects.User;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;

import java.io.IOException;


public class LoginActivity extends Activity implements OnClickListener,
        ConnectionCallbacks, OnConnectionFailedListener, PlusClient.OnAccessRevokedListener {

    /**
     * max's values
     * project number =   1023637778529
     * API KEY= AIzaSyCRftoO8hmXEoEDBF75SapiefJ8xh3_Up4
     */

    /**
     * eidan's values
     * project number =   266943873561
     * API KEY= AIzaSyB7uaYL60o0MJtTs18_G0mspWWXOlUybzk
     */
    private static final int DIALOG_GET_GOOGLE_PLAY_SERVICES = 1;
    private static final int REQUEST_CODE_SIGN_IN = 2;
    private static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 6;

    private GoogleCloudMessaging gcm;
    private PlusClient mPlusClient;

    private ConnectionResult mConnectionResult = null;
    private TextView mSignInStatus;
    private ProgressDialog backgroundProgressDialog;

    private String email;
    private String phoneNumber;
    private String name;
    private String rid;
    //private final  String SENDER_ID = "1023637778529"; //max's old value
    private final String SENDER_ID = "266943873561";
    private Boolean isReisteredInOurDB, isRegisteredInPhone;
    private User user;
    private Context context;
    private String pictureUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        backgroundProgressDialog = new ProgressDialog(this);
        backgroundProgressDialog.setMessage( "signing in...");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mPlusClient = new PlusClient.Builder(this, this, this)
                .setActions("http://schemas.google.com/AddActivity").build();

        Utils.LOGD("maxagi: mPlusClient = :" + mPlusClient);

        SignInButton mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(this);
        mSignInStatus = (TextView) findViewById(R.id.tvSignInStatus);
        View mSignOutButton = findViewById(R.id.sign_out_button);
        mSignOutButton.setOnClickListener(this);
        View mRevokeAccessButton = findViewById(R.id.revoke_access_button);
        mRevokeAccessButton.setOnClickListener(this);
        context = getApplicationContext();

        /*check if google play services are available*/

        checkPlayServices();

        Utils.LOGD("maxagi: onCreate finished");

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Utils.LOGD("maxagi: onConnected started");
        Utils.LOGD("maxagi:mPlusClient in onConnected is =" + mPlusClient);

        /* greet the user */

        if (mPlusClient.getCurrentPerson() == null) {
            Utils.LOGD("maxagi: mPlusClient.getCurrentPerson is null ");
            signoutAndDisconnect();
            Toast.makeText(LoginActivity.this, "Error getting account details!\n" +
                    "you sure you have internet access?", Toast.LENGTH_LONG).show();
        } else {
            Utils.LOGD("maxagi: mPlusClient.getCurrentPerson=  " + (mPlusClient.getCurrentPerson()));
            String currentPersonName = mPlusClient.getCurrentPerson().getDisplayName();

            pictureUrl = mPlusClient.getCurrentPerson().getImage().getUrl();
            Utils.LOGD("pictureUrl = " + pictureUrl);

            mSignInStatus.setText(getString(R.string.signed_in_status, currentPersonName));

            email = mPlusClient.getAccountName();
            name = mPlusClient.getCurrentPerson().getDisplayName();

            /**
             now :
             check if the user's google account is registered in the phone  DB , if yes , move to DashBoard
             if not  check (in background)  if his registed in our DB and register him if he isn't  and store user in phone DB.
             **/

            isRegisteredInPhone = checkIfRegisteredInPhone();
            if (isRegisteredInPhone) {
                moveToNextView();
            } else {
                backgroundCheckIfRegistered();
            }
        }
    }

    /**
     * check if user is registered in our DB. if not ,call backgroundGetRegid(); *
     */
    private void backgroundCheckIfRegistered() {
        Utils.LOGD("maxagi: in backgroundCheckIfRegistered()");
        new AsyncTask<Void, Void, Void>() {


            @Override
            protected void onPreExecute() {
                super.onPreExecute();


            //    backgroundProgressDialog = ProgressDialog
              // .show(LoginActivity.this, "checking if user is registered in our DB ", "wait !", true);
                backgroundProgressDialog.show();

                Utils.LOGD("maxagi:  onPreExecute CheckIfRegistered();");
                Utils.LOGD("maxagi: user before backgroundCheckIfRegistered  = " + user);
            }

            @Override
            protected Void doInBackground(Void... params) {
                Utils.LOGD("maxagi: in backgroundCheckIfRegistered();doInBackground");
                user = UserFunctions.getUserByEmail(email);
                Utils.LOGD("maxagi: user retrieved from network DB= " + user);
                isReisteredInOurDB = (user != null);
                return null;
            }


            @Override
            protected void onPostExecute(Void v) {
                Utils.LOGD("maxagi: in background CheckIfRegistered();onPostExecute");
                super.onPostExecute(v);

                if (!isReisteredInOurDB) {
                    backgroundGetRegid();
                } else {
                    //store user in phone DB
                    DatabaseFunctions.upgradeDatabase(context);
                    DatabaseFunctions.storeUserDetails(context, user, DatabaseHandler.TABLE_USER);
                    DatabaseFunctions.storeUserDetails(context, user, DatabaseHandler.TABLE_USER);

                    Utils.LOGD("maxagi:storring in phone DB user = " + user);
                    User debug = DatabaseFunctions.getUserDetails(getApplicationContext());
                    Utils.LOGD("maxagi: user stored in phone DB = " + debug);
                    moveToNextView();
                }

            }
        }.execute(null, null, null);
    }


    /**
     * get a regID from google, and then call  askUserPhoneNumber();
     */
    private void backgroundGetRegid() {
        final Context context = getApplicationContext();
        Utils.LOGD("maxagi: in backgroundGetRegid();");
        new AsyncTask<Void, Void, Object>() {
            //ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                Utils.LOGD("maxagi: in backgroundGetRegid();onPreExecute");
                super.onPreExecute();
                gcm = GoogleCloudMessaging.getInstance(getApplicationContext());

            }

            @Override
            protected Object doInBackground(Void... params) {
                Utils.LOGD("maxagi: in backgroundGetRegid();doInBackground");
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    rid = gcm.register(SENDER_ID);
                } catch (IOException ex) {
                    return ex;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object error) {
                if (error != null) {
                    Toast.makeText(context,
                            "registration error " + error, Toast.LENGTH_LONG).show();
                    return;
                }
                Utils.LOGD("maxagi: in backgroundGetRegid();onPostExecute");
                askUserPhoneNumber();
            }
        }.execute(null, null, null);
    }

    /**
     * ask the user's phone number , and then call backgroundRegisterUser();
     */
    private void askUserPhoneNumber() {
        Utils.LOGD("maxagi: in askUserPhoneNumber");
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setInverseBackgroundForced(true);
        alert.setTitle("Please Enter Your Phone Number");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setBackgroundColor(0); // 0 == Transparent . looks better
        alert.setView(input);
        Utils.LOGD("maxagi: in askUserPhoneNumber Stage 2 ");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                phoneNumber = input.getText() != null ? input.getText().toString() : "";

                //check that  user entered 10 digits and starts with 05
                if (phoneNumber.matches("05\\d{8}")) {
                    backgroundRegisterUser();

                } else {
                    Toast.makeText(getApplicationContext(), "you have to enter a valid phone number !", Toast.LENGTH_LONG).show();
                    backgroundProgressDialog.dismiss();
                    signoutAndDisconnect();
                }
            }

        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                signoutAndDisconnect();
            }
        });
        alert.show();
        Utils.LOGD("maxagi: in askUserPhoneNumber Stage 3 ");
    }

    private void backgroundRegisterUser() {// if the user already exists in our DB , do nothing
        AsyncTask<Void, Void, Void> register = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected Void doInBackground(Void... params) {
                Utils.LOGD("maxagi:registering " + email);
                user = UserFunctions.createUser(email, name, phoneNumber, rid, pictureUrl);
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                if (user == null) {
                    Toast.makeText(LoginActivity.this, "error registering user in database", Toast.LENGTH_LONG).show();
                    return;
                }

                Utils.LOGD("maxagi: onPostExecute");
                context = getApplicationContext();
                // Store the user's details inside our local database for later use
                DatabaseFunctions.upgradeDatabase(context);
                DatabaseFunctions.storeUserDetails(context, user, DatabaseHandler.TABLE_USER);
                Utils.LOGD("eidan: testing if we stored user details successfully");
                Utils.LOGD("eidan: user=" + DatabaseFunctions.getUserDetails(context));
                moveToNextView();
            }
        };
        register.execute();
    }

    @Override
    public void onClick(View view) {
        Utils.LOGD("maxagi: onClick - View = " + view);
        Utils.LOGD("maxagi: onClick  : mPlusClient status  = " + mPlusClient);

        if (view.getId() == R.id.sign_in_button) {
            if (mPlusClient.isConnected()) {
                Toast.makeText(this, "you are already signed in!", Toast.LENGTH_LONG).show();
            } else {
                mPlusClient.connect();
                Utils.LOGD("maxagi: mConnectionResult before trying to connect = " + mConnectionResult);

                if (mConnectionResult == null) return;

                try {
                    mConnectionResult.startResolutionForResult(LoginActivity.this, REQUEST_CODE_SIGN_IN);

                } catch (IntentSender.SendIntentException e) {
                    Utils.LOGD("Exception caught :  " + e.getMessage());
                    mConnectionResult = null;
                    mPlusClient.connect();
                }
            }
        } else if (view.getId() == R.id.sign_out_button) {
            signoutAndDisconnect();

        } else {
            if (mPlusClient.isConnected()) {
                mPlusClient.revokeAccessAndDisconnect(this);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_SIGN_IN
                || requestCode == REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES) {
            if (resultCode == RESULT_OK && !mPlusClient.isConnected()
                    && !mPlusClient.isConnecting()) {

                // This time, connect should succeed.
                mConnectionResult = null;
                mPlusClient.connect();

                Utils.LOGD("maxagi: onActivityResult : mConnectionResult AFTER  trying to connect = " + mConnectionResult);
                Utils.LOGD("maxagi: onActivityResult result code   = " + resultCode);
            }
        }
    }

    @Override
    public void onAccessRevoked(ConnectionResult status) {
        Utils.LOGD("maxagi: onAccessRevoked ");
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
        Utils.LOGD("onStart");
        super.onStart();
        if (!mPlusClient.isConnected() && !mPlusClient.isConnecting())
            mPlusClient.connect();
    }

    @Override
    protected void onStop() {
        Utils.LOGD("onStop");
       if( backgroundProgressDialog !=null )
           backgroundProgressDialog.dismiss();
        super.onStop();
        mPlusClient.disconnect();
    }

    @Override
    public void onDestroy() {
        Utils.LOGD("onDestroy");
        if( backgroundProgressDialog !=null )
            backgroundProgressDialog.dismiss();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        Utils.LOGD("onPause");
        Utils.LOGD("maxagi: onPause:   mPlusClient.isConnected   = " + mPlusClient.isConnected());
        Utils.LOGD("maxagi    onPause: mPlusClient.isConnecting   = " + mPlusClient.isConnecting());
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this, REQUEST_CODE_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mPlusClient.connect();
            }
        }
        // Save the intent so that we can start an activity when the user clicks
        // the sign-in button.
        mConnectionResult = result;
    }


    @Override
    public void onDisconnected() {
        Utils.LOGD("maxagi :onDisconnected");
    }

    @Override
    protected void onResume() {
        Utils.LOGD("maxagi :onResume");
        super.onResume();
        if (!mPlusClient.isConnected() && !mPlusClient.isConnecting()) {
            Utils.LOGD("maxagi :onResume reconnecting ");
            mPlusClient.connect();
        }
        checkPlayServices();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        DIALOG_GET_GOOGLE_PLAY_SERVICES).show();
            } else {
                Utils.LOGD("This device is not supported.");
                finish();
            }
            Utils.LOGD("maxagi :checkPlayServices : false");
            return false;
        }
        return true;
    }

    private void moveToNextView() {

       if( backgroundProgressDialog !=null )
           backgroundProgressDialog.dismiss();

        Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
        startActivity(i);
        this.finish();
    }

    private void signoutAndDisconnect() {
        if (mPlusClient.isConnected()) {
            mPlusClient.clearDefaultAccount();
            mPlusClient.disconnect();

            // Remove the current user from the local database
            DatabaseFunctions.resetTables(getApplicationContext());
            mSignInStatus.setText("signed out");
        } else {
            Toast.makeText(LoginActivity.this, "user is  not connected / connecting ", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkIfRegisteredInPhone() {
        user = DatabaseFunctions.getUserDetails(context);
        Utils.LOGD("maxagi: user retrieved from phone DB= " + user);
        return (user != null);
    }

}