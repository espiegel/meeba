package com.meeba.google.database;
import android.content.Context;
import com.meeba.google.objects.User;
import com.meeba.google.util.Utils;
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

      /**
       * @param context
       * @param users
       * store users in contacts table in phone DB
       */
      public static void storeContacts(Context context,  List<User> users) {
            for (User user : users) {
                  Utils.LOGD("storeContacts: storing  in database " + user.getName()  );
                  storeUserDetails(context, user, DatabaseHandler.TABLE_CONTACTS);
            }
      }

      /**
       * @param context
       * @return list of  saved contacts  from phone DB
       */
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
       * @param tableName    table's  name
       */
      public static void storeUserDetails(Context context, User user, String tableName) {
            // Store only if there isn't a user stored already
            if (tableName.equals(DatabaseHandler.TABLE_USER) &&  userIsStored(context)) {
                  Utils.LOGD(user.getName() + "storeUserDetails:   already stored in  " + tableName);
                  return;
            }

            if (tableName.equals(DatabaseHandler.TABLE_CONTACTS) &&  contactIsStored(context,user ))   {
                  Utils.LOGD(user.getName() + "storeUserDetails :  already stored in  " + tableName);
                  return;
            }

            Utils.LOGD( "storeUserDetails : adding   " + user.getName() + " to table: "  + tableName);
            DatabaseHandler db = getDatabase(context);
            db.addUser(tableName, user.getUid(), user.getPhone_number(), user.getRid(), user.getCreated_at(), user.getEmail(), user.getName(), user.getPicture_url());
      }

      /**
       * Get the current user using this application
       *@param tableName  table's  name
       * @param context Application context
       * @return Returns user object
       */
      public static User getUserDetails(Context context, String tableName) {
            DatabaseHandler db = getDatabase(context);
            if (!userIsStored(context)) {
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



      private static boolean userIsStored(Context context) {
                  return (getDatabase(context).getRowCount(DatabaseHandler.TABLE_USER) > 0);
      }

      /**
       * @param context application context
       * @param user the user to be searched in contacts table
       * @return true iff user is stored in the contacts table
       */
      private static boolean contactIsStored(Context context, User user) {
            List<User> users = loadContacts(context);
            for(User u :users )  {
                  if(u.getName().equals(user.getName()))
                          return true;
            }
            return false ;
      }
}