package com.bentonow.bentonow.controllers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.bentonow.bentonow.R;
import com.facebook.appevents.AppEventsLogger;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseFragmentActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        BentoApplication.onResume();
        AppEventsLogger.activateApp(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        BentoApplication.onPause();
        AppEventsLogger.deactivateApp(this);
    }
}
