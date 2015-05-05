package com.bentonow.bentonow.model;

import com.orm.SugarRecord;

/**
 * Created by gonzalo on 27/04/2015.
 */
public class Menu extends SugarRecord<Menu> {
    String itemId;
    String qty;

    public Menu(){

    }

    public Menu(String itemId, String qty) {
        this.itemId = itemId;
        this.qty = qty;
    }

}
