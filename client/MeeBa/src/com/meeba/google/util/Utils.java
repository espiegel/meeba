package com.meeba.google.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.DefaultHttpClient;

public class Utils {

    public static final boolean DEBUG = true;
    public static final String BASE_URL = "http://54.214.243.219/meeba/";

    private static Utils mInstance = null;
    private CookieStore mCookie = null;
    private Object mLock = new Object();

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
}
