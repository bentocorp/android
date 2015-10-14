/**
 * @author Kokusho Torres
 * 30/09/2014
 */

package com.bentonow.bentonow.service.gcm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.NotificationUtils;
import com.bentonow.bentonow.controllers.BentoApplication;


public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "GcmBroadcastReceiver";

    @Override
    public void onReceive(Context context, final Intent intent) {
        DebugUtils.logDebug(TAG, intent.getExtras());
        if (intent.getExtras().containsKey(NotificationUtils.TAG_MIXPANEL)) {
            BentoApplication.instance.doInBackground(new Runnable() {
                public void run() {
                    try {
                        NotificationUtils.displayNotification(intent.getExtras().getString("mp_message"));
                    } catch (Exception e) {
                        DebugUtils.logError("GcmBroadcastReceiver", e);
                    }
                }
            });
        }

    }

}
