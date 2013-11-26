package com.meeba.google.util;

import com.google.gson.Gson;
import com.meeba.google.objects.Event;
import com.meeba.google.objects.User;

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

    /**
     * Get a user by his email
     * @param email Email of the user
     * @return Returns a User object of the user with the same email or null if none was found.
     */
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

    /**
     * Get a user by his uid
     * @param uid uid of the user
     * @return Returns a User object of the user with the same uid or null if none was found.
     */
    public static User getUserByUid(int uid) {
        try {
            Gson lGson = new Gson();
            JSONObject lJsonObject = (JSONObject) JSONParser.doGETRequest(Utils.BASE_URL + "getUserByUid/" + uid);

            if(lJsonObject == null)
                return null;

            Utils.LOGD("lJsonObject = "+ lJsonObject.toString());
            return lGson.fromJson(lJsonObject.getString("user"), User.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a new user
     * @param email Email of the new user
     * @param name Name of the new user
     * @param phone Phone number of the new user
     * @param rid Registration ID of the new user (from GCM)
     * @param pictureUrl Url of google plus picture
     * @return Returns a User object of the created user or null if user creation failed.
     */
    public static User createUser(String email, String name, String phone, String rid, String pictureUrl) {
        try {
            Gson lGson = new Gson();

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("phone", phone));
            params.add(new BasicNameValuePair("rid", rid));
            params.add(new BasicNameValuePair("picture_url", pictureUrl));

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

    /**
     * Get the events that user is participating in. Events that he is a host of and events that he is invited to.
     * @param uid User if of the user.
     * @return Returns a list of all events or null if the web service failed.
     */
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

    /**
     * Gets a list of all users from a list of phone numbers
     * @param phones A list of all phone numbers. Every phone number must be a string of digits only.
     * @return Returns a list of users with the given phone numbers.
     */
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

    /**
     * Creates a new event. Sends out invitations to all users and sends them a push notification.
     * @param host_uid User id of the host
     * @param where Where the event is taking place
     * @param when When the event is taking place
     * @param uids A List of Strings containing the uids of all the invited guests.
     * @return Returns the created event on success or null if an error occurred.
     */
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

    /**
     * Accept an invitation
     * @param uid The user id of the current user that is accepting this invitation
     * @param eid The event id that this user is accepting
     * @return Returns true on success and false on failure.
     */
    public static boolean acceptInvite(int uid, int eid) {
        return respondToInvite(uid, eid, 1);
    }

    /**
     * Decline an invitation
     * @param uid The user id of the current user that is declining this invitation
     * @param eid The event id that this user is declining
     * @return Returns true on success and false on failure.
     */
    public static boolean declineInvite(int uid, int eid) {
        return respondToInvite(uid, eid, -1);
    }

    /**
     * TODO
     * @param eid Event id
     * @return Returns a list of all users that are a part of this event
     */
    public static List<User> getUsersByEvent(int eid) {
        List<User> users = new ArrayList<User>();

        try {
            Gson lGson = new Gson();
            JSONObject lJsonObject = (JSONObject) JSONParser.doGETRequest(Utils.BASE_URL + "getUsersByEvent/" + eid);

            if(lJsonObject == null)
                return null;

            Utils.LOGD("lJsonObject = "+ lJsonObject.toString());

            if(lJsonObject.getInt("success") == 1) {
                for(int i=0;i<lJsonObject.getJSONObject("users").getJSONArray("guests").length();i++) {
                    users.add(lGson.fromJson(lJsonObject.getJSONObject("users").getJSONArray("guests").get(i).toString(), User.class));
                }

                for(User e : users) {
                    Utils.LOGD("user = "+e.toString());
                }
                return users;
            }
            else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
}
