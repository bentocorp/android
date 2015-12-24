package com.bentonow.bentonow.service;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.dao.OrderDao;
import com.bentonow.bentonow.listener.InterfaceCustomerService;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.Stock;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.util.ArrayList;

/**
 * Created by Kokusho on 21/12/15.
 */
public class BentoCustomerService extends Service {

    public static final String TAG = "BentoCustomerService";

    private static InterfaceCustomerService mListener;

    private final WebSocketServiceBinder binder = new WebSocketServiceBinder();
    private static Handler mHandler;
    private static Runnable mLoadingTask;

    public boolean bSendRequest;

    @Override
    public void onCreate() {
        DebugUtils.logDebug(TAG, "Creating Service...");
        bSendRequest = true;
        getBentoData(false);
    }

    private void saveNewData(String responseString) {
        try {
            Stock.set(responseString);
            BackendText.set(responseString);
            Settings.set(responseString);
            Menu.set(responseString);


            Menu mMenu = Menu.get();

            boolean bHasMenu = mMenu != null;
            boolean bIsConnected = mListener != null;

            if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.APP_FIRST_RUN)) {
                if (BentoNowUtils.isLastVersionApp(this)) {
                    if (mMenu == null) {
                        if (!SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.STORE_STATUS).equals("closed")) {
                            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, "closed");
                            BentoNowUtils.openErrorActivity(this);
                        }
                    } else {
                        if (!Settings.status.equals(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.STORE_STATUS))
                                && SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_APP_IN_FRONT)) {

                            DebugUtils.logDebug(TAG, "Should change from: " + SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.STORE_STATUS) + " to " + Settings.status);

                            switch (Settings.status) {
                                case "open":
                                    if (mListener != null) {
                                        if (mMenu != null)
                                            mListener.openBuildBentoActivity();
                                        else
                                            mListener.openMainActivity();
                                    }
                                    break;
                                case "sold out":
                                case "closed":
                                    if (mListener != null)
                                        mListener.openErrorActivity();
                                    break;
                            }
                        } else {
                            OrderDao mOrderDao = new OrderDao();
                            Order mOrder = mOrderDao.getCurrentOrder();

                            if (mOrder != null && mMenu != null && SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_APP_IN_FRONT))
                                if (!mOrder.MealName.equals(mMenu.meal_name) || !mOrder.MenuType.equals(mMenu.menu_type)) {
                                    DebugUtils.logDebug(TAG, "New Menu: " + mMenu.meal_name + "||" + mMenu.menu_type);
                                    WidgetsUtils.createShortToast(R.string.error_new_menu_type);
                                    if (mListener != null) {
                                        mOrderDao.cleanUp();
                                        mListener.openBuildBentoActivity();
                                    }
                                }
                        }
                    }
                }

            }

            DebugUtils.logDebug(TAG, "New Data: " + "Status: " + Settings.status + " HasMenu: " + bHasMenu + " Listener:" + bIsConnected);

        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }
    }

    private void getBentoData(boolean cCheckStatus) {
        if (bSendRequest || !cCheckStatus) {
            bSendRequest = false;

            BentoRestClient.get("/init/" + BentoNowUtils.getTodayDate(), null, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    if (mListener != null) {
                        DebugUtils.logError(TAG, "Cannot loadData");
                    }
                    onFinish();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    if (mListener != null) {
                        DebugUtils.logDebug(TAG, "Get New Data");
                        saveNewData(responseString);
                    }

                    onFinish();
                }

                @Override
                public void onFinish() {
                    startTimerTask();
                }
            });

        }
    }

    private void startTimerTask() {
        getHandler().postDelayed(getLoadingTask(), 1000 * 30);
    }

    public void disconnectBentoService() {
        DebugUtils.logDebug(TAG, "destroying WebSocketService");
        getHandler();
    }

    public void setServiceListener(InterfaceCustomerService mListener) {
        if (mListener == null) {
            DebugUtils.logDebug(TAG, "Unbind Service");
        } else {
            DebugUtils.logDebug(TAG, "Bind Service");
        }

        this.mListener = mListener;
    }

    private Runnable getLoadingTask() {
        if (mLoadingTask == null) {
            mLoadingTask = new Runnable() {
                public void run() {
                    //DebugUtils.logDebug(TAG, "Start Task");
                    bSendRequest = true;
                    getBentoData(true);
                }
            };
        }
        return mLoadingTask;
    }

    private Handler getHandler() {
        if (mHandler == null)
            mHandler = new Handler();

        mHandler.removeCallbacks(getLoadingTask());
        return mHandler;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        disconnectBentoService();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
        return true;
    }

    public class WebSocketServiceBinder extends Binder {
        public BentoCustomerService getService() {
            return BentoCustomerService.this;
        }
    }
}
