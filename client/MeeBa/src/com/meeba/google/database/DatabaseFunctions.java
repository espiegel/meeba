package com.meeba.google.database;

import android.content.Context;

import com.meeba.google.Objects.User;
import com.meeba.google.Util.Utils;

import java.util.HashMap;

/**
 * Created by Eidan on 11/16/13.
 */
public class DatabaseFunctions {

    private static DatabaseHandler getDatabase(Context context) {
        // Store user details in SQLite Database
        final DatabaseHandler db = new DatabaseHandler(context);

        return db;
    }

    /**
     * Use this only once on login
     * @param context Application context
     * @param user User to be stored
     */
    public static void storeUserDetails(Context context, User user) {
        // Store only if there isn't a user stored already
        if(userIsStored(context)) {
            Utils.LOGD("user already stored here");
            return;
        }
        DatabaseHandler db = getDatabase(context);
        db.addUser(user.getUid(), user.getPhone_number(), user.getRid(), user.getCreated_at(), user.getEmail(), user.getName());
    }

    /**
     * Get the current user using this application
     * @param context Application context
     * @return Returns user object
     */
    public static User getUserDetails(Context context) {
        DatabaseHandler db = getDatabase(context);

        return db.getUserDetails();
    }

    public static void upgradeDatabase(Context context) {
        DatabaseHandler db = getDatabase(context);
        db.onUpgrade(db.getWritableDatabase(), 1, 1);
    }

    public static void resetTables(Context context) {
        getDatabase(context).resetTables();
    }

    private static boolean userIsStored(Context context) {
        return (getDatabase(context).getRowCount(DatabaseHandler.TABLE_USER) > 0);
    }
}
