package com.meeba.google.Util;

import com.google.gson.Gson;
import com.meeba.google.Objects.Event;
import com.meeba.google.Objects.User;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eidan on 11/8/13.
 *
 * Important: All of these methods must be used asynchronously with the AsyncTask class
 * in order for the UI thread to not be frozen.
 * If not used asynchronously you will generate a runtime error.
 */
public class UserFunctions {

    // TODO: will complete this once server code is ready

    public static User getUserByEmail(String email) {
        try {
            Gson lGson = new Gson();
            JSONObject lJsonObject = (JSONObject) JSONParser.doGETRequest(Utils.BASE_URL + "getUserByEmail/" + email);

            if(lJsonObject == null)
                return null;

            Utils.LOGD("lJsonObject = "+ lJsonObject.toString());
            return lGson.fromJson(lJsonObject.getString("user"), User.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static User createUser(String email, String name, String phone, String rid) {
        try {
            Gson lGson = new Gson();

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("phone", phone));
            params.add(new BasicNameValuePair("rid", rid));

            JSONObject lJsonObject = (JSONObject) JSONParser.doPOSTRequest(Utils.BASE_URL + "createUser", params);

            if(lJsonObject == null)
                return null;

            Utils.LOGD("lJsonObject = "+ lJsonObject.toString());
            return lGson.fromJson(lJsonObject.getString("user"), User.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<Event> getEventsByUser(int uid) {
        try {
            List<Event> events = new ArrayList<Event>();
            Gson lGson = new Gson();
            JSONObject lJsonObject = (JSONObject) JSONParser.doGETRequest(Utils.BASE_URL + "getEventsByUser/" + uid);

            if(lJsonObject == null)
                return null;

            Utils.LOGD("lJsonObject = "+ lJsonObject.toString());

            for(int i=0;i<lJsonObject.getJSONArray("events").length();i++) {
                events.add(lGson.fromJson(lJsonObject.getJSONArray("events").get(i).toString(), Event.class));
            }

            for(Event e : events) {
                Utils.LOGD("event = "+e.toString());
            }
            return events;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<User> getUsersByPhones(List<String> phones) {
        try {
            List<User> users = new ArrayList<User>();
            Gson lGson = new Gson();

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            JSONArray jsonPhones = new JSONArray(phones);

            params.add(new BasicNameValuePair("phones", jsonPhones.toString()));

            JSONObject lJsonObject = (JSONObject) JSONParser.doPOSTRequest(Utils.BASE_URL + "getUsersByPhones", params);

            if(lJsonObject == null)
                return null;

            Utils.LOGD("lJsonObject = "+ lJsonObject.toString());

            for(int i=0;i<lJsonObject.getJSONArray("users").length();i++) {
                users.add(lGson.fromJson(lJsonObject.getJSONArray("users").get(i).toString(), User.class));
            }

            for(User e : users) {
                Utils.LOGD("user = "+e.toString());
            }
            return users;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Event createEvent(int host_uid, String where, String when, List<String> uids) {
        try {
            Gson lGson = new Gson();

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("host_uid", String.valueOf(host_uid)));
            params.add(new BasicNameValuePair("where", where));
            params.add(new BasicNameValuePair("when", when));

            JSONArray uidArray = new JSONArray(uids);
            params.add(new BasicNameValuePair("uid", uidArray.toString()));

            JSONObject lJsonObject = (JSONObject) JSONParser.doPOSTRequest(Utils.BASE_URL + "createEvent", params);

            if(lJsonObject == null)
                return null;

            Utils.LOGD("lJsonObject = "+ lJsonObject.toString());
            return lGson.fromJson(lJsonObject.getString("event"), Event.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static boolean respondToInvite(int uid, int eid, int status) {
        try {
            Gson lGson = new Gson();

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("uid", String.valueOf(uid)));
            params.add(new BasicNameValuePair("eid", String.valueOf(eid)));
            params.add(new BasicNameValuePair("status", String.valueOf(status)));

            JSONObject lJsonObject = (JSONObject) JSONParser.doPOSTRequest(Utils.BASE_URL + "respondToInvite", params);

            if(lJsonObject == null)
                return false;

            Utils.LOGD("lJsonObject = "+ lJsonObject.toString());

            if(lJsonObject.getInt("success") == 1) {
                return true;
            }
            else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
    public static boolean acceptInvite(int uid, int eid) {
        return respondToInvite(uid, eid, 1);
    }

    public static boolean declineInvite(int uid, int eid) {
        return respondToInvite(uid, eid, -1);
    }

    /**
     * TODO
     * @param eid Event id
     * @return Returns a list of all users that are a part of this event
     */
    public static List<User> getUsersOfEvent(int eid) {
        return null;
    }
}
