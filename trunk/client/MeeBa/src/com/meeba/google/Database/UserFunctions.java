package com.meeba.google.Database;

import com.meeba.google.Objects.Event;
import com.meeba.google.Objects.User;

import java.util.List;

/**
 * Created by Eidan on 11/8/13.
 */
public class UserFunctions {

    // TODO: will complete this once server code is ready

    User getUserByEmail(String email) {
        return null;
    }

    User createUser(String email, String name, String phone, String rid) {
        return null;
    }

    List<Event> getEventsByUser(int uid) {
        return null;
    }

    List<User> getUsersByPhones(List<String> phones) {
        return null;
    }

    Event createEvent(int uid, String where, String when, List<String> emails) {
        return null;
    }

    void acceptInvite(int uid, int eid) {

    }

    void declineInvite(int uid, int eid) {

    }

    List<User> getUsersOfEvent(int eid) {
        return null;
    }
}
