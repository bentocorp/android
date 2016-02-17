package com.bentonow.bentonow.Utils;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BentoApplication;
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
        FacebookUtil.trackEvent(event);
    }

    public static void signUpUser(User mUser) {
        if (mUser != null) {
            getMixpanelApi().alias(mUser.email, mUser.email);
            setProfileProperties(mUser);
        }
    }

    public static void logInUser(User mUser) {
        if (mUser != null) {
            MixpanelAPI.People people = getMixpanelApi().getPeople();
            people.identify(mUser.email);
            people.initPushHandling(BentoApplication.instance.getString(R.string.google_project_key));
            setProfileProperties(mUser);
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.USER_NAME, mUser.email);
        }
    }

    public static void setProfileProperties(User mUser) {
        getMixpanelApi().getPeople().identify(mUser.email);
        getMixpanelApi().getPeople().set("$name", mUser.firstname + " " + mUser.lastname);
        getMixpanelApi().getPeople().set("$email", mUser.email);
        getMixpanelApi().getPeople().set("$phone", mUser.phone);
        getMixpanelApi().getPeople().set("$created", BentoNowUtils.getMixpanelDate());
        getMixpanelApi().getPeople().set("Last Login Address", BentoNowUtils.getFullAddress());
        trackRevenue(0, mUser);
    }

    public static void trackRevenue(double iRevenue, User mUser) {
        JSONObject properties = new JSONObject();
        try {
            FacebookUtil.trackRevenue(iRevenue);
            properties.put("$time", BentoNowUtils.getMixpanelDate());
            getMixpanelApi().getPeople().identify(mUser.email);
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
