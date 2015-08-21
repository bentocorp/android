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

import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.bentonow.bentonow.controllers.errors.ErrorActivity;
import com.bentonow.bentonow.controllers.init.MainActivity;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.Stock;
import com.bentonow.bentonow.model.User;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

public class BentoService extends Service {

    private static final String TAG = "BentoService";
    private static BentoService instance;
    private Handler handler;
    private Runnable task;

    public static String date = "";

    public static boolean isRunning() {
        return instance != null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG,"onCreate()");
        instance=this;

        loadData();
    }

    @Override
    public void onDestroy() {
        instance = null;
        Log.i(TAG, "onDestroy()");
        if (handler != null && task != null) {
            handler.removeCallbacks(task);
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "Service.onStartCommand()");
        return(START_NOT_STICKY);
    }

    public static void init () {
        date = Menu.getTodayDate();
    }

    void startTask () {
        task = new Runnable() {
            public void run() {
                loadData();
            }
        };
        handler = new Handler();
        handler.postDelayed(task, 1000 * 30);
    }

    void loadData() {
        checkUserLocation();

        String currDate = Menu.getTodayDate();

        Log.i(TAG, "date: " + date + " current date: " + currDate);

        if (!date.equals(currDate)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        //noinspection deprecation
        BentoRestClient.get("/init/all", null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "cannot loadData");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i(TAG, responseString != null ? responseString : "null");
                set(responseString);
            }
        });
        startTask();

    }

    void set (String responseString) {
        Stock.set(responseString);
        Settings.set(responseString);

        if (!Settings.status.equals(BentoApplication.status) && !BentoApplication.status.equals("main")) {
            Intent intent = null;
            switch (Settings.status) {
                case "open":
                    intent = new Intent(this, MainActivity.class);
                    break;
                case "sold out":
                case "closed":
                    intent = new Intent(this, ErrorActivity.class);
                    break;
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    void checkUserLocation () {
        if (User.location != null) return;

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