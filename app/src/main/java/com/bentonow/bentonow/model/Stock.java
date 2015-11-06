package com.bentonow.bentonow.model;

import android.util.Log;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Stock {
    static final String TAG = "model.Stock";
    static List<Stock> list;

    public int itemId;
    public int qty;

    public static void set(String data) {
        list = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(data);

            if (json.has("MenuStatus")) {
                data = json.getString("MenuStatus");
            } else {
                data = json.getJSONObject("/status/all").getString("menu");
            }
            Gson gson = new Gson();
            list = gson.fromJson(data, new TypeToken<List<Stock>>() {
            }.getType());
            Log.i(TAG, "stock: " + list.size());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean isSold(int itemId, boolean countCurrent) {
        int iCurrent = countCurrent ? 1 : 0;

        for (Stock stock : list) {
            if (stock.itemId == itemId) {
                int iCurrentStock = stock.qty - (Order.countItemsById(itemId) + iCurrent);
                if (iCurrentStock < 0) {
                    DebugUtils.logDebug(TAG, "isSold: " + itemId + ": " + " QTY:" + stock.qty + " Stock:" + iCurrentStock);
                    return true;
                } else
                    return false;
            }
        }
        return false;
    }


    public static boolean isSold(String type) {
        int sold = 0;
        int qty = 0;
        Menu menu = Menu.get();

        if (menu != null) {
            for (DishModel dishModel : menu.dishModels) {
                if (!dishModel.type.equals(type)) continue;
                if (isSold(dishModel.itemId, true)) ++sold;
                ++qty;
            }
        }

        Log.i(TAG, "isSold " + type + " qty: " + qty + " sold: " + sold);

        return sold >= qty;
    }

    public static boolean isSold() {
        return isSold("main") || isSold("side");
    }
}
