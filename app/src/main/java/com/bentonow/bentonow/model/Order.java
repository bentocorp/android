package com.bentonow.bentonow.model;

import android.location.Address;

import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.dao.OrderDao;
import com.bentonow.bentonow.model.order.OrderDetails;
import com.bentonow.bentonow.model.order.OrderEta;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.model.order.OrderStripe;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    //****
    // Static members
    //****

    static final String TAG = "Order";


    public LatLng location;
    public Address address;
    public int currentOrderItem = 0;
    public List<OrderItem> OrderItems = new ArrayList<>();
    public OrderDetails OrderDetails = new OrderDetails();
    public OrderStripe Stripe = new OrderStripe();
    public OrderEta Eta = new OrderEta();
    public String CouponCode = null;
    public String IdempotentToken = null;
    public String Platform = null;
    public String MenuType = "";
    public String MealName = "";
    public int order_pk = 0;


    @Override
    public String toString() {
        String sLocation = SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.LOCATION);
        String sAddress = SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.ADDRESS);

        location = new Gson().fromJson(sLocation, LatLng.class);
        address = new Gson().fromJson(sAddress, Address.class);

        OrderDetails.coords.lat = location.latitude;
        OrderDetails.coords.lng = location.longitude;

        OrderDetails.address.number = address.getSubThoroughfare();
        OrderDetails.address.street = address.getThoroughfare();
        OrderDetails.address.city = address.getLocality();
        OrderDetails.address.state = address.getAdminArea();
        OrderDetails.address.zip = address.getPostalCode();

        return new GsonBuilder().serializeNulls().create().toJson(this).replace("lng", "long").replaceAll("itemId", "id");
    }


}
