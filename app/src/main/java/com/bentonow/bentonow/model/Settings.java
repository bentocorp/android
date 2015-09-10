package com.bentonow.bentonow.model;

import android.content.SharedPreferences;
import android.location.Address;
import android.util.Log;

import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Settings {
    static String TAG = "model.Settings";

    public class Meal {
        public int order;
        public String name;
        public String startTime;
    }

    static public int buffer_minutes;
    static public int geofence_order_radius_meters;
    static public String serviceArea_dinner;
    static public String serviceArea_dinner_map;
    static public String serviceArea_lunch;
    static public String serviceArea_lunch_map;
    static public String status = "";
    static public String tzName;
    static public int min_version;
    static public Meal lunch;
    static public Meal dinner;
    static public String prefs_name = "BentoPrefs";
    static public SharedPreferences prefs;

    static public void set(String data) {
        try {
            JSONObject settings = new JSONObject(data).getJSONObject("settings");

            buffer_minutes = settings.getInt("buffer_minutes");
            geofence_order_radius_meters = settings.getInt("geofence_order_radius_meters");
            serviceArea_dinner = settings.getString("serviceArea_dinner");
            serviceArea_dinner_map = settings.getString("serviceArea_dinner_map");
            serviceArea_lunch = settings.getString("serviceArea_lunch");
            serviceArea_lunch_map = settings.getString("serviceArea_lunch_map");
            status = settings.getString("status");
            tzName = settings.getString("tzName");
            min_version = new JSONObject(data).getInt("android_min_version");

            JSONObject meal = new JSONObject(data).getJSONObject("meals");
            Gson gson = new Gson();

            lunch = gson.fromJson(meal.getString("2"), Meal.class);
            dinner = gson.fromJson(meal.getString("3"), Meal.class);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    static public String[] getServiceArea() {
        try {
            int dinnerTime = Integer.parseInt(Settings.dinner.startTime.replace(":", ""));
            int currentTime = Integer.parseInt(new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date()).replace(":", ""));

            if (dinnerTime >= currentTime) {
                return serviceArea_dinner.split(" ");
            } else {
                return serviceArea_lunch.split(" ");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        return new String[0];
    }

    static public boolean isInServiceArea(LatLng location) {
        if (location != null) {
            Log.i(TAG, "inServiceArea: Location " + location.toString());
            String[] area = getServiceArea();
            List<LatLng> latLngList = new ArrayList<>();

            for (String row : area) {
                String[] latLng = row.split(",");
                latLngList.add(new LatLng(Double.valueOf(latLng[1]), Double.valueOf(latLng[0])));
            }

            Log.i(TAG, "inServiceArea: Area " + latLngList.toString());

            return PolyUtil.containsLocation(location, latLngList, false);
        } else {
            Log.i(TAG, "inServiceArea: Location null");
            return false;
        }
    }

    static public String currentMenuType() {
        try {
            int dinnerTime = Integer.parseInt(Settings.dinner.startTime.replace(":", ""));
            int currentTime = Integer.parseInt(new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date()).replace(":", ""));

            return dinnerTime >= currentTime ? "lunch" : "dinner";
        } catch (Exception ignore) {
        }

        return "";
    }

    public static void load() {
        String user = SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.USER);
        String location = SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.LOCATION);
        String address = SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.ADDRESS);
        String backendText = SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.BACKENDTEXT);

        if (User.current == null && !user.isEmpty())
            User.current = new Gson().fromJson(user, User.class);
        if (Order.location == null && !location.isEmpty())
            Order.location = new Gson().fromJson(location, LatLng.class);
        if (Order.address == null && !address.isEmpty())
            Order.address = new Gson().fromJson(address, Address.class);
        if (BackendText.list.size() == 0 && !backendText.isEmpty())
            BackendText.set(backendText);
    }

    public static void save() {
        Gson gson = new Gson();
        String user = "";
        String location = "";
        String address = "";
        String backendText = gson.toJson(BackendText.list);

        if (User.current != null)
            user = gson.toJson(User.current);
        if (Order.location != null)
            location = gson.toJson(Order.location);
        if (Order.address != null)
            address = gson.toJson(Order.address);

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.USER, user);
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.LOCATION, location);
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ADDRESS, address);

        if (BackendText.list.size() > 0)
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.BACKENDTEXT, backendText);
    }

    public static void updateUser(User mUserInfo) {
        Gson gson = new Gson();
        String user;

        if (User.current != null) {
            User.current.coupon_code = mUserInfo.coupon_code;
            user = gson.toJson(User.current);
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.USER, user);
        }


    }
}
