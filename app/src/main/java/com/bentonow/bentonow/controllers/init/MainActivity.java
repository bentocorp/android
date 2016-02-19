package com.bentonow.bentonow.controllers.init;

import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bentonow.bentonow.BuildConfig;
import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.GoogleLocationUtil;
import com.bentonow.bentonow.Utils.LocationUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.parse.InitParse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;


public class MainActivity extends BaseFragmentActivity implements View.OnClickListener {

    static final String TAG = "MainActivity";
    public static boolean bIsOpen = false;
    private TextView txtVersion;
    private TextView txtMessage;
    private Dialog mDialogPlayServices;
    private ConfirmationDialog mConfirmationDialog;
    private ConfirmationDialog mDialogNotifications;

    private int retry = 0;

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
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.MENU_TODAY, "");
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.MENU_NEXT, "");
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STATUS_ALL, "");
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.BACKENDTEXT, "");
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.SETTINGS, "");
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.MEALS, "");

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.APP_FIRST_RUN, false);

        GoogleLocationUtil.getGoogleApiClient(false);

        if (BentoNowUtils.B_APPIUM_TESTING) {
            GoogleLocationUtil.setAppiumLocation(true);
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.APP_FIRST_RUN, true);
        }

        trackAppOpen();

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
            if (mDialogPlayServices == null || !mDialogPlayServices.isShowing()) {
                mDialogPlayServices = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1);
                mDialogPlayServices.show();
            }
        }

        GoogleAnalyticsUtil.sendScreenView("Splash");

        super.onResume();
    }

    private void loadData() {
        BentoRestClient.get(BentoRestClient.getInitUrl(), null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (bIsOpen) {
                    ++retry;
                    DebugUtils.logDebug(TAG, "retry: " + retry);
                    loadData();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getTxtMessage().setText("We seem to have trouble connecting to the network, please wait while we retry " + (retry > 0 ? "(" + retry + ") " : ""));
                        }
                    });
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                if (bIsOpen) {
                    DebugUtils.logDebug(TAG, "onSuccess: " + statusCode);
                    InitParse.parseInitTwo(responseString);
                    checkAppStatus();
                }
            }
        });


    }

    private void checkAppStatus() {
        DebugUtils.logDebug(TAG, "checkAppStatus");

        if (BentoNowUtils.isLastVersionApp(MainActivity.this)) {
            if (LocationUtils.isGpsEnable(MainActivity.this)) {
                MixpanelUtils.track("Allow Location Services");
                openNextScreen();
            } else {
                MixpanelUtils.track("Don't Allow Location Services");
                if (mConfirmationDialog == null || !mConfirmationDialog.isShowing()) {
                    mConfirmationDialog = new ConfirmationDialog(MainActivity.this, "Enable GPS", "GPS is disabled in your device. Enable it?");
                    mConfirmationDialog.addAcceptButton("Yes", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        }
                    });
                    mConfirmationDialog.addCancelButton("No", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openNextScreen();
                        }
                    });
                    mConfirmationDialog.show();
                }
            }

        } else {
            finish();
        }

    }

    private void openNextScreen() {
        if (!SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.APP_FIRST_RUN)) {
            MixpanelUtils.track("App Installed");
            Intent intent = new Intent(this, GettingStartedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            finish();
            startActivity(intent);
        } else if (!SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.ALREADY_SHOW_NOTIFICATIONS)) {
            showEnableNotificationDialog();
        } else {
            if (LocationUtils.isGpsEnable(MainActivity.this)) {
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.LOCATION, "");
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ADDRESS, "");
            }

            Intent intent = new Intent(this, DeliveryLocationActivity.class);
            intent.putExtra(ConstantUtils.TAG_OPEN_SCREEN, ConstantUtils.optOpenScreen.BUILD_BENTO);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            finish();
            startActivity(intent);
        }
    }


    private void showEnableNotificationDialog() {
        if (mDialogNotifications == null || !mDialogNotifications.isShowing()) {
            mDialogNotifications = new ConfirmationDialog(MainActivity.this, "Receive Notifications", "Notifications may include alerts, sounds, and icon badges. These can be configured in Settings", false);
            mDialogNotifications.addAcceptButton(IosCopyDao.get("build-not-complete-confirmation-2"), MainActivity.this);
            mDialogNotifications.addCancelButton(IosCopyDao.get("build-not-complete-confirmation-1"), MainActivity.this);
            mDialogNotifications.show();
        }
    }


    private void trackAppOpen() {
        try {
            Location mCurrentLocation = GoogleLocationUtil.getCurrentLocation();
            JSONObject params = new JSONObject();
            params.put("coordinates", mCurrentLocation == null ? "0.0,0.0" : mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
            MixpanelUtils.track("App Launched", params);
        } catch (Exception e) {
            DebugUtils.logError(TAG, "track(): " + e.toString());
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_accept:
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ALREADY_SHOW_NOTIFICATIONS, true);
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.SHOW_NOTIFICATIONS, true);
                openNextScreen();
                break;
            case R.id.button_cancel:
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ALREADY_SHOW_NOTIFICATIONS, true);
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.SHOW_NOTIFICATIONS, false);
                openNextScreen();
                break;
        }
    }

    @Override
    protected void onStop() {
        bIsOpen = false;
        GoogleLocationUtil.stopLocationUpdates();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private TextView getTxtVersion() {
        if (txtVersion == null)
            txtVersion = (TextView) findViewById(R.id.txt_version);
        return txtVersion;
    }

    private TextView getTxtMessage() {
        if (txtMessage == null)
            txtMessage = (TextView) findViewById(R.id.txt_message);
        return txtMessage;
    }

}
