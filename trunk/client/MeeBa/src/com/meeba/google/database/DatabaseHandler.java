package com.meeba.google.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.meeba.google.objects.User;
import com.meeba.google.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Eidan on 11/16/13.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "meeba_db";

    // Login table name
    public static final String TABLE_USER = "user";

    // Contacts  table name
    public static final String TABLE_CONTACTS = "contacts";

    // Login Table & Contacts Table  Columns names
    private static final String KEY_UID = "uid";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone_number";
    private static final String KEY_RID = "rid";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_PICTURE_URL = "picture_url";
    private static final String KEY_IS_DUMMY = "is_dummy";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        //create the Login table :
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_UID + " INTEGER PRIMARY KEY,"  // 0
                + KEY_EMAIL + " TEXT UNIQUE,"        // 1
                + KEY_NAME + " TEXT,"                // 2
                + KEY_PHONE + " TEXT,"               // 3
                + KEY_RID + " TEXT,"                 // 4
                + KEY_CREATED_AT + " TEXT,"          // 5
                + KEY_PICTURE_URL + " TEXT,"      // 6
                + KEY_IS_DUMMY        + " TEXT" + ")";   // 7
        Utils.LOGD(CREATE_LOGIN_TABLE);
        db.execSQL(CREATE_LOGIN_TABLE);

        //create the Contacts  table :
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_UID + " INTEGER,"              // 0
                + KEY_EMAIL + " TEXT,"               // 1
                + KEY_NAME + " TEXT,"                // 2
                + KEY_PHONE + " TEXT,"               // 3
                + KEY_RID + " TEXT,"                 // 4
                + KEY_CREATED_AT   + " TEXT,"          // 5
                + KEY_PICTURE_URL + " TEXT,"      // 6
                + KEY_IS_DUMMY        + " TEXT" + ")";   // 7
        Utils.LOGD(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion > oldVersion) {
            Utils.LOGD("Upgrading database from version "+oldVersion+" to version "+newVersion);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
            // Create tables again

            onCreate(db);
        }
    }

    /**
     * Storing user details in  Login database
     */
    public void addUser(String tableName, User user) {
        int uid = user.getUid();
        String phone = user.getPhone_number();
        String rid = user.getRid();
        String created_at = user.getCreated_at();
        String email = user.getEmail();
        String name = user.getName();
        String picture_url = user.getPicture_url();
        String is_dummy = String  .valueOf( user.getIs_dummy());

        Utils.LOGD("adding to  " + tableName + " user =" + user);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_UID, uid);
        values.put(KEY_PHONE, phone);
        values.put(KEY_RID, rid);
        values.put(KEY_CREATED_AT, created_at);
        values.put(KEY_EMAIL, email);
        values.put(KEY_NAME, name);
        values.put(KEY_PICTURE_URL, picture_url);
        values.put(KEY_IS_DUMMY, is_dummy);

        // Inserting Row
        db.insert(tableName, null, values);
        //db.close(); // Closing database connection
    }

    /**
     * Getting user data from  Login database
     */
    public User getUserDetails() {
        HashMap<String, String> userDetails = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            userDetails.put(KEY_UID, cursor.getString(0));
            userDetails.put(KEY_EMAIL, cursor.getString(1));
            userDetails.put(KEY_NAME, cursor.getString(2));
            userDetails.put(KEY_PHONE, cursor.getString(3));
            userDetails.put(KEY_RID, cursor.getString(4));
            userDetails.put(KEY_CREATED_AT, cursor.getString(5));
            userDetails.put(KEY_PICTURE_URL, cursor.getString(6));
            userDetails.put(KEY_IS_DUMMY, cursor.getString(7));
        }
        cursor.close();
        //db.close();

        for (String s : userDetails.values()) {
            Utils.LOGD("DB: " + s);
        }
        // return user
        User user = new User(
                Integer.valueOf(userDetails.get(KEY_UID)),
                userDetails.get(KEY_EMAIL),
                userDetails.get(KEY_NAME),
                userDetails.get(KEY_PHONE),
                userDetails.get(KEY_RID),
                userDetails.get(KEY_CREATED_AT),
                userDetails.get(KEY_PICTURE_URL),
                Integer.valueOf( userDetails.get(KEY_IS_DUMMY))
        );
        return user;
    }

    /**
     * @return a list of all the users stored in contacts table
     */
    public List<User> getContacts() {
        List<User> contacts = new ArrayList<User>();
        HashMap<String, String> contactDetails = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Move to first row
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.getCount() > 0) {
                contactDetails.put(KEY_UID, cursor.getString(0));
                contactDetails.put(KEY_EMAIL, cursor.getString(1));
                contactDetails.put(KEY_NAME, cursor.getString(2));
                contactDetails.put(KEY_PHONE, cursor.getString(3));
                contactDetails.put(KEY_RID, cursor.getString(4));
                contactDetails.put(KEY_CREATED_AT, cursor.getString(5));
                contactDetails.put(KEY_PICTURE_URL, cursor.getString(6));
                contactDetails.put(KEY_IS_DUMMY, cursor.getString(7));


                User contact = new User(Integer.valueOf(contactDetails.get(KEY_UID)), contactDetails.get(KEY_EMAIL),
                        contactDetails.get(KEY_NAME), contactDetails.get(KEY_PHONE), contactDetails.get(KEY_RID),
                        contactDetails.get(KEY_CREATED_AT), contactDetails.get(KEY_PICTURE_URL)  , Integer.valueOf(contactDetails.get(KEY_IS_DUMMY) ));

                //Utils.LOGD("Got contact from database, user = "+contact);
                contacts.add(contact);
            }
            cursor.moveToNext();
        }

        cursor.close();
        //db.close();

        return contacts;
    }

    /**
     * Get a contact from the database that has the same uid as the user argument
     * @param uid uid of user to get
     * @return Returns the user or null if not found
     */
    public User getContact(int uid) {
        HashMap<String, String> contactDetails = new HashMap<String, String>();

        SQLiteDatabase db = this.getReadableDatabase();
        User contact = null;

        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_UID, KEY_EMAIL, KEY_NAME, KEY_PHONE, KEY_RID, KEY_CREATED_AT, KEY_PICTURE_URL,KEY_IS_DUMMY },
                KEY_UID + " = ?", new String[] { String.valueOf(uid) }, null, null, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            contactDetails.put(KEY_UID, cursor.getString(0));
            contactDetails.put(KEY_EMAIL, cursor.getString(1));
            contactDetails.put(KEY_NAME, cursor.getString(2));
            contactDetails.put(KEY_PHONE, cursor.getString(3));
            contactDetails.put(KEY_RID, cursor.getString(4));
            contactDetails.put(KEY_CREATED_AT, cursor.getString(5));
            contactDetails.put(KEY_PICTURE_URL, cursor.getString(6));
            contactDetails.put(KEY_IS_DUMMY, cursor.getString(7));

            contact = new User(Integer.valueOf(contactDetails.get(KEY_UID)), contactDetails.get(KEY_EMAIL),
                    contactDetails.get(KEY_NAME), contactDetails.get(KEY_PHONE), contactDetails.get(KEY_RID),
                    contactDetails.get(KEY_CREATED_AT), contactDetails.get(KEY_PICTURE_URL)  , Integer.valueOf(contactDetails.get(KEY_IS_DUMMY)) );

        }

        cursor.close();
        //db.close();

        return contact;
    }

    /**
     * Re crate database
     * Delete all tables and create them again
     */
    public void resetTables() {

        Utils.LOGD("resetting tables");
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows


        if (tableExists(TABLE_USER))
            db.delete(TABLE_USER, null, null);

        if (tableExists(TABLE_CONTACTS))
            db.delete(TABLE_CONTACTS, null, null);

        //db.close();
    }

    /**
     * @param tableName the table's name
     * @return true iff the table exists
     */
    private boolean tableExists(String tableName) {
        SQLiteDatabase mDatabase = this.getWritableDatabase();
        boolean tableExists = false;

        try {
            assert mDatabase != null;
            mDatabase.query(tableName, null,
                    null, null, null, null, null);
            tableExists = true;
        } catch (Exception e) {

            Utils.LOGD(tableName + " doesn't exist :(((");
        } finally {
            //mDatabase.close();
        }
        return tableExists;
    }

    /**
     * Getting user login status
     * return no. of rows in the table
     *
     * @param tableName name of table to check
     */
    public int getRowCount(String tableName) {
        if (!tableName.equals(TABLE_USER) && !tableName.equals(TABLE_CONTACTS))
            return -1;

        String countQuery = "SELECT * FROM " + tableName;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        //db.close();
        cursor.close();

        // return row count
        Utils.LOGD(tableName + " row count is " + rowCount);
        return rowCount;
    }

    /**
     * Returns whether a contact exists
     * @param user User to check
     * @return returns True or False
     */
    public boolean contactExists(User user) {
        boolean result;
        SQLiteDatabase db = this.getReadableDatabase();

        if(user.getUid() != Utils.DUMMY_USER) {
            String[] columns = { KEY_UID };
            Cursor cursor = db.query(TABLE_CONTACTS, columns, KEY_UID + " = " + user.getUid(), null, null, null, null);
            result = (cursor.getCount() > 0);
            cursor.close();
            //db.close();

            return result;
        } else {
            String[] columns = { KEY_UID };
            Cursor cursor = db.query(TABLE_CONTACTS, columns, KEY_NAME + " = ? AND " + KEY_PHONE + " = ?", new String[] { user.getName(), user.getPhone_number() }, null, null, null);
            result = (cursor.getCount() > 0);
            cursor.close();
            //db.close();

            return result;
        }
    }

    /**
     * Deletes a user from the contact table. If it is a real meeba user then delete by uid, otherwise
     * we delete by phone number
     * @param user user to be deleted
     * @return returns true on success and false on failure
     */
    //TODO what if user dosent exist? will this crash??
    public boolean removeContact(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result;
        if(user.getUid() != Utils.DUMMY_USER) {
            result = db.delete(TABLE_CONTACTS, KEY_UID + " = ?", new String[] { String.valueOf(user.getUid()) });
        } else {
            result = db.delete(TABLE_CONTACTS, KEY_PHONE + " = ?", new String[] {user.getPhone_number() });
        }

        return result > 0;
    }

    /**
     * Updates an existing contact. Can't update a contact that doesn't have meeba
     * @param user User to update. Searches by uid, updates all other values
     * @return Returns true on success and false on failure
     */
    public boolean updateContact(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result;
        if(user.getUid() != Utils.DUMMY_USER) {
            ContentValues args = new ContentValues();
            args.put(KEY_NAME, user.getName());
            args.put(KEY_EMAIL, user.getEmail());
            args.put(KEY_PICTURE_URL, user.getPicture_url());
            args.put(KEY_PHONE, user.getPhone_number());
            args.put(KEY_RID, user.getRid());

            result = db.update(TABLE_CONTACTS, args, KEY_UID + " = ?", new String[] { String.valueOf(user.getUid()) });

            return result > 0;
        }
        return false;
    }
}
