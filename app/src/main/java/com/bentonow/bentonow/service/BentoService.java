package com.bentonow.bentonow.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.Stock;
import com.bentonow.bentonow.model.User;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

public class BentoService extends Service {

    private static final String TAG = "BentoService";
    private static Handler handler;
    private static Runnable task;

    public static String date = "";

    public static boolean isRunning() {
        if (task == null || handler == null)
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_BENTO_SERVICE_RUNNING, false);

        Log.i(TAG, "isRunning: " + SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_BENTO_SERVICE_RUNNING));
        return SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_BENTO_SERVICE_RUNNING);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_BENTO_SERVICE_RUNNING, true);

        loadData();
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        if (handler != null && task != null) {
            handler.removeCallbacks(task);
        }
        super.onDestroy();
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_BENTO_SERVICE_RUNNING, false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "onStartCommand()");
        return (START_NOT_STICKY);
    }

    public static void init() {
        date = BentoNowUtils.getTodayDate();
        Log.i(TAG, "init");
    }

    void startTask() {
        task = new Runnable() {
            public void run() {
                loadData();
            }
        };
        handler = new Handler();
        handler.postDelayed(task, 1000 * 30);
        Log.i(TAG, "startTask");
    }

    void loadData() {
        Log.i(TAG, "loadData");
        checkUserLocation();

        String currDate = BentoNowUtils.getTodayDate();

        Log.i(TAG, "date: " + date + " current date: " + currDate);

        if (!date.equals(currDate)) {
            BentoNowUtils.openMainActivity(this);
        }

        //noinspection deprecation
        BentoRestClient.get("/init/all", null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "cannot loadData");
                onDestroy();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i(TAG, responseString != null ? responseString : "null");
                set(responseString);
            }
        });
        startTask();

    }

    void set(String responseString) {
        Log.i(TAG, "set");

        try {
            Stock.set(responseString);
            Settings.set(responseString);

            if (!Settings.status.equals(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.STORE_STATUS)) &&
                    !SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.STORE_STATUS).equals("main")) {
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_STORE_CHANGIN, true);
                switch (Settings.status) {
                    case "open":
                        BentoNowUtils.openMainActivity(this);
                        break;
                    case "sold out":
                    case "closed":
                        BentoNowUtils.openErrorActivity(this);
                        break;
                }

            }
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }
    }

    void checkUserLocation() {
        if (User.location != null)
            return;
        Log.i(TAG, "checkUserLocation");

        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null) {
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } else {
            User.location = new LatLng(location.getLatitude(), location.getLongitude());
        }

        if (location == null) {
            final LocationListener mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(final Location location) {
                    User.location = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.i(TAG, "location: " + User.location.toString());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.i(TAG, "location: disabled");
                }
            };
            mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);
        } else {
            User.location = new LatLng(location.getLatitude(), location.getLongitude());
            Log.i(TAG, "location: " + User.location.toString());
        }
    }
}