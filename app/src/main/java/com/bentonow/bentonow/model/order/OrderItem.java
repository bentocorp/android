package com.bentonow.bentonow.model.order;

import com.bentonow.bentonow.model.Item;
import com.bentonow.bentonow.model.Settings;

import java.util.ArrayList;
import java.util.List;

public class OrderItem {
    public String item_type = "CustomerBentoBox";
    public int unit_price = 0;
    public boolean bIsSoldoOut = false;
    public List<Item> items = new ArrayList<>();

    public OrderItem() {
        try {
            unit_price = (int) Settings.price;
        } catch (Exception ignore) {
        }

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
