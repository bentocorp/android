package com.bentonow.bentonow.model;

import com.orm.SugarRecord;

/**
 * Created by gonzalo on 29/04/2015.
 */
public class Item extends SugarRecord<Item>{
    public String item_type;
    public String main;
    public String side1;
    public String side2;
    public String side3;
    public String side4;
    public String orderid;
    public String completed = "no";

    public Item(){

    }

    public Item(String item_type, String main, String side1, String side2, String side3, String side4, String orderid, String completed) {
        this.item_type = item_type;
        this.main = main;
        this.side1 = side1;
        this.side2 = side2;
        this.side3 = side3;
        this.side4 = side4;
        this.orderid = orderid;
        this.completed = completed;
    }

    @Override
    public String toString() {
        super.toString();
        return "Main: "+main+", Side1: "+side1+", Side2: "+side2+", Side3: "+side3+", Side4: "+side4+", Order_id: "+orderid+", Completed: "+completed;
    }

    public boolean isFull() {
        boolean isFull = true;
        if ( this.main == null ) isFull = false;
        if ( this.side1 == null ) isFull = false;
        if ( this.side2 == null ) isFull = false;
        if ( this.side3 == null ) isFull = false;
        if ( this.side4 == null ) isFull = false;
        return isFull;
    }

    public String[] sideItems(){
        String[] toReturn = new String[]{main,side1,side2,side3,side4};
        return toReturn;
    }

}
