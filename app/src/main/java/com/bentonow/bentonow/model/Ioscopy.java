package com.bentonow.bentonow.model;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by gonzalo on 12/05/2015.
 */
public class Ioscopy extends SugarRecord<Ioscopy> {
    String key;
    String value;
    String type;

    public Ioscopy(){}

    public Ioscopy(String key, String value, String type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString() {
        super.toString();
        return "id: "+getId()+", key: "+this.key +", value: "+this.value+", type: "+this.type;
    }

    public static String getKeyValue(String key) {
        String value = "";
        List<Ioscopy> tmp = Ioscopy.find(Ioscopy.class, "key=?", key);
        for( Ioscopy row : tmp ) {
            if( row.value != null ) value = row.value;
        }
        return value;
    }
}
