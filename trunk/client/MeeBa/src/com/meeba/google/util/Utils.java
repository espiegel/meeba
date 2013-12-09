package com.meeba.google.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Utils {

    public static final String BUNDLE_EVENT = "event";
    public static final boolean DEBUG = true;
    public static final String BASE_URL = "http://54.214.243.219/meeba/";
    public static final int DUMMY_USER = -1;

    private static Utils mInstance = null;
    private CookieStore mCookie = null;
    private Object mLock = new Object();

    private static ImageLoader mImageLoader = ImageLoader.getInstance();

    public static Utils getInstance() {
        if (mInstance == null) {
            mInstance = new Utils();
        }
        return mInstance;
    }

    public DefaultHttpClient getHttpClient() {
        final DefaultHttpClient httpClient = new DefaultHttpClient();
        synchronized (mLock) {
            if (mCookie == null) {
                mCookie = httpClient.getCookieStore();
            } else {
                httpClient.setCookieStore(mCookie);
            }
        }
        return httpClient;
    }

    public static void showToast(Context ctx, String text) {
        Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();

    }

    public static void LOGD(String message) {
        Log.d("debug", message);
    }

    public static void hideSoftKeyboard(Activity a) {
        InputMethodManager inputMethodManager = (InputMethodManager) a.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (a.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(a.getCurrentFocus().getWindowToken(), 0);
    }

    public static void setupUI(View view, final Activity a) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(a);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView, a);
            }
        }
    }

    public static Map<String, String> allPhoneNumbersAndName(ContentResolver contentResolver) {
        Map<String, String> phonesMap = new TreeMap<String, String>();

        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts._ID));
            String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if (Integer.parseInt(hasPhone) == 1) {
                int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                String contact = cursor.getString(nameFieldColumnIndex);

                // You know it has a number so now query it like this
                Cursor phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                while (phones.moveToNext()) {
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    // Filter out all the "-"'s and "*"'s from the phone number
                    phoneNumber = phoneNumber.replaceAll("\\+972", "0").replaceAll(" ", "").replaceAll("-", "").replaceAll("\\*", "").replaceAll("[)(]]", "");

                    phonesMap.put(phoneNumber, contact);
                }
                phones.close();
            }
        }
        cursor.close();

        return phonesMap;
    }

    public static List<String> phoneList(Map<String, String> hashlist) {
        List<String> phoneList = new ArrayList<String>();
        for (String s : hashlist.keySet()) {
            phoneList.add(s);
        }
        return phoneList;
    }

    public static ImageLoaderConfiguration getImageLoaderConfig(Context context) {
        DisplayImageOptions mDisplayImageOptions = new DisplayImageOptions.Builder().cacheInMemory(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .defaultDisplayImageOptions(mDisplayImageOptions)
                .build();

        return config;
    }

    public static ImageLoader getImageLoader(Context context) {
        if(!mImageLoader.isInited()) {
            mImageLoader.init(Utils.getImageLoaderConfig(context));
        }

        return mImageLoader;
    }
}
