package com.bentonow.bentonow.model;

import android.util.Log;

import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    static public void set(String data) {
        list = new ArrayList<>();
        JSONObject json = null;

        try {
            json = new JSONObject(data);
            setMenuWithString(json.getString("/menu/{date}"));
            Log.i(TAG, "menus: " + list.size());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        try {
            if (json != null) {
                setMenuWithString(json.getString("/menu/next/{date}"));
                Log.i(TAG, "menus next: " + list.size());
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    static void setMenuWithString(String data) {
        try {
            JSONObject menu = new JSONObject(data).getJSONObject("menus");
            Gson gson = new Gson();
            Menu row;

            if (menu.has("lunch")) {
                row = gson.fromJson(menu.getJSONObject("lunch").getString("Menu"), Menu.class);
                row.items = gson.fromJson(menu.getJSONObject("lunch").getString("MenuItems"), new TypeToken<List<Item>>() {
                }.getType());
                list.add(row);
            }

            if (menu.has("dinner")) {
                row = gson.fromJson(menu.getJSONObject("dinner").getString("Menu"), Menu.class);
                row.items = gson.fromJson(menu.getJSONObject("dinner").getString("MenuItems"), new TypeToken<List<Item>>() {
                }.getType());
                list.add(row);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

    }

    static public Menu get() {
        if (list != null) {
            int lunchTime = Integer.parseInt(Settings.lunch.startTime.replace(":", ""));
            int dinnerTime = Integer.parseInt(Settings.dinner.startTime.replace(":", ""));

            if (lunchTime <= BentoNowUtils.getCurrentTime() && dinnerTime > BentoNowUtils.getCurrentTime()) {
                // Try to get the lunch menu
                for (Menu menu : list) {
                    if (menu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate()) && menu.meal_name.equals("lunch"))
                        return menu;
                }

            } else if (dinnerTime <= BentoNowUtils.getCurrentTime() && 220000 >= BentoNowUtils.getCurrentTime()) {
                // Try to get the dinner menu
                for (Menu menu : list) {
                    if (menu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate()) && menu.meal_name.equals("dinner"))
                        return menu;
                }
            }

        }

        return null;
    }

    static public Menu getNext() {
        try {
            String sMenuType = Settings.currentMenuType();
            Calendar mToday = Calendar.getInstance();
            int lunchTime = Integer.parseInt(Settings.lunch.startTime.replace(":", ""));
            int dinnerTime = Integer.parseInt(Settings.dinner.startTime.replace(":", ""));

            if (list == null)
                return null;

            // Try to get the next menu
            if (mToday.get(Calendar.HOUR_OF_DAY) < 21) {
                if ((lunchTime + Settings.buffer_minutes * 100) <= BentoNowUtils.getCurrentTime() && (dinnerTime + Settings.buffer_minutes * 100) > BentoNowUtils.getCurrentTime()) {
                    // Try to get the lunch menu
                    for (Menu menu : list) {
                        if (menu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate()) && menu.meal_name.equals(sMenuType))
                            return menu;
                    }

                } else if ((dinnerTime + Settings.buffer_minutes * 100) <= BentoNowUtils.getCurrentTime() && 220000 >= BentoNowUtils.getCurrentTime()) {
                    // Try to get the dinner menu
                    for (Menu menu : list) {
                        if (menu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate()) && menu.meal_name.equals("dinner")) {
                            return menu;
                        }
                    }
                }

            }
            // Try to get the other day menu
            for (Menu menu : list) {
                if (!menu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate())) {
                    return menu;
                }
            }

        } catch (Exception ignore) {
            DebugUtils.logError("getNext()", ignore);
        }

        return null;
    }

}
