package com.bentonow.bentonow.model;

import android.util.Log;

import com.bentonow.bentonow.Bentonow;
import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by gonzalo on 29/04/2015.
 */
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
        super.toString();
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
        String address = "";
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
}
