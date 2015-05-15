package com.bentonow.bentonow;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bentonow.bentonow.model.Ioscopy;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gonzalo on 11/05/2015.
 */
public class OrderItemsListAdapter extends BaseAdapter {

        private static final String TAG = "OrderItemsListAdapter";
        private final ArrayList<HashMap<String, String>> data;
        private final Context activity;
        private final LayoutInflater inflater;
    private final Typeface tf;


    public OrderItemsListAdapter(Context a, ArrayList<HashMap<String, String>> datos) {
            data = datos;
            activity = a;
            inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            tf = Typeface.createFromAsset(a.getAssets(), Config.ASSET.FONT.OPENSANS_REGULAR);
        }

        public int getCount() {
            return data.size();
        }

        public Object getItem(int i) {
            return i;
        }

        public long getItemId(int i) {
            return i;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            Holder holder = new Holder();
            if (view == null) {
                view = inflater.inflate(R.layout.listitem_items, null);
                holder.main_title = (TextView) view.findViewById(R.id.main_item_name);
                holder.main_title.setTypeface(tf);
                holder.item_price = (TextView) view.findViewById(R.id.item_price);
                holder.item_price.setTypeface(tf);
                view.setTag(holder);
            } else {
                holder = (Holder) view.getTag();
            }

            //
            final HashMap<String, String> row = data.get(position);
            Log.i(TAG, "row: " + row.toString());
            holder.main_title.setText(row.get(Config.DISH.NAME));
            holder.item_price.setText("$"+Ioscopy.getKeyValue(Config.IOSCOPY.PRICE));
            return view;
        }

        private static class Holder {
            public TextView main_title;
            public TextView item_price;
        }
    }