package com.bentonow.bentonow.Utils;

import com.bentonow.bentonow.controllers.BentoApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by kokusho on 1/12/16.
 */
public class GoogleAnalyticsUtil {

    public static Tracker getDefaultTracker() {
        return BentoApplication.instance.getDefaultTracker();
    }

    public static void sendScreenView(String sName) {
        getDefaultTracker().setScreenName(sName);
        getDefaultTracker().send(new HitBuilders.ScreenViewBuilder().build());
        getDefaultTracker().send(new HitBuilders.EventBuilder().setCategory("UX").setAction("Screen").setLabel(sName).build());
    }

    public static void restartEvent() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.USER_NAME, "");
        BentoApplication.instance.restartTracker();
    }

}
