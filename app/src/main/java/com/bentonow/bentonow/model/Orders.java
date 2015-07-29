package com.bentonow.bentonow.model;

import android.util.Log;

import com.bentonow.bentonow.Bentonow;
import com.bentonow.bentonow.Config;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;


public class Orders extends SugarRecord<Orders> {
    public String today;
    public String address_number;
    public String address_street;
    public String address_city;
    public String address_state;
    public String address_zip;
    public String coords_lat;
    public String coords_long;
    public String tax_cents;
    public String tip_cents;
    public String total_cents;
    public String completed;
    public String couponcode;
    public String amountoff;

    public Orders(){

    }

    public Orders(String today, String address_number, String address_street, String address_city, String address_state, String address_zip, String coords_lat, String coords_long, String tax_cents, String tip_cents, String total_cents, String completed ) {
        this.today = today;
        this.address_number = address_number;
        this.address_street = address_street;
        this.address_city = address_city;
        this.address_state = address_state;
        this.address_zip = address_zip;
        this.coords_lat = coords_lat;
        this.coords_long = coords_long;
        this.tax_cents = tax_cents;
        this.tip_cents = tip_cents;
        this.total_cents = total_cents;
        this.completed = completed;
    }

    @Override
    public String toString() {
        return "id: "+getId()+", today: "+today+", address_number: "+address_number+", address_street: "+address_street+", address_city: "+address_city+", address_state: "+address_state+", address_zip: "+address_zip+", coords_lat: "+coords_lat+", coords_long: "+coords_long+", tax_cents: "+tax_cents+", tip_cents: "+tip_cents+", total_cents: "+total_cents+", completed: "+completed;
    }

    public static long findPendingOrderId(String todayDate) {
        String TAG= "Orders";
        Log.i(TAG,"findPendingOrderId()");
        long pending_order_id = 0;
        List<Orders> pending_orders = Orders.find(Orders.class, "completed = ? AND today = ?", "no",todayDate);
        for ( Orders order : pending_orders) {
            Log.i(TAG,order.toString());
            pending_order_id = order.getId();
        }
        return pending_order_id;
    }

    public String getOrderAddress(){
        String address;
        String address_number_aux = "";
        String address_street_aux = "";
        String address_city_aux = "";
        String address_state_aux = "";
        String address_zip_aux = "";
        if ( this.address_number != null ) address_number_aux = this.address_number;
        if ( this.address_street != null ) address_street_aux = this.address_street;
        if ( this.address_city != null ) address_city_aux = this.address_city;
        if ( this.address_state != null ) address_state_aux = this.address_state;
        if ( this.address_zip != null ) address_zip_aux = this.address_zip;
        address = address_number_aux+" "+address_street_aux+", "+address_city_aux+", "+address_zip_aux+" "+address_state_aux;
        return address;
    }

    public String getOrderAddressStreet(){
        String address;
        String address_number_aux = "";
        String address_street_aux = "";
        if ( this.address_number != null ) address_number_aux = this.address_number;
        if ( this.address_street != null ) address_street_aux = this.address_street;
        address = address_number_aux+" "+address_street_aux;
        return address;
    }

    public static Long getLastOrderId() {
        Long last_order_address = null;
        List<Orders> orders = Orders.find(Orders.class, null);
        for( Orders lastOrder : orders ){
            last_order_address = lastOrder.getId();
        }
        return last_order_address;
    }

    public static boolean addressVerification(LatLng location) {
        List<LatLng> sfpolygon = new ArrayList<>();
        String[] serviceArea_dinner = Config.serviceArea_dinner.split(" ");
        for (String aServiceArea_dinner : serviceArea_dinner) {
            String[] loc = aServiceArea_dinner.split(",");
            double lat = Double.valueOf(loc[1]);
            double lng = Double.valueOf(loc[0]);
            sfpolygon.add(new LatLng(lat, lng));
        }

        return PolyUtil.containsLocation(new LatLng(location.latitude, location.longitude), sfpolygon, false);
    }

    public static Long itemWithDishOutOfStock() {
        List<Item> pendingOrderItems = Item.find(Item.class, "orderid = ?", String.valueOf(Bentonow.pending_order_id));
        for ( Item item : pendingOrderItems ){
            if ( item.main!=null && !item.main.isEmpty() ) {
                Dish dish = Dish.findDish(item.main);
                if (dish.isSoldOut(false)){
                    return item.getId();
                }
            }
            if ( item.side1!=null && !item.side1.isEmpty() ) {
                Dish dish = Dish.findDish(item.side1);
                if (dish.isSoldOut(false)){
                    return item.getId();
                }
            }
            if ( item.side2!=null && !item.side2.isEmpty() ) {
                Dish dish = Dish.findDish(item.side2);
                if (dish.isSoldOut(false)){
                    return item.getId();
                }
            }
            if ( item.side3!=null && !item.side3.isEmpty() ) {
                Dish dish = Dish.findDish(item.side3);
                if (dish.isSoldOut(false)){
                    return item.getId();
                }
            }
            if ( item.side4!=null && !item.side4.isEmpty() ) {
                Dish dish = Dish.findDish(item.side4);
                if (dish.isSoldOut(false)){
                    return item.getId();
                }
            }
        }
        return null;
    }

    public static Long getLastItemId() {
        Long last_item_id = null;
        List<Orders> orders = Orders.find(Orders.class, null);
        for( Orders lastOrder : orders ){
            last_item_id = lastOrder.getId();
        }
        return last_item_id;
    }
}
