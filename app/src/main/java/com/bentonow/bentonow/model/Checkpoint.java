package com.bentonow.bentonow.model;

import com.orm.SugarRecord;

/**
 * Created by gonzalo on 07/04/2015.
 */
public class Checkpoint extends SugarRecord<Checkpoint> {

    public String isfirst;

    public Checkpoint() {
    }

    public Checkpoint(String isfirst) {
        this.isfirst = isfirst;
    }

    @Override
    public String toString() {
        return this.isfirst;
    }
}
