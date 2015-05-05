package com.bentonow.bentonow;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.bentonow.bentonow.model.Item;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gonzalo on 24/04/2015.
 */
public class MenuItemSideAdapter extends BaseAdapter {

    private static final String TAG = "MenuItemAdapter";
    private final ArrayList<HashMap<String, String>> data;
    private final ArrayList<HashMap<String, String>> data2;
    private final Context activity;
    private final AQuery listAq;
    private final LayoutInflater inflater;
    private Holder current_holder;


    public MenuItemSideAdapter(Context a, ArrayList<HashMap<String, String>> datos, ArrayList<HashMap<String, String>> datos2) {
        data = datos;
        data2 = datos2;
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
            view = inflater.inflate(R.layout.inc_menu_item_side, null);
            holder.img = (ImageView) view.findViewById(R.id.menu_item_image);
            holder.main_title = (TextView) view.findViewById(R.id.main_menu_item_name);
            holder.desc_title = (TextView) view.findViewById(R.id.main_menu_item_name_2);
            holder.overlay_menu_detail = (RelativeLayout) view.findViewById(R.id.overlay_menu_detail);
            holder.main_menu_item_description = (TextView)view.findViewById(R.id.main_menu_item_description);
            holder.btn_add_to_bento_side1 = (TextView)view.findViewById(R.id.btn_add_to_bento_side1);
            ///
            holder.img_2 = (ImageView) view.findViewById(R.id.menu_item_image2);
            holder.main_title_2 = (TextView) view.findViewById(R.id.main_menu_item_name2);
            holder.desc_title_2 = (TextView) view.findViewById(R.id.main_menu_item_name_22);
            holder.overlay_menu_detail_2 = (RelativeLayout) view.findViewById(R.id.overlay_menu_detail2);
            holder.main_menu_item_description_2 = (TextView)view.findViewById(R.id.main_menu_item_description2);
            holder.btn_add_to_bento_side2 = (TextView)view.findViewById(R.id.btn_add_to_bento_side2);

            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }


        final HashMap<String, String> row = data.get(position);

        /// 1
        final Holder finalHolder = holder;
        holder.main_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( current_holder != null ) {
                    if (current_holder.pressed)
                        hideItemDetails(current_holder);
                    if (current_holder.pressed_2)
                        hideItemDetails2(current_holder);
                }

                current_holder = finalHolder;
                showItemDetails(finalHolder);
            }
        });

        holder.overlay_menu_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "finalHolder.pressed: " + finalHolder.pressed.toString());
                hideItemDetails(finalHolder);
                //if(finalHolder.pressed) {}
            }
        });

        holder.btn_add_to_bento_side1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"btn_add_to_bento_side1 row.get(itemId): "+row.get("itemId"));
                Item bento = Item.findById(Item.class,Bentonow.pending_bento_id);
                Log.i(TAG,bento.toString());
                Log.i(TAG,"Bentonow.current_side: "+Bentonow.current_side);
                switch (Bentonow.current_side){
                    case 1:
                        Log.i(TAG,"btn1 side1");
                        bento.side1 = String.valueOf(row.get("itemId"));
                        break;
                    case 2:
                        Log.i(TAG,"btn1 side2");
                        bento.side2 = String.valueOf(row.get("itemId"));
                        break;
                    case 3:
                        Log.i(TAG,"btn1 side3");
                        bento.side3 = String.valueOf(row.get("itemId"));
                        break;
                    case 4:
                        Log.i(TAG,"btn1 side4");
                        bento.side4 = String.valueOf(row.get("itemId"));
                        break;
                }
                bento.save();
                BentoSelectSideActivity.goToMain();
            }
        });

        ///
        AQuery imgaq = listAq.recycle(view);
        holder.main_title.setText(row.get("name").toUpperCase());
        holder.desc_title.setText(row.get("name").toUpperCase());
        holder.main_menu_item_description.setText(row.get("description"));

        //imgaq.id(holder.img).image(row.get("image1"), true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, 1.0f);
        Picasso.with(activity)
                .load(row.get("image1"))
                .placeholder(R.drawable.tmp_trans)
                .error(R.drawable.tmp_trans)
                .into(holder.img);
        /// 2
        final HashMap<String, String> row2 = data2.get(position);
        holder.main_title_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_holder != null) {
                    if (current_holder.pressed)
                        hideItemDetails(current_holder);
                    if (current_holder.pressed_2)
                        hideItemDetails2(current_holder);
                }

                current_holder = finalHolder;
                showItemDetails2(finalHolder);
                //if(!finalHolder.pressed_2) {}
            }
        });

        holder.btn_add_to_bento_side2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "btn_add_to_bento_side2 row2.get(itemId): " + row2.get("itemId"));
                Item bento = Item.findById(Item.class, Bentonow.pending_bento_id);
                Log.i(TAG, bento.toString());
                switch (Bentonow.current_side) {
                    case 1:
                        Log.i(TAG, "btn2 side1");
                        bento.side1 = String.valueOf(row2.get("itemId"));
                        break;
                    case 2:
                        Log.i(TAG, "btn2 side2");
                        bento.side2 = String.valueOf(row2.get("itemId"));
                        break;
                    case 3:
                        Log.i(TAG, "btn2 side3");
                        bento.side3 = String.valueOf(row2.get("itemId"));
                        break;
                    case 4:
                        Log.i(TAG, "btn2 side4");
                        bento.side4 = String.valueOf(row2.get("itemId"));
                        break;
                }
                bento.save();
                BentoSelectSideActivity.goToMain();
            }
        });

        holder.overlay_menu_detail_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "finalHolder.pressed: " + finalHolder.pressed_2.toString());
                if (finalHolder.pressed_2) {
                    hideItemDetails2(finalHolder);
                }
            }
        });


        AQuery imgaq_2 = listAq.recycle(view);
        holder.main_title_2.setText(row2.get("name").toUpperCase());
        holder.desc_title_2.setText(row2.get("name").toUpperCase());
        holder.main_menu_item_description_2.setText(row2.get("description"));
        //imgaq_2.id(holder.img_2).image(row2.get("image1"), true, true, 0, 0, null, AQuery.FADE_IN_NETWORK, 1.0f);
        Picasso.with(activity)
                .load(row2.get("image1"))
                .placeholder(R.drawable.tmp_trans)
                .error(R.drawable.tmp_trans)
                .into(holder.img_2);

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

    /// 2
    private void showItemDetails2(Holder aHolder) {
        aHolder.pressed_2 = true;
        aHolder.overlay_menu_detail_2.setVisibility(View.VISIBLE);
        aHolder.main_title_2.setVisibility(View.INVISIBLE);
    }

    private void hideItemDetails2(Holder aHolder) {
        aHolder.pressed_2 = false;
        aHolder.overlay_menu_detail_2.setVisibility(View.INVISIBLE);
        aHolder.main_title_2.setVisibility(View.VISIBLE);
    }

    private static class Holder {
        public Boolean pressed = false;
        public ImageView img;
        public TextView main_title;
        public TextView desc_title;
        public RelativeLayout overlay_menu_detail;
        public TextView main_menu_item_description;

        public Boolean pressed_2 = false;
        public ImageView img_2;
        public TextView main_title_2;
        public TextView desc_title_2;
        public RelativeLayout overlay_menu_detail_2;
        public TextView main_menu_item_description_2;
        public TextView btn_add_to_bento_side1;
        public TextView btn_add_to_bento_side2;
    }
}