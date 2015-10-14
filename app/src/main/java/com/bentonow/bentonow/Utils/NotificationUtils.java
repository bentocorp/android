/**
 * @author Kokusho Torres
 * 03/11/2014
 */

package com.bentonow.bentonow.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.bentonow.bentonow.controllers.init.MainActivity;

public class NotificationUtils {

    private static final int idMixpanel = 913413123;

    public static final String TAG_MIXPANEL = "mp_message";


    public static void displayNotification(String sMessage) {
        try {
            Intent cIntent;
            NotificationManager mNotificationManager;
            PendingIntent contentIntent;
            NotificationCompat.Builder mNotifyBuilder;

            mNotificationManager = (NotificationManager) BentoApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE);

            mNotifyBuilder = new NotificationCompat.Builder(BentoApplication.instance);

            cIntent = new Intent(BentoApplication.instance, MainActivity.class);

            cIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            mNotifyBuilder.setSmallIcon(R.mipmap.ic_bento);
            mNotifyBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

            mNotifyBuilder.setContentTitle(BentoApplication.instance.getString(R.string.app_name));
            mNotifyBuilder.setContentText(sMessage);
            mNotifyBuilder.setDefaults(Notification.DEFAULT_SOUND);

            mNotifyBuilder.setAutoCancel(true).setVibrate(new long[]{500, 500});

            contentIntent = PendingIntent.getActivity(BentoApplication.instance, 0, cIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mNotifyBuilder.setContentIntent(contentIntent);

            mNotificationManager.cancelAll();
            mNotificationManager.notify(idMixpanel, mNotifyBuilder.build());

        } catch (Exception ex) {
            DebugUtils.logError("Notification", ex.toString());
        }
    }


    public static void cancelNotification(Context ctx, int notifyId) {
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(notifyId);
    }

    public static void cancelAllNotification(Context ctx) {
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

}
