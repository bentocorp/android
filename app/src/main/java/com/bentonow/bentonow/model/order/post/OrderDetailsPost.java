package com.bentonow.bentonow.model.order.post;

import android.location.Address;

import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.model.order.OrderAddress;
import com.bentonow.bentonow.model.order.OrderDetails;
import com.bentonow.bentonow.model.order.OrderLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

public class OrderDetailsPost {
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
    public OrderLocation coords = new OrderLocation();
    public OrderAddress address = new OrderAddress();

    public OrderDetailsPost(OrderDetails mOrderDetails) {
        coupon_discount_cents = mOrderDetails.coupon_discount_cents;
        tip_percentage = mOrderDetails.tip_percentage;
        tax_cents = mOrderDetails.tax_cents;
        tip_cents = mOrderDetails.tip_cents;
        total_cents = mOrderDetails.total_cents;
        delivery_price = mOrderDetails.delivery_price;
        items_total = mOrderDetails.items_total;
        tax_percentage = mOrderDetails.tax_percentage;
        subtotal = mOrderDetails.subtotal;
        total_cents_without_coupon = mOrderDetails.total_cents_without_coupon;

        String sLocation = SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.LOCATION);
        String sAddress = SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.ADDRESS);

        LatLng location = new Gson().fromJson(sLocation, LatLng.class);
        Address mAddress = new Gson().fromJson(sAddress, Address.class);

        coords.lat = location.latitude;
        coords.lng = location.longitude;

        address.number = mAddress.getSubThoroughfare();
        address.street = mAddress.getThoroughfare();
        address.city = mAddress.getLocality();
        address.state = mAddress.getAdminArea();
        address.zip = mAddress.getPostalCode();
    }

}