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
                + KEY_PICTURE_URL + " TEXT" + ")";   // 6
        Utils.LOGD(CREATE_LOGIN_TABLE);
        db.execSQL(CREATE_LOGIN_TABLE);

        //create the Contacts  table :
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_UID + " INTEGER PRIMARY KEY,"  // 0
                + KEY_EMAIL + " TEXT UNIQUE,"        // 1
                + KEY_NAME + " TEXT,"                // 2
                + KEY_PHONE + " TEXT,"               // 3
                + KEY_RID + " TEXT,"                 // 4
                + KEY_CREATED_AT + " TEXT,"          // 5
                + KEY_PICTURE_URL + " TEXT" + ")";   // 6
        Utils.LOGD(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        // Create tables again

        onCreate(db);
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
        }
        cursor.close();
        //db.close();

        for (String s : userDetails.values()) {
            Utils.LOGD("DB: " + s);
        }
        // return user
        User user = new User(Integer.valueOf(userDetails.get(KEY_UID)), userDetails.get(KEY_EMAIL),
                userDetails.get(KEY_NAME), userDetails.get(KEY_PHONE), userDetails.get(KEY_RID),
                userDetails.get(KEY_CREATED_AT), userDetails.get(KEY_PICTURE_URL));
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

                User contact = new User(Integer.valueOf(contactDetails.get(KEY_UID)), contactDetails.get(KEY_EMAIL),
                        contactDetails.get(KEY_NAME), contactDetails.get(KEY_PHONE), contactDetails.get(KEY_RID),
                        contactDetails.get(KEY_CREATED_AT), contactDetails.get(KEY_PICTURE_URL));

                Utils.LOGD("Got contact from database, user = "+contact);
                contacts.add(contact);
            }
            cursor.moveToNext();
        }

        cursor.close();
        //db.close();

        return contacts;
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

        String[] columns = { KEY_UID };
        Cursor cursor = db.query(TABLE_CONTACTS, columns, KEY_UID + " = " + user.getUid(), null, null, null, null);
        result = (cursor.getCount() > 0);

        cursor.close();
        //db.close();

        return result;
    }
}
