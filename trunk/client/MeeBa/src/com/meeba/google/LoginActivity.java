package com.meeba.google;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.*;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;

public class LoginActivity extends Activity implements OnClickListener,
ConnectionCallbacks, OnConnectionFailedListener //, OnAccessRevokedListener 
{


	private static final int REQUEST_CODE_SIGN_IN = 1;
	private static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 2;

	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;
	private SignInButton mSignInButton;
	private TextView mSignInStatus;




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mPlusClient = new PlusClient.Builder(this, this, this)
		.setActions("http://schemas.google.com/AddActivity").build();


		mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
		mSignInButton.setOnClickListener(this);
		mSignInStatus = (TextView) findViewById(R.id.tvSignInStatus);



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
       
		
	}


	@Override
	public void onDisconnected() {
		mPlusClient.connect();

	}

	@Override
	public void onClick(View view) {
		if(mPlusClient.isConnected()){
			Toast.makeText(this, "you are already signed in!", Toast.LENGTH_LONG).show();
		}


		int available = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (available != ConnectionResult.SUCCESS) {
			Toast.makeText(this, "Google Play Services Unavailble!", Toast.LENGTH_LONG).show();
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

/*
	@Override
	public void onAccessRevoked(ConnectionResult arg0) {
		
	}
*/

}
