package com.bentonow.bentonow.controllers;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.dao.BentoDao;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.dao.OrderDao;
import com.bentonow.bentonow.dao.UserDao;
import com.bentonow.bentonow.service.BentoCustomerService;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONObject;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseFragmentActivity extends FragmentActivity {

    private String TAG = getClass() != null ? getClass().getName() : "BaseFragmentActivity";

    public BentoCustomerService mBentoService = null;
    public ServiceConnection mConnection = new WebSocketServiceConnection();
    public boolean mBound = false;

    protected OrderDao mOrderDao = new OrderDao();
    protected BentoDao mBentoDao = new BentoDao();
    protected DishDao mDishDao = new DishDao();
    protected UserDao userDao = new UserDao();

    protected long lStart, lEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_APP_IN_FRONT, true);

       /* CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );*/

    }

    private class WebSocketServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            DebugUtils.logDebug(TAG, "Successfully bounded to " + name.getClassName());
            BentoCustomerService.WebSocketServiceBinder webSocketServiceBinder = (BentoCustomerService.WebSocketServiceBinder) binder;
            mBentoService = webSocketServiceBinder.getService();
            onConnectService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DebugUtils.logDebug(TAG, "Disconnected from service " + name);
            mBound = true;
        }
    }

    public void onConnectService() {

    }

    protected void trackViewedScreen(String sScreen) {
        lEnd = System.currentTimeMillis();
        try {
            JSONObject params = new JSONObject();
            params.put("duration", (lEnd - lStart) / 1000);
            MixpanelUtils.track(sScreen, params);
        } catch (Exception e) {
            DebugUtils.logError(TAG, e);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_APP_IN_FRONT, true);
        AppEventsLogger.activateApp(this);

    }

    @Override
    protected void onStart() {
        lStart = System.currentTimeMillis();
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_APP_IN_FRONT, true);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MixpanelUtils.getMixpanelApi().flush();
    }

    @Override
    protected void onPause() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_APP_IN_FRONT, false);
        AppEventsLogger.deactivateApp(this);
        super.onPause();
    }

}
