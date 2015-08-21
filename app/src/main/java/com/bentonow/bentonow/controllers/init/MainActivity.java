package com.bentonow.bentonow.controllers.init;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bentonow.bentonow.BuildConfig;
import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.Mixpanel;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.bentonow.bentonow.controllers.errors.ErrorVersionActivity;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.controllers.order.BuildBentoActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Item;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.Stock;
import com.bentonow.bentonow.model.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.loopj.android.http.TextHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class MainActivity extends Activity {

    static final String TAG = "MainActivity";
    int retry = 0;
    CountDownTimer timer;

    public static boolean bIsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        BentoApplication.status = "main";
    }

    @Override
    protected void onResume() {
        bIsOpen = true;

        Order.cleanUp();

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (resultCode == ConnectionResult.SUCCESS) {
            if (getIntent() != null && getIntent().getBooleanExtra("checkLocation", false)) {
                waitForUserLocation();
            } else {
                loadData();
            }
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
            dialog.show();
        }

        BentoApplication.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        BentoApplication.onPause();
    }

    @Override
    protected void onStop() {
        bIsOpen = false;
        super.onStop();
    }

    void loadData() {
        //noinspection deprecation
        BentoRestClient.get("/init/" + Menu.getTodayDate(), null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                ((TextView) findViewById(R.id.txt_message)).setText(
                        "We seem to have trouble connecting to the network, please wait while we retry"
                                + (retry > 0 ? "(" + retry + ")" : "")
                );
                ++retry;
                Log.i(TAG, "retry: " + retry);
                loadData();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i(TAG, responseString != null ? responseString : "null");
                set(responseString);
            }
        });
    }

    void set(String responseString) {
        BackendText.set(responseString);
        Menu.set(responseString);
        Stock.set(responseString);
        Settings.set(responseString);

        Menu menu = Menu.get();

        if (menu != null) {
            for (Item item : menu.items) {
                if (item.image1 == null || item.image1.isEmpty()) continue;
                try {
                    Picasso.with(getApplicationContext()).load(item.image1).fetch(null);
                    Log.i(TAG, "caching: " + item.image1);
                } catch (Exception ignore) {
                }
            }
        }

        checkFirstRun();
    }

    void checkFirstRun() {
        Log.i(TAG, "firstRun " + SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.APP_FIRST_RUN));
        if (!SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.APP_FIRST_RUN)) {
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.APP_FIRST_RUN, true);

            startActivity(new Intent(this, GettingStartedActivity.class));
            finish();
        } else {
            checkAppStatus();
        }
    }

    void checkAppStatus() {
        if (Settings.min_version > BuildConfig.VERSION_CODE) {
            startActivity(new Intent(this, ErrorVersionActivity.class));
            finish();
        } else if (!Settings.status.equals("open")) {
            BentoNowUtils.openErrorActivity(this);
            finish();
        } else if (Order.pendingOrders()) {
            startActivity(new Intent(this, BuildBentoActivity.class));
            finish();
        } else {
            waitForUserLocation();
        }
    }

    void waitForUserLocation() {
        Log.i(TAG, "location " + User.location);
        if (User.location == null && Order.location == null) {
            final TextView message = (TextView) findViewById(R.id.txt_message);
            message.setVisibility(View.VISIBLE);
            message.setText("Searching for your location...");
            int skipWaitTime = 3;
            timer = new CountDownTimer(skipWaitTime * 1000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    Log.i(TAG, "location " + User.location);
                    if (User.location != null) {
                        timer.cancel();
                        goNext();
                    }
                }

                @Override
                public void onFinish() {
                    message.append("\nTap the screen to skip");
                    findViewById(R.id.content).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            goNext();
                        }
                    });
                }
            }.start();
        } else {
            goNext();
        }
    }

    void goNext() {
        if (Settings.isInServiceArea(User.location) || Settings.isInServiceArea(Order.location)) {
            startActivity(new Intent(this, BuildBentoActivity.class));
        } else {
            Mixpanel.track(this, "Opening app outside of service area");
            startActivity(new Intent(this, DeliveryLocationActivity.class));
        }

        finish();
    }
}
