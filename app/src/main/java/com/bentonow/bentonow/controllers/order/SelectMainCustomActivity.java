package com.bentonow.bentonow.controllers.order;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.adapter.CustomMainListAdapter;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.listener.ListenerCustomDish;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;

import java.util.ArrayList;


public class SelectMainCustomActivity extends BaseFragmentActivity implements View.OnClickListener, AdapterView.OnItemClickListener, ListenerCustomDish {

    static final String TAG = "SelectMainCustomActivity";

    private ListView mListView;

    private CustomMainListAdapter mListAdapter;

    private int orderIndex;

    private Order mOrder;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_main);

        initActionbar();

        Menu mMenu = getIntent().getParcelableExtra(Menu.TAG);

        if (mMenu == null) {
            ConfirmationDialog mDialog = new ConfirmationDialog(SelectMainCustomActivity.this, "Error", "There is no current menu to show");
            mDialog.addAcceptButton("OK", SelectMainCustomActivity.this);
            mDialog.show();
        } else {
            getListView().setAdapter(getListAdapter());
            getListView().setOnItemClickListener(this);

            mOrder = mOrderDao.getCurrentOrder();

            orderIndex = mOrder.currentOrderItem;

            getListAdapter().setCurrentAdded(mOrder.OrderItems.get(orderIndex).items.get(0));
            getListAdapter().setCurrentSelected(mOrder.OrderItems.get(orderIndex).items.get(0));

            ArrayList<DishModel> aSoldDish = new ArrayList<>();

            for (DishModel dishModel : mMenu.dishModels) {
                if (dishModel.type.equals("main")) {
                    if (mDishDao.isSoldOut(dishModel, true, !SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_ORDER_AHEAD_MENU)))
                        aSoldDish.add(dishModel);
                    else
                        getListAdapter().add(dishModel);
                }
            }

            getListAdapter().addAll(aSoldDish);
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
    public void onAddedClick(int iDishPosition) {
        //if (currentSelectedItem.itemId == Order.current.OrderItems.get(orderIndex).items.get(0).itemId)

        //Order.current.OrderItems.get(orderIndex).items.set(0, null);
        MixpanelUtils.track("Withdrew Main Dish");

        DishModel mDish = mDishDao.getEmptyDish(mOrder.OrderItems.get(orderIndex).order_pk);
        mDish.dish_pk = mOrder.OrderItems.get(orderIndex).items.get(0).dish_pk;
        mDish.bento_pk = mOrder.OrderItems.get(orderIndex).items.get(0).bento_pk;
        mDishDao.updateDishItem(mDish);

        getListAdapter().setCurrentAdded(null);

        getListAdapter().notifyDataSetChanged();
        //onBackPressed();
    }

    @Override
    public void onAddToBentoClick(int iDishPosition) {
        if (getListAdapter().getItem(iDishPosition).can_be_added == 0)
            return;

        DishModel mDish = getListAdapter().getCurrentSelected();
        mDish.dish_pk = mOrder.OrderItems.get(orderIndex).items.get(0).dish_pk;
        mDish.bento_pk = mOrder.OrderItems.get(orderIndex).items.get(0).bento_pk;
        mDishDao.updateDishItem(mDish);

        onBackPressed();
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getListAdapter().setCurrentSelected(getListAdapter().getItem(position));
        getListAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        MixpanelUtils.track("Viewed Choose Your Main Dish Screen");
        super.onDestroy();
    }

    private CustomMainListAdapter getListAdapter() {
        if (mListAdapter == null)
            mListAdapter = new CustomMainListAdapter(SelectMainCustomActivity.this, !SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_ORDER_AHEAD_MENU), SelectMainCustomActivity.this);
        return mListAdapter;
    }

    private ListView getListView() {
        if (mListView == null)
            mListView = (ListView) findViewById(R.id.list_select_main);
        return mListView;
    }


}
