package com.bentonow.bentonow.controllers.init;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.Utils.maps.LocationUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.model.gatekeeper.GateKeeperModel;
import com.bentonow.bentonow.parse.InitParse;
import com.bentonow.bentonow.ui.material.ProgressBarCircularIndeterminate;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends BaseFragmentActivity implements View.OnClickListener {

    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1001;
    static final String TAG = "MainActivity";
    public static boolean bIsOpen = false;
    private TextView txtVersion;
    private TextView txtMessage;
    private ProgressBarCircularIndeterminate homeLoader;
    private ConfirmationDialog mConfirmationDialog;
    private ConfirmationDialog mDialogNotifications;
    private ConfirmationDialog mDialogDailyNotifications;

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

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.CLEAR_ORDERS_FROM_SUMMARY, false);

        MenuDao.gateKeeper = new GateKeeperModel();

        if (BentoNowUtils.B_APPIUM_TESTING) {
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.APP_FIRST_RUN, true);
        }
        mOrderDao.cleanUp();

        MixpanelUtils.logInUser(userDao.getCurrentUser());

        startRegistrationService();

    }

    @Override
    protected void onResume() {
        bIsOpen = true;

        DebugUtils.logDebug(TAG, "onResume");

        GoogleAnalyticsUtil.sendScreenView("Splash");

        if (BuildConfig.DEBUG)
            getTxtVersion().setText(AndroidUtil.getAppVersionName(this));

        super.onResume();
    }

    private void startRegistrationService() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int code = api.isGooglePlayServicesAvailable(this);
        if (code == ConnectionResult.SUCCESS) {
            onActivityResult(REQUEST_GOOGLE_PLAY_SERVICES, Activity.RESULT_OK, null);
        } else if (api.isUserResolvableError(code) && api.showErrorDialogFragment(this, code, REQUEST_GOOGLE_PLAY_SERVICES)) {
        } else {
            WidgetsUtils.createShortToast(GoogleApiAvailability.getInstance().getErrorString(code));
        }
    }


    private void checkBentoStatus() {
        if (LocationUtils.isGpsEnable(MainActivity.this)) {
            MixpanelUtils.track("Allow Location Services");
            loadData();
        } else {
            MixpanelUtils.track("Don't Allow Location Services");
            if (mConfirmationDialog == null || !mConfirmationDialog.isShowing()) {
                mConfirmationDialog = new ConfirmationDialog(MainActivity.this, "Enable GPS", "GPS is disabled in your device. Enable it?", false);
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
                        loadData();
                    }
                });
                mConfirmationDialog.show();
            }
        }
    }

    private void loadData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getHomeLoader().setVisibility(View.VISIBLE);
            }
        });
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
                    openNextScreen();
                }
            }
        });


    }

    private void openNextScreen() {
        trackAppOpen();
        if (!SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.APP_FIRST_RUN)) {
            MixpanelUtils.track("App Installed");
            Intent intent = new Intent(this, GettingStartedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            finish();
            startActivity(intent);
        } else if (!SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.ALREADY_SHOW_NOTIFICATIONS)) {
            showEnableNotificationDialog();
        } else if (!SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.ALREADY_SHOW_DAILY_NOTIFICATIONS)) {
            showEnableDailyNotificationDialog();
        } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, ConstantUtils.MANIFEST_LOCATION_PERMISSION);
        } else {
            openDeliveryLocation();
        }
    }

    private void openDeliveryLocation() {
        Intent intent = new Intent(this, DeliveryLocationActivity.class);
        intent.putExtra(ConstantUtils.TAG_OPEN_SCREEN, ConstantUtils.optOpenScreen.BUILD_BENTO);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        finish();
        startActivity(intent);
    }

    private void showEnableNotificationDialog() {
        if (mDialogNotifications == null || !mDialogNotifications.isShowing()) {
            mDialogNotifications = new ConfirmationDialog(MainActivity.this, IosCopyDao.get("notif-optin-exist-txt1"), IosCopyDao.get("notif-optin-exist-txt2"), false);
            mDialogNotifications.addAcceptButton(IosCopyDao.get("build-not-complete-confirmation-2"), MainActivity.this);
            mDialogNotifications.addCancelButton(IosCopyDao.get("build-not-complete-confirmation-1"), MainActivity.this);
            mDialogNotifications.show();
        }
    }

    private void showEnableDailyNotificationDialog() {
        if (mDialogDailyNotifications == null || !mDialogDailyNotifications.isShowing()) {
            mDialogDailyNotifications = new ConfirmationDialog(MainActivity.this, IosCopyDao.get("notif-optin-exist-txt1"), IosCopyDao.get("daily_reminder_question"), false);
            mDialogDailyNotifications.addAcceptButton(IosCopyDao.get("build-not-complete-confirmation-2"), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.SHOW_DAILY_NOTIFICATIONS, true);
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ALREADY_SHOW_DAILY_NOTIFICATIONS, true);
                    MixpanelUtils.logInUser(userDao.getCurrentUser());
                    openNextScreen();
                }
            });
            mDialogDailyNotifications.addCancelButton(IosCopyDao.get("build-not-complete-confirmation-1"), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.SHOW_DAILY_NOTIFICATIONS, false);
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ALREADY_SHOW_DAILY_NOTIFICATIONS, true);
                    MixpanelUtils.logInUser(userDao.getCurrentUser());
                    openNextScreen();
                }
            });
            mDialogDailyNotifications.show();
        }
    }


    private void trackAppOpen() {
        try {
            JSONObject params = new JSONObject();
            LatLng mLastLocation = BentoNowUtils.getOrderLocation();
            params.put("coordinates", mLastLocation == null ? "0.0,0.0" : mLastLocation.latitude + "," + mLastLocation.longitude);
            MixpanelUtils.track("App Launched", params);
        } catch (Exception e) {
            DebugUtils.logError(TAG, "track(): " + e.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    checkBentoStatus();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ConstantUtils.MANIFEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openDeliveryLocation();
                else openDeliveryLocation();
                break;
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

    private ProgressBarCircularIndeterminate getHomeLoader() {
        if (homeLoader == null)
            homeLoader = (ProgressBarCircularIndeterminate) findViewById(R.id.home_preloader);
        return homeLoader;
    }

}
