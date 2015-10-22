package com.bentonow.bentonow.controllers;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.listener.InterfaceWebRequest;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.service.BentoService;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;

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

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        MixpanelUtils.getMixpanelApi();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        Settings.load();

        try {
            MixpanelUtils.logInUser();
            FacebookSdk.sdkInitialize(this);
            Fabric.with(this, new Crashlytics());
            BentoService.init();
            BentoRestClient.init();
        } catch (Exception e) {
            DebugUtils.logError(TAG, e);
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

        BentoNowUtils.runBentoService(instance);
    }


    public void doInBackground(Runnable runnable) {
        new Thread(runnable).start();
    }


    public void webRequest(final InterfaceWebRequest interfaceWebRequest) {
        doInBackground(new Runnable() {
            @Override
            public void run() {
                interfaceWebRequest.dispatchRequest();
            }
        });
    }

    public void handlerPost(Runnable runnable) {
        mHandler.post(runnable);
    }
}
