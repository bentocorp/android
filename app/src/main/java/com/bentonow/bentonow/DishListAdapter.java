package com.bentonow.bentonow;

/**
 * Created by gonzalo on 30/05/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bentonow.bentonow.model.Item;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DishListAdapter extends BaseAdapter {

    private static final String TAG = "MenuItemAdapter";
    private final ArrayList<HashMap<String, String>> data;
    private final Activity activity;
    //private final AQuery listAq;
    private final LayoutInflater inflater;
    private Holder current_holder;
    //private HashMap<String, String> row;


    public DishListAdapter(Activity a, ArrayList<HashMap<String, String>> datos) {
        data = datos;
        activity = a;
        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //listAq = new AQuery(activity);
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
            view = inflater.inflate(R.layout.inc_menu_item_side, null);
            holder.container = (RelativeLayout)view.findViewById(R.id.container);
            holder.img = (ImageView) view.findViewById(R.id.menu_item_image);
            holder.main_title = (TextView) view.findViewById(R.id.main_menu_item_name);
            holder.desc_title = (TextView) view.findViewById(R.id.main_menu_item_name_2);
            holder.overlay_menu_detail = (RelativeLayout) view.findViewById(R.id.overlay_menu_detail);
            holder.main_menu_item_description = (TextView)view.findViewById(R.id.main_menu_item_description);
            //holder.btn_add_to_bento = (LinearLayout)view.findViewById(R.id.btn_add_to_bento_side1);
            holder.btn_add_to_bento = (TextView)view.findViewById(R.id.btn_add_to_bento);
            holder.btn_add_to_bento_solded  = (TextView)view.findViewById(R.id.btn_add_to_bento_solded);
            holder.soldout_flag = (ImageView)view.findViewById(R.id.soldout_flag);
            holder.btn_added = (LinearLayout)view.findViewById(R.id.btn_added);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        //Log.i(TAG,Bentonow.current_side);
        List<Item> allOrderItems = Item.find(Item.class, "orderid=?", String.valueOf(Bentonow.pending_order_id));

        try {
            holder.row = data.get(position);
            Log.i(TAG, "row: " + holder.row.toString());
            final Holder finalHolder = holder;
            Item bento = Item.findById(Item.class, Bentonow.pending_bento_id);
            finalHolder.iid = holder.row.get(Config.DISH._ID);

            int order_dish_id_total = 0;
            for (Item oItem : allOrderItems) {
                if (oItem.side1 != null && oItem.side1.equals(finalHolder.iid))
                    order_dish_id_total++;
                if (oItem.side2 != null && oItem.side2.equals(finalHolder.iid))
                    order_dish_id_total++;
                if (oItem.side3 != null && oItem.side3.equals(finalHolder.iid))
                    order_dish_id_total++;
                if (oItem.side4 != null && oItem.side4.equals(finalHolder.iid))
                    order_dish_id_total++;
            }
            holder.rest_quantity = (int) (Integer.valueOf(holder.row.get("qty")) - order_dish_id_total);

            switch (Bentonow.current_side){
                case Config.SIDE.MAIN:
                    if( finalHolder.iid.equals(bento.main) ){
                        selectedDish(finalHolder);
                    }
                    break;
                case Config.SIDE.SIDE_1:
                    if( finalHolder.iid.equals(bento.side1) ){
                        selectedDish(finalHolder);
                    }
                    break;
                case Config.SIDE.SIDE_2:
                    if( finalHolder.iid.equals(bento.side2) ){
                        selectedDish(finalHolder);
                    }
                    break;
                case Config.SIDE.SIDE_3:
                    if( finalHolder.iid.equals(bento.side3) ){
                        selectedDish(finalHolder);
                    }
                    break;
                case Config.SIDE.SIDE_4:
                    if( finalHolder.iid.equals(bento.side4) ){
                        selectedDish(finalHolder);
                    }
                    break;
            }


                //ADD LISTENERS
                holder.main_title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ( current_holder != null ) {
                            if ( current_holder.pressed ) {
                                hideItemDetails(current_holder);
                            }
                        }

                        current_holder = finalHolder;
                        showItemDetails(finalHolder);
                    }
                });

                holder.overlay_menu_detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Log.i(TAG, "finalHolder.pressed: " + finalHolder.pressed.toString());
                        //if (!finalHolder.iid.equals(Bentonow.current_dish_selected)) {
                            hideItemDetails(finalHolder);
                        //}
                        //if(finalHolder.pressed) {}
                    }
                });


                // ADD TO BENTO
                if ( holder.rest_quantity == 0 && !holder.pressed ) {
                    //holder.soldout_flag.setImageResource(R.drawable.tmp_trans);
                    holder.soldout_flag.setVisibility(View.VISIBLE);
                    holder.btn_add_to_bento.setVisibility(View.GONE);
                    holder.btn_add_to_bento_solded.setVisibility(View.VISIBLE);
                }else{
                    holder.soldout_flag.setImageResource(R.drawable.tmp_trans);
                }

                holder.btn_add_to_bento.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "btn_add_to_bento row.get(itemId): " + finalHolder.row.get(Config.DISH._ID));
                        Item bento = Item.findById(Item.class, Bentonow.pending_bento_id);
                        Log.i(TAG, bento.toString());
                        Log.i(TAG, "Bentonow.current_side: " + Bentonow.current_side);
                        switch (Bentonow.current_side) {
                            case Config.SIDE.MAIN:
                                Log.i(TAG, "btn0 side0");
                                bento.main = String.valueOf(finalHolder.row.get(Config.DISH._ID));
                                break;
                            case Config.SIDE.SIDE_1:
                                Log.i(TAG, "btn1 side1");
                                bento.side1 = String.valueOf(finalHolder.row.get(Config.DISH._ID));
                                break;
                            case Config.SIDE.SIDE_2:
                                Log.i(TAG, "btn1 side2");
                                bento.side2 = String.valueOf(finalHolder.row.get(Config.DISH._ID));
                                break;
                            case Config.SIDE.SIDE_3:
                                Log.i(TAG, "btn1 side3");
                                bento.side3 = String.valueOf(finalHolder.row.get(Config.DISH._ID));
                                break;
                            case Config.SIDE.SIDE_4:
                                Log.i(TAG, "btn1 side4");
                                bento.side4 = String.valueOf(finalHolder.row.get(Config.DISH._ID));
                                break;
                        }
                        bento.save();
                        //SelectSideActivity.goToMain();
                        Intent intent = new Intent(activity, BuildBentoActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                        activity.overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
                    }
                });

                // REMOVE FROM BENTO
                holder.btn_added.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "btn_add_to_bento row.get(itemId): " + finalHolder.row.get(Config.DISH._ID));
                        Item bento = Item.findById(Item.class, Bentonow.pending_bento_id);
                        Log.i(TAG, bento.toString());
                        Log.i(TAG, "Bentonow.current_side: " + Bentonow.current_side);
                        finalHolder.iid = "";
                        switch (Bentonow.current_side) {
                            case Config.SIDE.MAIN:
                                Log.i(TAG, "removed side0");
                                bento.main = null;
                                break;
                            case Config.SIDE.SIDE_1:
                                Log.i(TAG, "removed side1");
                                bento.side1 = null;
                                break;
                            case Config.SIDE.SIDE_2:
                                Log.i(TAG, "removed side2");
                                bento.side2 = null;
                                break;
                            case Config.SIDE.SIDE_3:
                                Log.i(TAG, "removed side3");
                                bento.side3 = null;
                                break;
                            case Config.SIDE.SIDE_4:
                                Log.i(TAG, "removed side4");
                                bento.side4 = null;
                                break;
                        }
                        bento.save();
                        unSelectDish(finalHolder);
                    }
                });
            //}

            /// COL 1 ADD LABELS
            String title = "";
            String description = "";
            try {
                title = holder.row.get("name").toUpperCase();
                description = holder.row.get("description");
            }catch (NullPointerException ignored){

            }

            holder.main_title.setText(title);
            holder.desc_title.setText(title);
            holder.main_menu_item_description.setText(description);

            if(!holder.row.get("image1").isEmpty()) {
                Picasso.with(activity)
                        .load(holder.row.get("image1"))
                        .placeholder(R.drawable.tmp_trans)
                        .error(R.drawable.tmp_trans)
                        .into(holder.img);
            }
        }catch (IndexOutOfBoundsException ignore){
            Log.i(TAG,"Col 1 empty");
            holder.main_title.setText("");
            holder.img.setImageResource(R.drawable.tmp_trans);
        }

        return view;
    }

    private void selectedDish(Holder aHolder) {
        Bentonow.current_dish_selected = aHolder.iid;
        aHolder.selected = true;
        aHolder.pressed = true;
        aHolder.main_title.setVisibility(View.GONE);
        aHolder.btn_add_to_bento.setVisibility(View.GONE);
        aHolder.btn_added.setVisibility(View.VISIBLE);
        aHolder.overlay_menu_detail.setVisibility(View.VISIBLE);
    }


    private void unSelectDish(Holder aHolder) {
        Bentonow.current_dish_selected = "";
        aHolder.pressed = false;
        aHolder.selected = false;
        aHolder.main_title.setVisibility(View.VISIBLE);
        aHolder.btn_add_to_bento.setVisibility(View.VISIBLE);
        aHolder.btn_added.setVisibility(View.GONE);
        aHolder.overlay_menu_detail.setVisibility(View.GONE);
    }

    private void showItemDetails(Holder aHolder) {
        aHolder.pressed = true;
        aHolder.soldout_flag.setVisibility(View.INVISIBLE);
        aHolder.overlay_menu_detail.setVisibility(View.VISIBLE);
        aHolder.main_title.setVisibility(View.GONE);
    }

    private void hideItemDetails(Holder aHolder) {
        aHolder.pressed = false;
        aHolder.soldout_flag.setVisibility(View.VISIBLE);
        aHolder.overlay_menu_detail.setVisibility(View.GONE);
        aHolder.main_title.setVisibility(View.VISIBLE);
    }

    private static class Holder {
        public boolean selected = false;
        public Boolean pressed = false;
        public ImageView img;
        public TextView main_title;
        public TextView desc_title;
        public RelativeLayout overlay_menu_detail;
        public TextView main_menu_item_description;
        public TextView btn_add_to_bento;
        public RelativeLayout container;
        public HashMap<String, String> row;
        public LinearLayout btn_added;
        public String iid;
        public TextView btn_add_to_bento_solded;
        public ImageView soldout_flag;
        public int rest_quantity;
    }
}