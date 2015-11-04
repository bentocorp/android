package com.bentonow.bentonow.Utils;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.User;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

public class MixpanelUtils {

    public static MixpanelAPI mMixpanel;

    public static MixpanelAPI getMixpanelApi() {
        if (mMixpanel == null)
            mMixpanel = MixpanelAPI.getInstance(BentoApplication.instance, BentoApplication.instance.getString(R.string.mixpanel_api_key));

        return mMixpanel;
    }

    public static void track(String event) {
        FacebookUtil.trackEvent(event);
        getMixpanelApi().track(event);
    }

    public static void track(String event, JSONObject params) {
        getMixpanelApi().track(event, params);
    }

    public static void signUpUser() {
        if (User.current != null) {
            getMixpanelApi().alias(User.current.email, User.current.email);
            setProfileProperties();
        }
    }

    public static void logInUser() {
        if (User.current != null) {
            MixpanelAPI.People people = getMixpanelApi().getPeople();
            people.identify(User.current.email);
            people.initPushHandling(BentoApplication.instance.getString(R.string.google_project_key));
            setProfileProperties();
        }
    }

    public static void setProfileProperties() {
        getMixpanelApi().getPeople().identify(User.current.email);
        getMixpanelApi().getPeople().set("$name", User.current.firstname + " " + User.current.lastname);
        getMixpanelApi().getPeople().set("$email", User.current.email);
        getMixpanelApi().getPeople().set("$phone", User.current.phone);
        getMixpanelApi().getPeople().set("$created", BentoNowUtils.getMixpanelDate());
        getMixpanelApi().getPeople().set("Last Login Address", LocationUtils.getCustomAddress(Order.address));
        trackRevenue(0);
    }

    public static void trackRevenue(double iRevenue) {
        JSONObject properties = new JSONObject();
        try {
            FacebookUtil.trackRevenue(iRevenue);
            properties.put("$time", BentoNowUtils.getMixpanelDate());
            getMixpanelApi().getPeople().identify(User.current.email);
            getMixpanelApi().getPeople().trackCharge(iRevenue, properties);
        } catch (Exception ex) {
            DebugUtils.logError("trackRevenue()", ex);
        }
    }

    public static void clearPreferences() {
        getMixpanelApi().reset();
        getMixpanelApi().clearSuperProperties();
    }
}
