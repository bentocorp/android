package com.bentonow.bentonow.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.Stock;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

public class BentoService extends Service {

    private static final String TAG = "BentoService";
    private static Handler handler;
    private static Runnable task;

    public static String date = "";

    public static boolean isRunning() {
        if (task == null || handler == null)
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_BENTO_SERVICE_RUNNING, false);

        Log.i(TAG, "isRunning: " + SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_BENTO_SERVICE_RUNNING));
        return SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_BENTO_SERVICE_RUNNING);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_BENTO_SERVICE_RUNNING, true);

        loadData();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");

        if (handler != null && task != null)
            handler.removeCallbacks(task);

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_BENTO_SERVICE_RUNNING, false);

        BentoNowUtils.runBentoService(BentoService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "onStartCommand()");
        return (START_NOT_STICKY);
    }

    public static void init() {
        date = BentoNowUtils.getTodayDate();
        Log.i(TAG, "init");
    }

    void startTask() {
        task = new Runnable() {
            public void run() {
                loadData();
            }
        };
        handler = new Handler();

        if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_APP_IN_FRONT)) {
            handler.postDelayed(task, 1000 * 90);
            DebugUtils.logDebug(TAG, "startTask");
        } else {
            DebugUtils.logError(TAG, "Task Stop: " + "appIsNotInFront");
        }

    }

    void loadData() {
        String currDate = BentoNowUtils.getTodayDate();
        Log.i(TAG, "loadData" + " current date: " + currDate);

        if (!date.equals(currDate)) {
            BentoNowUtils.openMainActivity(this);
        }

        //noinspection deprecation
        BentoRestClient.get("/init/" + BentoNowUtils.getTodayDate(), null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "cannot loadData");
                onDestroy();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                set(responseString);
            }
        });
        startTask();

    }

    void set(String responseString) {

        try {
            Stock.set(responseString);
            BackendText.set(responseString);
            Settings.set(responseString);
            Menu.set(responseString);

            DebugUtils.logDebug(TAG, "set To Current Status: " + Settings.status);

            Menu mMenu = Menu.get();

            if (mMenu == null) {
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, "closed");
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_STORE_CHANGIN, true);
                BentoNowUtils.openErrorActivity(this);

            } else if (!Settings.status.equals(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.STORE_STATUS))) {
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_STORE_CHANGIN, true);
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);
                switch (Settings.status) {
                    case "open":
                        BentoNowUtils.openMainActivity(this);
                        break;
                    case "sold out":
                    case "closed":
                        BentoNowUtils.openErrorActivity(this);
                        break;
                }
            } else {
                if (Order.current != null)
                    if (!Order.current.MealName.equals(mMenu.meal_name) || !Order.current.MenuType.equals(mMenu.menu_type)) {
                        DebugUtils.logDebug(TAG, "New Menu: " + mMenu.meal_name + "||" + mMenu.menu_type);
                        WidgetsUtils.createShortToast(R.string.error_new_menu_type);
                        Order.cleanUp();
                        BentoNowUtils.openBuildBentoActivity(this);
                    }
            }

        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }
    }

}