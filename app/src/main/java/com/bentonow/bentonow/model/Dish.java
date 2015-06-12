package com.bentonow.bentonow.model;

import android.util.Log;

import com.bentonow.bentonow.Bentonow;
import com.bentonow.bentonow.Config;
import com.orm.SugarRecord;

import java.util.HashMap;
import java.util.List;

/**
 * Created by gonzalo on 27/04/2015.
 */
public class Dish extends SugarRecord<Dish> {
    private static final String TAG = "Dish SugarRecord<Dish>";
    public String _id;
    public String name;
    public String description;
    public String type;
    public String image1;
    public String max_per_order;
    public String today;
    public String qty;

    public Dish(){}

    public Dish(String itemId, String name, String description, String type, String image1, String max_per_order, String date, String qty ) {
        this._id = itemId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.image1 = image1;
        this.max_per_order = max_per_order;
        this.today = date;
        this.qty = qty;
    }

    public static long getIdBy_id(String _id) {
        //Log.i(TAG,"getIdByKey(String _id: "+_id+")");
        long dish_id = 0;
        List<Dish> dishes = Dish.find(Dish.class, "_id = ?", _id);
        for ( Dish each_dish : dishes) {
            dish_id = each_dish.getId();
        }
        return dish_id;
    }

    public static Dish findDish( String dish_id ){
        List<Dish> dishes = Dish.find(Dish.class, "_id=?", dish_id);
        Long main_dish_id = null;
        for (Dish dsh : dishes) {
            main_dish_id = dsh.getId();
        }
        return Dish.findById(Dish.class, main_dish_id);
    }

    public static boolean canBeAdded ( String dish_id ) {
        Dish dishInfo = Dish.findDish(dish_id );

        if (dishInfo == null) return false;
        if (dishInfo.max_per_order == null) return true;

        try {
            int maxPerOrder = Integer.parseInt(dishInfo.max_per_order);

            List<Item> allOrderItems = Item.find(Item.class, "orderid=?", String.valueOf(Bentonow.pending_order_id));

            for (Item oItem : allOrderItems) {
                if (oItem.side1 != null && oItem.side1.equals(dish_id))
                    maxPerOrder--;
                if (oItem.side2 != null && oItem.side2.equals(dish_id))
                    maxPerOrder--;
                if (oItem.side3 != null && oItem.side3.equals(dish_id))
                    maxPerOrder--;
                if (oItem.side4 != null && oItem.side4.equals(dish_id))
                    maxPerOrder--;
            }

            if (maxPerOrder <= 0) return false;
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static boolean isSoldOut ( String menuDate ) {
        List<Dish> main_dishes = Dish.find(Dish.class, "today = ?",menuDate);
        for( Dish dish : main_dishes ){
            try {
                if (Integer.parseInt(dish.qty, 0) > 0) return false;
            } catch (Exception e) {
            }
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "id: "+getId()+", _id: "+this._id +", name: "+ this.name + "description: "+this.description+ ", type: "+this.type+", image1: "+this.image1+ ", max_per_order: "+this.max_per_order+", date: "+this.today+", qty:"+this.qty;
    }
}
