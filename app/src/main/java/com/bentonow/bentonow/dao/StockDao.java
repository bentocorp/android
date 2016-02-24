package com.bentonow.bentonow.dao;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Stock;
import com.bentonow.bentonow.parse.InitParse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kokusho on 1/18/16.
 */
public class StockDao {
    static final String TAG = "StockDao";

    public static List<Stock> listStock = new ArrayList<>();

    public static boolean isSold(int itemId, boolean countCurrent) {
        refreshData();

        int iCurrent = countCurrent ? 1 : 0;
        DishDao mDishDao = new DishDao();


        for (int a = 0; a < listStock.size(); a++) {
            try {
                int iStockId = Integer.parseInt(listStock.get(a).itemId);
                int iStockQty = Integer.parseInt(listStock.get(a).qty);
                if (iStockId == itemId) {
                    int iCurrentStock = iStockQty - (mDishDao.countItemsById(itemId) + iCurrent);
                    if (iCurrentStock < 0) {
                        DebugUtils.logDebug(TAG, "isSold: " + itemId + ": " + " QTY:" + iStockQty + " Stock:" + iCurrentStock);
                        return true;
                    } else
                        return false;
                }
            } catch (Exception ex) {
                DebugUtils.logError(TAG, ex);
                return false;
            }
        }
        return true;
    }


    public static boolean isSold(String type) {
        refreshData();

        int sold = 0;
        int qty = 0;
        Menu menu = MenuDao.getCurrentMenu();

        if (menu != null) {
            for (DishModel dishModel : menu.dishModels) {
                if (!dishModel.type.equals(type)) continue;
                if (isSold(dishModel.itemId, true)) ++sold;
                ++qty;
            }
        }

        DebugUtils.logDebug(TAG, "isSold " + type + " qty: " + qty + " sold: " + sold);

        return sold >= qty;
    }

    public static boolean isSold() {
        refreshData();
        return isSold("main") || isSold("side");
    }

    public static void refreshData() {
        if (listStock == null || listStock.isEmpty()) {
            try {
                InitParse.parseStock(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.STATUS_ALL));
            } catch (Exception ex) {
                DebugUtils.logDebug(TAG, "refreshData: " + ex.toString());
            }
        }
    }
}
