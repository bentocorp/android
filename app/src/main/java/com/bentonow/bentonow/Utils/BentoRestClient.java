package com.bentonow.bentonow.Utils;

import android.location.Location;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;

public class BentoRestClient {
    static final String TAG = "BentoRestClient";
    //static final String BASE_URL = BuildConfig.DEBUG ? "https://api2.dev.bentonow.com" : "https://api2.bentonow.com";
    static final String BASE_URL = BentoApplication.instance.getString(R.string.server_api_url);
    static AsyncHttpClient client = new AsyncHttpClient();

    public static void init() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException {
        // We initialize a default Keystore
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        // We load the KeyStore
        trustStore.load(null, null);
        // We initialize a new SSLSocketFacrory
        CustomSSLSocketFactory socketFactory = new CustomSSLSocketFactory(trustStore);
        // We set that all host names are allowed in the socket factory
        socketFactory.setHostnameVerifier(CustomSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        // We set the SSL Factory
        client.setSSLSocketFactory(socketFactory);
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        DebugUtils.logDebug(TAG, "[GET] " + getAbsoluteUrl(url));
        DebugUtils.logDebug(TAG, "[params] " + (params != null ? params.toString() : "null"));
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        DebugUtils.logDebug(TAG, "[POST] " + getAbsoluteUrl(url));
        DebugUtils.logDebug(TAG, "[params] " + (params != null ? params.toString() : "null"));
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void getCustom(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        DebugUtils.logDebug(TAG, "[GET] " + url);
        DebugUtils.logDebug(TAG, "[params] " + (params != null ? params.toString() : "null"));
        client.get(url, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public static String getInitUrl() {
        return "/init2?date=" + BentoNowUtils.getTodayDateInit2();
    }

    public static String getInit2Url() {
        LatLng location = new Gson().fromJson(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.LOCATION), LatLng.class);
        if (location == null)
            return getInitUrl();
        else
            return "/init2?date=" + BentoNowUtils.getTodayDateInit2() + "&copy=1&gatekeeper=1&lat=" + location.latitude + "&long=" + location.longitude;
    }

    public static String getInit2Url(LatLng mLocation) {
        if (mLocation == null)
            return getInitUrl();
        else
            return "/init2?date=" + BentoNowUtils.getTodayDateInit2() + "&copy=1&gatekeeper=1&lat=" + mLocation.latitude + "&long=" + mLocation.longitude;
    }

    public static String getInit2Url(Location mLocation) {
        if (mLocation == null)
            return getInitUrl();
        else
            return "/init2?date=" + BentoNowUtils.getTodayDateInit2() + "&copy=1&gatekeeper=1&lat=" + mLocation.getLatitude() + "&long=" + mLocation.getLongitude();
    }

    private static class CustomSSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public CustomSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }
}
