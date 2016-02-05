package com.bentonow.bentonow.dao;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.model.MealModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.gatekeeper.GateKeeperModel;
import com.bentonow.bentonow.parse.InitParse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by kokusho on 1/18/16.
 */
public class MenuDao {
    static final String TAG = "MenuDao";

    public static int min_version;
    static public int eta_min;
    static public int eta_max;
    static public GateKeeperModel gateKeeper = new GateKeeperModel();
    static public MealModel lunch = new MealModel();
    static public MealModel dinner = new MealModel();
    public static List<Menu> mListToday = new ArrayList<>();
    public static List<Menu> mListNextDay = new ArrayList<>();
    public static Menu mCurrentMenu;


    static public Menu getCurrentMenu() {
        if (mCurrentMenu == null) {
            try {
                mCurrentMenu = new Gson().fromJson(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.CURRENT_MENU), Menu.class);
            } catch (Exception ex) {
                DebugUtils.logError(TAG, "getCurrentMenu(): " + ex.toString());
            }
        }
        return mCurrentMenu;
    }

    static public void setCurrentMenu(Menu mMenu) {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.CURRENT_MENU, new Gson().toJson(mMenu));
        mCurrentMenu = mMenu;
    }

    static public Menu getNextMenu() {
        refreshDataNextDay();

        try {
            if (mListNextDay.isEmpty())
                return null;
            else
                return mListNextDay.get(0);

        } catch (Exception ignore) {
            DebugUtils.logError("getNext()", ignore.toString());
        }

        return null;
    }

    static public Menu getTodayMenu() {
        refreshDataToday();
        try {
            if (mListToday.isEmpty() || gateKeeper.getAppOnDemandWidget() == null)
                return null;
            else {
                for (int a = 0; a < mListToday.size(); a++)
                    if (mListToday.get(a).meal_name.equals(gateKeeper.getAppOnDemandWidget().getMealMode()))
                        return mListToday.get(a);
            }
        } catch (Exception ignore) {
            DebugUtils.logError("getNext()", ignore.toString());
        }
        return null;

    }


    static public String currentMenuType() {
        refreshMealsData();

        try {
            int dinnerTime = Integer.parseInt(dinner.startTime.replace(":", ""));
            int currentTime = Integer.parseInt(new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date()).replace(":", ""));

            return dinnerTime >= currentTime ? "lunch" : "dinner";
        } catch (Exception ignore) {
            DebugUtils.logError(ignore);
        }

        return "";
    }

    public static boolean hasNewMenus(ArrayList<String> aMenusId) {
        aMenusId.removeAll(getCurrentMenuIds());
        return !aMenusId.isEmpty();
    }

    public static ArrayList<String> getCurrentMenuIds() {
        ArrayList<String> aNewMenus = new ArrayList<>();

       /* if (gateKeeper.getAppOnDemandWidget() != null && gateKeeper.getAppOnDemandWidget().getMenu() != null)
            aNewMenus.add(gateKeeper.getAppOnDemandWidget().getMenu().meal_name);

        for (Menu mCurrentMenu : mListToday)
            aNewMenus.add(mCurrentMenu.meal_name);*/

        if (gateKeeper.getAvailableServices() != null && gateKeeper.getAvailableServices().mOrderAhead != null)
            for (Menu mOAMenu : gateKeeper.getAvailableServices().mOrderAhead.availableMenus)
                aNewMenus.add(mOAMenu.menu_id);

        DebugUtils.logDebug(TAG, "Menus: " + aNewMenus.toString());

        return aNewMenus;
    }


    public static boolean hasCurrentMenu(String sMenuId) {
        boolean bHasMenu = false;

        refreshDataToday();

        if (!mListToday.isEmpty())
            for (int a = 0; a < mListToday.size(); a++)
                if (mListToday.get(a).menu_id.equals(sMenuId))
                    bHasMenu = true;

        if (!bHasMenu && gateKeeper.getAvailableServices() != null && gateKeeper.getAvailableServices().mOrderAhead != null)
            for (int b = 0; b < gateKeeper.getAvailableServices().mOrderAhead.availableMenus.size(); b++)
                if (gateKeeper.getAvailableServices().mOrderAhead.availableMenus.get(b).menu_id.equals(sMenuId))
                    bHasMenu = true;

        return bHasMenu;
    }

    public static void refreshDataToday() {
        if (MenuDao.mListToday == null || MenuDao.mListToday.isEmpty())
            try {
                InitParse.parseMenu(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.MENU_TODAY), true);
            } catch (Exception ex) {
                DebugUtils.logDebug(TAG, "refreshData: " + ex.toString());
            }


    }

    public static void refreshDataNextDay() {
        if (MenuDao.mListNextDay == null || MenuDao.mListNextDay.isEmpty())
            try {
                InitParse.parseMenu(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.MENU_NEXT), false);
            } catch (Exception ex) {
                DebugUtils.logDebug(TAG, "refreshData: " + ex.toString());
            }


    }

    public static void refreshMealsData() {
        if (MenuDao.lunch == null || MenuDao.dinner == null)
            try {
                InitParse.parseMeals(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.MEALS));
            } catch (Exception ex) {
                DebugUtils.logDebug(TAG, "refreshData: " + ex.toString());
            }


    }

    public static Menu cloneMenu(Menu mMenu) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.fromJson(gson.toJson(mMenu), Menu.class);
    }

}
