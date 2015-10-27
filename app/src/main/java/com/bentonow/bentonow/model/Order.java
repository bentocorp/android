package com.bentonow.bentonow.model;

import android.location.Address;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.dao.OrderDao;
import com.bentonow.bentonow.model.order.OrderDetails;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.model.order.OrderStripe;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    //****
    // Static members
    //****

    static final String TAG = "Order";

    public static Order current = new Order();
    public static LatLng location;
    public static Address address;

    public transient int currentOrderItem = 0;
    public List<OrderItem> OrderItems = new ArrayList<>();
    public OrderDetails OrderDetails = new OrderDetails();
    public OrderStripe Stripe = new OrderStripe();
    public String CouponCode = null;
    public String IdempotentToken = null;
    public String Platform = null;
    public String MenuType = "";
    public String MealName = "";


    public static int countItemsById(int itemId) {
        int count = 0;
        if (current != null) {
            for (OrderItem orderItem : current.OrderItems) {
                for (DishModel dishModel : orderItem.items) {
                    if (dishModel != null && dishModel.itemId == itemId) ++count;
                }
            }
        }

        return count;
    }

    public static boolean pendingOrders() {
        return countCompletedOrders() > 0;
    }

    public static int countCompletedOrders() {
        int count = 0;

        for (OrderItem item : current.OrderItems) {
            if (item.isComplete()) ++count;
        }

        return count;
    }

    public static void cleanUp() {
        current = null;
    }

    public static String getFullAddress() {
        if (address == null) return "";

        String addrss = "";

        for (int i = 0; i < address.getMaxAddressLineIndex(); ++i) {
            if (addrss.length() > 0) addrss += ", ";
            addrss += address.getAddressLine(i);
        }

        return addrss;
    }

    public static String getStreetAddress() {
        if (address == null) return "";
        return address.getThoroughfare() + ", " + address.getSubThoroughfare();
    }

    @Override
    public String toString() {
        OrderDetails.coords.lat = location.latitude;
        OrderDetails.coords.lng = location.longitude;

        OrderDetails.address.number = address.getSubThoroughfare();
        OrderDetails.address.street = address.getThoroughfare();
        OrderDetails.address.city = address.getLocality();
        OrderDetails.address.state = address.getAdminArea();
        OrderDetails.address.zip = address.getPostalCode();

        return new GsonBuilder().serializeNulls().create().toJson(this).replace("lng", "long").replaceAll("itemId", "id");
    }

    public static void calculate() {
        double bento_total = 0;
        double subtotal;
        double subtotal_taxed;
        double tax;
        double delivery_percent;
        double tip;
        double total;

        double tax_percent = Settings.tax_percent;

        current.OrderDetails.delivery_price = Settings.delivery_price;

        delivery_percent = (current.OrderDetails.delivery_price * 100);

        for (OrderItem mItem : current.OrderItems)
            bento_total += OrderDao.getPriceByOrder(mItem) * 100;


        subtotal = bento_total + delivery_percent;

        if (subtotal - current.OrderDetails.coupon_discount_cents <= 0)
            subtotal_taxed = 0;
        else
            subtotal_taxed = subtotal - current.OrderDetails.coupon_discount_cents;

        tip = Math.round(bento_total * current.OrderDetails.tip_percentage / 100);
        current.OrderDetails.tip_cents = tip;

        tax = Math.round(subtotal_taxed * (tax_percent / 100));

        current.OrderDetails.tax_cents = tax;

        if ((subtotal + tax + tip - current.OrderDetails.coupon_discount_cents) <= 0)
            total = 0;
        else {
            if ((subtotal + tax + tip - current.OrderDetails.coupon_discount_cents) < 50)
                total = 50;
            else
                total = subtotal + tax + tip - current.OrderDetails.coupon_discount_cents;
        }


        DebugUtils.logDebug(TAG, "Subtotal: " + subtotal);
        DebugUtils.logDebug(TAG, "Subtotal Taxed: " + subtotal_taxed);
        DebugUtils.logDebug(TAG, "Tax: " + tax);
        DebugUtils.logDebug(TAG, "Tax double: " + Settings.tax_percent);
        DebugUtils.logDebug(TAG, "Tip: " + tip);
        DebugUtils.logDebug(TAG, "Total: " + total);
        DebugUtils.logDebug(TAG, "Delivery Price: " + Settings.delivery_price);

        current.OrderDetails.total_cents = Math.round(total);
    }

    public static void clearIncomplete() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < current.OrderItems.size(); ++i) {
            OrderItem orderItem = current.OrderItems.get(i);

            if (orderItem.isComplete()) continue;
            ids.add(i);
        }

        Collections.reverse(ids);

        for (Integer i : ids) {
            current.OrderItems.remove(i);
        }
    }
}
