package com.bentonow.bentonow.model.order;

import com.bentonow.bentonow.model.DishModel;

import java.util.ArrayList;
import java.util.List;

public class OrderItem {
    public String item_type = "CustomerBentoBox";
    public boolean bIsSoldoOut = false;
    public double unit_price = 0;
    public List<DishModel> items = new ArrayList<>();
    public int order_pk = 0;

    public OrderItem() {
    }

}
