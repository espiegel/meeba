package com.meeba.google.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.meeba.google.R;
import com.meeba.google.adapters.GuestArrayAdapter;
import com.meeba.google.database.DatabaseFunctions;
import com.meeba.google.dialogs.ContactDetailsDialog;
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
public class EventPageActivity extends SherlockFragmentActivity {
    private final int STATUS_ACCEPTED = 1;
    private final int STATUS_REJECTED = -1;
    private final int STATUS_UNKNOWN = 0;

    private TextView mTxtHost;
    private TextView mTxtTitle;
    private TextView mTxtWhere;
    private TextView mTxtWhen;
    private ListView mListView;
    private Event mEvent;
    private GuestArrayAdapter mGuestArrayAdapter = null;

    //add
    private ImageButton statusImgButton;
    private ImageView mMy_picture;
    private TextView mMy_name;

    private int eid;
    private ImageView mImageHost;
    private AsyncTask<Void, Void, Void> refreshGuests = null;

    private ImageLoader mImageLoader;
    private User mMyCurrentUser;  // User to be retrieved from  retrieved from phone DB
    private User mMe;             // User to be retrieved from current Event guests
    private User mHost;           // Host of the event
    private int mInviteStatus;    // The current users event status  of the current event (if he is invited)
    private int newStatus = 0;    // The status to change to, when clicking the Image Button


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventpage_activity);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Event Page");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        statusImgButton = (ImageButton) findViewById(R.id.statusImgBtn);
        mTxtHost = (TextView) findViewById(R.id.txtHost);
        mTxtTitle = (TextView) findViewById(R.id.txtTitle);
        mTxtWhere = (TextView) findViewById(R.id.txtWhere);
        mTxtWhen = (TextView) findViewById(R.id.txtWhen);
        mListView = (ListView) findViewById(R.id.listGuests);
        mImageHost = (ImageView) findViewById(R.id.eventpage_host_pic);

        mMy_name = (TextView) findViewById(R.id.myname);
        mMy_picture = (ImageView) findViewById(R.id.myPicture);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mEvent = (Event) bundle.getSerializable(Utils.BUNDLE_EVENT);
        Utils.LOGD("bundle event=" + mEvent);
        mHost = mEvent.getHost();
        mTxtHost.setText(mHost.getName());
        mTxtTitle.setText(mEvent.getTitle());
        mTxtWhere.setText(mEvent.getWhere());
        mTxtWhen.setText(mEvent.getWhen());

        //get the current user from phone DB:
        mMyCurrentUser = DatabaseFunctions.getUserDetails(getApplicationContext());
        mMy_name.setText(mMyCurrentUser.getName());

        eid = mEvent.getEid();

        mImageLoader = Utils.getImageLoader(this);
        mImageLoader.displayImage(mHost.getPicture_url(), mImageHost);
        mImageLoader.displayImage(mMyCurrentUser.getPicture_url(), mMy_picture);

        RelativeLayout guestLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        LinearLayout hostLayout = (LinearLayout) findViewById(R.id.hostLayout);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                User user = (User) adapterView.getItemAtPosition(position);
                showContactDialog(user);
                return false;
            }
        });
        guestLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showContactDialog(mMyCurrentUser);
                return false;
            }
        });
        hostLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showContactDialog(mHost);
                return false;
            }
        });
        //if the host is the current user , he shouldn't be able to change his status
        if (mMyCurrentUser.getUid() == mHost.getUid()) {
            Utils.LOGD(" max debug " + mMy_name + "  " + mMy_picture + " " + "  " + mMyCurrentUser);
            mMy_name.setVisibility(View.GONE);
            mMy_picture.setVisibility(View.GONE);
            statusImgButton.setVisibility(View.GONE);
        } else {
            /** get the current user, and his invite status , and set the status of the image button **/
            new AsyncTask<Void, Void, User>() {

                @Override
                protected User doInBackground(Void... voids) {
                    List<User> guestList = UserFunctions.getUsersByEvent(eid);
                    if (guestList == null) {
                        return null;
                    }
                    for (User u : guestList) {
                        if (u.getUid() == mMyCurrentUser.getUid()) {
                            mMe = u;
                        }
                    }
                    return mMe;
                }

                @Override
                protected void onPostExecute(User me) {
                    mInviteStatus = me.getInvite_status();

                    if (mInviteStatus == STATUS_ACCEPTED) {
                        statusImgButton.setImageDrawable(getResources().getDrawable(R.drawable.green_check_boxed));
                        statusImgButton.setTag(1);
                    } else if (mInviteStatus == STATUS_UNKNOWN) {
                        statusImgButton.setImageDrawable(getResources().getDrawable(R.drawable.empty_box));
                        statusImgButton.setTag(0);
                    } else {
                        statusImgButton.setImageDrawable(getResources().getDrawable(R.drawable.red_cross_boxed));
                        statusImgButton.setTag(STATUS_REJECTED);
                    }


                    //set click listener on the image button :
                    statusImgButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int status = (Integer) statusImgButton.getTag();
                            Utils.LOGD("status: = " + status);
                            tryToChangeStatus();
                        }
                    });
                }
            }.execute();
        }

        /** rest of onCreate : **/
        LinearLayout layoutWhere = (LinearLayout) findViewById(R.id.layoutWhere);
        layoutWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String query = mTxtWhere.getText().toString();
                AlertDialog.Builder builder = new AlertDialog.Builder(EventPageActivity.this);
                builder.setMessage("Do you want to search for " + query + " using Waze?")
                        .setIcon(R.drawable.waze_icon)
                        .setTitle("Waze Navigation")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    String url = "waze://?q=" + mTxtWhere.getText();
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
                                    startActivity(intent);
                                } finally {
                                    dialogInterface.dismiss();
                                }
                            }
                        });
                builder.create().show();
            }
        });


        refreshGuests();
    }

    /**
     * try to change the user's invite status from  mInviteStatus  to the  opposite status  *
     */
    private void tryToChangeStatus() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(EventPageActivity.this);

        switch (mInviteStatus) {
            case STATUS_ACCEPTED: {
                alertDialog.setMessage("Decline the event?");
                // Setting  "YES" Button
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EventPageActivity.this, "Event declined", Toast.LENGTH_SHORT).show();
                        newStatus = STATUS_REJECTED;
                        asyncChangeStatus();
                    }
                });

                // Setting  "NO" Button
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
            break;

            case STATUS_REJECTED: {
                alertDialog.setMessage("Attend the event?");
                // Setting  "YES" Button
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EventPageActivity.this, "Event attended", Toast.LENGTH_SHORT).show();
                        newStatus = STATUS_ACCEPTED;
                        asyncChangeStatus();
                    }
                });
                // Setting  "NO" Button
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();

            }
            break;

            case STATUS_UNKNOWN: {
                alertDialog.setMessage("Change your invite status:");
                alertDialog.setIcon(R.drawable.green_check);

                // Setting  "YES" Button
                alertDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EventPageActivity.this, "Event attended", Toast.LENGTH_SHORT).show();
                        newStatus = STATUS_ACCEPTED;
                        asyncChangeStatus();
                    }
                });

                // Setting  "NO" Button
                alertDialog.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EventPageActivity.this, "Event declined", Toast.LENGTH_SHORT).show();
                        newStatus = STATUS_REJECTED;
                        asyncChangeStatus();
                    }
                });

                // Setting  "Cancel" Button
                alertDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();

            }
            break;
            default:
        }
    }

    /**
     * change the invite status the the status stored in the global var : newStatus*
     */
    private void asyncChangeStatus() {
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog loading = new ProgressDialog(EventPageActivity.this);

            @Override
            protected void onPreExecute() {
                this.loading.setMessage("changing status  ... ");
                this.loading.show();
            }

            @Override
            protected Void doInBackground(Void... params) {

                if (newStatus == STATUS_ACCEPTED) {
                    mMe.setInvite_status(STATUS_ACCEPTED);
                    UserFunctions.acceptInvite(mMe.getUid(), eid);
                } else if (newStatus == STATUS_REJECTED) {
                    mMe.setInvite_status(STATUS_REJECTED);
                    UserFunctions.declineInvite(mMe.getUid(), eid);
                }
                return null;
            }

            protected void onPostExecute(Void v) {
                if (loading.isShowing())
                    loading.dismiss();
                if (newStatus == STATUS_ACCEPTED)
                    statusImgButton.setImageDrawable(getResources().getDrawable(R.drawable.green_check_boxed));
                else
                    statusImgButton.setImageDrawable(getResources().getDrawable(R.drawable.red_cross));

                mInviteStatus = newStatus;
            }

        }.execute();
    }


    private void showContactDialog(User user) {
        ContactDetailsDialog dialog = new ContactDetailsDialog(user);
        dialog.show(getSupportFragmentManager(), ContactDetailsDialog.TAG);
    }

    private void refreshGuests() {
        refreshGuests = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                List<User> guestListWithoutMe = new ArrayList<User>();

                List<User> guestList = UserFunctions.getUsersByEvent(eid);
                if (guestList == null) {
                    return null;
                }
                for (User u : guestList) {
                    if (u.getUid() != mMyCurrentUser.getUid()) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        if (refreshGuests != null) {
            refreshGuests.cancel(true);
        }

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
}