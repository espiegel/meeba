package com.meeba.google.Database;

import com.meeba.google.Objects.Event;
import com.meeba.google.Objects.User;

import java.util.List;

/**
 * Created by Eidan on 11/8/13.
 */
public class UserFunctions {

    // TODO: will complete this once server code is ready

    public static User getUserByEmail(String email) {
        return null;
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
