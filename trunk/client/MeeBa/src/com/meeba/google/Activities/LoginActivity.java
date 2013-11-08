package com.meeba.google.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.*;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;
import com.meeba.google.R;

public class LoginActivity extends Activity implements OnClickListener,
ConnectionCallbacks, OnConnectionFailedListener , PlusClient.OnAccessRevokedListener
{

    private static final int DIALOG_GET_GOOGLE_PLAY_SERVICES = 1;
	private static final int REQUEST_CODE_SIGN_IN = 1;
	private static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 2;

	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;
	private SignInButton mSignInButton;
	private TextView mSignInStatus;
    private View mSignOutButton;
    private View mRevokeAccessButton;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mPlusClient = new PlusClient.Builder(this, this, this)
		.setActions("http://schemas.google.com/AddActivity").build();


		mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
		mSignInButton.setOnClickListener(this);
		mSignInStatus = (TextView) findViewById(R.id.tvSignInStatus);

        mSignOutButton = findViewById(R.id.sign_out_button);
        mSignOutButton.setOnClickListener(this);
        mRevokeAccessButton = findViewById(R.id.revoke_access_button);
        mRevokeAccessButton.setOnClickListener(this);


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
		//mPlusClient.revokeAccessAndDisconnect(this); //for debugging use mPlusClient.disconnect(); instead
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
	public void onConnected(Bundle connectionHint) {
		//Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

		String currentPersonName = mPlusClient.getCurrentPerson() != null
                ? mPlusClient.getCurrentPerson().getDisplayName()
                :  getString(R.string.unknown_person);
               mSignInStatus.setText(getString(R.string.signed_in_status, currentPersonName));

        /*check if the user's google account is registered in the DB ,
         if not , ask his number , otherwise resume to next screen*/

        //until  Eidan finishes his DB methods , we cant check if the user is registered ,so we will  assume he isn't:
        askUserPhoneNumber();


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
                String value = input.getText().toString();
                Toast.makeText(getApplicationContext(), value,
                        Toast.LENGTH_LONG).show();

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }


    @Override
	public void onDisconnected() {
		mPlusClient.connect();

	}

    @Override
    public void onClick(View view) {
        if (view.getId()==R.id.sign_in_button){

        if(mPlusClient.isConnected()){
            Toast.makeText(this, "you are already signed in!", Toast.LENGTH_LONG).show();
        }

            int available = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            if (available != ConnectionResult.SUCCESS) {
                /* show a dialog that offers you to install  google play services */
                Dialog dialog = GooglePlayServicesUtil.
               getErrorDialog(available, this, DIALOG_GET_GOOGLE_PLAY_SERVICES);
                dialog.show();
                return;
            }

        if (view.getId() == R.id.sign_in_button && !mPlusClient.isConnected()) {
            try {
                mConnectionResult.startResolutionForResult(this, REQUEST_CODE_SIGN_IN);

            } catch (IntentSender.SendIntentException e) {
                mPlusClient.connect();
            }
        }
      }

        else if (view.getId()==R.id.sign_out_button){
            if (mPlusClient.isConnected()) {
                mPlusClient.clearDefaultAccount();
                mPlusClient.disconnect();
                mPlusClient.connect();
                mSignInStatus.setText("signed out");
            }
        }

        else {//(view.getId()==R.id.revoke_access_button
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
}
