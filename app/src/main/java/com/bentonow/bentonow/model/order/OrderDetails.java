package com.bentonow.bentonow.model.order;

public class OrderDetails {
    public OrderAddress address = new OrderAddress();
    public OrderLocation coords = new OrderLocation();
    public int coupon_discount_cents = 0;
    public int tip_percentage = 15;
    public long tax_cents = 0;
    public int tip_cents = 0;
    public long total_cents = 0;
    public long delivery_price = 0;
}