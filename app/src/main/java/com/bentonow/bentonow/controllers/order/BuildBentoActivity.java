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
import com.bentonow.bentonow.Utils.Mixpanel;
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
import com.bentonow.bentonow.model.Stock;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.ui.BackendButton;
import com.bentonow.bentonow.ui.CustomDialog;
import com.bentonow.bentonow.ui.ItemHolder;
import com.google.gson.Gson;

import org.json.JSONObject;

public class BuildBentoActivity extends BaseActivity implements View.OnClickListener {

    static final String TAG = "BuildBentoActivity";

    ImageView actionbar_right_btn;
    BackendButton btn_continue;
    BackendButton btn_add_another_bento;
    TextView actionbar_right_badge;

    int orderIndex;
    ItemHolder mainHolder;
    ItemHolder side1Holder;
    ItemHolder side2Holder;
    ItemHolder side3Holder;
    ItemHolder side4Holder;

    CustomDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_build_bento);

        btn_continue = (BackendButton) findViewById(R.id.btn_continue);
        btn_add_another_bento = (BackendButton) findViewById(R.id.btn_add_another_bento);
        actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);
        actionbar_right_badge = (TextView) findViewById(R.id.actionbar_right_badge);

        initActionbar();
        initOrder();

        Log.i(TAG, new Gson().toJson(Menu.list));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Order.current == null) {
            Order.current = new Order();

            Mixpanel.track(this, "Began building a Bento");
        }

        orderIndex = Order.current.currentOrderItem;

        if (Order.current.OrderItems.size() == 0) {
            Order.current.OrderItems.add(new OrderItem());
        } else if (Order.current.OrderItems.size() <= orderIndex) {
            orderIndex = Order.current.OrderItems.size() - 1;
        }

        updateUI();
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(BackendText.get("build-title"));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_user);
        actionbar_left_btn.setOnClickListener(this);

        actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);
        actionbar_right_btn.setImageResource(R.drawable.ic_ab_bento);
        actionbar_right_btn.setOnClickListener(this);
    }

    void initOrder() {
        mainHolder = new ItemHolder(
                this,
                null,
                R.id.img_main,
                R.id.txt_main_title,
                0,
                R.id.img_main_sold_out,
                R.id.btn_add_main,
                R.id.container_main_title
        );

        side1Holder = new ItemHolder(
                this,
                null,
                R.id.img_side1,
                R.id.txt_side1_title,
                0,
                R.id.img_side1_sold_out,
                R.id.btn_add_side1,
                R.id.container_side1_title
        );

        side2Holder = new ItemHolder(
                this,
                null,
                R.id.img_side2,
                R.id.txt_side2_title,
                0,
                R.id.img_side2_sold_out,
                R.id.btn_add_side2,
                R.id.container_side2_title
        );

        side3Holder = new ItemHolder(
                this,
                null,
                R.id.img_side3,
                R.id.txt_side3_title,
                0,
                R.id.img_side3_sold_out,
                R.id.btn_add_side3,
                R.id.container_side3_title
        );

        side4Holder = new ItemHolder(
                this,
                null,
                R.id.img_side4,
                R.id.txt_side4_title,
                0,
                R.id.img_side4_sold_out,
                R.id.btn_add_side4,
                R.id.container_side4_title
        );
    }

    public void updateUI() {
        if (Order.current.OrderItems.get(orderIndex).isComplete() && !Stock.isSold()) {
            btn_continue.setBackgroundColor(getResources().getColor(R.color.btn_green));
            btn_continue.setText(BackendText.get("build-button-2"));
            btn_add_another_bento.setBackgroundResource(R.drawable.btn_gray);
            btn_add_another_bento.setTextColor(getResources().getColor(R.color.btn_green));
        } else {
            btn_continue.setBackgroundColor(getResources().getColor(R.color.gray));
            btn_continue.setText(BackendText.get("build-button-1"));
            btn_add_another_bento.setBackgroundResource(R.drawable.btn_gray_pressed);
            btn_add_another_bento.setTextColor(getResources().getColor(R.color.btn_green_trans));
        }

        if (Order.countCompletedOrders() == 0) {
            actionbar_right_badge.setVisibility(View.GONE);
            actionbar_right_badge.setText("0");
            actionbar_right_btn.setImageResource(R.drawable.ic_ab_bento);
        } else {
            actionbar_right_badge.setVisibility(View.VISIBLE);
            actionbar_right_badge.setText(Order.countCompletedOrders() + "");
            actionbar_right_btn.setImageResource(R.drawable.ic_ab_bento_completed);
        }

        OrderItem item = Order.current.OrderItems.get(orderIndex);

        mainHolder.setData(item.items.get(0), false);
        side1Holder.setData(item.items.get(1), false);
        side2Holder.setData(item.items.get(2), false);
        side3Holder.setData(item.items.get(3), false);
        side4Holder.setData(item.items.get(4), false);
    }

    void autocomplete() {
        OrderItem orderItem = Order.current.OrderItems.get(orderIndex);
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

    public void onAddMainPressed(View view) {
        Intent intent = new Intent(this, SelectMainActivity.class);
        intent.putExtra("orderIndex", orderIndex);
        startActivity(intent);
    }

    public void onAddSide1Pressed(View view) {
        Intent intent = new Intent(this, SelectSideActivity.class);
        intent.putExtra("orderIndex", orderIndex);
        intent.putExtra("itemIndex", 1);
        startActivity(intent);
    }

    public void onAddSide2Pressed(View view) {
        Intent intent = new Intent(this, SelectSideActivity.class);
        intent.putExtra("orderIndex", orderIndex);
        intent.putExtra("itemIndex", 2);
        startActivity(intent);
    }

    public void onAddSide3Pressed(View view) {
        Intent intent = new Intent(this, SelectSideActivity.class);
        intent.putExtra("orderIndex", orderIndex);
        intent.putExtra("itemIndex", 3);
        startActivity(intent);
    }

    public void onAddSide4Pressed(View view) {
        Intent intent = new Intent(this, SelectSideActivity.class);
        intent.putExtra("orderIndex", orderIndex);
        intent.putExtra("itemIndex", 4);
        startActivity(intent);
    }

    public void onAddAnotherBentoPressed(View view) {
        if (!Order.current.OrderItems.get(orderIndex).isComplete() || Stock.isSold()) return;

        Order.current.OrderItems.add(new OrderItem());
        Order.current.currentOrderItem = orderIndex = Order.current.OrderItems.size() - 1;

        updateUI();
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
            if (BentoNowUtils.calculateSoldOutItems().isEmpty()) {
                track();
                startActivity(new Intent(this, CompleteOrderActivity.class));
            }else
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

            Mixpanel.track(this, "Bento requested", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_STORE_CHANGIN))
            finish();
    }


}
