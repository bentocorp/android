package com.bentonow.bentonow.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;

public class Item {

    static final String TAG = "model.Item";

    public int itemId;
    public String name;
    public String description;
    public String type;
    public String image1;
    public int max_per_order;

    public boolean isSoldOut (boolean countCurrent) {
        boolean value = Stock.isSold(itemId, countCurrent);
        Log.i(TAG, "isSoldOut " + name + " " + (value ? "YES" : "NO"));
        return value;
    }

    public boolean canBeAdded () {
        boolean value = max_per_order > Order.countItemsById(itemId);
        Log.i(TAG, "canBeAdded " + name + " " + (value ? "YES" : "NO"));
        return value;
    }

    public static Item getFirstAvailable (String type, int[] tryExcludeIds) {
        Menu menu = Menu.get();

        if (menu != null) {
            if (tryExcludeIds != null) {
                String ids = Arrays.toString(tryExcludeIds);
                for (Item item : menu.items) {
                    if (!item.type.equals(type) || item.isSoldOut(true) || !item.canBeAdded() || ids.contains(""+item.itemId))
                        continue;
                    return item;
                }
            }

            for (Item item : menu.items) {
                if (!item.type.equals(type) || item.isSoldOut(true) || !item.canBeAdded())
                    continue;
                return item;
            }
        }

        return null;
    }

    public Item clone () {
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.fromJson(gson.toJson(this), Item.class);
    }
}
