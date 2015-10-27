package com.bentonow.bentonow.dao;

import com.bentonow.bentonow.model.DishModel;
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


}
