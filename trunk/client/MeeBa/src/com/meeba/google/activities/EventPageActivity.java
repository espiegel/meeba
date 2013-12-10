package com.meeba.google.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.meeba.google.R;
import com.meeba.google.adapters.GuestArrayAdapter;
import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.objects.Event;
import com.meeba.google.objects.User;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eidan on 11/19/13.
 */
public class EventPageActivity extends SherlockActivity {
    private TextView mTxtHost;
    private TextView mTxtWhere;
    private TextView mTxtWhen;
    private ListView mListView;
    private Event mEvent;
    private GuestArrayAdapter mGuestArrayAdapter = null;
  //  private ImageView mAcceptButton;

    //add
    private ImageView mMy_picture;
    private TextView mMy_name;
 //   private Switch my_status;
    private CheckBox mMy_status;

    private int eid;
    private ImageView mImageHost;
    private AsyncTask<Void, Void, Void> refreshHostPicture = null;
    private AsyncTask<Void, Void, Void> refreshMyPicture = null;
    private AsyncTask<Void, Void, Void> refreshMyOwnPicture = null;
    private AsyncTask<Void, Void, Void> refreshGuests = null;
   // private AsyncTask<Void, Void, Void> refreshSwitch = null;
    private AsyncTask<Void, Void, Void> refreshCheckBox = null;
  //  private AsyncTask<Void, Void, User> initSwitch = null;
    private AsyncTask<Void, Void, User> initCheckBox = null;

    private ImageLoader mImageLoader;
    private User mCurrentUser;
    private User mMyCurrentUser;
    private User mMe;
   // final Activity eventPage = this;

    @SuppressLint("NewApi")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventpage_activity);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Event Page");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);



        mTxtHost = (TextView) findViewById(R.id.txtHost);
        mTxtWhere = (TextView) findViewById(R.id.txtWhere);
        mTxtWhen = (TextView) findViewById(R.id.txtWhen);
        mListView = (ListView) findViewById(R.id.listGuests);
        mImageHost = (ImageView) findViewById(R.id.hostPicture);

        //add
        mMy_name = (TextView) findViewById(R.id.myname);
        mMy_picture = (ImageView) findViewById(R.id.myPicture);
  //      my_status = (Switch) findViewById(R.id.switch1);
   //     mAcceptButton = (ImageView) findViewById(R.id.action_accept_invite_status);

        mMy_status = (CheckBox) findViewById(R.id.checkInviteBox);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();


        mEvent = (Event) bundle.getSerializable(Utils.BUNDLE_EVENT);
        Utils.LOGD("bundle event=" + mEvent);
        mTxtHost.setText(mEvent.getHost_name());
        mTxtWhere.setText(mEvent.getWhere());
        mTxtWhen.setText(mEvent.getWhen());

        //add
        mMyCurrentUser = DatabaseFunctions.getUserDetails(getApplicationContext());
        mMy_name.setText(mMyCurrentUser.getName());

        eid = mEvent.getEid();


        if(mMyCurrentUser.getUid()==mEvent.getHost_uid()){
            mMy_status.setVisibility(View.GONE);
            mMy_name.setVisibility(View.GONE);
            mMy_picture.setVisibility(View.GONE);
            refreshHostPicture();
           // refreshMyPicture();
            refreshMyOwnPicture();
            refreshGuests();
        }
