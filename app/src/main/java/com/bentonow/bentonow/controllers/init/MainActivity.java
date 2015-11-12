package com.bentonow.bentonow.controllers.init;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bentonow.bentonow.BuildConfig;
import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleLocationUtil;
import com.bentonow.bentonow.Utils.LocationUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.errors.ErrorVersionActivity;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.Stock;
import com.bentonow.bentonow.model.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;


public class MainActivity extends BaseFragmentActivity {

    static final String TAG = "MainActivity";
    int retry = 0;
    CountDownTimer timer;

    private TextView txtVersion;

    public static boolean bIsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getCallingActivity() != null)
            Log.i(TAG, "callerActivity " + getCallingActivity());

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_STORE_CHANGIN, false);
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, "open");

        GoogleLocationUtil.getGoogleApiClient();

        if (BentoNowUtils.B_APPIUM_TESTING) {
            GoogleLocationUtil.setAppiumLocation(true);
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.APP_FIRST_RUN, true);

        }

        Order.cleanUp();
    }

    @Override
    protected void onResume() {
        bIsOpen = true;

        Log.i(TAG, "onResume");

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

        if (BuildConfig.DEBUG)
            getTxtVersion().setText(AndroidUtil.getAppVersionName(this));

        super.onResume();
    }

    @Override
    protected void onStop() {
        bIsOpen = false;
        GoogleLocationUtil.stopLocationUpdates();
        super.onStop();
    }

    void loadData() {
        Log.i(TAG, "loadData");
        //noinspection deprecation
        BentoRestClient.get("/init/" + BentoNowUtils.getTodayDate(), null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                ((TextView) findViewById(R.id.txt_message))
                        .setText("We seem to have trouble connecting to the network, please wait while we retry" + (retry > 0 ? "(" + retry + ")" : ""));
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

    void set(final String responseString) {
        Log.i(TAG, "set");

        BackendText.set(responseString);
        Menu.set(responseString);
        Stock.set(responseString);
        Settings.set(responseString);

        checkFirstRun();
    }

    void checkFirstRun() {
        if (!SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.APP_FIRST_RUN)) {
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
       /* } else if (Order.pendingOrders()) {
            BentoNowUtils.openBuildBentoActivity(this);
            finish();*/
        } else {
            waitForUserLocation();
        }
    }

    void waitForUserLocation() {
        if (LocationUtils.mCurrentLocation == null) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    GoogleLocationUtil.getGoogleApiClient();
                }
            });

            final TextView message = (TextView) findViewById(R.id.txt_message);
            message.setVisibility(View.VISIBLE);
            message.setText("Searching for your location...");
            int skipWaitTime = 3;
            timer = new CountDownTimer(skipWaitTime * 1000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    Log.i(TAG, "location " + LocationUtils.mCurrentLocation);
                    if (LocationUtils.mCurrentLocation != null) {
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
        trackAppOpen();
        Menu mCurrentMenu = Menu.get();

        if (mCurrentMenu == null || !Settings.status.equals("open")) {
            Log.i(TAG, "goNext ErrorActivity");

            if (Settings.status.equals("open"))
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, "closed");
            else
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);

            BentoNowUtils.openErrorActivity(this);
        } else if (Settings.isInServiceArea(LocationUtils.mCurrentLocation)) {
            BentoNowUtils.openBuildBentoActivity(this);
        } else {
            Log.i(TAG, "goNext DeliveryLocationActivity");
            MixpanelUtils.track("Opened App Outside of Service Area");
            Intent intent = new Intent(this, DeliveryLocationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        finish();

    }

    private void trackAppOpen() {
        try {
            JSONObject params = new JSONObject();
            params.put("coordinates", LocationUtils.mCurrentLocation == null ? "0.0,0.0" : LocationUtils.mCurrentLocation.latitude + "," + LocationUtils.mCurrentLocation.longitude);
            MixpanelUtils.track("App Launched", params);
        } catch (Exception e) {
            DebugUtils.logError(TAG, "track(): " + e.toString());
        }
    }

    private TextView getTxtVersion() {
        if (txtVersion == null)
            txtVersion = (TextView) findViewById(R.id.txt_version);
        return txtVersion;
    }
}
