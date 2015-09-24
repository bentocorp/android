package com.bentonow.bentonow.Utils;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.User;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

public class MixpanelUtils {

    public static MixpanelAPI mMixpanel;

    public static MixpanelAPI getInstance() {
        if (mMixpanel == null)
            mMixpanel = MixpanelAPI.getInstance(BentoApplication.instance, BentoApplication.instance.getString(R.string.mixpanel_api_key));

        return mMixpanel;
    }

    public static void track(String event) {
        getInstance().track(event);
    }

    public static void track(String event, JSONObject params) {
        getInstance().track(event, params);
    }

    public static void signUpUser() {
        if (User.current != null) {
            getInstance().alias(User.current.email, User.current.email);
            setProfileProperties();
        }
    }

    public static void logInUser() {
        if (User.current != null) {
            getInstance().identify(User.current.email);
            setProfileProperties();
        }
    }

    public static void setProfileProperties() {
        getInstance().getPeople().identify(User.current.email);
        getInstance().getPeople().set("$name", User.current.firstname + " " + User.current.lastname);
        getInstance().getPeople().set("$email", User.current.email);
        getInstance().getPeople().set("$phone", User.current.phone);
        getInstance().getPeople().set("$created", BentoNowUtils.getMixpanelDate());
        getInstance().getPeople().set("Last Login Address", LocationUtils.getFullAddress(Order.address));
        trackRevenue(0);
    }

    public static void trackRevenue(double iRevenue) {
        JSONObject properties = new JSONObject();
        try {
            properties.put("$time", BentoNowUtils.getMixpanelDate());
            getInstance().getPeople().identify(User.current.email);
            getInstance().getPeople().trackCharge(iRevenue, properties);
        } catch (Exception ex) {
            DebugUtils.logError("trackRevenue()", ex);
        }
    }

    public static void clearPreferences() {
        getInstance().reset();
        getInstance().clearSuperProperties();
    }
}
