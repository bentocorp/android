package com.bentonow.bentonow.controllers.order;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.controllers.session.SettingsActivity;
import com.bentonow.bentonow.controllers.session.SignUpActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Item;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.ui.CustomDialog;
import com.google.gson.Gson;

import org.json.JSONObject;

public class BuildFixedBentoActivity extends BaseActivity implements View.OnClickListener {

    static final String TAG = "BuildFixedBentoActivity";

    private ImageView menuItemBuildBento;

    private CustomDialog dialog;

    private int orderIndex = 0;
    public static boolean bIsOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_build_fix_bento);

        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(BackendText.get("build-title"));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_user);
        actionbar_left_btn.setOnClickListener(this);

        getMenuItemBuildBento().setImageResource(R.drawable.ic_ab_bento);
        getMenuItemBuildBento().setOnClickListener(this);

        initOrder();

        Log.i(TAG, new Gson().toJson(Menu.list));
    }

    void initOrder() {
    }

    public void updateUI() {
    }

    void autocomplete() {
        OrderItem orderItem = Order.current.OrderItems.get(0);
        Item item;

        if (orderItem.items.get(0) == null) {
            orderItem.items.set(0, Item.getFirstAvailable("main", null));
        }

        int[] ids = new int[4];

        for (int i = 1; i < 5; ++i) {
            if (orderItem.items.get(i) == null) {
                item = Item.getFirstAvailable("side", ids);

                if (item == null) continue;

                ids[i - 1] = item.itemId;

                item = item.clone();
                item.type += i;
                orderItem.items.set(i, item);
            }
        }

        updateUI();
    }

    //****
    // Click
    //****

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.actionbar_right_btn:
                if (Order.current.OrderItems.get(orderIndex).isComplete()) {
                    onContinueOrderPressed(null);
                } else {
                    dialog = new CustomDialog(
                            this,
                            BackendText.get("build-not-complete-text"),
                            BackendText.get("build-not-complete-confirmation-2"),
                            BackendText.get("build-not-complete-confirmation-1")
                    );
                    dialog.setOnOkPressed(this);
                    dialog.setOnCancelPressed(this);
                    dialog.show();
                }
                break;
            case R.id.btn_ok:
                dialog.dismiss();
                autocomplete();

                if (Order.countCompletedOrders() > 0) {
                    onContinueOrderPressed(null);
                }
                break;
            case R.id.btn_cancel:
                dialog.dismiss();

                if (Order.current.OrderItems.size() > 1) {
                    Order.current.OrderItems.remove(orderIndex);
                    orderIndex = Order.current.OrderItems.size() - 1;
                    onContinueOrderPressed(null);
                }
                break;
        }
    }


    public void onContinueOrderPressed(View view) {
        if (!Order.current.OrderItems.get(orderIndex).isComplete()) {
            if (Order.current.OrderItems.get(orderIndex).items.get(0) == null) {
                startActivity(new Intent(this, SelectMainActivity.class));
            } else if (Order.current.OrderItems.get(orderIndex).items.get(1) == null) {
                Intent intent = new Intent(this, SelectSideActivity.class);
                intent.putExtra("orderIndex", orderIndex);
                intent.putExtra("itemIndex", 1);
                startActivity(intent);
            } else if (Order.current.OrderItems.get(orderIndex).items.get(2) == null) {
                Intent intent = new Intent(this, SelectSideActivity.class);
                intent.putExtra("orderIndex", orderIndex);
                intent.putExtra("itemIndex", 2);
                startActivity(intent);
            } else if (Order.current.OrderItems.get(orderIndex).items.get(3) == null) {
                Intent intent = new Intent(this, SelectSideActivity.class);
                intent.putExtra("orderIndex", orderIndex);
                intent.putExtra("itemIndex", 3);
                startActivity(intent);
            } else if (Order.current.OrderItems.get(orderIndex).items.get(4) == null) {
                Intent intent = new Intent(this, SelectSideActivity.class);
                intent.putExtra("orderIndex", orderIndex);
                intent.putExtra("itemIndex", 4);
                startActivity(intent);
            }
        } else if (User.current == null) {
            track();
            startActivity(new Intent(this, SignUpActivity.class));
        } else if (Order.location == null || Order.address == null) {
            Intent intent = new Intent(this, DeliveryLocationActivity.class);
            intent.putExtra(DeliveryLocationActivity.TAG_DELIVERY_ACTION, ConstantUtils.optDeliveryAction.COMPLETE_ORDER);
            startActivity(intent);
        } else {
            if (!BentoNowUtils.isSoldOutOrder(Order.current.OrderItems.get(orderIndex))) {
                Order.current.OrderItems.get(orderIndex).bIsSoldoOut = false;
                track();
                startActivity(new Intent(this, CompleteOrderActivity.class));
            } else
                WidgetsUtils.createShortToast(R.string.error_sold_out_items);
        }
    }

    //****
    // Mixpanel
    //****

    private void track() {
        try {
            OrderItem item = Order.current.OrderItems.get(orderIndex);

            JSONObject params = new JSONObject();
            params.put("main", item.items.get(0) == null ? "0" : item.items.get(0).itemId);
            params.put("side1", item.items.get(1) == null ? "0" : item.items.get(1).itemId);
            params.put("side2", item.items.get(2) == null ? "0" : item.items.get(2).itemId);
            params.put("side3", item.items.get(3) == null ? "0" : item.items.get(3).itemId);
            params.put("side4", item.items.get(4) == null ? "0" : item.items.get(4).itemId);

            MixpanelUtils.track("Bento Requested", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bIsOpen = true;

        if (Order.current == null) {
            Order.current = new Order();

            MixpanelUtils.track("Began Building A Bento");
        }

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

    private ImageView getMenuItemBuildBento() {
        if (menuItemBuildBento == null)
            menuItemBuildBento = (ImageView) findViewById(R.id.actionbar_right_btn);
        return menuItemBuildBento;
    }
}
