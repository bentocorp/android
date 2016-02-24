package com.bentonow.bentonow.dao;

import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.parse.InitParse;

/**
 * Created by kokusho on 1/18/16.
 */
public class SettingsDao {
    static final String TAG = "SettingsDao";

    public static Settings mSettings;

    static public String[] getServiceArea() {
        refreshData();

        try {
            if (BentoNowUtils.getCurrentTime() < 163000)
                return mSettings.serviceArea_lunch.split(" ");
            else
                return mSettings.serviceArea_dinner.split(" ");

        } catch (Exception e) {
            DebugUtils.logError("getServiceArea()", e);
        }

        return new String[0];
    }

    public static void refreshData() {
        if (mSettings == null) {
            try {
                mSettings = new Settings();
                InitParse.parseSettings(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.SETTINGS));
            } catch (Exception ex) {
                DebugUtils.logDebug(TAG, "refreshData: " + ex.toString());
            }
        }
    }

    public static Settings getCurrent() {
        refreshData();
        return mSettings;
    }
}
