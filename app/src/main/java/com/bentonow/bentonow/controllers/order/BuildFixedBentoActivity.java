package com.bentonow.bentonow.controllers.order;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.adapter.BuildBentoFixListAdapter;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.controllers.session.SettingsActivity;
import com.bentonow.bentonow.controllers.session.SignUpActivity;
import com.bentonow.bentonow.listener.ListenerMainDishFix;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Item;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Stock;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.ui.BackendButton;

import org.json.JSONObject;

import java.util.ArrayList;

public class BuildFixedBentoActivity extends BaseActivity implements View.OnClickListener, ListenerMainDishFix {

    static final String TAG = "BuildFixedBentoActivity";

    private ImageView menuItemBuildBento;
    private ImageView actionbarLeftBtn;
    private TextView txtToolbarTitle;
    private TextView txtToolbarBadge;
    private BackendButton btnComplete;
    private ListView mListBento;

    private BuildBentoFixListAdapter aListBento;

    private ArrayList<Item> aSideDish = new ArrayList<>();

    public static boolean bIsOpen = false;

    private Menu mMenu;

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

    }

    private void addMainDishes() {
        mMenu = Menu.get();

        if (mMenu == null) {
            ConfirmationDialog mDialog = new ConfirmationDialog(BuildFixedBentoActivity.this, null, "There is no current menu to show");
            mDialog.addAcceptButton("OK", BuildFixedBentoActivity.this);
            mDialog.show();
        } else {
            getAdapterListBento().clear();
            for (Item item : mMenu.items) {
                if (item.type.equals("main"))
                    getAdapterListBento().add(item);
            }

            setSideDishList();
        }

        getAdapterListBento().addAll();
    }

    private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getTxtToolbarBadge().setVisibility(Order.current.OrderItems.size() != 0 ? View.VISIBLE : View.INVISIBLE);
                getTxtToolbarBadge().setText(String.valueOf(Order.current.OrderItems.size()));

                if (!Order.current.OrderItems.isEmpty() && !Stock.isSold()) {
                    getButtonComplete().setBackgroundColor(getResources().getColor(R.color.btn_green));
                    getButtonComplete().setText(BackendText.get("build-button-2"));
                } else {
                    getButtonComplete().setBackgroundColor(getResources().getColor(R.color.gray));
                    getButtonComplete().setText(BackendText.get("build-button-1"));
                }
            }
        });
    }

    private void autocompleteBento(Item mDish) {
        OrderItem orderItem = new OrderItem();

        if (orderItem.items.get(0) == null) {
            if (mDish == null)
                orderItem.items.set(0, Item.getFirstAvailable("main", null));
            else
                orderItem.items.set(0, mDish);
        }

        for (int a = 0; a < aSideDish.size(); a++) {
            orderItem.items.set(a + 1, aSideDish.get(a));
        }

        Order.current.OrderItems.add(orderItem);

        updateUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.actionbar_right_btn:
                if (!Order.current.OrderItems.isEmpty()) {
                    onContinueOrderPressed();
                } else {
                    ConfirmationDialog mDialog = new ConfirmationDialog(BuildFixedBentoActivity.this, null, BackendText.get("build-not-complete-text"));
                    mDialog.addAcceptButton(BackendText.get("build-not-complete-confirmation-2"), BuildFixedBentoActivity.this);
                    mDialog.addAcceptButton(BackendText.get("build-not-complete-confirmation-1"), BuildFixedBentoActivity.this);
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


    private void onContinueOrderPressed() {
        if (!Order.current.OrderItems.isEmpty()) {

            boolean bOrderComplete = true;
            for (OrderItem mOrder : Order.current.OrderItems) {
                bOrderComplete = mOrder.isComplete();
                if (!bOrderComplete)
                    return;
            }

            if (bOrderComplete)
                if (User.current == null) {
                    trackBuildBentos();
                    startActivity(new Intent(this, SignUpActivity.class));
                } else if (Order.location == null || Order.address == null) {
                    Intent intent = new Intent(this, DeliveryLocationActivity.class);
                    intent.putExtra(DeliveryLocationActivity.TAG_DELIVERY_ACTION, ConstantUtils.optDeliveryAction.COMPLETE_ORDER);
                    startActivity(intent);
                } else {
                    trackBuildBentos();
                    startActivity(new Intent(this, CompleteOrderActivity.class));
                }
            else
                WidgetsUtils.createShortToast("There are not enough dishes to build a bento, try again later");
        } else
            WidgetsUtils.createShortToast("Add some Bentos in your cart");
    }

    private void trackBuildBentos() {
        try {
            for (OrderItem item : Order.current.OrderItems) {

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
            Item item;

            for (int i = 1; i < 5; ++i) {
                item = Item.getFirstAvailable("side", ids);

                if (item == null)
                    continue;

                ids[i - 1] = item.itemId;

                item = item.clone();
                item.type += i;
                aSideDish.add(item);
            }
        }
    }

    private void openSideFixActivity(Item mDish) {
        if (aSideDish.isEmpty())
            WidgetsUtils.createShortToast("Empty Side Message");
        else {
            Intent iSideFixActivity = new Intent(this, SelectSideFixActivity.class);
            iSideFixActivity.putExtra(Item.TAG, mDish);
            iSideFixActivity.putParcelableArrayListExtra(Item.TAG_LIST, aSideDish);
            startActivity(iSideFixActivity);
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
        super.onResume();
        bIsOpen = true;


        if (Order.current == null) {
            Order.current = new Order();

            MixpanelUtils.track("Began Building A Bento");
        }

        addMainDishes();
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bIsOpen = false;
        if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_STORE_CHANGIN))
            finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        bIsOpen = false;
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


}
