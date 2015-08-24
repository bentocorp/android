package com.bentonow.bentonow.model;

import android.location.Address;

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

    public static int countItemsById (int itemId) {
        int count = 0;
        if (current != null) {
            for (OrderItem orderItem : current.OrderItems) {
                for (Item item : orderItem.items) {
                    if (item != null && item.itemId == itemId) ++count;
                }
            }
        }

        return count;
    }

    public static boolean pendingOrders () {
        return countCompletedOrders() > 0;
    }

    public static int countCompletedOrders () {
        int count = 0;

        for (OrderItem item : current.OrderItems) {
            if (item.isComplete()) ++count;
        }

        return count;
    }

    public static void cleanUp () {

        current = new Order();
    }

    public static String getFullAddress () {
        if (address == null) return "";

        String addrss = "";

        for (int i=0; i < address.getMaxAddressLineIndex(); ++i) {
            if (addrss.length() > 0) addrss += ", ";
            addrss += address.getAddressLine(i);
        }

        return addrss;
    }

    public static String getStreetAddress () {
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

    public static void calculate () {
        int order = 0;
        long total;
        double tax_percent = Double.parseDouble(BackendText.get("tax_percent"));

        for (OrderItem item : current.OrderItems) {
            order += item.unit_price * 100;
        }

        current.OrderDetails.tip_cents = order * current.OrderDetails.tip_percentage / 100;

        if (current.OrderDetails.coupon_discount_cents > order) {
            current.OrderDetails.tax_cents = 0;
        } else {
            current.OrderDetails.tax_cents = Math.round((order - current.OrderDetails.coupon_discount_cents) * (tax_percent / 100));
        }

        total = (current.OrderDetails.tax_cents + current.OrderDetails.tip_cents + order) - current.OrderDetails.coupon_discount_cents;

        if (total < 50 && total > 10) {
            total = 50;
        } else if (total <= 0) {
            total = 0;
        }

        current.OrderDetails.total_cents = total;
    }

    //****
    // Instance members
    //****

    public transient int currentOrderItem = 0;
    public List<OrderItem> OrderItems = new ArrayList<>();
    public OrderDetails OrderDetails = new OrderDetails();
    public OrderStripe Stripe = new OrderStripe();
    public String CouponCode = null;

    public static void clearIncomplete() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i=0; i < current.OrderItems.size(); ++i) {
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
