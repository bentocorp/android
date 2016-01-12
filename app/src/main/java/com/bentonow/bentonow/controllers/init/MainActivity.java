package com.bentonow.bentonow.controllers.init;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.IntentCompat;
import android.view.View;
import android.widget.TextView;

import com.bentonow.bentonow.BuildConfig;
import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleLocationUtil;
import com.bentonow.bentonow.Utils.LocationUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.SocialNetworksUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.Stock;
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

        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            final String intentAction = intent.getAction();
            if (intent != null && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, "open");
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.LOCATION, "");
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ADDRESS, "");

        GoogleLocationUtil.getGoogleApiClient();

        if (BentoNowUtils.B_APPIUM_TESTING) {
            GoogleLocationUtil.setAppiumLocation(true);
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.APP_FIRST_RUN, true);
        }

        trackAppOpen();

        LocationUtils.mCurrentLocation = null;

        SocialNetworksUtil.generateKeyHash();

        mOrderDao.cleanUp();
    }

    @Override
    protected void onResume() {
        bIsOpen = true;

        DebugUtils.logDebug(TAG, "onResume");

        if (BuildConfig.DEBUG)
            getTxtVersion().setText(AndroidUtil.getAppVersionName(this));

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (resultCode == ConnectionResult.SUCCESS) {
            loadData();
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
            dialog.show();
        }

        super.onResume();
    }

    @Override
    protected void onStop() {
        bIsOpen = false;
        GoogleLocationUtil.stopLocationUpdates();
        super.onStop();
    }

    void loadData() {
        BentoRestClient.get("/init/" + BentoNowUtils.getTodayDate(), null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                ((TextView) findViewById(R.id.txt_message))
                        .setText("We seem to have trouble connecting to the network, please wait while we retry" + (retry > 0 ? "(" + retry + ")" : ""));
                ++retry;
                DebugUtils.logDebug(TAG, "retry: " + retry);
                loadData();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                DebugUtils.logDebug(TAG, responseString != null ? responseString : "null");
                set(responseString);
            }
        });


    }

    void set(final String responseString) {
        DebugUtils.logDebug(TAG, "set");

        BackendText.set(responseString);
        Menu.set(responseString);
        Stock.set(responseString);
        Settings.set(responseString);

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);

        checkAppStatus();
    }

    void checkAppStatus() {
        DebugUtils.logDebug(TAG, "checkAppStatus");

        if (BentoNowUtils.isLastVersionApp(MainActivity.this)) {
            if (!SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.APP_FIRST_RUN)) {
                Intent intent = new Intent(this, GettingStartedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                finish();
                startActivity(intent);
            } else {
                if (!Settings.status.equals("open")) {
                    BentoNowUtils.openErrorActivity(this);
                    finish();
                } else {
                    waitForUserLocation();
                }
            }
        } else {
            finish();
        }

    }

    void waitForUserLocation() {
        if (LocationUtils.isGpsEnable(MainActivity.this)) {
            getCurrentLocation();
        } else {
            ConfirmationDialog mDialog = new ConfirmationDialog(MainActivity.this, "Enable GPS", "GPS is disabled in your device. Enable it?");
            mDialog.addAcceptButton("Yes", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(callGPSSettingIntent);
                }
            });
            mDialog.addCancelButton("No", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getCurrentLocation();
                }
            });
            mDialog.show();
        }
    }

    private void getCurrentLocation() {
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
                    DebugUtils.logDebug(TAG, "location " + LocationUtils.mCurrentLocation);
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
        Menu mCurrentMenu = Menu.get();

        if (mCurrentMenu == null || !Settings.status.equals("open")) {
            if (Settings.status.equals("open"))
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, "closed");

            BentoNowUtils.openErrorActivity(this);
            DebugUtils.logDebug(TAG, "goNext ErrorActivity");
        } else if (Settings.isInServiceArea(LocationUtils.mCurrentLocation)) {
            BentoNowUtils.openBuildBentoActivity(this);
            DebugUtils.logDebug(TAG, "goNext BuildBentoActivity");
        } else {
            DebugUtils.logDebug(TAG, "goNext DeliveryLocationActivity");
            MixpanelUtils.track("Opened App Outside of Service Area");
            Intent intent = new Intent(this, DeliveryLocationActivity.class);
            intent.putExtra(ConstantUtils.TAG_OPEN_SCREEN, ConstantUtils.optOpenScreen.BUILD_BENTO);
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
