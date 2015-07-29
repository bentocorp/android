package com.bentonow.bentonow.Utils;

import android.content.Context;

import com.bentonow.bentonow.R;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

public class Mixpanel {
    public static MixpanelAPI getInstance (Context context) {
        return MixpanelAPI.getInstance(context, context.getString(R.string.mixpanel_api_key));
    }

    public static void track (Context context, String event) {
        getInstance(context).track(event);
    }

    public static void track (Context context, String event, JSONObject params) {
        getInstance(context).track(event, params);
    }
}
