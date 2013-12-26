package com.meeba.google.util;

import android.graphics.Bitmap;
import android.util.Base64;

import com.google.gson.Gson;
import com.meeba.google.objects.Event;
import com.meeba.google.objects.User;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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
    public static User createUser(String email, String name, String phone, String rid, String pictureUrl, int isDummy) {
        try {
            Gson lGson = new Gson();

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("phone", phone));
            params.add(new BasicNameValuePair("rid", rid));
            params.add(new BasicNameValuePair("picture_url", pictureUrl));
            params.add(new BasicNameValuePair("is_dummy", String.valueOf(isDummy)));

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
    public static List<Event> getEventsByUser(int uid) throws Exception {
        try {
            List<Event> events = new ArrayList<Event>();
            Gson lGson = new Gson();
            JSONObject lJsonObject = (JSONObject) JSONParser.doGETRequest(Utils.BASE_URL + "getEventsByUser/" + uid);
            if(lJsonObject == null)
                return null;

            Utils.LOGD("lJsonObject = "+ lJsonObject.toString());

            for(int i=0;i<lJsonObject.getJSONArray("events").length();i++) {
                Utils.LOGD("events = "+lJsonObject.getJSONArray("events").get(i).toString());
                events.add(lGson.fromJson(lJsonObject.getJSONArray("events").get(i).toString(), Event.class));
            }

            for(Event e : events) {
                Utils.LOGD("event = "+e.toString());
            }

            return events;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
           // return null;
        }
    }

    /**
     * Get the events that user is participating in and has a certain invite status in. (Event hosts count as status 1)
     * @param uid User if of the user.
     * @param status Invited status of the user. Must be one of {-1,0,1}
     * @return Returns a list of all events or null if the web service failed.
     */
    public static List<Event> getEventsByUser(int uid, int status) throws Exception {
        if(status != 0 && status != -1 && status != 1) {
            return null;
        }

        try {
            List<Event> events = new ArrayList<Event>();
            Gson lGson = new Gson();
            JSONObject lJsonObject = (JSONObject) JSONParser.doGETRequest(Utils.BASE_URL + "getEventsByUser/" + uid + "/" + status);
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
            throw ex;
            // return null;
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
    public static Event createEvent(int host_uid,String title, String where, String when, List<String> uids) {
        try {
            Gson lGson = new Gson();

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("host_uid", String.valueOf(host_uid)));
            params.add(new BasicNameValuePair("title", title));
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
     * Updates an event with new values: title, where, when.
     * @param eid eid of event to update
     * @param title new title
     * @param where new where
     * @param when new when
     * @return Returns new event on success and null on failure
     */
    public static Event updateEvent(int eid, String title, String when, String where) {
        try {
            Gson lGson = new Gson();

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("eid", String.valueOf(eid)));
            params.add(new BasicNameValuePair("title", title));
            params.add(new BasicNameValuePair("when", when));
            params.add(new BasicNameValuePair("where", where));

            JSONObject lJsonObject = (JSONObject) JSONParser.doPOSTRequest(Utils.BASE_URL + "updateEvent", params);

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

    // Respond to an invite
    private static boolean respondToInvite(int uid, int eid, int status) {
        try {
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

    /**
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

    /**
     * Deletes an event.
     * @param eid Event id
     * @return Returns true on success and false on failure.
     */
    public static boolean deleteEvent(int eid) {
        try {
            JSONObject lJsonObject = (JSONObject) JSONParser.doGETRequest(Utils.BASE_URL + "deleteEvent/" + eid);

            if(lJsonObject == null)
                return false;

            Utils.LOGD("lJsonObject = "+ lJsonObject.toString());

            if(lJsonObject.getInt("success") == 1) {
                return true;
            }
            else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gives a list of place suggestions given a query input. Uses Google Places API.
     * @param input The input string to perform autocompletion on
     * @return Returns a list of autocompletion suggestions or null on failure.
     */
    public static List<String> placeAutocomplete(String input) {
        List<String> suggestions = new ArrayList<String>();

        try {
            Utils.LOGD(Utils.BASE_URL + "placeAutocomplete/" + input);
            JSONObject lJsonObject = (JSONObject) JSONParser.doGETRequest(Utils.BASE_URL + "placeAutocomplete/" + input);

            if(lJsonObject == null)
                return null;

            Utils.LOGD("lJsonObject = "+ lJsonObject.toString());

            if(lJsonObject.getInt("success") == 1) {
                JSONArray places = lJsonObject.getJSONArray("places");
                for(int i=0;i<places.length();i++) {
                    suggestions.add((String)places.get(i));
                }

                Utils.LOGD("suggestions = "+suggestions);
                return suggestions;
            }
            else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String bitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String strBitMap = Base64.encodeToString(b, Base64.DEFAULT);
        return strBitMap;
    }

    public static boolean uploadImage(int eid, Bitmap bitmap) {
        String pictureData = bitMapToString(bitmap);

        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("eid", String.valueOf(eid)));
            params.add(new BasicNameValuePair("pictureData", pictureData));

            JSONObject lJsonObject = (JSONObject) JSONParser.doPOSTRequest(Utils.BASE_URL + "uploadEventPicture", params);

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
