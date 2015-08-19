package com.bentonow.bentonow.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Menu {
    static final String TAG = "model.Menu";
    public static List<Menu> list;

    public String name;
    public String for_date;
    public String menu_type;
    public String meal_type;
    public String meal_name;
    public String meal_order;
    public String day_text;
    public List<Item> items;

    static public void set (String data) {
        list = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(data);
            setMenuWithString(json.getString("/menu/{date}"));
            setMenuWithString(json.getString("/menu/next/{date}"));
            Log.i(TAG, "menus: " + list.size());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    static void setMenuWithString (String data) {
        try {
            JSONObject menu = new JSONObject(data).getJSONObject("menus");
            Gson gson = new Gson();
            Menu row;

            if (menu.has("lunch")) {
                row = gson.fromJson(menu.getJSONObject("lunch").getString("Menu"), Menu.class);
                row.items = gson.fromJson(menu.getJSONObject("lunch").getString("MenuItems"), new TypeToken<List<Item>>() {}.getType());
                list.add(row);
            }

            if (menu.has("dinner")) {
                row = gson.fromJson(menu.getJSONObject("dinner").getString("Menu"), Menu.class);
                row.items = gson.fromJson(menu.getJSONObject("dinner").getString("MenuItems"), new TypeToken<List<Item>>() {}.getType());
                list.add(row);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

    }

    static public Menu get () {
        if (list != null) {
            String type = Settings.currentMenuType();

            for (Menu menu : list) {
                if (!menu.for_date.replace("-", "").equals(getTodayDate()) || !menu.meal_name.equals(type))
                    continue;
                return menu;
            }
        }

        return null;
    }

    static public Menu getNext () {
        int lunchTime = Integer.parseInt(Settings.lunch.startTime.replace(":", ""));
        int dinnerTime = Integer.parseInt(Settings.dinner.startTime.replace(":", ""));
        int currentTime = Integer.parseInt(new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date()).replace(":", ""));

        if (list == null) return null;

        if ((lunchTime + Settings.buffer_minutes*100) > currentTime) {
            // Try to get the lunch menu
            for (Menu menu : list) {
                if (menu.for_date.replace("-", "").equals(getTodayDate()) || menu.meal_name.equals("lunch")) continue;
                return menu;
            }
            // Try to get the dinner menu
            for (Menu menu : list) {
                if (menu.for_date.replace("-", "").equals(getTodayDate()) || menu.meal_name.equals("dinner")) continue;
                return menu;
            }
        } else if ((dinnerTime + Settings.buffer_minutes*100) > currentTime) {
            // Try to get the dinner menu
            for (Menu menu : list) {
                if (menu.for_date.replace("-", "").equals(getTodayDate()) || menu.meal_name.equals("dinner")) continue;
                return menu;
            }
        }

        // Try to get the lunch menu
        for (Menu menu : list) {
            if (!menu.for_date.replace("-", "").equals(getTodayDate()) || menu.meal_name.equals("lunch")) continue;
            return menu;
        }

        // Try to get the lunch menu
        for (Menu menu : list) {
            if (!menu.for_date.replace("-", "").equals(getTodayDate()) || menu.meal_name.equals("dinner")) continue;
            return menu;
        }

        return null;
    }

    static public String getTodayDate () {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }
}
