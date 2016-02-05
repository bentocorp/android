package com.bentonow.bentonow.model.order.post;

import com.bentonow.bentonow.model.order.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderItemPost {
    public List<DishPost> items = new ArrayList<>();
    public double unit_price = 0;
    public String item_type;

    public OrderItemPost(OrderItem mOrderItem) {
        unit_price = mOrderItem.unit_price;
        item_type = String.valueOf(mOrderItem.item_type);
        for (int a = 0; a < mOrderItem.items.size(); a++)
            items.add(new DishPost(mOrderItem.items.get(a)));
    }

}
