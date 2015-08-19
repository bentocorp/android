package com.bentonow.bentonow.controllers.errors;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.Mixpanel;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.model.Item;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.ui.ItemHolder;
import com.wsdcamp.list.LazyListAdapter;
import com.wsdcamp.list.LazyListAdapterInterface;

import java.util.ArrayList;
import java.util.List;


public class NextDayMenuActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    static final String TAG = "NextDayMenuActivity";

    public Item currentSelectedItem = null;

    LazyListAdapter mainAdapter;
    LazyListAdapter sideAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");

        setContentView(R.layout.activity_next_day_menu);

        Mixpanel.track(this, "Previewed Today's Menu");

        Menu menu = Menu.getNext();

        if (menu == null) {
            onBackPressed();
        } else {
            List<Item> data_main = new ArrayList<>();
            List<Item> data_side = new ArrayList<>();

            for (Item item : menu.items) {
                if (item.type.equals("main")) {
                    data_main.add(item);
                } else {
                    data_side.add(item);
                }
            }

            mainAdapter = new LazyListAdapter(new MenuListAdapter(this, data_main));
            sideAdapter = new LazyListAdapter(new MenuListAdapter(this, data_side));

            GridView list = (GridView) findViewById(R.id.list_main);
            GridView grid = (GridView) findViewById(R.id.list_side);

            list.setAdapter(mainAdapter);
            grid.setAdapter(sideAdapter);

            list.setOnItemClickListener(this);
            grid.setOnItemClickListener(this);
        }
        initActionbar();
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(getIntent().getStringExtra("title"));

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
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        currentSelectedItem = ((ItemHolder) view.getTag()).item;

        if (currentSelectedItem != null) {
            Log.i(TAG, "currentSelectedItem: " + currentSelectedItem.name);
        } else {
            Log.i(TAG, "currentSelectedItem: null");
        }

        mainAdapter.notifyDataSetChanged();
        sideAdapter.notifyDataSetChanged();
    }

    class MenuListAdapter implements LazyListAdapterInterface {
        List<Item> data = new ArrayList<>();
        NextDayMenuActivity context;
        LayoutInflater inflater;

        public MenuListAdapter(NextDayMenuActivity context, List<Item> data) {
            this.context = context;
            this.data = data;
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                        context,
                        convertView,
                        R.id.img,
                        R.id.txt_title,
                        R.id.txt_description,
                        R.id.img_sold_out,
                        R.id.img_gradient,
                        R.id.btn,
                        R.id.btn_added
                );

                convertView.setTag(holder);
            } else {
                holder = (ItemHolder) convertView.getTag();
            }

            holder.setData(data.get(position), false);

            if (context.currentSelectedItem != null && holder.item != null) {
                holder.selected = context.currentSelectedItem.itemId == holder.item.itemId;
            }

            holder.updateUI(false);
            holder.btn.setVisibility(View.GONE);
            holder.btn_added.setVisibility(View.GONE);
            holder.soldOut.setVisibility(View.GONE);

            return convertView;
        }
    }
}
