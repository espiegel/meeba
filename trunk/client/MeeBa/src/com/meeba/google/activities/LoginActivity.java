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
import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.objects.User;
import com.meeba.google.R;
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
      private static final int REQUEST_CODE_SIGN_IN = 1;
      private static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 2;

      private GoogleCloudMessaging gcm;
      private PlusClient mPlusClient;

      private ConnectionResult mConnectionResult;
      private TextView mSignInStatus;
      ProgressDialog signingInProgressBar;

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
                  Toast.makeText(getApplicationContext(), "Error getting account details!\n" +
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
                  //  ProgressDialog progressDialog;

                  @Override
                  protected void onPreExecute() {
                        super.onPreExecute();
                        //     progressDialog = ProgressDialog
                        //        .show(LoginActivity.this, "checking if user is registered in our DB ", "wait !", true);
                        Utils.LOGD("maxagi:  onPreExecute CheckIfRegistered();");
                        Utils.LOGD("maxagi: user before backgroundCheckIfRegistered  = " + user);
                  }

                  @Override
                  protected Void doInBackground(Void... params) {
                        Utils.LOGD("maxagi: in backgroundCheckIfRegistered();doInBackground");
                        user = UserFunctions.getUserByEmail(email);
                        Utils.LOGD("maxagi: user retrieved from phone DB= " + user);
                        isReisteredInOurDB = (user != null);
                        return null;
                  }

                  @Override
                  protected void onPostExecute(Void v) {
                        Utils.LOGD("maxagi: in background CheckIfRegistered();onPostExecute");
                        super.onPostExecute(v);
                        //         progressDialog.dismiss();

                        if (!isReisteredInOurDB) {
                              backgroundGetRegid();
                        } else {
                              //store user in phone DB
                              DatabaseFunctions.upgradeDatabase(context);
                              DatabaseFunctions.storeUserDetails_LoginTable(context, user);
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
                        // progressDialog = ProgressDialog
                        // .show(LoginActivity.this, "getting  regID from google", "wait!", true);
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
                        //  progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), rid, Toast.LENGTH_LONG).show();
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
            Toast.makeText(getApplicationContext(), "time to register " + email, Toast.LENGTH_LONG).show();

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
                              Toast.makeText(getApplicationContext(), "error registering user in database", Toast.LENGTH_LONG).show();
                              return;
                        }

                        Utils.LOGD("maxagi: onPostExecute");
                        context = getApplicationContext();
                        // Store the user's details inside our local database for later use
                        DatabaseFunctions.upgradeDatabase(context);
                        DatabaseFunctions.storeUserDetails_LoginTable(context, user);
                        Utils.LOGD("eidan: testing if we stored user details successfully");
                        Utils.LOGD("eidan: user=" + DatabaseFunctions.getUserDetails_LoginTable(context));
                        moveToNextView();
                  }
            };
            register.execute();
      }

      @Override
      public void onClick(View view) {
            Utils.LOGD("maxagi: onClick - View = " + view);
            Utils.LOGD("maxagi: FIRST mConnectionResult=" + mConnectionResult);

            if (view.getId() == R.id.sign_in_button) {
                  Utils.LOGD("maxagi: onClick  : mPlusClient status  = " + mPlusClient);
                  if (mPlusClient.isConnected()) {
                        Utils.LOGD("maxagi: onClick - mPlusClient is connected");
                        Toast.makeText(this, "you are already signed in!", Toast.LENGTH_LONG).show();
                  } else {
                        try {
                              Utils.LOGD("maxagi: mConnectionResult before trying to connect = " + mConnectionResult);
                              if (mConnectionResult == null) {
                                    Toast.makeText(this, "can't connect\n you sure you have internet access?", Toast.LENGTH_LONG).show();
                                    return;
                              }
                              signingInProgressBar = ProgressDialog.show(this, "signing in ...", "please wait ", true);
                              //this method return immediately but works on background
                              mConnectionResult.startResolutionForResult(this, REQUEST_CODE_SIGN_IN);
                              Utils.LOGD("maxagi: mConnectionResult AFTER  trying to connect = " + mConnectionResult);

                        } catch (IntentSender.SendIntentException e) {
                              Utils.LOGD("maxagi: SendIntentException e= " + e);
                              Utils.LOGD("maxagi: mConnectionResult errorResult = " + mConnectionResult.getErrorCode());
                              mPlusClient.connect();
                              Utils.LOGD("maxagi:trying to connect again");
                        }
                  }
            } else if (view.getId() == R.id.sign_out_button) {
                  signoutAndDisconnect();

            } else { //(view.getId()==R.id.revoke_access_button
                  if (mPlusClient.isConnected()) {
                        mPlusClient.revokeAccessAndDisconnect(this);
                  }
            }
      }

      @Override
      public void onActivityResult(int requestCode, int resultCode, Intent data) {
            Utils.LOGD("maxagi:onActivityResult : resultCode = " + resultCode);
            Utils.LOGD("maxagi:onActivityResult : mPlusClient.isConnected = " + mPlusClient.isConnected());
            Utils.LOGD("maxagi:onActivityResult :mPlusClient.isConnecting = " + mPlusClient.isConnecting());
            if (requestCode == REQUEST_CODE_SIGN_IN
                    || requestCode == REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES) {

                  if (resultCode == RESULT_OK && !mPlusClient.isConnected()
                          && !mPlusClient.isConnecting()) {

                        // This time, connect should succeed.
                        mPlusClient.connect();

                        Utils.LOGD("maxagi: mConnectionResult AFTER  trying to connect = " + mConnectionResult);
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
            if (signingInProgressBar != null) {
                  signingInProgressBar.dismiss();
            }

            Utils.LOGD("maxagi:onConnectionFailed ; result = " + result);
            // Save the intent so that we can start an activity when the user clicks the sign-in button.
            mConnectionResult = result;

            if (mConnectionResult.getErrorCode() == ConnectionResult.INTERNAL_ERROR) {
                  //in this case the next try should work
                  Toast.makeText(getApplicationContext(), "connection failed, please try again ", Toast.LENGTH_LONG).show();
            }
      }

      @Override
      public void onDisconnected() {
            Utils.LOGD("maxagi :onDisconnected");

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

      private void moveToNextView() {
            Intent i = new Intent(getApplicationContext(), DashboardActivity.class);
            Bundle bundle = new Bundle();
            /** pass the uid to the DashboardActivity **/
            bundle.putInt("uid", user.getUid());
            i.putExtras(bundle);
            if (signingInProgressBar != null) {
                  signingInProgressBar.dismiss();
            }
           //startActivity(i);
      }

      private void signoutAndDisconnect() {
            if (mPlusClient.isConnected()) {
                  mPlusClient.clearDefaultAccount();
                  mPlusClient.disconnect();

                  //connect the mPlusClient again so user can try again
                  mPlusClient.connect();

                  // Remove the current user from the local database
                  DatabaseFunctions.resetTables(getApplicationContext());
                  mSignInStatus.setText("signed out");
            } else {
                  Toast.makeText(getApplicationContext(), "user is  not connected / connecting ", Toast.LENGTH_LONG).show();
            }
      }

      private boolean checkIfRegisteredInPhone() {
            user = DatabaseFunctions.getUserDetails_LoginTable(context);
            Utils.LOGD("maxagi: user retrieved from phone DB= " + user);
            return (user != null);

      }

}