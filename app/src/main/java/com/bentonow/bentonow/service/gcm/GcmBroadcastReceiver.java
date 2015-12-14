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


public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "GcmBroadcastReceiver";

    @Override
    public void onReceive(Context context, final Intent intent) {
        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey(NotificationUtils.TAG_MIXPANEL)) {
            DebugUtils.logDebug(TAG, intent.getExtras());
            new Thread(new Runnable() {
                public void run() {
                    try {
                        NotificationUtils.displayNotification(intent.getExtras().getString("mp_message"));
                    } catch (Exception e) {
                        DebugUtils.logError("GcmBroadcastReceiver", e);
                    }
                }
            }).start();
        }

    }

}
