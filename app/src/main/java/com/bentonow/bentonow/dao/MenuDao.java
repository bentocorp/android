package com.bentonow.bentonow.dao;

import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.model.MealModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.gatekeeper.GateKeeperModel;
import com.bentonow.bentonow.parse.InitParse;

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
    static public GateKeeperModel gateKeeper;
    static public MealModel lunch;
    static public MealModel dinner;
    public static List<Menu> list = new ArrayList<>();


    static public Menu get() {
        refreshData();

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
        refreshData();
        
        try {
            String sMenuType = currentMenuType();
            int lunchTime = Integer.parseInt(lunch.startTime.replace(":", ""));
            int dinnerTime = Integer.parseInt(dinner.startTime.replace(":", ""));

            if (list == null)
                return null;

            if (SettingsDao.getCurrent().status.equals("sold out")) {
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
                if ((lunchTime + SettingsDao.getCurrent().buffer_minutes * 100) >= BentoNowUtils.getCurrentTime()) {
                    // Try to get the lunch menu
                    for (Menu menu : list) {
                        if (menu.for_date.replace("-", "").equals(BentoNowUtils.getTodayDate()))
                            return menu;
                    }

                } else if ((dinnerTime + SettingsDao.getCurrent().buffer_minutes * 100) >= BentoNowUtils.getCurrentTime()) {
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


    static public String currentMenuType() {
        refreshData();

        try {
            int dinnerTime = Integer.parseInt(dinner.startTime.replace(":", ""));
            int currentTime = Integer.parseInt(new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date()).replace(":", ""));

            return dinnerTime >= currentTime ? "lunch" : "dinner";
        } catch (Exception ignore) {
            DebugUtils.logError(ignore);
        }

        return "";
    }

    public static void refreshData() {
        if (MenuDao.dinner == null || MenuDao.lunch == null)
            try {
                InitParse.parseSettings(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.MEALS));
            } catch (Exception ex) {
                DebugUtils.logDebug(TAG, "refreshData: " + ex.toString());
            }


    }


}
