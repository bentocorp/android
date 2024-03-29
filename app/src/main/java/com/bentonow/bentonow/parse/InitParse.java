package com.bentonow.bentonow.parse;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.dao.SettingsDao;
import com.bentonow.bentonow.dao.StockDao;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.MealModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.OrderAheadModel;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.Stock;
import com.bentonow.bentonow.model.gatekeeper.AppOnDemandWidgetModel;
import com.bentonow.bentonow.model.gatekeeper.Hash;
import com.bentonow.bentonow.model.menu.TimesModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kokusho on 1/18/16.
 */
public class InitParse extends MainParser {
    private static final String TAG = "InitParse";
    private static final String timeForce = "12:40:00";

    public static void parseInitTwo(String sData) {
        init = System.currentTimeMillis();

        String sStatusAll;
        String sIosCopy;
        String sMenuToday;
        String sMenuNextDay;
        String sGateKeeper;
        String sSettings;
        String sMeals;
        JSONObject jsonInit;

        try {
            jsonInit = new JSONObject(sData);
        } catch (Exception ex) {
            DebugUtils.logDebug(TAG, ex);
            return;
        }

        try {
            sStatusAll = jsonInit.getJSONObject("/status/all").getString("menu");
            parseStock(sStatusAll);
        } catch (Exception e) {
            DebugUtils.logError(TAG, "New Stock:" + e.toString());
        }

        try {
            sIosCopy = jsonInit.getString("/ioscopy");
            parseIosCopy(sIosCopy);
        } catch (Exception e) {
            DebugUtils.logError(TAG, "Ios Copy: " + e.toString());
        }

        MenuDao.mListToday.clear();

        try {
            sMenuToday = jsonInit.getString("/menu/{date}");
            parseMenu(sMenuToday, true);
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.MENU_TODAY, sMenuToday);
            DebugUtils.logDebug(TAG, "/menu/{date}: " + MenuDao.mListToday.size());
        } catch (Exception e) {
            DebugUtils.logError(TAG, "/menu/{date}" + e.getMessage());
        }

