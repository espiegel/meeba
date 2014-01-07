package com.meeba.google.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Stack;

/**
 * Created by Max on 05/01/14.
 */
public class JsonEventsStack {

    public static String toJson(String uid, Stack<String> waitList) throws JSONException {
        JSONObject jsonWaitList = new JSONObject();

        jsonWaitList.put("uid", uid);
        JSONArray jsonEventsStack = new JSONArray();
        for (String eid : waitList) {
            jsonEventsStack.put(eid);
        }

        jsonWaitList.put("events", jsonEventsStack);

        return jsonWaitList.toString();
    }

    public static Stack<String> getEventsStackfromJson(String jsonObject) throws JSONException {
        JSONObject jObj ;
        JSONArray jArray;
        Stack<String> waitList ;

        jObj = new JSONObject(jsonObject);

        jArray = jObj.getJSONArray("events");
        waitList = new Stack<String>();
        int n = jArray.length();
        for (int i = 0; i < jArray.length(); i++) {
            waitList.push((String) jArray.get(n - i - 1));
        }

        return waitList;
    }

    public static String getUidfromJson(String jsonObject) throws JSONException {
        JSONObject jObj;
        String uid ;
        jObj = new JSONObject(jsonObject);
        uid = jObj.getString("uid");
        return uid;
    }
}