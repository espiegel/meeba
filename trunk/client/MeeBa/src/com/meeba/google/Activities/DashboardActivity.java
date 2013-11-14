package com.meeba.google.Activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.Intent;

import com.meeba.google.Objects.Event;
import com.meeba.google.R;
import com.meeba.google.Util.UserFunctions;
import com.meeba.google.Util.Utils;

import android.widget.Button;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by Padi on 07/11/13.
 */
public class DashboardActivity extends Activity {
    /*List<Event>   myEvents;*/
    /*List<Event> eventList = new ArrayList<Event>();*/
    List<Event> eventList = new LinkedList<Event>();

    ArrayList<String> eventsItems;
    ArrayAdapter<String> eventItemAdapter;
    List<String> ListEventInfo;
   /* ListAdapter listAdp;*/
    private int uid = 1;
    private ListView mListViewDash;
    /*EventArrayAdapter eventArrayAdapter;*/
    Button createEvent;

   /* Intent intent = getIntent();*/
   /* String userID = intent.getStringExtra(User);*/
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);
        // Constants used when saving and restoring
        createDashboard();





    }

    private void asyncRefresh(List<Event> eventList ) {
            AsyncTask<Void, Void, List<Event>> task = new AsyncTask<Void, Void, List<Event>>() {
                protected void onPreExecute() {
                    Utils.LOGD("onPreExecute");
                }

                protected List<Event> doInBackground(Void... params) {
                    Utils.LOGD("doInBackground");


                    return UserFunctions.getEventsByUser(uid);

                }

                protected void onPostExecute(List<Event> v) {
                    Utils.LOGD("onPostExecute");

                }

            };
            task.execute();

    }



    private void createDashboard() {
        asyncRefresh(eventList);


        mListViewDash = (ListView) findViewById(R.id.listViewDashboard);

        eventsItems = new ArrayList<String>();
        eventsItems.add(0,"shtota");
        eventsItems.add(0,"whatThe Fuck");

        for (Event element : eventList) {
            String eventInfo= "where:" + element.getWhere() + " when:" + element.getWhen();
                   /* Toast.makeText(getApplicationContext(),element.getWhere(),Toast.LENGTH_LONG).show();*/
            eventsItems.add(0,eventInfo);
        }
        eventsItems.add(0,"after for");
        eventsItems.add(0,"the end");

        eventItemAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, eventsItems);
        mListViewDash.setAdapter(eventItemAdapter);








// Toast.makeText(getApplicationContext(),eventList.get(0).getWhen().toString(),Toast.LENGTH_LONG).show();





       /* eventArrayAdapter = new EventArrayAdapter(this,  eventList);*/


    /*
        mListViewDash.setAdapter( eventArrayAdapter);

        registerForContextMenu(mListViewDash);*/

        createEvent = (Button) findViewById(R.id.createEvent);

        createEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  Utils.LOGD("on click");
                //when we click on the button it will bring us to the  WhereWhenActivity activity
                Intent i = new Intent(getApplicationContext(),
                        WhereWhenActivity.class);
                //build this so i can use user's input in the post activity
                Bundle bundle = new Bundle();
                bundle.putInt("uid",uid);
                i.putExtras(bundle);
                startActivity(i);

            }
        });




    }


}