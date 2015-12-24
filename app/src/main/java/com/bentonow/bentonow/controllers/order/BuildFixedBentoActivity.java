package com.bentonow.bentonow.controllers.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.adapter.BuildBentoFixListAdapter;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.listener.InterfaceCustomerService;
import com.bentonow.bentonow.listener.ListenerMainDishFix;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.Stock;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.service.BentoCustomerService;
import com.bentonow.bentonow.ui.AutoFitTxtView;
import com.bentonow.bentonow.ui.BackendButton;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.mixpanel.android.mpmetrics.Tweak;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author José Torres Fuentes
 */

public class BuildFixedBentoActivity extends BaseActivity implements View.OnClickListener, ListenerMainDishFix, InterfaceCustomerService {

    static final String TAG = "BuildFixedBentoActivity";

    private ImageView menuItemBuildBento;
    private ImageView actionbarLeftBtn;
    private TextView txtToolbarTitle;
    private TextView txtToolbarBadge;
    private BackendButton btnComplete;
    private ListView mListBento;
    private AutoFitTxtView txtPromoName;

    private BuildBentoFixListAdapter aListBento;

    private ArrayList<DishModel> aSideDish = new ArrayList<>();

    //private static Tweak<Boolean> showBanner = MixpanelAPI.booleanTweak("Show Fix Banner", false);

    private Menu mMenu;

