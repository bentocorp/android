package com.bentonow.bentonow.Utils;

import android.os.Bundle;

import com.bentonow.bentonow.controllers.BentoApplication;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Created by Jose Torres on 11/4/15.
 */
public class FacebookUtil {

    private static AppEventsLogger aEventLogger;

    private static AppEventsLogger getAppEventLogger() {
        if (aEventLogger == null)
            aEventLogger = AppEventsLogger.newLogger(BentoApplication.instance);
        return aEventLogger;
    }

    public static void trackRevenue(double iRevenue) {
        getAppEventLogger().logPurchase(BigDecimal.valueOf(iRevenue), Currency.getInstance("USD"));
    }

    public static void trackEvent(String sEvent) {
        getAppEventLogger().logEvent(sEvent);
    }

    public static void trackEvent(Bundle mBundle) {
        Bundle parameters = new Bundle();
        parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "USD");
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product");
        parameters.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, "HDFU-8452");

        getAppEventLogger().logEvent(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, 54.23, parameters);
    }

}
