package com.bentonow.bentonow.model;

import android.util.Log;

import com.bentonow.bentonow.BuildConfig;
import com.bentonow.bentonow.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class Shop {

    final static String TAG = "Shop";

    // Open / Closed
    public static String status;

    public static boolean isOpen () {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE);

        Log.i(TAG, status);

        try {
            JSONObject menu;

            // Check if there is a menu
            if (hour < Config.DinnerStartTime) {
                menu = getMenu(currentMenu, "lunch");

                // We only support custom lunch
                if (menu != null && !menu.getJSONObject("Menu").getString("menu_type").equals("custom"))
                    return false;
            } else {
                menu = getMenu(currentMenu, "dinner");

                // We only support custom lunch
                if (menu != null && !menu.getJSONObject("Menu").getString("menu_type").equals("custom"))
                    return false;
            }

            // This is only for debug the application
            if (BuildConfig.DEBUG) {
                if (hour < Config.LunchStartTime) {
                    return false;
                } else if (hour < Config.DinnerStartTime && hour > Config.LunchStartTime + 300) {
                    return false;
                }
            }

            return status.equals("open");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isSoldOut () {
        try {
            if (status.equals("sold out")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Menu
    public static JSONObject currentMenu;
    public static JSONObject nextMenu;

    public static JSONObject getCurrentMenu () {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE);

        if (hour < Config.DinnerStartTime) {
            return getMenu(currentMenu, "lunch");
        } else {
            return getMenu(currentMenu, "dinner");
        }
    }

    public static JSONArray getCurrentMenuItems () {
        try {
            JSONObject menu = getCurrentMenu();
            return (JSONArray) menu.get(Config.API_MENUITEMS_TAG);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONObject getNextMenu () {
        JSONObject menu = null;

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE);
        int timeBuffer = 100;

        // Adds a buffer if the store don't open on time
        if (hour < Config.LunchStartTime + timeBuffer) menu = getMenu(currentMenu, "lunch");
        if (hour < Config.DinnerStartTime + timeBuffer && menu == null) menu = getMenu(currentMenu, "dinner");
        if (menu == null) menu = getMenu(nextMenu, "lunch");
        if (menu == null) menu = getMenu(nextMenu, "dinner");

        return menu;
    }

    public static String getNextMenuType () {
        String menu = null;

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY) * 100 + calendar.get(Calendar.MINUTE);
        int timeBuffer = 100;

        // Adds a buffer if the store don't open on time
        if (hour < Config.LunchStartTime + timeBuffer && getMenu(currentMenu, "lunch") != null) menu = "Lunch";
        if (hour < Config.DinnerStartTime + timeBuffer && menu == null && getMenu(currentMenu, "dinner") != null) menu = "Dinner";
        if (menu == null && getMenu(nextMenu, "lunch") != null) menu = "Lunch";
        if (menu == null && getMenu(nextMenu, "dinner") != null) menu = "Dinner";

        return menu;
    }

    static JSONObject getMenu (JSONObject menu, String key) {
        try {
            return menu.getJSONObject(key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