        try {
            sMenuNextDay = jsonInit.getString("/menu/next/{date}");
            parseMenu(sMenuNextDay, false);
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.MENU_NEXT, sMenuNextDay);
            DebugUtils.logDebug(TAG, "/menu/next/{date}: " + MenuDao.mListToday.size());
        } catch (Exception e) {
            DebugUtils.logError(TAG, "/menu/next/{date}" + e.getMessage());
        }

        try {
            sGateKeeper = jsonInit.getString("/gatekeeper/here/{lat}/{long}");
            parseGateKeeper(sGateKeeper);
        } catch (Exception e) {
            DebugUtils.logError(TAG, "/gatekeeper/here/{lat}/{long}: " + e.getMessage());
        }

        try {
            sSettings = new JSONObject(sData).getString("settings");
            parseSettings(sSettings);
        } catch (Exception e) {
            DebugUtils.logError(TAG, "Settings: " + e.toString());
        }

        try {
            MenuDao.min_version = new JSONObject(sData).getInt("android_min_version");
        } catch (Exception ex) {
            DebugUtils.logError(TAG, "Min Version: " + ex.toString());
        }


        try {
            JSONObject jsonEta = new JSONObject(sData).getJSONObject("eta");
            MenuDao.eta_min = jsonEta.getInt("eta_min");
            MenuDao.eta_max = jsonEta.getInt("eta_max");
        } catch (Exception ex) {
            DebugUtils.logError(TAG, "Eta: " + ex.toString());
        }

        try {
            sMeals = new JSONObject(sData).getString("meals");
            parseMeals(sMeals);
        } catch (Exception e) {
            DebugUtils.logError(TAG, "Meals: " + e.toString());
        }

        now = System.currentTimeMillis();
        DebugUtils.logDebug("Parse en :: " + (now - init) + " ms");
    }

    public static void parseStock(String sData) {
        if (sData == null || sData.equals("null") || sData.isEmpty())
            return;

        Gson gson = new Gson();

        StockDao.listStock = gson.fromJson(sData, new TypeToken<List<Stock>>() {
        }.getType());

        if (StockDao.listStock != null)
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STATUS_ALL, sData);

        DebugUtils.logDebug(TAG, "New Stock: " + StockDao.listStock.size());
    }

    public static void parseOutOfStock(String sData) {
        try {
            JSONObject json = new JSONObject(sData);

            if (json.has("MenuStatus")) {
                sData = json.getString("MenuStatus");
            } else {
                sData = json.getJSONObject("/status/all").getString("menu");
            }

            parseStock(sData);

        } catch (Exception e) {
            DebugUtils.logError(TAG, e.getMessage());
            e.printStackTrace();
        }

        DebugUtils.logDebug(TAG, "New Stock: " + StockDao.listStock.size());
    }

    public static void parseIosCopy(String sIosCopy) {
        if (sIosCopy == null || sIosCopy.equals("null") || sIosCopy.isEmpty())
            return;

        Gson gson = new Gson();
        IosCopyDao.listBackEnd = gson.fromJson(sIosCopy, new TypeToken<List<BackendText>>() {
        }.getType());

        if (IosCopyDao.listBackEnd != null)
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.BACKENDTEXT, sIosCopy);

        DebugUtils.logDebug(TAG, "Ios Copy: " + IosCopyDao.listBackEnd.size());

    }

    public static void parseSettings(String sSettings) {
        try {
            JSONObject jsonSettings = new JSONObject(sSettings);

            SettingsDao.mSettings = new Settings();

            SettingsDao.mSettings.buffer_minutes = jsonSettings.getInt("buffer_minutes");
            SettingsDao.mSettings.geofence_order_radius_meters = jsonSettings.getInt("geofence_order_radius_meters");
            SettingsDao.mSettings.serviceArea_dinner = jsonSettings.getString("serviceArea_dinner");
            SettingsDao.mSettings.serviceArea_dinner_map = jsonSettings.getString("serviceArea_dinner_map");
            SettingsDao.mSettings.serviceArea_lunch = jsonSettings.getString("serviceArea_lunch");
            SettingsDao.mSettings.serviceArea_lunch_map = jsonSettings.getString("serviceArea_lunch_map");
            SettingsDao.mSettings.delivery_price = jsonSettings.getDouble("delivery_price");
            SettingsDao.mSettings.price = jsonSettings.getDouble("price");
            SettingsDao.mSettings.sale_price = jsonSettings.getDouble("sale_price");
            SettingsDao.mSettings.tax_percent = jsonSettings.getDouble("tax_percent");
            SettingsDao.mSettings.tzName = jsonSettings.getString("tzName");
            SettingsDao.mSettings.pod_mode = jsonSettings.getString("pod_mode");
            SettingsDao.mSettings.oa_countdown_remaining_mins = jsonSettings.getString("oa_countdown_remaining_mins");


        } catch (Exception ex) {
            DebugUtils.logError(TAG, "parseSettings: " + ex.toString());
        }

        if (SettingsDao.mSettings != null)
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.SETTINGS, sSettings);
    }

    public static void parseMeals(String sMeals) {
        if (sMeals == null || sMeals.equals("null") || sMeals.isEmpty())
            return;

        try {
            JSONObject jsonMeal = new JSONObject(sMeals);
            Gson gson = new Gson();

            MenuDao.lunch = gson.fromJson(jsonMeal.getString("2"), MealModel.class);
            MenuDao.dinner = gson.fromJson(jsonMeal.getString("3"), MealModel.class);

            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.MEALS, sMeals);

        } catch (Exception ex) {
            DebugUtils.logError(TAG, "parseMeals: " + ex.toString());
        }

    }

    public static void parseMenu(String data, boolean bIsToday) {
        if (data == null || data.equals("null") || data.isEmpty())
            return;

        try {
            JSONObject menu = new JSONObject(data).getJSONObject("menus");
            Gson gson = new Gson();
            Menu row;

            if (parseSection(menu, "lunch")) {
                JSONObject mLunch = menu.getJSONObject("lunch");
                if (mLunch != null && mLunch.has("Menu")) {
                    row = gson.fromJson(mLunch.getString("Menu"), Menu.class);

                    if (parseSection(mLunch, "MenuItems"))
                        row.dishModels = gson.fromJson(mLunch.getString("MenuItems"), new TypeToken<List<DishModel>>() {
                        }.getType());

                    if (parseSection(mLunch, "OAOnlyItems"))
                        row.oaItems = gson.fromJson(mLunch.getString("OAOnlyItems"), new TypeToken<List<DishModel>>() {
                        }.getType());

                    if (bIsToday)
                        MenuDao.mListToday.add(row);
                    else
                        MenuDao.mListNextDay.add(row);
                }
            }

            if (parseSection(menu, "dinner")) {
                JSONObject mDinner = menu.getJSONObject("dinner");
                if (parseSection(mDinner, "Menu")) {
                    row = gson.fromJson(mDinner.getString("Menu"), Menu.class);
                    if (parseSection(mDinner, "MenuItems"))
                        row.dishModels = gson.fromJson(mDinner.getString("MenuItems"), new TypeToken<List<DishModel>>() {
                        }.getType());

                    if (parseSection(mDinner, "OAOnlyItems"))
                        row.oaItems = gson.fromJson(mDinner.getString("OAOnlyItems"), new TypeToken<List<DishModel>>() {
                        }.getType());

                    if (bIsToday)
                        MenuDao.mListToday.add(row);
                    else
                        MenuDao.mListNextDay.add(row);

                }
            }

        } catch (Exception e) {
            DebugUtils.logError(TAG, "parseMenu(): " + e.getMessage());
        }

    }

    public static void parseGateKeeper(String sGateKeeper) {
        if (sGateKeeper == null || sGateKeeper.equals("null") || sGateKeeper.isEmpty())
            return;

        try {
            JSONObject jsonGateKeeper = new JSONObject(sGateKeeper);
            Gson gson = new Gson();

            if (jsonGateKeeper.has("isInAnyZone"))
                MenuDao.gateKeeper.setIsInAnyZone(jsonGateKeeper.getBoolean("isInAnyZone"));
            if (jsonGateKeeper.has("hasService"))
                MenuDao.gateKeeper.setHasServices(jsonGateKeeper.getBoolean("hasService"));
            if (jsonGateKeeper.has("appState"))
                MenuDao.gateKeeper.setAppState(jsonGateKeeper.getString("appState"));
            if (jsonGateKeeper.has("CurrentMealType"))
                MenuDao.gateKeeper.setCurrentMealType(jsonGateKeeper.getString("CurrentMealType"));

            if (parseSection(jsonGateKeeper, "MyZones")) {
                JSONObject jsonMyZones = new JSONObject(jsonGateKeeper.getString("MyZones"));

                if (jsonMyZones.has("OnDemand"))
                    MenuDao.gateKeeper.setOnDemand(jsonMyZones.getBoolean("OnDemand"));

                if (jsonMyZones.has("OrderAhead"))
                    MenuDao.gateKeeper.setOrderAhead(jsonMyZones.getBoolean("OrderAhead"));
            }

            if (parseSection(jsonGateKeeper, "MealTypes")) {
                JSONObject jsonMealType = new JSONObject(jsonGateKeeper.getString("MealTypes"));

                if (parseSection(jsonMealType, "hash")) {
                    JSONObject jsonHash = new JSONObject(jsonMealType.getString("hash"));

                    if (jsonHash.has("2")) {
                        MenuDao.gateKeeper.getMealTypes().setTwo(gson.fromJson(jsonHash.getString("2"), Hash.class));
                        //  MenuDao.gateKeeper.getMealTypes().getTwo().setOaCutoff(timeForce);
                    }
                    if (jsonHash.has("3")) {
                        MenuDao.gateKeeper.getMealTypes().setThree(gson.fromJson(jsonHash.getString("3"), Hash.class));
                        //  MenuDao.gateKeeper.getMealTypes().getTwo().setOaCutoff(timeForce);
                    }
                }

                if (jsonMealType.has("ordering"))
                    MenuDao.gateKeeper.getMealTypes().setOrdering((ArrayList<String>) gson.fromJson(jsonMealType.getString("ordering"), new TypeToken<ArrayList<String>>() {
                    }.getType()));
            }

            if (parseSection(jsonGateKeeper, "appOnDemandWidget")) {
                JSONObject jsonOnDemandWidget = new JSONObject(jsonGateKeeper.getString("appOnDemandWidget"));
                MenuDao.gateKeeper.setAppOnDemandWidget(gson.fromJson(jsonGateKeeper.getString("appOnDemandWidget"), AppOnDemandWidgetModel.class));
                MenuDao.gateKeeper.getAppOnDemandWidget().setMenu(null);

                if (parseSection(jsonOnDemandWidget, "menuPreview")) {
                    try {
                        JSONObject jsonMenuPreview = new JSONObject(jsonOnDemandWidget.getString("menuPreview"));
                        if (jsonMenuPreview.has("Menu")) {
                            Menu mMenuPreview = gson.fromJson(jsonMenuPreview.getString("Menu"), Menu.class);
                            if (jsonMenuPreview.has("MenuItems")) {
                                mMenuPreview.dishModels = gson.fromJson(jsonMenuPreview.getString("MenuItems"), new TypeToken<List<DishModel>>() {
                                }.getType());
                                MenuDao.gateKeeper.getAppOnDemandWidget().setMenu(mMenuPreview);
                            }

                        }
                    } catch (Exception ex) {
                        DebugUtils.logDebug(TAG, ex);
                    }
                }
            }

            if (parseSection(jsonGateKeeper, "AvailableServices")) {
                JSONObject jsonServices = new JSONObject(jsonGateKeeper.getString("AvailableServices"));

                if (jsonServices.has("OnDemand"))
                    MenuDao.gateKeeper.getAvailableServices().OnDemand = (jsonServices.getBoolean("OnDemand"));

                if (parseSection(jsonServices, "OrderAhead")) {
                    OrderAheadModel mOrderAhead = new OrderAheadModel();
                    JSONObject jsonOrderAhead = new JSONObject(jsonServices.getString("OrderAhead"));

                    mOrderAhead.kitchen = jsonOrderAhead.getString("kitchen");
                    mOrderAhead.zone = jsonOrderAhead.getString("zone");
                    mOrderAhead.title = jsonOrderAhead.getString("title");

                    if (parseSection(jsonOrderAhead, "availableMenus")) {
                        JSONObject jsonAvailableMenus = new JSONObject(jsonOrderAhead.getString("availableMenus"));

                        if (jsonAvailableMenus.has("menus")) {
                            JSONArray jsonMenus = jsonAvailableMenus.getJSONArray("menus");

                            for (int a = 0; a < jsonMenus.length(); a++) {
                                JSONObject jsonMenu = jsonMenus.getJSONObject(a);
                                if (jsonMenu.has("Menu")) {
                                    Menu mMenu = gson.fromJson(jsonMenu.getString("Menu"), Menu.class);
                                    if (jsonMenu.has("MenuItems"))
                                        mMenu.dishModels = gson.fromJson(jsonMenu.getString("MenuItems"), new TypeToken<List<DishModel>>() {
                                        }.getType());
                                    if (jsonMenu.has("Times")) {
                                        ArrayList<TimesModel> listTime = gson.fromJson(jsonMenu.getString("Times"), new TypeToken<List<TimesModel>>() {
                                        }.getType());
                                        for (int b = 0; b < listTime.size(); b++) {
                                            if (listTime.get(b).available)
                                                mMenu.listTimeModel.add(listTime.get(b));
                                        }
                                    }
                                    if (jsonMenu.has("DefaultTimeMode"))
                                        mMenu.DefaultTimeMode = jsonMenu.getString("DefaultTimeMode");

                                    mOrderAhead.availableMenus.add(mMenu);
                                }
                            }
                        }
                    }

                    MenuDao.gateKeeper.getAvailableServices().mOrderAhead = mOrderAhead;
                }


            }

        } catch (Exception ex) {
            DebugUtils.logError(TAG, "GateKeeper Error: " + ex.toString());
        }

        DebugUtils.logDebug(TAG, "GateKeeper Saved: " + (MenuDao.gateKeeper != null));
    }

    public static String getAppState(String sResponse) {
        JSONObject jsonInit;

        try {
            jsonInit = new JSONObject(sResponse);

            String sGateKeeper = jsonInit.getString("/gatekeeper/here/{lat}/{long}");

            if (sGateKeeper == null || sGateKeeper.equals("null") || sGateKeeper.isEmpty())
                return "";

            JSONObject jsonGateKeeper = new JSONObject(sGateKeeper);

            if (jsonGateKeeper.has("appState"))
                return jsonGateKeeper.getString("appState");

        } catch (Exception e) {
            DebugUtils.logError(TAG, "getAppState: " + e.getMessage());
        }
        return "";
    }
}
