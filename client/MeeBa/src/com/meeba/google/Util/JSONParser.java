package com.meeba.google.Util;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class JSONParser {
    private static final String STREAM_ENCODING = "iso-8859-1";

    private static final String HOST = "Host";
    private static final String HOST_VALUE = "54.214.243.219";

    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ACCEPT_ENCODING_VALUE = "gzip";

    private static final String CONNECTION = "Connection";
    private static final String CONNECTION_VALUE = "keep-alive";

    private static final String PROXY_CONNECTION = "Proxy-Connection";
    private static final String PROXY_CONNECTION_VALUE = "keep-alive";

    public static Object doGETRequest(String url) throws ClientProtocolException, IOException {
        HttpGet httpGet = new HttpGet(url);

        httpGet.addHeader(HOST, HOST_VALUE);
        //httpGet.addHeader(ACCEPT_ENCODING, ACCEPT_ENCODING_VALUE);
        httpGet.addHeader(CONNECTION, CONNECTION_VALUE);
        httpGet.addHeader(PROXY_CONNECTION, PROXY_CONNECTION_VALUE);

        HttpEntity httpEntity = Utils.getInstance().getHttpClient().execute(httpGet).getEntity();
        InputStream lInputStream = httpEntity.getContent();

        try {
            BufferedReader lBufferedReader = new BufferedReader(new InputStreamReader(lInputStream, STREAM_ENCODING), 8);
            StringBuilder lStringBuilder = new StringBuilder();
            String line = null;
            while ((line = lBufferedReader.readLine()) != null) {
                lStringBuilder.append(line + "\n");
            }
            lInputStream.close();

            Utils.LOGD("JSON STRING = "+lStringBuilder.toString());
            JSONObject lJSONObject = new JSONObject(lStringBuilder.toString());

            if (lJSONObject.getInt("success") == 1) {
                return lJSONObject;
            }
            return null;
        } catch (Exception e) {
            Utils.LOGD(e.getMessage());
            //System.err.println(toString() + " - ErrorMessage: " + e.getMessage());
        }
        return null;
    }

    /*public static Object doPOSTRequest(String url, List<NameValuePair> params) throws ClientProtocolException, IOException {
        HttpPost httpPost = new HttpPost(url);

        httpPost.addHeader(HOST, HOST_VALUE);
        //httpPost.addHeader(ACCEPT_ENCODING, ACCEPT_ENCODING_VALUE);
        httpPost.addHeader(CONNECTION, CONNECTION_VALUE);
        httpPost.addHeader(PROXY_CONNECTION, PROXY_CONNECTION_VALUE);

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpEntity httpEntity = Utils.getInstance().getHttpClient().execute(httpPost).getEntity();
        InputStream lInputStream = httpEntity.getContent();

        try {
            BufferedReader lBufferedReader = new BufferedReader(new InputStreamReader(lInputStream, STREAM_ENCODING), 8);
            StringBuilder lStringBuilder = new StringBuilder();
            String line = null;
            while ((line = lBufferedReader.readLine()) != null) {
                lStringBuilder.append(line + "\n");
            }
            lInputStream.close();

            Utils.LOGD("JSON STRING = "+lStringBuilder.toString());
            JSONObject lJSONObject = new JSONObject(lStringBuilder.toString());

            if (lJSONObject.getInt("success") == 1) {
                return lJSONObject;
            }
            return null;
        } catch (Exception e) {
            Utils.LOGD(e.getMessage());
            //System.err.println(toString() + " - ErrorMessage: " + e.getMessage());
        }

        return null;
    }*/

    public static Object doPOSTRequest(String url, List<NameValuePair> params) {
        try {
            return doPOSTRequest(url, new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object doPOSTRequest(String url, String json) throws ClientProtocolException, IOException {
        StringEntity s = new StringEntity(json,"UTF-8");
        s.setContentEncoding("UTF-8");
        s.setContentType("application/json");

        return doPOSTRequest(url, s);
    }

    public static Object doPOSTRequest(String url, HttpEntity entity) {
        HttpPost httpPost = new HttpPost(url);

        httpPost.addHeader(HOST, HOST_VALUE);
        httpPost.addHeader(CONNECTION, CONNECTION_VALUE);
        httpPost.addHeader(PROXY_CONNECTION, PROXY_CONNECTION_VALUE);

        httpPost.setEntity(entity);

        try {
            HttpEntity httpEntity = Utils.getInstance().getHttpClient().execute(httpPost).getEntity();
            InputStream lInputStream = httpEntity.getContent();

            BufferedReader lBufferedReader = new BufferedReader(new InputStreamReader(lInputStream, STREAM_ENCODING), 8);
            StringBuilder lStringBuilder = new StringBuilder();
            String line = null;
            while ((line = lBufferedReader.readLine()) != null) {
                lStringBuilder.append(line + "\n");
            }
            lInputStream.close();

            Utils.LOGD("JSON STRING = "+lStringBuilder.toString());
            JSONObject lJSONObject = new JSONObject(lStringBuilder.toString());

            if (lJSONObject.getInt("success") == 1) {
                return lJSONObject;
            }
            return null;
        } catch (Exception e) {
            Utils.LOGD(e.getMessage());
            //System.err.println(toString() + " - ErrorMessage: " + e.getMessage());
        }

        return null;
    }

}