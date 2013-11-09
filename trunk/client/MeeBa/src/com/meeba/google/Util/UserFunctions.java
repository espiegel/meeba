package com.meeba.google.Util;

import com.google.gson.Gson;
import com.meeba.google.Objects.Event;
import com.meeba.google.Objects.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eidan on 11/8/13.
 */
public class UserFunctions {

    // TODO: will complete this once server code is ready

    public static User getUserByEmail(String email) {
        try {
            Gson lGson = new Gson();
            JSONObject lJsonObject = (JSONObject) new JSONParser().doGETRequest(Utils.BASE_URL + "getUserByEmail/" + email);

            return lGson.fromJson(lJsonObject.getString("user"), User.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static User createUser(String email, String name, String phone, String rid) {
        return null;
    }

    public static List<Event> getEventsByUser(int uid) {
        return null;
    }

    public static List<User> getUsersByPhones(List<String> phones) {
        return null;
    }

    public static Event createEvent(int uid, String where, String when, List<String> emails) {
        return null;
    }

    public static void acceptInvite(int uid, int eid) {

    }

    public static void declineInvite(int uid, int eid) {

    }

    public static List<User> getUsersOfEvent(int eid) {
        return null;
    }
}
