package com.meeba.google.util;

import android.content.Context;
import android.util.Log;
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
}
