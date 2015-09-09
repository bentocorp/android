package com.bentonow.bentonow.controllers;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.bentonow.bentonow.BuildConfig;
import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.Mixpanel;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.service.BentoService;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class BentoApplication extends Application {
    private static final String TAG = "BentoApplication";

    static Timer timer;
    static TimerTask stopService;
    public static BentoApplication instance;

    private Handler mHandler = new Handler();
    public static String status;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        Log.i(TAG, "onCreate");

        if (!BuildConfig.DEBUG) {
            Mixpanel.getInstance(this);
        }


        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );


        Settings.load(this);

        try {
            FacebookSdk.sdkInitialize(this);
            Fabric.with(this, new Crashlytics());
            BentoService.init();
            BentoRestClient.init();
        } catch (KeyStoreException | KeyManagementException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }
    }

    static public void onPause() {
        timer = new Timer();

        Log.i(TAG, "onPause");

        stopService = new TimerTask() {
            @Override
            public void run() {
                DebugUtils.logDebug(TAG, "stopping service");
                try {
                    instance.stopService(new Intent(instance, BentoService.class));
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_BENTO_SERVICE_RUNNING, false);
                } catch (Exception e) {
                    DebugUtils.logError(TAG, "stopping service: " + e.toString());
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_BENTO_SERVICE_RUNNING, false);
                }
            }
        };

        // After timer reached the service will be stop
        timer.schedule(stopService, 5 * 1000);
    }

    static public void onResume() {
        Log.i(TAG, "onResume");

        if (timer != null) {
            Log.i(TAG, "cancel timer");
            timer.cancel();
            timer = null;
        }

        if (!BentoService.isRunning()) {
            Log.i(TAG, "starting service");
            try {
                instance.startService(new Intent(instance, BentoService.class));
            } catch (Exception e) {
                DebugUtils.logError("BentoService: ", e);
            }
        }
    }

    public void handlerPost(Runnable runnable) {
        mHandler.post(runnable);
    }
}
