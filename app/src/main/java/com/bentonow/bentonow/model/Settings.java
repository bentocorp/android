package com.bentonow.bentonow.model;

import android.location.Address;
import android.util.Log;

import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
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
    static String TAG = "Settings Model";

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
    static public double delivery_price;
    static public double price;
    static public double sale_price;
    static public double tax_percent;
    static public String tzName;
    static public int min_version;
    static public Meal lunch;
    static public Meal dinner;

    static public void set(String data) {
        try {
            JSONObject jsonSettings = new JSONObject(data).getJSONObject("settings");

            buffer_minutes = jsonSettings.getInt("buffer_minutes");
            geofence_order_radius_meters = jsonSettings.getInt("geofence_order_radius_meters");
            serviceArea_dinner = jsonSettings.getString("serviceArea_dinner");
            serviceArea_dinner_map = jsonSettings.getString("serviceArea_dinner_map");
            serviceArea_lunch = jsonSettings.getString("serviceArea_lunch");
            serviceArea_lunch_map = jsonSettings.getString("serviceArea_lunch_map");
            delivery_price = jsonSettings.getDouble("delivery_price");
            price = jsonSettings.getDouble("price");
            sale_price = jsonSettings.getDouble("sale_price");
            tax_percent = jsonSettings.getDouble("tax_percent");
            status = jsonSettings.getString("status");
            tzName = jsonSettings.getString("tzName");
            min_version = new JSONObject(data).getInt("android_min_version");

            DebugUtils.logDebug(TAG, "Settings: " + jsonSettings);

            JSONObject meal = new JSONObject(data).getJSONObject("meals");
            Gson gson = new Gson();

            lunch = gson.fromJson(meal.getString("2"), Meal.class);
            dinner = gson.fromJson(meal.getString("3"), Meal.class);
        } catch (Exception e) {
            DebugUtils.logError(TAG, "Set: " + e);
        }
    }

    static public String[] getServiceArea() {
        try {
            if (BentoNowUtils.getCurrentTime() < 163000)
                return serviceArea_lunch.split(" ");
            else
                return serviceArea_dinner.split(" ");

        } catch (Exception e) {
            DebugUtils.logError("getServiceArea()", e);
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
            DebugUtils.logError(ignore);
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

}
