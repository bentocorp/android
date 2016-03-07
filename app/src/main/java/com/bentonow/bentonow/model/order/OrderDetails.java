package com.bentonow.bentonow.model.order;

public class OrderDetails {
    public int coupon_discount_cents = 0;
    public double tip_percentage = 15;
    public double tax_cents = 0;
    public double tip_cents = 0;
    public double total_cents = 0;
    public double delivery_price = 0;
    public double items_total = 0;
    public double tax_percentage = 0;
    public double subtotal = 0;
    public double total_cents_without_coupon = 0;
    public OrderAddress address = new OrderAddress();
    public OrderLocation coords = new OrderLocation();
}