package com.bentonow.bentonow.Utils;

import android.content.Context;

import com.bentonow.bentonow.R;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

public class MixpanelUtils {

    public static MixpanelAPI mMixpanel;

    public static MixpanelAPI getInstance(Context context) {
        if (mMixpanel == null)
            mMixpanel = MixpanelAPI.getInstance(context, context.getString(R.string.mixpanel_api_key));

        return mMixpanel;
    }

    public static void track(Context context, String event) {
        getInstance(context).track(event);
    }

    public static void track(Context context, String event, JSONObject params) {
        getInstance(context).track(event, params);
    }
}