    private Order mOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_build_fix_bento);

        getTxtToolbarTitle().setText(BackendText.get("build-title"));

        getActionbarLeftBtn().setImageResource(R.drawable.ic_ab_user);
        getMenuItemBuildBento().setImageResource(R.drawable.ic_ab_bento);

        getActionbarLeftBtn().setOnClickListener(this);
        getMenuItemBuildBento().setOnClickListener(this);
        getButtonComplete().setOnClickListener(this);

        getListBento().setAdapter(getAdapterListBento());

        getTxtPromoName().setText(String.format(getString(R.string.build_bento_price), BentoNowUtils.getDefaultPriceBento(DishDao.getLowestMainPrice())));

        BentoNowUtils.rotateBanner(getTxtPromoName());

        DebugUtils.logDebug(TAG, "Create: ");

    }

    private void addMainDishes() {
        if (mMenu == null) {
            ConfirmationDialog mDialog = new ConfirmationDialog(BuildFixedBentoActivity.this, null, "There is no current menu to show");
            mDialog.addAcceptButton("OK", BuildFixedBentoActivity.this);
            mDialog.show();
        } else {
            getAdapterListBento().clear();

            ArrayList<DishModel> aSoldDish = new ArrayList<>();

            for (DishModel dishModel : mMenu.dishModels)
                if (dishModel.type.equals("main"))
                    if (DishDao.isSoldOut(dishModel, true))
                        aSoldDish.add(dishModel);
                    else
                        getAdapterListBento().add(dishModel);

            getAdapterListBento().addAll(aSoldDish);

            setSideDishList();
        }

        getAdapterListBento().addAll();
    }

    private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getTxtToolbarBadge().setVisibility(mOrder.OrderItems.size() != 0 ? View.VISIBLE : View.INVISIBLE);
                getTxtToolbarBadge().setText(String.valueOf(mOrder.OrderItems.size()));

                if (!mOrder.OrderItems.isEmpty() && !Stock.isSold()) {
                    getButtonComplete().setBackgroundColor(getResources().getColor(R.color.btn_green));
                    getButtonComplete().setText(BackendText.get("build-button-2"));
                } else {
                    getButtonComplete().setBackgroundColor(getResources().getColor(R.color.gray));
                    getButtonComplete().setText(BackendText.get("build-button-1"));
                }
            }
        });
    }

    private void autocompleteBento(DishModel mDish) {
        OrderItem orderItem = mBentoDao.getNewBento();

        if (orderItem.items.get(0).name.isEmpty()) {
            if (mDish == null)
                mDish = mDishDao.getFirstAvailable("main", null);
        }

        mDishDao.updateDishItem(orderItem.items.get(0), mDish);
        orderItem.items.set(0, mDish);

        for (int a = 0; a < aSideDish.size(); a++) {
            mDishDao.updateDishItem(orderItem.items.get(a + 1), aSideDish.get(a));
            orderItem.items.set(a + 1, aSideDish.get(a));
        }

        mOrder.OrderItems.add(orderItem);

        updateUI();
    }


    private void onContinueOrderPressed() {
        if (!mOrder.OrderItems.isEmpty()) {

            for (OrderItem mBento : mOrder.OrderItems) {
                if (!mBentoDao.isBentoComplete(mBento)) {
                    WidgetsUtils.createShortToast("The Bento is Not complete");
                    return;
                }
            }

            if (BentoNowUtils.isValidCompleteOrder(BuildFixedBentoActivity.this)) {
                trackBuildBentos();
                BentoNowUtils.openCompleteOrderActivity(BuildFixedBentoActivity.this);
            }
           /* else
                WidgetsUtils.createShortToast("There are not enough dishes to build a bento, try again later");*/
        } else
            WidgetsUtils.createShortToast("Add some Bentos in your cart");
    }

    private void trackBuildBentos() {
        try {
            for (OrderItem item : mOrder.OrderItems) {

                JSONObject params = new JSONObject();
                params.put("main", item.items.get(0) == null ? "0" : item.items.get(0).itemId);
                params.put("side1", item.items.get(1) == null ? "0" : item.items.get(1).itemId);
                params.put("side2", item.items.get(2) == null ? "0" : item.items.get(2).itemId);
                params.put("side3", item.items.get(3) == null ? "0" : item.items.get(3).itemId);
                params.put("side4", item.items.get(4) == null ? "0" : item.items.get(4).itemId);

                MixpanelUtils.track("Bento Requested", params);
            }
        } catch (Exception e) {
            DebugUtils.logError("trackBuildBentos()", e);
        }
    }

    private void setSideDishList() {
        if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_ORDER_SOLD_OUT)) {
            aSideDish.clear();
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_ORDER_SOLD_OUT, false);
        }

        if (aSideDish.isEmpty()) {
            int[] ids = new int[4];
            DishModel dishModel;

            for (int i = 1; i < 5; ++i) {
                dishModel = mDishDao.getFirstAvailable("side", ids);

                if (dishModel == null)
                    continue;

                ids[i - 1] = dishModel.itemId;

                dishModel = DishDao.clone(dishModel);
                dishModel.type += i;
                aSideDish.add(dishModel);
            }
        }
    }

    private void openSideFixActivity(DishModel mDish) {
        if (aSideDish.isEmpty())
            WidgetsUtils.createShortToast("Empty Side Message");
        else {
            Intent iSideFixActivity = new Intent(this, SelectSideFixActivity.class);
            iSideFixActivity.putExtra(DishModel.TAG, mDish);
            iSideFixActivity.putParcelableArrayListExtra(DishModel.TAG_LIST, aSideDish);
            startActivity(iSideFixActivity);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                BentoNowUtils.openSettingsActivity(BuildFixedBentoActivity.this);
                break;
            case R.id.actionbar_right_btn:
                if (!mOrder.OrderItems.isEmpty()) {
                    onContinueOrderPressed();
                } else {
                    ConfirmationDialog mDialog = new ConfirmationDialog(BuildFixedBentoActivity.this, null, BackendText.get("build-not-complete-text"));
                    mDialog.addAcceptButton(BackendText.get("build-not-complete-confirmation-2"), BuildFixedBentoActivity.this);
                    mDialog.addCancelButton(BackendText.get("build-not-complete-confirmation-1"), BuildFixedBentoActivity.this);
                    mDialog.show();
                }
                break;
            case R.id.button_accept:
                autocompleteBento(null);

                onContinueOrderPressed();
                break;
            case R.id.btn_continue:
                onContinueOrderPressed();
                break;
        }
    }

    @Override
    public void onDishClick(final int iDishPosition) {
        openSideFixActivity(getAdapterListBento().getItem(iDishPosition));
    }

    @Override
    public void onAddToBentoClick(int iDishPosition) {
        autocompleteBento(getAdapterListBento().getItem(iDishPosition));

        WidgetsUtils.createShortToast(R.string.added_to_cart);
    }

    @Override
    protected void onResume() {
        mMenu = Menu.get();
        mOrder = mOrderDao.getCurrentOrder();

        if (mMenu == null || !Settings.status.equals("open")) {
            openErrorActivity();
        } else {
            if (mOrder == null) {
                mOrder = new Order();
                mOrder.MealName = mMenu.meal_name;
                mOrder.MenuType = mMenu.menu_type;

                mOrderDao.insertNewOrder(mOrder);

                MixpanelUtils.track("Began Building A Bento");
            }

            addMainDishes();
            updateUI();
        }

        super.onResume();
    }


    @Override
    public void openErrorActivity() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);
        BentoNowUtils.openErrorActivity(BuildFixedBentoActivity.this);
    }

    @Override
    public void openMainActivity() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);
        BentoNowUtils.openMainActivity(BuildFixedBentoActivity.this);
    }

    @Override
    public void openBuildBentoActivity() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);
    }

    @Override
    public void onConnectService() {
        DebugUtils.logDebug(TAG, "Service Connected");
        mBentoService.setServiceListener(BuildFixedBentoActivity.this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BentoCustomerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mBound) {
            mBentoService.setServiceListener(null);
            unbindService(mConnection);
            mBound = false;
        }
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

    private TextView getTxtToolbarBadge() {
        if (txtToolbarBadge == null)
            txtToolbarBadge = (TextView) findViewById(R.id.actionbar_right_badge);
        return txtToolbarBadge;
    }

    private ImageView getMenuItemBuildBento() {
        if (menuItemBuildBento == null)
            menuItemBuildBento = (ImageView) findViewById(R.id.actionbar_right_btn);
        return menuItemBuildBento;
    }

    private ListView getListBento() {
        if (mListBento == null)
            mListBento = (ListView) findViewById(R.id.list_bento);
        return mListBento;
    }

    private BuildBentoFixListAdapter getAdapterListBento() {
        if (aListBento == null)
            aListBento = new BuildBentoFixListAdapter(this, this);
        return aListBento;
    }

    private BackendButton getButtonComplete() {
        if (btnComplete == null)
            btnComplete = (BackendButton) findViewById(R.id.btn_continue);
        return btnComplete;
    }

    private AutoFitTxtView getTxtPromoName() {
        if (txtPromoName == null)
            txtPromoName = (AutoFitTxtView) findViewById(R.id.txt_promo_name);
        return txtPromoName;
    }

}
