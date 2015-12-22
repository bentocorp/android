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
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.ui.ItemHolder;
import com.wsdcamp.list.LazyListAdapter;
import com.wsdcamp.list.LazyListAdapterInterface;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class NextDayMenuActivity extends BaseFragmentActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    static final String TAG = "NextDayMenuActivity";

    public DishModel currentSelectedDishModel = null;

    LazyListAdapter mainAdapter;
    LazyListAdapter sideAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugUtils.logDebug(TAG, "onCreate()");

        setContentView(R.layout.activity_next_day_menu);

        MixpanelUtils.track("Previewed Today's Menu");

        Menu menu = Menu.getNext();

        if (menu == null) {
            onBackPressed();
        } else {
            List<DishModel> data_main = new ArrayList<>();
            List<DishModel> data_side = new ArrayList<>();

            for (DishModel dishModel : menu.dishModels) {
                if (dishModel.type.equals("main")) {
                    data_main.add(dishModel);
                } else {
                    data_side.add(dishModel);
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
        currentSelectedDishModel = ((ItemHolder) view.getTag()).dishModel;

        if (currentSelectedDishModel != null) {
            DebugUtils.logDebug(TAG, "currentSelectedItem: " + currentSelectedDishModel.name);
        } else {
            DebugUtils.logDebug(TAG, "currentSelectedItem: null");
        }

        mainAdapter.notifyDataSetChanged();
        sideAdapter.notifyDataSetChanged();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    class MenuListAdapter implements LazyListAdapterInterface {
        List<DishModel> data = new ArrayList<>();
        NextDayMenuActivity context;
        LayoutInflater inflater;

        public MenuListAdapter(NextDayMenuActivity context, List<DishModel> data) {
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
                        R.id.btn_add_to_bento,
                        R.id.btn_added
                );

                convertView.setTag(holder);
            } else {
                holder = (ItemHolder) convertView.getTag();
            }

            holder.setData(data.get(position), false);

            if (context.currentSelectedDishModel != null && holder.dishModel != null) {
                holder.selected = context.currentSelectedDishModel.itemId == holder.dishModel.itemId;
            }

            holder.updateUI(false);
            holder.btn_add_to_bento.setVisibility(View.GONE);
            holder.btn_added.setVisibility(View.GONE);
            holder.soldOut.setVisibility(View.GONE);

            return convertView;
        }
    }

   /* @Override
    public void onBackPressed() {
        BentoNowUtils.openErrorActivity(NextDayMenuActivity.this);
        finish();
    }*/
}
