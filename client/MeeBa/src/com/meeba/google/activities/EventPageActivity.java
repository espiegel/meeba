package com.meeba.google.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.meeba.google.R;
import com.meeba.google.adapters.GuestArrayAdapter;
import com.meeba.google.objects.Event;
import com.meeba.google.objects.User;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

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

    private int eid;
    private ImageView mImageHost;
    private AsyncTask<Void, Void, Void> refreshHostPicture = null;
    private ImageLoader mImageLoader;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventpage_activity);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Event Page");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        mTxtHost = (TextView)findViewById(R.id.txtHost);
        mTxtWhere = (TextView)findViewById(R.id.txtWhere);
        mTxtWhen = (TextView)findViewById(R.id.txtWhen);
        mListView = (ListView)findViewById(R.id.listGuests);
        mImageHost = (ImageView)findViewById(R.id.hostPicture);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mEvent = (Event)bundle.getSerializable(Utils.BUNDLE_EVENT);
        Utils.LOGD("bundle event=" + mEvent);
        mTxtHost.setText(mEvent.getHost_name());
        mTxtWhere.setText(mEvent.getWhere());
        mTxtWhen.setText(mEvent.getWhen());

        eid = mEvent.getEid();

        refreshHostPicture();
        refreshGuests();
    }

    private void refreshHostPicture() {
        refreshHostPicture = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final User host = UserFunctions.getUserByUid(mEvent.getHost_uid());

                if(host == null) {
                    return null;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(EventPageActivity.this).build();
                        mImageLoader = ImageLoader.getInstance();
                        mImageLoader.init(config);
                        mImageLoader.displayImage(host.getPicture_url(), mImageHost);
                    }
                });
                return null;
            }
        };
        refreshHostPicture.execute();
    }

    private void refreshGuests() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                List<User> guestList = UserFunctions.getUsersByEvent(eid);
                if(guestList == null) {
                    return null;
                }

                mGuestArrayAdapter = new GuestArrayAdapter(EventPageActivity.this,  guestList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListView.setAdapter(mGuestArrayAdapter);
                    }
                });

                return null;
            }
        }.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        if(refreshHostPicture != null) {
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
        startActivity(intent);
        finish();
    }
}