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
import com.bentonow.bentonow.Utils.GoogleLocationUtil;
import com.bentonow.bentonow.Utils.LocationUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.dao.SettingsDao;
import com.bentonow.bentonow.parse.InitParse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;


public class MainActivity extends BaseFragmentActivity {

    static final String TAG = "MainActivity";

    private TextView txtVersion;

    public static boolean bIsOpen = false;

    private Dialog mDialogPlayServices;
    private ConfirmationDialog mConfirmationDialog;

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
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.LOCATION, "");
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ADDRESS, "");

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

        super.onResume();
    }

    private void loadData() {
        BentoRestClient.get(BentoRestClient.getInitUrl(), null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                ((TextView) findViewById(R.id.txt_message)).setText("We seem to have trouble connecting to the network, please wait while we retry " + (retry > 0 ? "(" + retry + ") " : ""));
                ++retry;
                DebugUtils.logDebug(TAG, "retry: " + retry);
                loadData();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                DebugUtils.logDebug(TAG, "onSuccess: " + statusCode);
                InitParse.parseInitTwo(responseString);
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, SettingsDao.getCurrent().status);
                checkAppStatus();
            }
        });


    }

    private void checkAppStatus() {
        DebugUtils.logDebug(TAG, "checkAppStatus");

        if (BentoNowUtils.isLastVersionApp(MainActivity.this)) {
            if (LocationUtils.isGpsEnable(MainActivity.this)) {
                MixpanelUtils.track("Allow Location Services");
                openNextScren();
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
                            openNextScren();
                        }
                    });
                    mConfirmationDialog.show();
                }
            }

        } else {
            finish();
        }

    }

    private void openNextScren() {
        if (!SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.APP_FIRST_RUN)) {
            MixpanelUtils.track("App Installed");
            Intent intent = new Intent(this, GettingStartedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            finish();
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, DeliveryLocationActivity.class);
            intent.putExtra(ConstantUtils.TAG_OPEN_SCREEN, ConstantUtils.optOpenScreen.BUILD_BENTO);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            finish();
            startActivity(intent);
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
    protected void onStop() {
        bIsOpen = false;
        GoogleLocationUtil.stopLocationUpdates();
        super.onStop();
    }

    private TextView getTxtVersion() {
        if (txtVersion == null)
            txtVersion = (TextView) findViewById(R.id.txt_version);
        return txtVersion;
    }
}
