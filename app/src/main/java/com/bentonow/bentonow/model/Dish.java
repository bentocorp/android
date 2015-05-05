package com.bentonow.bentonow.model;

import com.orm.SugarRecord;

/**
 * Created by gonzalo on 27/04/2015.
 */
public class Dish extends SugarRecord<Dish> {
    public String _id;
    public String name;
    public String description;
    public String type;
    public String image1;
    public String max_per_order;
    public String today;

    public Dish(){}

    public Dish(String itemId, String name, String description, String type, String image1, String max_per_order, String date) {
        this._id = itemId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.image1 = image1;
        this.max_per_order = max_per_order;
        this.today = date;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
