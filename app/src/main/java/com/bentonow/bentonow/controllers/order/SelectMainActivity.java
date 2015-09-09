package com.bentonow.bentonow.controllers.order;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.ui.CustomDialog;
import com.bentonow.bentonow.model.Item;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.ui.ItemHolder;
import com.wsdcamp.list.LazyListAdapter;
import com.wsdcamp.list.LazyListAdapterInterface;

import java.util.ArrayList;
import java.util.List;


public class SelectMainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, LazyListAdapterInterface {

    static final String TAG = "SelectMainActivity";

    List<Item> data = new ArrayList<>();
    LayoutInflater inflater;
    int orderIndex;
    Item currentAddedItem;
    Item currentSelectedItem;
    LazyListAdapter adapter;
    CustomDialog dialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_main);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        initActionbar();

        Menu menu = Menu.get();

        if (menu == null) {
            dialog = new CustomDialog(this, "There is no current menu to show", "OK", null);
            dialog.setOnOkPressed(this);
            dialog.show();
        } else {
            orderIndex = Order.current.currentOrderItem;
            currentSelectedItem = Order.current.OrderItems.get(orderIndex).items.get(0);
            currentAddedItem = Order.current.OrderItems.get(orderIndex).items.get(0);

            for (Item item : menu.items) {
                if (!item.type.equals("main")) continue;
                data.add(item);
            }

            adapter = new LazyListAdapter(this);
            ListView list = (ListView) findViewById(R.id.list);
            list.setAdapter(adapter);
            list.setOnItemClickListener(this);
        }
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(getResources().getString(R.string.bento_builder_actionbar_title));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
            case R.id.btn:
                if (currentSelectedItem.isSoldOut(true))
                    return;

                Order.current.OrderItems.get(orderIndex).items.set(0, currentSelectedItem);

                onBackPressed();
                break;
            case R.id.btn_added:

                //if (currentSelectedItem.itemId == Order.current.OrderItems.get(orderIndex).items.get(0).itemId)
                Order.current.OrderItems.get(orderIndex).items.set(0, null);
                currentAddedItem = null;

                adapter.notifyDataSetChanged();
                //onBackPressed();
                break;
            case R.id.btn_ok:
                dialog.dismiss();
                onBackPressed();
                break;
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_add, null);

            holder = new ItemHolder(
                    this,
                    convertView,
                    R.id.img,
                    R.id.txt_title,
                    R.id.txt_description,
                    R.id.img_sold_out,
                    R.id.img_gradient,
                    R.id.btn,
                    R.id.btn_added
            );

            holder.btn.setOnClickListener(this);
            holder.btn_added.setOnClickListener(this);

            convertView.setTag(holder);
        } else {
            holder = (ItemHolder) convertView.getTag();
        }

        holder.setData(data.get(position), true);
        holder.added = (currentAddedItem != null && currentAddedItem.itemId == holder.item.itemId);
        holder.selected = (currentSelectedItem != null && currentSelectedItem.itemId == holder.item.itemId);
        holder.updateUI(!holder.added);

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        currentSelectedItem = ((ItemHolder) view.getTag()).item;
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_STORE_CHANGIN))
            finish();
    }
}
