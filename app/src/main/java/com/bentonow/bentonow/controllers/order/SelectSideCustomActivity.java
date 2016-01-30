package com.bentonow.bentonow.controllers.order;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.adapter.CustomSideListAdapter;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.listener.ListenerCustomDish;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;

import java.util.ArrayList;


public class SelectSideCustomActivity extends BaseFragmentActivity implements View.OnClickListener, AdapterView.OnItemClickListener, ListenerCustomDish {

    static final String TAG = "SelectSideActivity";

    private GridView mGridView;

    private CustomSideListAdapter mListAdapter;

    private Order mOrder;

    int orderIndex;
    int itemIndex;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_side);

        initActionbar();

        Menu mMenu = getIntent().getParcelableExtra(Menu.TAG);

        mOrder = mOrderDao.getCurrentOrder();

        if (mMenu == null) {
            ConfirmationDialog mDialog = new ConfirmationDialog(SelectSideCustomActivity.this, null, "There is no current menu to show");
            mDialog.addAcceptButton("OK", SelectSideCustomActivity.this);
            mDialog.show();
        } else {
            orderIndex = mOrder.currentOrderItem;
            itemIndex = getIntent().getIntExtra("itemIndex", 0);

            getGridView().setAdapter(getListAdapter());
            getGridView().setOnItemClickListener(this);

            getListAdapter().setCurrentSelected(mOrder.OrderItems.get(orderIndex).items.get(itemIndex));
            getListAdapter().setCurrentAdded(mOrder.OrderItems.get(orderIndex).items.get(itemIndex));

            ArrayList<DishModel> aSoldDish = new ArrayList<>();

            for (DishModel dishModel : mMenu.dishModels) {
                if (dishModel.type.equals("side"))
                    if (mDishDao.isSoldOut(dishModel, true, !SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_ORDER_AHEAD_MENU)))
                        aSoldDish.add(dishModel);
                    else
                        getListAdapter().add(dishModel);
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
        if (mDishDao.isSoldOut(getListAdapter().getCurrentSelected(), true, !SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_ORDER_AHEAD_MENU)) || !mDishDao.canBeAdded(getListAdapter().getCurrentSelected()))
            return;
        DishModel dishModel = DishDao.clone(getListAdapter().getCurrentSelected());
        dishModel.type += itemIndex;
        dishModel.dish_pk = mOrder.OrderItems.get(orderIndex).items.get(itemIndex).dish_pk;
        dishModel.bento_pk = mOrder.OrderItems.get(orderIndex).items.get(itemIndex).bento_pk;

        //Order.current.OrderItems.get(orderIndex).items.set(itemIndex, dishModel);

        mDishDao.updateDishItem(dishModel);

        DebugUtils.logDebug(TAG, "added " + dishModel.type);

        onBackPressed();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getListAdapter().setCurrentSelected(getListAdapter().getItem(position));
        getListAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        MixpanelUtils.track("Viewed Choose Your Side Dish Screen");
        super.onDestroy();
    }

    private CustomSideListAdapter getListAdapter() {
        if (mListAdapter == null)
            mListAdapter = new CustomSideListAdapter(SelectSideCustomActivity.this, !SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_ORDER_AHEAD_MENU), SelectSideCustomActivity.this);
        return mListAdapter;
    }

    private GridView getGridView() {
        if (mGridView == null)
            mGridView = (GridView) findViewById(R.id.grid_side_custom);
        return mGridView;
    }

}