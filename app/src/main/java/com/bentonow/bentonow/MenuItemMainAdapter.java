package com.bentonow.bentonow;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.bentonow.bentonow.model.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MenuItemMainAdapter extends BaseAdapter {

    private static final String TAG = "MenuItemAdapter";
    private final ArrayList<HashMap<String, String>> data;
    private final Context activity;
    private final AQuery listAq;
    private final LayoutInflater inflater;
    private Holder current_holder;


    public MenuItemMainAdapter(Context a, ArrayList<HashMap<String, String>> datos) {
        data = datos;
        activity = a;
        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listAq = new AQuery(activity);
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
            view = inflater.inflate(R.layout.inc_menu_item_row, null);
            holder.img = (ImageView) view.findViewById(R.id.menu_item_image);
            holder.main_title = (TextView) view.findViewById(R.id.main_menu_item_name);
            holder.desc_title = (TextView) view.findViewById(R.id.main_menu_item_name_2);
            holder.overlay_menu_detail = (RelativeLayout) view.findViewById(R.id.overlay_menu_detail);
            holder.main_menu_item_description = (TextView)view.findViewById(R.id.main_menu_item_description);
            holder.btn_add_to_bento_main = (TextView)view.findViewById(R.id.btn_add_to_bento_main);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        ///
        final HashMap<String, String> row = data.get(position);

        ///
        final Holder finalHolder = holder;
        holder.main_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(current_holder != null && current_holder.pressed) {
                    hideItemDetails(current_holder);
                }
                current_holder = finalHolder;
                if(!finalHolder.pressed) {
                    showItemDetails(finalHolder);
                }
            }
        });

        holder.btn_add_to_bento_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                Item bento = Item.findById(Item.class,Bentonow.pending_bento_id);
                bento.main = row.get("itemId");
                bento.save();
                BentoSelectMainActivity.goToMain();
            }
        });

        holder.overlay_menu_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"finalHolder.pressed: "+finalHolder.pressed.toString());
                if(finalHolder.pressed) {
                    hideItemDetails(finalHolder);
                }
            }
        });

        AQuery imgaq = listAq.recycle(view);
        holder.main_title.setText(row.get("name").toUpperCase());
        holder.desc_title.setText(row.get("name").toUpperCase());
        holder.main_menu_item_description.setText(row.get("description"));
        imgaq.id(holder.img).image(row.get("image1"), true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, 1.0f);

        return view;
    }

    private void showItemDetails(Holder aHolder) {
        aHolder.pressed = true;
        aHolder.overlay_menu_detail.setVisibility(View.VISIBLE);
        aHolder.main_title.setVisibility(View.INVISIBLE);
    }

    private void hideItemDetails(Holder aHolder) {
        aHolder.pressed = false;
        aHolder.overlay_menu_detail.setVisibility(View.INVISIBLE);
        aHolder.main_title.setVisibility(View.VISIBLE);
    }

    private static class Holder {
        public Boolean pressed = false;
        public ImageView img;
        public TextView main_title;
        public TextView desc_title;
        public RelativeLayout overlay_menu_detail;
        public TextView main_menu_item_description;
        public TextView btn_add_to_bento_main;
    }
}