package com.bentonow.bentonow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bentonow.bentonow.model.Checkpoint;

import java.util.Calendar;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseActivity extends Activity {

    private static final String BASE_TAG = "BaseActivity";
    protected String todayDate;
    protected String tomorrowDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(BASE_TAG, "onCreate()");
        if ( !BentoService.isRunning() ){
            Intent service_intent = new Intent(getApplicationContext(), BentoService.class);
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
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        day = day-Config.aux_deduct_day;
        String monthString = month < 10 ? "0"+String.valueOf(month+1): String.valueOf(month);
        String dayString = day < 10 ? "0"+String.valueOf(day): String.valueOf(day);
        todayDate = String.valueOf(year)+monthString+dayString;
        //int tday = day+1;
        String TomorrowDayString = day < 10 ? "0"+String.valueOf(day): String.valueOf(day);
        tomorrowDate = String.valueOf(year)+monthString+TomorrowDayString;
    }

    public int getCurrentHourInt(){
        Calendar calendar = Calendar.getInstance();
        return (calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE));
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
        BentoApplication.onResume();
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
        BentoApplication.onPause();
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
