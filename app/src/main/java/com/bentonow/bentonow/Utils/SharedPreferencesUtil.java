/**
 * @author Kokusho Torres
 * 19/09/2014
 */
package com.bentonow.bentonow.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.bentonow.bentonow.controllers.BentoApplication;

public class SharedPreferencesUtil {

    private static final String SHARED_PROJECT = "SharedPreferencesBento";

    public static final String APP_FIRST_RUN = "app_first_run"; //String
    public static final String STORE_STATUS = "store_status"; //String
    public static final String LOCATION = "location"; //String
    public static final String ADDRESS = "address"; //String
    public static final String UUID_BENTO = "UUID_BENTO"; //String
    public static final String USER_NAME = "USER_NAME"; //String
    public static final String POD_MODE = "POD_MODE"; //String
    public static final String IS_ORDER_AHEAD_MENU = "IS_ORDER_AHEAD_MENU"; //boolean
    public static final String IS_ORDER_SOLD_OUT = "IS_ORDER_SOLD_OUT"; //boolean
    public static final String IS_APP_IN_FRONT = "IS_THE_APP_IN_FRONT"; //boolean
    public static final String CURRENT_USER_ID = "CURRENT_USER_ID"; //long
    public static final String CURRENT_MENU = "CURRENT_MENU"; //String
    public static final String ON_DEMAND_AVAILABLE = "ON_DEMAND_AVAILABLE"; //boolean
    public static final String ON_CONTINUE_FROM_ADD_ON = "ON_CONTINUE_FROM_ADD_ON"; //boolean
    public static final String CLEAR_ORDERS_FROM_SUMMARY = "CLEAR_ORDERS_FROM_SUMMARY"; //boolean
    public static final String ENABLE_BUILD_BENTO_CLICK = "ENABLE_BUILD_BENTO_CLICK"; //boolean
    public static final String ENABLE_SETTINGS_CLICK = "ENABLE_SETTINGS_CLICK"; //boolean
    public static final String ENABLE_ERROR_CLICK = "ENABLE_ERROR_CLICK"; //boolean
    public static final String SHOW_NOTIFICATIONS = "SHOW_NOTIFICATIONS"; //boolean
    public static final String ALREADY_SHOW_NOTIFICATIONS = "ALREADY_SHOW_NOTIFICATIONS"; //boolean

    //DATA FROM SERVER
    public static final String BACKENDTEXT = "backendText"; //String
    public static final String STATUS_ALL = "status_all"; //String
    public static final String SETTINGS = "settings"; //String
    public static final String MEALS = "meals"; //String
    public static final String MENU_TODAY = "menu_today"; //String
    public static final String MENU_NEXT = "menu_next"; //String
    public static final String GATE_KEEPER = "gate_keeper"; //String


    /**
     * Method that saves a String in Shared Preference
     *
     * @param key   The String that is going to be the key
     * @param value The String that is going to be saved
     */
    public static void setAppPreference(String key, String value) {
        SharedPreferences manager = BentoApplication.instance.getSharedPreferences(SHARED_PROJECT, 0);
        Editor editor = manager.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * Method that saves a Integer in Shared Preference
     *
     * @param key   The String that is going to be the key
     * @param value The Integer that is going to be saved
     */
    public static void setAppPreference(String key, int value) {
        SharedPreferences manager = BentoApplication.instance.getSharedPreferences(SHARED_PROJECT, 0);
        Editor editor = manager.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * Method that saves a Boolean in Shared Preference
     *
     * @param key   The String that is going to be the key
     * @param value The String that is going to be saved
     */
    public static void setAppPreference(String key, boolean value) {
        SharedPreferences manager = BentoApplication.instance.getSharedPreferences(SHARED_PROJECT, 0);
        Editor editor = manager.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * Method that saves a Long in Shared Preference
     *
     * @param key   The String that is going to be the key
     * @param value The Long that is going to be saved
     */
    public static void setAppPreference(String key, long value) {
        SharedPreferences manager = BentoApplication.instance.getSharedPreferences(SHARED_PROJECT, 0);
        Editor editor = manager.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    /**
     * Method that saves a Float in Shared Preference
     *
     * @param key   The String that is going to be the key
     * @param value The Long that is going to be saved
     */
    public static void setAppPreference(String key, float value) {
        SharedPreferences manager = BentoApplication.instance.getSharedPreferences(SHARED_PROJECT, 0);
        Editor editor = manager.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    /**
     * Method that returns a String from Shared Preference
     *
     * @param key The String that is going to be the key
     * @return String The string that was saved with the key
     */
    public static String getStringPreference(String key) {
        SharedPreferences manager = BentoApplication.instance.getSharedPreferences(SHARED_PROJECT, 0);
        return manager.getString(key, "");
    }

    /**
     * Method that returns a Integer from Shared Preference
     *
     * @param key The String that is going to be the key
     * @return The Integer that was saved with the key
     */
    public static int getIntPreference(String key) {
        SharedPreferences manager = BentoApplication.instance.getSharedPreferences(SHARED_PROJECT, 0);
        return manager.getInt(key, 0);
    }

    /**
     * Method that returns a Long from Shared Preference
     *
     * @param key The String that is going to be the key
     * @return The Long that was saved with the key
     */
    public static long getLongPreference(String key) {
        SharedPreferences manager = BentoApplication.instance.getSharedPreferences(SHARED_PROJECT, 0);
        return manager.getLong(key, 0);
    }

    /**
     * Method that returns a Long from Shared Preference
     *
     * @param key The String that is going to be the key
     * @return The Float that was saved with the key
     */
    public static float getFloatPreference(String key) {
        SharedPreferences manager = BentoApplication.instance.getSharedPreferences(SHARED_PROJECT, 0);
        return manager.getFloat(key, 0);
    }

    /**
     * Method that returns a Boolean from Shared Preference
     *
     * @param key The String that is going to be the key
     * @return The Boolean that was saved with the key
     */
    public static boolean getBooleanPreference(String key) {
        SharedPreferences manager = BentoApplication.instance.getSharedPreferences(SHARED_PROJECT, 0);
        return manager.getBoolean(key, false);
    }


    public static void clearAllPreferences() {
        SharedPreferences manager = BentoApplication.instance.getSharedPreferences(SHARED_PROJECT, 0);
        Editor editor = manager.edit();
        editor.clear();
        editor.commit();
    }

    public static SharedPreferences getPreferences(Context context, String app) {
        return context.getSharedPreferences(app, Context.MODE_PRIVATE);
    }
}
