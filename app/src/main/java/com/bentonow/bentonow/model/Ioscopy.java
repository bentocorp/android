package com.bentonow.bentonow.model;

import android.util.Log;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by gonzalo on 12/05/2015.
 */
public class Ioscopy extends SugarRecord<Ioscopy> {
    public String key;
    public String value;
    public String type;

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

    public static long getIdByKey(String key) {
        Log.i("model.Ioscopy", "getIdByKey(String _id: " + key + ")");
        long ioscopy_id = 0;
        List<Ioscopy> dishes = Ioscopy.find(Ioscopy.class, "key = ?", key);
        for ( Ioscopy each_result : dishes) {
            ioscopy_id = each_result.getId();
        }
        return ioscopy_id;
    }
}
