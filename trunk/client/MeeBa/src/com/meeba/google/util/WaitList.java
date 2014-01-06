package com.meeba.google.util;

import android.support.v4.util.ArrayMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Stack;

/**
 * Created by Max on 05/01/14.
 */
public class WaitList {
    private static Map<String, Stack<String>> waitList;


    public WaitList() {
        waitList = new ArrayMap<String, Stack<String>>();
    }

    public static void putInWaitList(String uid, String eid) {

        if (waitList.containsKey(uid)) {
            waitList.get(uid).push(eid);
        } else {
            Stack<String> events = new Stack<String>();
            events.push(eid);
            waitList.put(uid, events);
        }
    }

    public static Stack<String> getWaitingEvents(String uid) {
        return waitList.get(uid);
    }

    public static Map<String, Stack<String>> getWaitList() {
        return waitList;
    }

    public static void setWaitList(Map<String, Stack<String>> waitList) {
        WaitList.waitList = waitList;
    }

    /**
     * @return returns a Json array where each element is a json object wih fields  uid & event stack
     */
    public static String toJson() {

        JSONArray jWaitingList = new JSONArray();
        for (Map.Entry<String, Stack<String>> entry : waitList.entrySet()) {

            //get the uid:
            String uid = entry.getKey();

            //get the event stack
            Stack<String> events = entry.getValue();


            //build  the  events stack
            JSONArray jStack = new JSONArray();
            for (String eid : events) {
                jStack.put(eid);
            }

            //build a uid,events  pair
            JSONObject uidEventsPair = new JSONObject();
            try {
                uidEventsPair.put("uid", uid);
                uidEventsPair.put("events", jStack);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //put the  pair in jWaitingList
            jWaitingList.put(uidEventsPair);
        }

        Utils.LOGD("waitingList json  = " + jWaitingList.toString());
        return jWaitingList.toString();
    }

    public static Map<String, Stack<String>> fromJson(String jsonString) {
        Map<String, Stack<String>> realWaitingList = new ArrayMap<String, Stack<String>>();

        try {
            JSONArray jsonWaitingList = new JSONArray(jsonString);
            Utils.LOGD("jsonWaitingList json  = " + jsonWaitingList.toString());

            for (int i = 0; i < jsonWaitingList.length(); i++) {
                Stack<String> eventsStack = new Stack<String>();

                JSONObject uidStackPair = (JSONObject) jsonWaitingList.get(i);
                Utils.LOGD("uidStackPair json  = " + uidStackPair.toString());
                String uid = uidStackPair.getString("uid");
                Utils.LOGD("uidStackPair.getString  = " + uid);
                JSONArray jsonEventArray = uidStackPair.getJSONArray("events");
                Utils.LOGD("jsonEventArray  =  " + jsonEventArray);

                //create the events stack from the jason events array by pushing from end of array
                int len = jsonEventArray.length();
                for (int j = 0; j < jsonEventArray.length(); j++) {
                    eventsStack.push((String) jsonEventArray.get(len - j - 1));
                }
                realWaitingList.put(uid, eventsStack);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return realWaitingList;
    }
}
