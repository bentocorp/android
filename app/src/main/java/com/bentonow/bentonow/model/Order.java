package com.bentonow.bentonow.model;

import android.location.Address;

import com.bentonow.bentonow.Utils.AndroidUtil;
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
        double items_total = 0;
        double delivery_fee = Settings.delivery_price;
        double pre_coupon_subtotal;
        double post_coupon_subtotal;
        double coupon_discount;
        double tax;
        double tax_w_o_coupon;
        double subtotal_w_o_coupon;
        double subtotal;
        double tip;
        double total;
        double tax_percent = Settings.tax_percent;
        double total_w_o_coupon;

        coupon_discount = current.OrderDetails.coupon_discount_cents / 100;

        for (OrderItem mItem : current.OrderItems)
            items_total += OrderDao.getPriceByOrder(mItem);

        pre_coupon_subtotal = items_total + delivery_fee;

        if (pre_coupon_subtotal - coupon_discount < 0)
            post_coupon_subtotal = 0;
        else
            post_coupon_subtotal = pre_coupon_subtotal - coupon_discount;

        tax_w_o_coupon = pre_coupon_subtotal * (tax_percent / 100);

        subtotal_w_o_coupon = pre_coupon_subtotal + tax_w_o_coupon;

        tax = post_coupon_subtotal * (tax_percent / 100);

        tip = items_total * (current.OrderDetails.tip_percentage / 100);

        subtotal = post_coupon_subtotal + tax;

        if ((subtotal_w_o_coupon + tip) <= 0)
            total_w_o_coupon = 0;
        else if ((subtotal_w_o_coupon + tip) < 0.5)
            total_w_o_coupon = 50;
        else
            total_w_o_coupon = subtotal_w_o_coupon + tip;

        if (pre_coupon_subtotal + tax + tip - coupon_discount <= 0)
            total = 0;
        else if (pre_coupon_subtotal + tax + tip - coupon_discount < 0.5)
            total = 0.5;
        else
            total = pre_coupon_subtotal + tax + tip - coupon_discount;


        current.OrderDetails.coupon_discount_cents = (int) Math.round(coupon_discount * 100);
        current.OrderDetails.delivery_price = Settings.delivery_price;
        current.OrderDetails.items_total = AndroidUtil.round(items_total, 2);
        current.OrderDetails.subtotal = AndroidUtil.round(subtotal, 2);
        current.OrderDetails.tax_cents = AndroidUtil.round(tax, 2) * 100;
        current.OrderDetails.tax_percentage = AndroidUtil.round(tax_percent, 2);
        current.OrderDetails.tip_cents = AndroidUtil.round(tip, 2) * 100;
        current.OrderDetails.total_cents = AndroidUtil.round(total, 2) * 100;
        current.OrderDetails.total_cents_without_coupon = AndroidUtil.round(total_w_o_coupon, 2) * 100;

        DebugUtils.logDebug(TAG, "Item Price: " + current.OrderDetails.items_total);
        DebugUtils.logDebug(TAG, "Delivery Fee: " + delivery_fee);
        DebugUtils.logDebug(TAG, "Pre-Coupon Subtotal: " + pre_coupon_subtotal);
        DebugUtils.logDebug(TAG, "Post-Coupon Subtotal: " + post_coupon_subtotal);
        DebugUtils.logDebug(TAG, "Coupon (Promo) Discount: " + coupon_discount);
        DebugUtils.logDebug(TAG, "Tax w/o coupon: " + tax_w_o_coupon);
        DebugUtils.logDebug(TAG, "Tax: " + tax);
        DebugUtils.logDebug(TAG, "Tax double: " + Settings.tax_percent);
        DebugUtils.logDebug(TAG, "Subtotal w/o coupon: " + subtotal_w_o_coupon);
        DebugUtils.logDebug(TAG, "Subtotal: " + subtotal);
        DebugUtils.logDebug(TAG, "Tip: " + tip);
        DebugUtils.logDebug(TAG, "Total Cent W O Coupon: " + total_w_o_coupon);
        DebugUtils.logDebug(TAG, "Total: " + total);
        DebugUtils.logDebug(TAG, "Total Discount: " + (total_w_o_coupon - total));
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
