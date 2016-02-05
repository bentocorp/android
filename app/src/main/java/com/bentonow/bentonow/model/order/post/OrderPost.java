package com.bentonow.bentonow.model.order.post;

import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.order.OrderEta;
import com.bentonow.bentonow.model.order.OrderStripe;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class OrderPost {
    static final String TAG = "OrderPost";

    public OrderStripe Stripe = new OrderStripe();
    public OrderDetailsPost OrderDetails;
    public String order_type = "";
    public List<OrderItemPost> OrderItems = new ArrayList<>();
    public String AppVersion = "";
    public String kitchen = "";
    public String CouponCode = "";
    public String for_date = "";
    public String Platform = "";
    public String scheduled_window_start = "";
    public String scheduled_window_end = "";
    public String OrderAheadZone = "";
    public OrderEta Eta = new OrderEta();
    public String MenuId = "";
    public String IdempotentToken = "";

    public OrderPost(Order mOrder) {
        Stripe = mOrder.Stripe;
        OrderDetails = new OrderDetailsPost(mOrder.OrderDetails);
        order_type = mOrder.order_type;
        for (int a = 0; a < mOrder.OrderItems.size(); a++)
            OrderItems.add(new OrderItemPost(mOrder.OrderItems.get(a)));
        Eta = mOrder.Eta;
        CouponCode = mOrder.CouponCode;
        IdempotentToken = mOrder.IdempotentToken;
        Platform = mOrder.Platform;
        AppVersion = mOrder.AppVersion;
        kitchen = mOrder.kitchen;
        OrderAheadZone = mOrder.OrderAheadZone;
        for_date = mOrder.for_date;
        scheduled_window_start = mOrder.scheduled_window_start;
        scheduled_window_end = mOrder.scheduled_window_end;
        MenuId = mOrder.MenuId;
    }


    @Override
    public String toString() {
        return new GsonBuilder().serializeNulls().create().toJson(this).replace("lng", "long").replaceAll("itemId", "id");
    }


}
