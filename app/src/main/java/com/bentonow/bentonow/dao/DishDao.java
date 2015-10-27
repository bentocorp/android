package com.bentonow.bentonow.dao;

import android.util.Log;

import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.Stock;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jose Torres on 27/09/15.
 */
public class DishDao {
    static final String TAG = "DishModel";


    public static boolean isSoldOut(DishModel mDishModel, boolean countCurrent) {
        boolean value = Stock.isSold(mDishModel.itemId, countCurrent);
        Log.i(TAG, "isSoldOut " + mDishModel.name + " " + (value ? "YES" : "NO"));
        return value;
    }

    public static boolean canBeAdded(DishModel mDishModel) {
        boolean value = mDishModel.max_per_order > Order.countItemsById(mDishModel.itemId);
        Log.i(TAG, "canBeAdded " + mDishModel.name + " " + (value ? "YES" : "NO"));
        return value;
    }

    public static DishModel getFirstAvailable(String type, int[] tryExcludeIds) {
        Menu menu = Menu.get();

        if (menu != null) {
            List<DishModel> aDishes = new ArrayList<>();

            for (int a = 0; a < menu.dishModels.size(); a++) {
                aDishes.add(DishDao.clone(menu.dishModels.get(a)));
            }

            Collections.shuffle(aDishes);

            if (tryExcludeIds != null) {
                String ids = Arrays.toString(tryExcludeIds);

                for (DishModel dishModel : aDishes) {
                    if (dishModel.type.equals(type) && !DishDao.isSoldOut(dishModel, true) && DishDao.canBeAdded(dishModel) && !ids.contains("" + dishModel.itemId))
                        return dishModel;
                }
            }

            for (DishModel dishModel : aDishes) {
                if (dishModel.type.equals(type) && !DishDao.isSoldOut(dishModel, true) && DishDao.canBeAdded(dishModel))
                    return dishModel;
            }
        }

        return null;
    }

    public static DishModel clone(DishModel mDishModel) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.fromJson(gson.toJson(mDishModel), DishModel.class);
    }

    public static double getLowestMainPrice() {
        double dMinPrice = Settings.price;

        if (Menu.get() != null) {
            for (DishModel dishModel : Menu.get().dishModels) {
                if (dishModel.type.equals("main") && dishModel.price != 0 && dMinPrice > dishModel.price)
                    dMinPrice = dishModel.price;
            }
        }

        return dMinPrice;
    }


    public static double getDefaultPriceBento(double dPrice) {
        if (dPrice <= 0)
            return Settings.price;
        else
            return dPrice;
    }

}
