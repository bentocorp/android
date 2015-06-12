package com.bentonow.bentonow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bentonow.bentonow.model.Checkpoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by gonzalo on 21/04/2015.
 */
public class BaseActivity extends Activity {

    private static final String BASE_TAG = "BaseActivity";
    private Intent service_intent;
    protected String todayDate;
    protected String tomorrowDate;

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

    public boolean isFirstInit() {
        return Checkpoint.count(Checkpoint.class, null, null) == 0;
    }

    private void setTodayDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd",Locale.US);
        todayDate = df.format(new Date());
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


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //finishThisActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(BASE_TAG, "onPause()");
        Bentonow.app.isFocused = false;
    }


    public void overridePendingTransitionGoLeft() {
        overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
    }

    public void overridePendingTransitionGoRight() {
        overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
    }

    public void goToFAQ(){
        Intent intent = new Intent(getApplicationContext(), FaqActivity.class);
        startActivity(intent);
        overridePendingTransitionGoRight();
    }
}
