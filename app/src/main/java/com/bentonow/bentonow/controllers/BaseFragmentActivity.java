package com.bentonow.bentonow.controllers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.model.Menu;
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

        BentoApplication.status = "open";
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

        Menu mCurrentMenu = Menu.get();

        if (mCurrentMenu == null) {
            BentoNowUtils.openMainActivity(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BentoApplication.onPause();
        AppEventsLogger.deactivateApp(this);
    }
}
