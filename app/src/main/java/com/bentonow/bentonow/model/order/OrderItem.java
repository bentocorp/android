package com.bentonow.bentonow.model.order;

import com.bentonow.bentonow.model.DishModel;

import java.util.ArrayList;
import java.util.List;

public class OrderItem {
    public String item_type = "CustomerBentoBox";
    public boolean bIsSoldoOut = false;
    public double unit_price = 0;
    public List<DishModel> items = new ArrayList<>();

    public OrderItem() {
        items.add(null);
        items.add(null);
        items.add(null);
        items.add(null);
        items.add(null);
    }

    public boolean isComplete() {
        return items.get(0) != null &&
                items.get(1) != null &&
                items.get(2) != null &&
                items.get(3) != null &&
                items.get(4) != null;
    }
}
