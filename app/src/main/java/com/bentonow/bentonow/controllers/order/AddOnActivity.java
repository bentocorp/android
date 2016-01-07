package com.bentonow.bentonow.controllers.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.adapter.AddOnListAdapter;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.listener.ListenerAddOn;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.service.BentoCustomerService;
import com.bentonow.bentonow.ui.BackendButton;

import java.util.ArrayList;

/**
 * @author José Torres Fuentes
 */

public class AddOnActivity extends BaseFragmentActivity implements View.OnClickListener, ListenerAddOn {

    public static final String TAG = "AddOnActivity";

    public static final String TAG_OPEN_BY = "OpenByActivity";

    private ImageView menuItemBuildBento;
    private ImageView actionbarLeftBtn;
    private TextView txtToolbarTitle;
    private TextView txtToolbarRightBadge;
    private BackendButton btnFinalize;

    private RecyclerView mListAddOn;

    private AddOnListAdapter mAdapter;

    private Menu mMenu;

    private Order mOrder;
    private OrderItem mBento;

    private ConstantUtils.optOpenAddOn optOpenBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_on);

        getTxtToolbarTitle().setText("Choose Add-Ons");

        try {
            optOpenBy = (ConstantUtils.optOpenAddOn) getIntent().getExtras().get(TAG_OPEN_BY);
        } catch (Exception ex) {
            optOpenBy = ConstantUtils.optOpenAddOn.BUILDER;
            DebugUtils.logError(TAG, ex);
        }

        getActionbarLeftBtn().setImageResource(R.drawable.ic_ab_back);

        getMenuItemBuildBento().setVisibility(View.VISIBLE);
        getMenuItemBuildBento().setImageResource(R.drawable.ic_ab_bento_completed);
        getTxtToolbarRightBadge().setVisibility(View.VISIBLE);

        getActionbarLeftBtn().setOnClickListener(this);
        getMenuItemBuildBento().setOnClickListener(this);
        getBtnFinalize().setOnClickListener(this);

        getListAddOn().setHasFixedSize(true);
        getListAddOn().setAdapter(getListAdapter());

        mMenu = Menu.get();
        mBento = mBentoDao.getAddOnBento();

        updateUI();

        addAddOnDishes();

    }

    private void addAddOnDishes() {
        if (mMenu == null) {
            ConfirmationDialog mDialog = new ConfirmationDialog(AddOnActivity.this, null, "There is no current menu to show");
            mDialog.addAcceptButton("OK", AddOnActivity.this);
            mDialog.show();
        } else {
            getListAdapter().aListDish.clear();

            ArrayList<DishModel> aSoldDish = new ArrayList<>();

            for (DishModel dishModel : mMenu.dishModels)
                if (dishModel.type.equals("addon"))
                    if (DishDao.isSoldOut(dishModel, true))
                        aSoldDish.add(dishModel);
                    else {
                        dishModel.bento_pk = mBento.order_pk;
                        getListAdapter().aListDish.add(dishModel);
                    }

            getListAdapter().aListDish.addAll(aSoldDish);

        }

        getListAdapter().notifyDataSetChanged();
    }

    private void updateUI() {
        mOrder = mOrderDao.getCurrentOrder();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getTxtToolbarRightBadge().setText(mOrderDao.countCompletedOrders(mOrder) + "");
            }
        });
    }

    private void openSummaryScreen() {
        switch (optOpenBy) {
            case BUILDER:
                if (SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.MENU_TYPE).equals("fixed")) {
                    if (BentoNowUtils.isValidCompleteOrder(AddOnActivity.this)) {
                        Intent intent = new Intent(AddOnActivity.this, CompleteOrderActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        finish();
                        startActivity(intent);
                    }
                } else {
                    String sSoldOutItems = mOrderDao.calculateSoldOutItems(mOrder);
                    if (!sSoldOutItems.isEmpty()) {
                        updateUI();
                        WidgetsUtils.createShortToast(String.format(getString(R.string.error_sold_out_items), sSoldOutItems));
                    } else if (BentoNowUtils.isValidCompleteOrder(AddOnActivity.this)) {
                        Intent intent = new Intent(AddOnActivity.this, CompleteOrderActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        finish();
                        startActivity(intent);
                    } else {
                        finish();
                    }
                }
                break;
            case SUMMARY:
                finish();
                break;
        }

    }

    @Override
    public void onAddClick(int iDishPosition) {
        DishModel mDish = DishDao.clone(getListAdapter().aListDish.get(iDishPosition));

        mDishDao.insertDish(mDish);
        mBento.items.add(mDish);

        updateUI();

        getListAdapter().notifyDataSetChanged();
    }

    @Override
    public void onRemoveClick(int iDishPosition) {
        DishModel mDish = getListAdapter().aListDish.get(iDishPosition);
        boolean bRemove = false;

        for (int a = 0; a < mBento.items.size(); a++) {
            if (mDish.itemId == mBento.items.get(a).itemId) {
                mDishDao.removeDish(mBento.items.get(a));
                mBento.items.remove(a);
                bRemove = true;
                break;
            }
        }

        if (bRemove)
            updateUI();

        getListAdapter().notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                finish();
                break;
            case R.id.button_accept:
                finish();
                break;
            case R.id.btn_finalize:
                openSummaryScreen();
                break;
            case R.id.actionbar_right_btn:
                openSummaryScreen();
                break;
            default:
                DebugUtils.logError(TAG, "onClick(): " + v.getId());
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BentoCustomerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ImageView getActionbarLeftBtn() {
        if (actionbarLeftBtn == null)
            actionbarLeftBtn = (ImageView) findViewById(R.id.actionbar_left_btn);
        return actionbarLeftBtn;
    }

    private TextView getTxtToolbarTitle() {
        if (txtToolbarTitle == null)
            txtToolbarTitle = (TextView) findViewById(R.id.actionbar_title);
        return txtToolbarTitle;
    }


    private TextView getTxtToolbarRightBadge() {
        if (txtToolbarRightBadge == null)
            txtToolbarRightBadge = (TextView) findViewById(R.id.actionbar_right_badge);
        return txtToolbarRightBadge;
    }

    private ImageView getMenuItemBuildBento() {
        if (menuItemBuildBento == null)
            menuItemBuildBento = (ImageView) findViewById(R.id.actionbar_right_btn);
        return menuItemBuildBento;
    }

    private RecyclerView getListAddOn() {
        if (mListAddOn == null) {
            mListAddOn = (RecyclerView) findViewById(R.id.recycle_view_add_on);
            mListAddOn.setLayoutManager(new LinearLayoutManager(AddOnActivity.this));
        }
        return mListAddOn;
    }

    private BackendButton getBtnFinalize() {
        if (btnFinalize == null)
            btnFinalize = (BackendButton) findViewById(R.id.btn_finalize);
        return btnFinalize;
    }

    private AddOnListAdapter getListAdapter() {
        if (mAdapter == null)
            mAdapter = new AddOnListAdapter(AddOnActivity.this, AddOnActivity.this);
        return mAdapter;
    }
}