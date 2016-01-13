package com.bentonow.bentonow.Utils;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by kokusho on 1/12/16.
 */
public class GoogleAnalyticsUtil {
    private static Tracker mTracker;

    public static Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(BentoApplication.instance);
            mTracker = analytics.newTracker(R.string.google_api_key);
        }

        return mTracker;
    }

}
