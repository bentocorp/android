package com.bentonow.bentonow.Utils;

import android.util.Log;

import com.bentonow.bentonow.BuildConfig;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.conn.ssl.SSLSocketFactory;

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

public class BentoRestClient {
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

    static final String TAG = "BentoRestClient";
    static final String BASE_URL = BuildConfig.DEBUG ? "https://api2.dev.bentonow.com" : "https://api2.bentonow.com";

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
        Log.i(TAG, "[GET] " + getAbsoluteUrl(url));
        Log.i(TAG, "[params] " + (params != null ? params.toString() : "null"));
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Log.i(TAG, "[POST] " + getAbsoluteUrl(url));
        Log.i(TAG, "[params] " + (params != null ? params.toString() : "null"));
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void postFace(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        Log.i(TAG, "[POST] https://api2.bentonow.com" + url);
        Log.i(TAG, "[params] " + (params != null ? params.toString() : "null"));
        client.post("https://api2.bentonow.com" + url, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
