package com.bentonow.bentonow.controllers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.facebook.appevents.AppEventsLogger;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseFragmentActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_APP_IN_FRONT, true);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_APP_IN_FRONT, true);
        BentoApplication.onResume();
        AppEventsLogger.activateApp(this);

    }

    @Override
    protected void onStart() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_APP_IN_FRONT, true);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MixpanelUtils.getMixpanelApi().flush();
    }

    @Override
    protected void onPause() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_APP_IN_FRONT, false);
        BentoApplication.onPause();
        AppEventsLogger.deactivateApp(this);

        super.onPause();
    }

}
