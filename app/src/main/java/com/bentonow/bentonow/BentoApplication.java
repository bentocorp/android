package com.bentonow.bentonow;

import android.content.Intent;
import android.util.Log;

import com.bentonow.bentonow.Utils.Mixpanel;
import com.orm.SugarApp;

import java.util.Timer;
import java.util.TimerTask;

public class BentoApplication extends SugarApp {

    private static final String TAG = "BentoApplication";

    static Timer timer;
    static TimerTask stopService;
    static BentoApplication instance = null;
    static public boolean started = false;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        if (!BuildConfig.DEBUG) {
            Mixpanel.getInstance(this);
        }
    }

    static public void onPause () {
        timer = new Timer();

        Log.i(TAG, "onPause");

        stopService = new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "stopping service");
                try {
                    instance.stopService(new Intent(instance, BentoService.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // After timer reached the service will be stop
        timer.schedule(stopService, 10 * 1000);
    }

    static public void onResume () {
        Log.i(TAG, "onResume");

        if (timer != null) {
            Log.i(TAG, "cancel timer");
            timer.cancel();
            timer = null;
        }

        if (!BentoService.isRunning() && instance != null && started) {
            Log.i(TAG, "starting service");
            try {
                instance.startService(new Intent(instance, BentoService.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
