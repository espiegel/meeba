package com.meeba.google.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.meeba.google.Objects.User;
import com.meeba.google.Util.Utils;

import java.util.HashMap;

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

    // Login Table Columns names
    private static final String KEY_UID = "uid";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone_number";
    private static final String KEY_RID = "rid";
    private static final String KEY_CREATED_AT = "created_at";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_UID + " INTEGER PRIMARY KEY,"  // 0
                + KEY_EMAIL + " TEXT UNIQUE,"        // 1
                + KEY_NAME + " TEXT,"                // 2
                + KEY_PHONE + " TEXT,"               // 3
                + KEY_RID + " TEXT,"                 // 4
                + KEY_CREATED_AT + " TEXT" + ")";    // 5
        Utils.LOGD(CREATE_LOGIN_TABLE);
        db.execSQL(CREATE_LOGIN_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(int uid, String phone, String rid, String created_at, String email, String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UID, uid); // uid
        values.put(KEY_PHONE, phone); // phone
        values.put(KEY_RID, rid); // rid
        values.put(KEY_CREATED_AT, created_at); // Created At
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_NAME, name); // uid

        // Inserting Row
        db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection
    }

    /**
     * Getting user data from database
     * */
    public User getUserDetails() {
        HashMap<String,String> userDetails = new HashMap<String,String>();
        String selectQuery = "SELECT * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0) {
            userDetails.put(KEY_UID, cursor.getString(0));
            userDetails.put(KEY_EMAIL, cursor.getString(1));
            userDetails.put(KEY_NAME, cursor.getString(2));
            userDetails.put(KEY_PHONE, cursor.getString(3));
            userDetails.put(KEY_RID, cursor.getString(4));
            userDetails.put(KEY_CREATED_AT, cursor.getString(5));
        }
        cursor.close();
        db.close();

        for(String s : userDetails.values()) {
            Utils.LOGD("DB: "+s);
        }
        // return user
        User user = new User(Integer.valueOf(userDetails.get(KEY_UID)), userDetails.get(KEY_EMAIL),
                userDetails.get(KEY_NAME), userDetails.get(KEY_PHONE),
                userDetails.get(KEY_RID), userDetails.get(KEY_CREATED_AT));
        return user;
    }

    /**
     * Re crate database
     * Delete all tables and create them again
     * */
    public void resetTables() {
        Utils.LOGD("resetting tables");
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();
    }

    /**
     * Getting user login status
     * return no. of rows in the table
     * @param tableName table to check
     * */
    public int getRowCount(String tableName) {
        if(!tableName.equals(TABLE_USER))
            return -1;

        String countQuery = "SELECT  * FROM " + tableName;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        // return row count
        return rowCount;
    }
}