else{
            initCheckBox();

            //    initSwitch();
   //     initImage();

        mMy_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                refreshCheckBox =  new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... voids) {

                        List<User> guestList = UserFunctions.getUsersByEvent(eid);
                        if (guestList == null) {
                            return null;
                        }
                        for(User u:guestList) {
                            if(u.getUid()==mMyCurrentUser.getUid()){
                                mMe = u;
                            }
                        }
                        if(isChecked){
                            if(mMe.getInvite_status()==1){

                            }
                            else{
                            mMe.setInvite_status(1);
                            UserFunctions.acceptInvite(mMe.getUid(), eid);
                            }
                        }
                        else{
                            if(mMe.getInvite_status()==-1){

                            }
                            else{
                            mMe.setInvite_status(-1);
                            UserFunctions.declineInvite(mMe.getUid(), eid);
                        }
                        }
                        return null;
                    }
                };
                refreshCheckBox.execute();

            }
        });



        refreshHostPicture();
     //   refreshMyPicture();
        refreshMyOwnPicture();
        refreshGuests();
    }
    }
    /*
    private void refreshMyPicture() {
        refreshMyPicture = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                if (mCurrentUser == null) {
                    return null;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageLoader = ImageLoader.getInstance();
                        if(!mImageLoader.isInited()) {
                            mImageLoader.init(Utils.getImageLoaderConfig(EventPageActivity.this));
                        }
                        mImageLoader.displayImage(mCurrentUser.getPicture_url(), mImageHost);
                    }
                });
                return null;
            }
        };
        refreshMyPicture.execute();
    }
    */
    private void refreshMyOwnPicture() {
        refreshMyOwnPicture = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {



                if (mMyCurrentUser == null) {
                    return null;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageLoader = ImageLoader.getInstance();
                        if(!mImageLoader.isInited()) {
                            mImageLoader.init(Utils.getImageLoaderConfig(EventPageActivity.this));
                        }
                        mImageLoader.displayImage(mMyCurrentUser.getPicture_url(), mMy_picture);
                    }
                });
                return null;
            }
        };
        refreshMyOwnPicture.execute();
    }

    private void refreshHostPicture() {
        refreshHostPicture = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                final User host = UserFunctions.getUserByUid(mEvent.getHost_uid());

                if (host == null) {
                    return null;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageLoader = ImageLoader.getInstance();
                        if(!mImageLoader.isInited()) {
                            mImageLoader.init(Utils.getImageLoaderConfig(EventPageActivity.this));
                        }
                        mImageLoader.displayImage(host.getPicture_url(), mImageHost);
                    }
                });
                return null;
            }
        };
        refreshHostPicture.execute();
    }

    private void refreshGuests() {
      refreshGuests =  new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                List<User> guestListWithoutMe = new ArrayList<User>();

                List<User> guestList = UserFunctions.getUsersByEvent(eid);
                if (guestList == null) {
                    return null;
                }
                for(User u:guestList) {

                    if(u.getUid()== mMyCurrentUser.getUid()){

                    }
                    else{
                        guestListWithoutMe.add(u);
                }
                }
                mGuestArrayAdapter = new GuestArrayAdapter(EventPageActivity.this, guestListWithoutMe);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListView.setAdapter(mGuestArrayAdapter);
                    }
                });

                return null;
            }
        };
              refreshGuests.execute();
    }

  //  @Override
 //   public boolean onOptionsItemSelected(MenuItem menuItem) {
  //      onBackPressed();
  //      return true;
  //  }

    @Override
    protected void onDestroy() {
        if (refreshHostPicture != null) {
            refreshHostPicture.cancel(true);
        }

        /*if(mImageLoader != null) {
            mImageLoader.stop();
            mImageLoader.destroy();
        }*/

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }






/*
    public void initSwitch (){

        initSwitch =  new AsyncTask<Void, Void, User>() {

            protected User doInBackground(Void... voids) {
                List<User> guestList = UserFunctions.getUsersByEvent(eid);
                if (guestList == null) {
                    return null;
                }
                for(User u:guestList) {
                    if(u.getUid()==myCurrentUser.getUid()){
                        me = u;
                    }
                }
                return me;
            }

            protected void onPostExecute(User me){
                if( me.getInvite_status()==1){
                    my_status.setChecked(true);
                }

                else{
                    my_status.setChecked(false);
                }
            }
        };
        initSwitch.execute();
    }
    */
 /*
    public void initImage (){

        initImage =  new AsyncTask<Void, Void, User>() {

            protected User doInBackground(Void... voids) {
                List<User> guestList = UserFunctions.getUsersByEvent(eid);
                if (guestList == null) {
                    return null;
                }
                for(User u:guestList) {
                    if(u.getUid()==myCurrentUser.getUid()){
                        me = u;
                    }
                }
                return me;
            }

            protected void onPostExecute(User me){
                if( me.getInvite_status()==1){
              //      my_status.setChecked(true);
                    //setImageResource(R.id.action_accept_invite_status);
                }

                else{
              //      my_status.setChecked(false);
                 //   mAcceptButton.setImageResource(R.id.action_decline_invite_status);
//                mAcceptButton.setBackgroundResource(R.id.action_decline_invite_status);
                }
            }
        };
        initImage.execute();
    }
*/
    /*

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.eventpage_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_accept_invite_status:
                changeInviteStatus();
                return true;

            default:
                break;
        }
        onBackPressed();
        return true;

    }

public void changeInviteStatus(){

}
*/


    public void initCheckBox(){
        initCheckBox =  new AsyncTask<Void, Void, User>() {

            protected User doInBackground(Void... voids) {
                List<User> guestList = UserFunctions.getUsersByEvent(eid);
                if (guestList == null) {
                    return null;
                }
                for(User u:guestList) {
                    if(u.getUid()==mMyCurrentUser.getUid()){
                        mMe = u;
                    }
                }
                return mMe;
            }

            protected void onPostExecute(User me){
                if( me.getInvite_status()==1){
                    mMy_status.setChecked(true);
                }

                else{
                    mMy_status.setChecked(false);
                }
            }
        };
        initCheckBox.execute();
    }


}