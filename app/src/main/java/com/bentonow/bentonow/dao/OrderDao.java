package com.bentonow.bentonow.dao;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.order.OrderItem;

/**
 * Created by Jose Torres on 10/27/15.
 */
public class OrderDao {

    public static double getPriceByOrder(OrderItem mOrder) {
        double dDishPrice = 0;

        for (DishModel mDish : mOrder.items)
            if (mDish.type.equals("main")) {
                dDishPrice = DishDao.getDefaultPriceBento(mDish.price);
                mOrder.unit_price = dDishPrice;
                break;
            }

        return DishDao.getDefaultPriceBento(dDishPrice);
    }


    public static String calculateSoldOutItems() {
        String sSoldOutItems = "";

        for (int a = 0; a < Order.current.OrderItems.size(); a++) {
            boolean bIsSoldOut = false;
            for (DishModel mDishModel : Order.current.OrderItems.get(a).items) {
                if (!sSoldOutItems.contains(mDishModel.name) && DishDao.isSoldOut(mDishModel, false)) {
                    bIsSoldOut = true;
                    sSoldOutItems += "\n- " + mDishModel.name;
                    DebugUtils.logDebug("calculateSoldOutItems:", mDishModel.name);
                }
            }

            Order.current.OrderItems.get(a).bIsSoldoOut = bIsSoldOut;
        }
        return sSoldOutItems;
    }

    public static boolean isSoldOutOrder(OrderItem mOrder) {
        boolean bIsSoldOut = false;
        for (DishModel mDishModel : mOrder.items) {
            if (DishDao.isSoldOut(mDishModel, false)) {
                bIsSoldOut = true;
                DebugUtils.logDebug("calculateSoldOutItems:", mDishModel.name);
            }
        }

        return bIsSoldOut;
    }

}
