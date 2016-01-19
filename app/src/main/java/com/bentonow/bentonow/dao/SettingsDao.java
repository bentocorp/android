package com.bentonow.bentonow.dao;

import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.parse.InitParse;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

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

    static public boolean isInServiceArea(LatLng location) {
        refreshData();

        if (location != null) {
            DebugUtils.logDebug(TAG, "inServiceArea: Location " + location.toString());
            String[] area = getServiceArea();
            List<LatLng> latLngList = new ArrayList<>();

            for (String row : area) {
                String[] latLng = row.split(",");
                latLngList.add(new LatLng(Double.valueOf(latLng[1]), Double.valueOf(latLng[0])));
            }

            DebugUtils.logDebug(TAG, "inServiceArea: Area " + latLngList.toString());

            return PolyUtil.containsLocation(location, latLngList, false);
        } else {
            DebugUtils.logDebug(TAG, "inServiceArea: Location null");
            return false;
        }
    }

    public static void refreshData() {
        if (mSettings == null) {
            try {
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
