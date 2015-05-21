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

    private void setTodayDate() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        day = day-Config.aux_deduct_day;
        String monthString = month < 10 ? "0"+String.valueOf(month+1): String.valueOf(month);
        String dayString = day < 10 ? "0"+String.valueOf(day): String.valueOf(day);
        todayDate = String.valueOf(year)+monthString+dayString;
        int tday = day+1;
        String TomorrowDayString = day < 10 ? "0"+String.valueOf(tday): String.valueOf(tday);
        tomorrowDate = String.valueOf(year)+monthString+TomorrowDayString;
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

    public class go{
        public void toLoginActivity(Config.from comeFrom){
            Config.AppNavigateMap.from = comeFrom;
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
            overridePendingTransitionGoRight();
        }

        public void fromLogin(){
            try {
                switch (Config.AppNavigateMap.from){
                    case SettingActivity:
                        Config.AppNavigateMap.from = null;
                        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                        startActivity(intent);
                        overridePendingTransitionGoLeft();
                        break;
                    default:
                        Config.AppNavigateMap.from = null;
                        Intent intent2 = new Intent(getApplicationContext(), BuildBentoActivity.class);
                        startActivity(intent2);
                        overridePendingTransitionGoLeft();
                        break;
                }
            }catch (NullPointerException ignored){
                Config.AppNavigateMap.from = null;
                Intent intent2 = new Intent(getApplicationContext(), BuildBentoActivity.class);
                startActivity(intent2);
                overridePendingTransitionGoLeft();
            }
        }
    }

}
