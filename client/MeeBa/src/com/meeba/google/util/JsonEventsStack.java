package com.meeba.google.util;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Stack;

/**
 * Created by Max on 05/01/14.
 */
public class JsonEventsStack {

    public static String toJson(Stack<String> waitList) {
        JSONArray jsonEventsStack = new JSONArray();
        for (String eid : waitList) {
            jsonEventsStack.put(eid);
        }
        return jsonEventsStack.toString();
    }

    public static Stack<String> fromJson(String jsonArray) {
        JSONArray jArray= null;
        try {
            jArray = new JSONArray(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Stack<String> waitList = new Stack<String>();
        int n = jArray.length();
        for (int i = 0; i < jArray.length(); i++) {
            try {
                waitList.push((String) jArray.get(n - i - 1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return waitList;
    }
}
