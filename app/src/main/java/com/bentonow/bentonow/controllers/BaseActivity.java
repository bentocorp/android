package com.bentonow.bentonow.controllers;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.model.Menu;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        if (Menu.get() == null) {
            BentoNowUtils.openMainActivity(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BentoApplication.onPause();
    }
}
