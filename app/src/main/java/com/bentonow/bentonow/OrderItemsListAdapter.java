package com.bentonow.bentonow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bentonow.bentonow.dialog.ConfirmDialog;
import com.bentonow.bentonow.dialog.ConfirmDialogInterface;
import com.bentonow.bentonow.model.Ioscopy;
import com.bentonow.bentonow.model.Item;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gonzalo on 11/05/2015.
 */
public class OrderItemsListAdapter extends BaseAdapter implements ConfirmDialogInterface {

    private static final String TAG = "OrderItemsListAdapter";
    private final ArrayList<HashMap<String, String>> data;
    private final Context activity;
    private final LayoutInflater inflater;
    private final Typeface tf;
    private final Boolean editMode;
    private final ConfirmDialog dialog;
    private Data lastItemSelected;
    private Activity context;


    public OrderItemsListAdapter(Activity a, ArrayList<HashMap<String, String>> datos, Boolean isEditMode, ConfirmDialog dialog) {
        data = datos;
        activity = a;
        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tf = Typeface.createFromAsset(a.getAssets(), Config.ASSET.FONT.OPENSANS_REGULAR);
        editMode = isEditMode;

        this.dialog = dialog;
        this.dialog.setContext(this);

        context = a;
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
            holder.btn_edit_remove = (ImageView)view.findViewById(R.id.btn_edit_remove);
            holder.complete_row = (LinearLayout)view.findViewById(R.id.complete_row);
            holder.btn_remove = (ImageView)view.findViewById(R.id.btn_remove);
            holder.item_price.setTypeface(tf);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        //
        final HashMap<String, String> row = data.get(position);

        if(editMode){
            holder.btn_edit_remove.setVisibility(View.VISIBLE);
            final Holder finalHolder = holder;
            holder.btn_edit_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finalHolder.btn_edit_remove.setVisibility(View.GONE);
                    finalHolder.item_price.setVisibility(View.GONE);
                    finalHolder.btn_remove.setVisibility(View.VISIBLE);
                }
            });
            holder.btn_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Long itemId = Long.valueOf(row.get(Config.DISH.ITEM_ID));
                    Log.i(TAG, "itemId: " + itemId);

                    long count = Item.count(Item.class, null, null);

                    if (count == 1) {
                        lastItemSelected = new Data();
                        lastItemSelected.holder = finalHolder;
                        lastItemSelected.itemId = itemId;

                        dialog.show();
                    } else {
                        deleteItem(finalHolder, itemId);
                    }
                }
            });
        }
        Log.i(TAG, "row: " + row.toString());
        holder.main_title.setText(row.get(Config.DISH.NAME));
        holder.item_price.setText("$"+Ioscopy.getKeyValue(Config.IOSCOPY.PRICE));
        return view;
    }

    private void deleteItem (Holder finalHolder, Long itemId) {
        Item item = Item.findById(Item.class, itemId);
        item.delete();
        finalHolder.complete_row.setVisibility(View.GONE);
        finalHolder.btn_edit_remove.setVisibility(View.GONE);
        finalHolder.item_price.setVisibility(View.GONE);
        finalHolder.main_title.setVisibility(View.GONE);
        finalHolder.btn_remove.setVisibility(View.GONE);
    }

    @Override
    public void onAccept() {
        dialog.hide();

        deleteItem(lastItemSelected.holder, lastItemSelected.itemId);
        lastItemSelected = null;

        Intent intent = new Intent(context, BuildBentoActivity.class);
        context.startActivity(intent);
        context.finish();
        context.overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
    }

    @Override
    public void onCancel() {
        dialog.hide();

        lastItemSelected.holder.btn_edit_remove.setVisibility(View.GONE);
        lastItemSelected.holder.btn_remove.setVisibility(View.GONE);
        lastItemSelected.holder.item_price.setVisibility(View.VISIBLE);
        lastItemSelected.holder.main_title.setVisibility(View.VISIBLE);

        lastItemSelected = null;
    }

    private static class Holder {
        public TextView main_title;
        public TextView item_price;
        public ImageView btn_edit_remove;
        public LinearLayout complete_row;
        public ImageView btn_remove;
    }

    private class Data {
        Long itemId;
        Holder holder;
    }
}