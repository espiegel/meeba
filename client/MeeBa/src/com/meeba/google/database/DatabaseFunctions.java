package com.meeba.google.database;

import android.content.Context;

import com.meeba.google.objects.User;
import com.meeba.google.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eidan on 11/16/13.
 */
public class DatabaseFunctions {

      private static DatabaseHandler getDatabase(Context context) {
            // Store user details in SQLite Database
            final DatabaseHandler db = new DatabaseHandler(context);

            return db;
      }

      public static void storeContacts(Context context,  List<User> users) {
            for (User user : users) {

                  Utils.LOGD("maxagi: storing user in database +" + user  );
                  storeUserDetails(context, user, DatabaseHandler.TABLE_CONTACTS);
            }
      }

      public static List<User> loadContacts(Context context) {
            DatabaseHandler db = getDatabase(context);
            List<User> users = db.getContacts();
            return users;
            }

      /**
       * Use this only once on login
       *
       * @param context Application context
       * @param user    User to be stored
       */
      public static void storeUserDetails(Context context, User user, String tableName) {
            // Store only if there isn't a user stored already
            if (userIsStored(context, tableName)) {
                  Utils.LOGD("user already stored here");
                  return;
            }
            DatabaseHandler db = getDatabase(context);
            db.addUser(DatabaseHandler.TABLE_USER, user.getUid(), user.getPhone_number(), user.getRid(), user.getCreated_at(), user.getEmail(), user.getName(), user.getPicture_url());
      }

      /**
       * Get the current user using this application
       *
       * @param context Application context
       * @return Returns user object
       */
      public static User getUserDetails(Context context, String tableName) {
            DatabaseHandler db = getDatabase(context);
            if (!userIsStored(context, tableName)) {
                  Utils.LOGD(" getUserDetails error : user is not stored ! ");
                  return null;
            }

            return db.getUserDetails(tableName);
      }

      public static void upgradeDatabase(Context context) {
            DatabaseHandler db = getDatabase(context);
            db.onUpgrade(db.getWritableDatabase(), 1, 1);
      }

      public static void resetTables(Context context) {
            getDatabase(context).resetTables();
      }

      private static boolean userIsStored(Context context, String tableName) {
            if (tableName.equals(DatabaseHandler.TABLE_USER))
                  return (getDatabase(context).getRowCount(DatabaseHandler.TABLE_USER) > 0);

            else if (tableName.equals(DatabaseHandler.TABLE_CONTACTS))
                  return (getDatabase(context).getRowCount(DatabaseHandler.TABLE_CONTACTS) > 0);
            else   return false;
      }

}