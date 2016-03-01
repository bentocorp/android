package com.bentonow.bentonow.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.dao.SettingsDao;
import com.bentonow.bentonow.listener.InterfaceCustomerService;
import com.bentonow.bentonow.parse.InitParse;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;


/**
 * Created by Kokusho on 21/12/15.
 */
public class BentoCustomerService extends Service {

    public static final String TAG = "BentoCustomerService";

    private static InterfaceCustomerService mListener;
    private static Handler mHandler;
    private static Runnable mLoadingTask;
    private final WebSocketServiceBinder binder = new WebSocketServiceBinder();
    public boolean bSendRequest;
    public int iNumTimesRequest;

    @Override
    public void onCreate() {
        DebugUtils.logDebug(TAG, "Creating Service...");
        bSendRequest = true;
        getBentoData(false);
        iNumTimesRequest = 0;
    }

    private void saveNewData(String responseString) {
        try {
            InitParse.parseInitTwo(responseString);

//            if (iNumTimesRequest > 2)
//                Settings.pod_mode = "4";


            boolean bIsConnected = mListener != null;

            if (BentoNowUtils.isLastVersionApp(this)) {
                if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_APP_IN_FRONT) && mListener != null)
                    if (MenuDao.gateKeeper.getAppState().contains("map,no_service"))
                        mListener.onMapNoService();
                    else if (MenuDao.gateKeeper.getAppState().contains("build"))
                        mListener.onBuild();
                    else if (MenuDao.gateKeeper.getAppState().contains("closed_wall"))
                        mListener.onClosedWall();
                    else if (MenuDao.gateKeeper.getAppState().contains("sold"))
                        mListener.onSold();
                    else
                        DebugUtils.logError(TAG, "Unknown State: " + MenuDao.gateKeeper.getAppState());

            }

            if (!MenuDao.gateKeeper.getAppState().equals(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.STORE_STATUS)))
                DebugUtils.logDebug(TAG, "Should change from: " + SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.STORE_STATUS) + " to " + MenuDao.gateKeeper.getAppState());

            DebugUtils.logDebug(TAG, "New Data: " + "Status: " + MenuDao.gateKeeper.getAppState() + " Listener: " + bIsConnected + " Pod: " + SettingsDao.getCurrent().pod_mode);

        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }
    }

    public void getBentoData(boolean cCheckStatus) {
        if (bSendRequest || !cCheckStatus) {
            bSendRequest = false;

            BentoRestClient.get(BentoRestClient.getInit2Url(), null, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    DebugUtils.logError(TAG, "Cannot loadData Has Listener: " + (mListener != null));

                    onFinish();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    if (mListener != null) {
                        iNumTimesRequest++;
                        DebugUtils.logDebug(TAG, "Get New Data: " + iNumTimesRequest);
                        saveNewData(responseString);
                    }

                    onFinish();
                }

                @Override
                public void onFinish() {
                    startTimerTask();
                }
            });

        } else
            DebugUtils.logDebug(TAG, "Request already sent");
    }

    private void startTimerTask() {
        getHandler().postDelayed(getLoadingTask(), 1000 * 60);
    }

    public void disconnectBentoService() {
        DebugUtils.logDebug(TAG, "destroying OrderSocketService");
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
