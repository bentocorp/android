package com.bentonow.bentonow.controllers.order;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.adapter.CustomSideListAdapter;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.listener.ListenerCustomDish;
import com.bentonow.bentonow.model.Item;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.ui.ItemHolder;


public class SelectSideCustomActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, ListenerCustomDish {

    static final String TAG = "SelectSideActivity";

    private GridView mGridView;

    int orderIndex;
    int itemIndex;

    private CustomSideListAdapter mListAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_side);

        initActionbar();

        Menu menu = Menu.get();

        if (menu == null) {
            ConfirmationDialog mDialog = new ConfirmationDialog(SelectSideCustomActivity.this, null, "There is no current menu to show");
            mDialog.addAcceptButton("OK", SelectSideCustomActivity.this);
            mDialog.show();
        } else {
            orderIndex = Order.current.currentOrderItem;
            itemIndex = getIntent().getIntExtra("itemIndex", 0);

            getGridView().setAdapter(getListAdapter());
            getGridView().setOnItemClickListener(this);

            getListAdapter().setCurrentSelected(Order.current.OrderItems.get(orderIndex).items.get(itemIndex));
            getListAdapter().setCurrentAdded(Order.current.OrderItems.get(orderIndex).items.get(itemIndex));

            for (Item item : menu.items) {
                if (!item.type.equals("side"))
                    continue;
                getListAdapter().add(item);
            }

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
            case R.id.button_accept:
                onBackPressed();
                break;
        }
    }


    @Override
    public void onAddedClick(int iDishPosition) {
    }

    @Override
    public void onAddToBentoClick(int iDishPosition) {
        if (getListAdapter().getCurrentSelected().isSoldOut(true))
            return;
        Item item = getListAdapter().getCurrentSelected().clone();
        item.type += itemIndex;
        Order.current.OrderItems.get(orderIndex).items.set(itemIndex, item);
        Log.i(TAG, "added " + item.type);

        onBackPressed();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getListAdapter().setCurrentSelected(((ItemHolder) view.getTag()).item);
        getListAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_STORE_CHANGIN))
            finish();
    }


    private CustomSideListAdapter getListAdapter() {
        if (mListAdapter == null)
            mListAdapter = new CustomSideListAdapter(SelectSideCustomActivity.this, SelectSideCustomActivity.this);
        return mListAdapter;
    }

    private GridView getGridView() {
        if (mGridView == null)
            mGridView = (GridView) findViewById(R.id.grid_side_custom);
        return mGridView;
    }

}