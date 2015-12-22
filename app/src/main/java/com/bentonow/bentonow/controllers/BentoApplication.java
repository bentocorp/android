package com.bentonow.bentonow.controllers;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.multidex.MultiDex;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.dao.UserDao;
import com.bentonow.bentonow.listener.InterfaceWebRequest;
import com.bentonow.bentonow.model.Settings;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;

import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class BentoApplication extends Application {
    private static final String TAG = "BentoApplication";

    public static BentoApplication instance;

    private Handler mHandler = new Handler();
    private UserDao userDao = new UserDao();

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
            MixpanelUtils.logInUser(userDao.getCurrentUser());
            FacebookSdk.sdkInitialize(this);
            Fabric.with(this, new Crashlytics());
            BentoRestClient.init();
        } catch (Exception e) {
            DebugUtils.logError(TAG, e);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
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
