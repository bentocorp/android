package com.bentonow.bentonow.model;

import android.util.Log;

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
        int min = countCurrent ? 0 : -1;
        for (Stock stock : list) {
            if (stock.itemId == itemId && stock.qty > 0 && (stock.qty - Order.countItemsById(itemId)) > min)
                return false;
        }

        return true;
    }

    public static boolean isSold(String type) {
        int sold = 0;
        int qty = 0;
        Menu menu = Menu.get();

        if (menu != null) {
            for (Item item : menu.items) {
                if (!item.type.equals(type)) continue;
                if (isSold(item.itemId, true)) ++sold;
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
