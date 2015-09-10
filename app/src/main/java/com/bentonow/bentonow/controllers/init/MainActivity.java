package com.bentonow.bentonow.controllers.init;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bentonow.bentonow.BuildConfig;
import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
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

    private TextView txtVersion;

    public static boolean bIsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");

        if (getCallingActivity() != null) Log.i(TAG, "callerActivity " + getCallingActivity());

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_STORE_CHANGIN, false);
    }

    @Override
    protected void onResume() {
        bIsOpen = true;

        Log.i(TAG, "onResume");

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
        BentoApplication.status = "main";

        if (BuildConfig.DEBUG)
            getTxtVersion().setText(AndroidUtil.getAppVersionName(this));

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "onPause");

        BentoApplication.onPause();
    }

    @Override
    protected void onStop() {
        bIsOpen = false;
        super.onStop();
    }

    void loadData() {
        Log.i(TAG, "loadData");
        //noinspection deprecation
        BentoRestClient.get("/init/" + BentoNowUtils.getTodayDate(), null, new TextHttpResponseHandler() {
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
        Log.i(TAG, "set");
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

            if (Settings.min_version > BuildConfig.VERSION_CODE) {
                Intent intent = new Intent(this, ErrorVersionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, GettingStartedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
            finish();
        } else {
            checkAppStatus();
        }
    }

    void checkAppStatus() {
        Log.i(TAG, "checkAppStatus");
        if (Settings.min_version > BuildConfig.VERSION_CODE) {
            Intent intent = new Intent(this, ErrorVersionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

            finish();
        } else if (!Settings.status.equals("open")) {
            BentoNowUtils.openErrorActivity(this);
            finish();
        } else if (Order.pendingOrders()) {
            Intent intent = new Intent(this, BuildBentoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);

            finish();
        } else {
            waitForUserLocation();
        }
    }

    void waitForUserLocation() {
        Log.i(TAG, "waitForUserLocation");
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
        if (Menu.get() == null || !Settings.status.equals("open")) {
            Log.i(TAG, "goNext ErrorActivity");
            BentoNowUtils.openErrorActivity(this);
            finish();
        } else if (Settings.isInServiceArea(User.location) || Settings.isInServiceArea(Order.location)) {
            Log.i(TAG, "goNext BuildBentoActivity");
            Intent intent = new Intent(this, BuildBentoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else {
            Log.i(TAG, "goNext DeliveryLocationActivity");
            Mixpanel.track(this, "Opening app outside of service area");
            Intent intent = new Intent(this, DeliveryLocationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }

        finish();
    }

    private TextView getTxtVersion() {
        if (txtVersion == null)
            txtVersion = (TextView) findViewById(R.id.txt_version);
        return txtVersion;
    }
}
