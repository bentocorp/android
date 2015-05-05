package com.bentonow.bentonow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by gonzalo on 21/04/2015.
 */
public class BaseActivity extends Activity {

    private static final String BASE_TAG = "BaseActivity";
    private Intent service_intent;
    protected String todayDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(BASE_TAG, "onCreate()");
        if ( !BentoService.isRunning() ){
            service_intent = new Intent(getApplicationContext(), BentoService.class);
            startService(service_intent);
        }

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        setTodayDate();
    }

    private void setTodayDate() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String monthString = month < 10 ? "0"+String.valueOf(month+1): String.valueOf(month);
        String dayString = day < 10 ? "0"+String.valueOf(day-Config.aux_deduct_day): String.valueOf(day-Config.aux_deduct_day);
        todayDate = String.valueOf(year)+monthString+dayString;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(BASE_TAG, "onResume()");
        Bentonow.app.isFocused = true;
    }

    public void finishThisActivity() {
        finish();
        overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishThisActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(BASE_TAG,"onPause()");
        Bentonow.app.isFocused = false;
    }

}
