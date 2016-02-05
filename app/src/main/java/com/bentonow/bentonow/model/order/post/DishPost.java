package com.bentonow.bentonow.model.order.post;

import com.bentonow.bentonow.model.DishModel;

public class DishPost {

    public static final String TAG = "DishPost";

    public int id;
    public String type = "";
    public String name = "";
    public String qty = "";
    public double unit_price;

    public DishPost(DishModel mDish) {
        id = mDish.itemId;
        type = String.valueOf(mDish.type);
        name = String.valueOf(mDish.name);
        qty = String.valueOf(mDish.qty);
        unit_price = mDish.unit_price;
    }

}
