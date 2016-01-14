package com.bentonow.bentonow.model;

import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    static final String TAG = "model.Menu";
    public static List<Menu> list;

    public String name = "";
    public String for_date = "";
    public String menu_type = "";
    public String meal_type = "";
    public String meal_name = "";
    public String meal_order = "";
    public String day_text = "";
    public List<DishModel> dishModels;

    static public void set(String data) {
        list = new ArrayList<>();
        JSONObject json = null;

        try {
            json = new JSONObject(data);
            setMenuWithString(json.getString("/menu/{date}"));
            DebugUtils.logDebug(TAG, "menus: " + list.size());
        } catch (Exception e) {
            DebugUtils.logError(TAG, "/menu/{date}" + e.getMessage());
        }

        try {
            if (json != null) {
                setMenuWithString(json.getString("/menu/next/{date}"));
                DebugUtils.logDebug(TAG, "menus next: " + list.size());
            }
        } catch (Exception e) {
            DebugUtils.logError(TAG, "/menu/next/{date}" + e.getMessage());
        }

    }

    static void setMenuWithString(String data) {
        try {
            JSONObject menu = new JSONObject(data).getJSONObject("menus");
            Gson gson = new Gson();
            Menu row;

            if (menu != null && menu.has("lunch")) {
                JSONObject mLunch = menu.getJSONObject("lunch");
                if (mLunch.has("Menu")) {
                    row = gson.fromJson(mLunch.getString("Menu"), Menu.class);
                    if (mLunch.has("MenuItems")) {
                        row.dishModels = gson.fromJson(mLunch.getString("MenuItems"), new TypeToken<List<DishModel>>() {
                        }.getType());
                        list.add(row);
                    }

                }
            }

            if (menu != null && menu.has("dinner")) {
                JSONObject mLunch = menu.getJSONObject("dinner");
                if (mLunch.has("Menu")) {
                    row = gson.fromJson(mLunch.getString("Menu"), Menu.class);
                    if (mLunch.has("MenuItems")) {
                        row.dishModels = gson.fromJson(mLunch.getString("MenuItems"), new TypeToken<List<DishModel>>() {
                        }.getType());
                        list.add(row);
                    }

                }
            }
        } catch (Exception e) {
            DebugUtils.logError(TAG, "setMenuWithString():" + e.getMessage());
        }

    }

    static public Menu get() {
        if (list != null) {
            if (BentoNowUtils.getCurrentTime() >= 0 && BentoNowUtils.getCurrentTime() < 163000) {
                // Try to get the lunch menu
                for (Menu menu : list) {
                    if (menu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate()) && menu.meal_name.equals("lunch")) {
                        return menu;
                    }
                }

            } else if (163000 <= BentoNowUtils.getCurrentTime() && 240000 > BentoNowUtils.getCurrentTime()) {
                // Try to get the dinner menu
                for (Menu menu : list) {
                    if (menu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate()) && menu.meal_name.equals("dinner")) {
                        return menu;
                    }
                }
            }

        }

        return null;
    }

    static public Menu getNext() {
        try {
            String sMenuType = Settings.currentMenuType();
            int lunchTime = Integer.parseInt(Settings.lunch.startTime.replace(":", ""));
            int dinnerTime = Integer.parseInt(Settings.dinner.startTime.replace(":", ""));

            if (list == null)
                return null;

            if (Settings.status.equals("sold out")) {
                if (BentoNowUtils.getCurrentTime() < 163000) {
                    // Try to get the lunch menu
                    for (Menu menu : list) {
                        if (menu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate()))
                            return menu;
                    }

                } else if (163000 <= BentoNowUtils.getCurrentTime()) {
                    // Try to get the dinner menu
                    for (Menu menu : list) {
                        if (menu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate()) && menu.meal_name.equals(sMenuType))
                            return menu;
                    }
                }
            } else {
                // Try to get the next menu when close
                if ((lunchTime + Settings.buffer_minutes * 100) >= BentoNowUtils.getCurrentTime()) {
                    // Try to get the lunch menu
                    for (Menu menu : list) {
                        if (menu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate()))
                            return menu;
                    }

                } else if ((dinnerTime + Settings.buffer_minutes * 100) >= BentoNowUtils.getCurrentTime()) {
                    // Try to get the dinner menu
                    for (Menu menu : list) {
                        if (menu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate()) && menu.meal_name.equals(sMenuType)) {
                            return menu;
                        }
                    }
                }

                // Try to get the other day menu
                for (Menu menu : list) {
                    if (!menu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate())) {
                        return menu;
                    }
                }
            }

        } catch (Exception ignore) {
            DebugUtils.logError("getNext()", ignore);
        }

        return null;
    }

}
