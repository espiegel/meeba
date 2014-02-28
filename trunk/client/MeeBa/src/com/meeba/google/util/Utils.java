package com.meeba.google.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.meeba.google.R;
import com.meeba.google.objects.Event;
import com.meeba.google.objects.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

public class Utils {

    public static final String BUNDLE_EVENT = "event";
    public static final boolean DEBUG = true;
    public static final String BASE_URL = "http://54.214.243.219/meeba/";
    public static final int DUMMY_USER = -1;
    public static final String DATE_FORMAT = "HH:mm dd/MM/yyyy";
    public static final String PRETTY_DATE_FORMAT = "EEE, MMM d, HH:mm";

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
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) a.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (a.getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(a.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            //this crashed to me , so i added try/catch here (max)
            e.printStackTrace();
        }

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

                    // Filter out all non-numeric characters
                    if (phoneNumber != null) {
                        phoneNumber = Utils.sanitizePhoneNumber(phoneNumber);
                        phonesMap.put(phoneNumber, contact);
                    }
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
        DisplayImageOptions mDisplayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .writeDebugLogs()
                .denyCacheImageMultipleSizesInMemory()
                .memoryCacheSize(4 * 1024 * 1024)
                .discCacheSize(10 * 1024 * 1024)
                .threadPoolSize(10)
                .defaultDisplayImageOptions(mDisplayImageOptions)
                .build();

        return config;
    }

    public static ImageLoader getImageLoader(Context context) {
        if (!mImageLoader.isInited()) {
            mImageLoader.init(Utils.getImageLoaderConfig(context));
        }

        return mImageLoader;
    }

    public static void showKeyboard(Context context, View view) {
        try {
            InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(view, 0);
        } catch (Exception e) {
        }
    }

    public static void hideKeyboard(Context context, View view) {
        try {
            InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
        }
    }

    public static void shareEvent(Context context, Event event, ImageView picture, List<User> guestList) {
        String guestsString = "";
        List<User> acceptedGuests = new ArrayList<User>();

        if (guestList != null) {
            for (User guest : guestList) {
                if (guest.getInvite_status() == 1) {
                    acceptedGuests.add(guest);
                }
            }
        }

        if (!acceptedGuests.isEmpty()) {
            guestsString = "\nwith:  ";
            for (User goingGuest : acceptedGuests) {
                if (goingGuest.getInvite_status() == 1) {
                    guestsString += goingGuest.getName() + ",";
                }
            }
            //remove last ","
            guestsString = guestsString.substring(0, guestsString.length() - 1);
        }

        String shareBody = "I'm going to: " + event.getTitle() +
                "\nat " + event.getWhere()
                + "\nat " + event.getWhen() +
                guestsString +
                "\n" + context.getString(R.string.generated);

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("image/*");

        if (isExternalStorageAvailable()) {
            try {
                picture.setDrawingCacheEnabled(true);
                Bitmap b = picture.getDrawingCache();
                File sdCard = Environment.getExternalStorageDirectory();
                File file = new File(sdCard, "meeba/event_picture_" + event.getEid() + ".jpg");
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                b.compress(Bitmap.CompressFormat.JPEG, 95, fos);

                Uri uri = Uri.fromFile(file);
                sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);

            } catch (Exception e) {
                Utils.LOGD("Failed to store event picture");
                e.printStackTrace();
            }
        }
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.share_event)));
    }

    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        return mExternalStorageAvailable && mExternalStorageWriteable;
    }

    // Still needs testing on non-israeli phone numbers since they can get screwed up since we depend
    // on user input.
    public static String sanitizePhoneNumber(String phone) {
        phone = phone.replaceAll("[^0-9]", "");
        // Deal with Israel country code
        if (phone.startsWith("972")) {
            phone = phone.replaceFirst("972", "0");
        }
        return phone;
    }

    public static String makePrettyDate(String formmatedDate) {
        Utils.LOGD("event from  arrayadapter  =" + formmatedDate);
        DateTime dt;
        String prettyDate;

        dt = parseDate(formmatedDate);
        if (dt == null)
            prettyDate = formmatedDate;
        else
            prettyDate = dt.toString(PRETTY_DATE_FORMAT);
        //    prettyDate = dt.dayOfWeek().getAsShortText() + ", " + dt.monthOfYear().getAsShortText() + " " + dt.dayOfMonth().getAsShortText() + ", " +
//                    DateTimeFormat.forPattern("HH:mm").print(dt);

        return prettyDate;
    }

    public static DateTime parseDate(String date) {
        DateTime dt = null;
        DateTimeFormatter dateParser = DateTimeFormat.forPattern(DATE_FORMAT);
        try {
            dt = dateParser.parseDateTime(date);
        } catch (Exception e1) {
            try {
                DateTimeFormatter oldDateParser = DateTimeFormat.forPattern(PRETTY_DATE_FORMAT);
                dt = oldDateParser.parseDateTime(date);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        //if parsing didn't succeed neither with DATE_FORMAT && PRETTY_DATE_FORMAT
        if (dt == null) {
            //make dt the current time . (its not an expected behaviour - but app wont crash
            dt = new DateTime();
        }
        return dt;
    }

    public static void pushToWaitingList(String phoneNumber, int iuid, int ieid,
                                         SharedPreferences sharedPrefs) throws JSONException {

        String uid = String.valueOf(iuid);
        String eid = String.valueOf(ieid);
        Stack<String> waitingStack;
        SharedPreferences.Editor editor = sharedPrefs.edit();
        String foundJsonObj = sharedPrefs.getString(phoneNumber, null);

        if (foundJsonObj != null) {
            waitingStack = JsonEventsStack.getEventsStackfromJson(foundJsonObj);
        } else {
            waitingStack = new Stack<String>();
        }

        waitingStack.push(eid);
        //convert to string
        String updatedJsonStack = JsonEventsStack.toJson(uid, waitingStack);
        //store new Stack in phone
        editor.putString(phoneNumber, updatedJsonStack);
        editor.commit();
        Utils.LOGD("pushToWaitingList : pushing  " + phoneNumber + "->" + updatedJsonStack);
    }

    public static void updateWaitingList(String phoneNumber, String uid,
                                         Stack<String> newWaitingList, SharedPreferences sharedPrefs) throws JSONException {

        SharedPreferences.Editor editor = sharedPrefs.edit();
        String updatedJsonStack = JsonEventsStack.toJson(uid, newWaitingList);
        editor.putString(phoneNumber, updatedJsonStack);
        editor.commit();
    }

    public static void deleteFromWaitingList(String eid, SharedPreferences sharedPrefs) {
        Utils.LOGD("deleting eid =  " + eid + " from waitingList ");
        SharedPreferences.Editor editor = sharedPrefs.edit();
        if (sharedPrefs.contains(eid)) {
            editor.remove(eid);
            editor.commit();
        }
    }

    public static List<Event> sortEvents(List<Event> events) {
        List<Event> pastEvents = new ArrayList<Event>();
        List<Event> futureEvents = new ArrayList<Event>();
        List<Event> sortedEvents = new ArrayList<Event>();
        EventComparator futureEventsComparator = new EventComparator(false);
        EventComparator pastEventsComparator = new EventComparator(true);
        DateTime now = new DateTime();
        Utils.LOGD("sortEvents :current time = " + now);


        for (Event ev : events) {
            Utils.LOGD("sortEvents :ev  = " + ev);
            Utils.LOGD("sortEvents : ev.getFormmatedWhen()  = " + ev.getFormmatedWhen());
            DateTime eventDate = null;

            eventDate = parseDate(ev.getFormmatedWhen());

            if (eventDate.isBefore(now)) {
                pastEvents.add(ev);
            } else {
                futureEvents.add(ev);
            }
            Collections.sort(pastEvents, pastEventsComparator);
            Collections.sort(futureEvents, futureEventsComparator);
        }

        Utils.LOGD("sortEvents :future events  = " + futureEvents);
        sortedEvents.addAll(futureEvents);
        sortedEvents.addAll(pastEvents);

        return sortedEvents;
    }

}