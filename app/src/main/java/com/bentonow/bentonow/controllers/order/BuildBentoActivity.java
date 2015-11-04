package com.bentonow.bentonow.controllers.order;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Stock;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.ui.AutoFitTxtView;
import com.bentonow.bentonow.ui.BackendButton;
import com.bentonow.bentonow.ui.ItemHolder;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.mixpanel.android.mpmetrics.Tweak;

import org.json.JSONObject;

public class BuildBentoActivity extends BaseActivity implements View.OnClickListener {

    static final String TAG = "BuildBentoActivity";

    private ImageView actionbar_right_btn;
    private BackendButton btn_continue;
    private BackendButton btn_add_another_bento;
    private TextView actionbar_right_badge;

    private int orderIndex;
    private ItemHolder mainHolder;
    private ItemHolder side1Holder;
    private ItemHolder side2Holder;
    private ItemHolder side3Holder;
    private ItemHolder side4Holder;
    private AutoFitTxtView txtPromoName;

    ConfirmationDialog mDialog;

    private Menu mMenu;

    public static boolean bIsOpen = false;
    private static Tweak<Boolean> showBanner = MixpanelAPI.booleanTweak("Show Banner", false);

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

        getTxtPromoName().setText(String.format(getString(R.string.build_bento_price), BentoNowUtils.getDefaultPriceBento(DishDao.getLowestMainPrice())));

        BentoNowUtils.rotateBanner(getTxtPromoName());


    }

    @Override
    protected void onResume() {
        super.onResume();
        bIsOpen = true;

        mMenu = Menu.get();

        if (mMenu == null) {
            BentoNowUtils.openErrorActivity(BuildBentoActivity.this);
        } else {
            if (Order.current == null) {
                Order.current = new Order();
                Order.current.MealName = mMenu.meal_name;
                Order.current.MenuType = mMenu.menu_type;

                MixpanelUtils.track("Began Building A Bento");
            }

            orderIndex = Order.current.currentOrderItem;

            if (Order.current.OrderItems.size() == 0) {
                Order.current.OrderItems.add(new OrderItem());
            } else if (Order.current.OrderItems.size() <= orderIndex) {
                orderIndex = Order.current.OrderItems.size() - 1;
            }

            updateUI();
        }


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
        DishModel dishModel;

        if (orderItem.items.get(0) == null) {
            orderItem.items.set(0, DishDao.getFirstAvailable("main", null));
        }

        int[] ids = new int[4];

        for (int i = 1; i < 5; ++i) {
            if (orderItem.items.get(i) == null) {
                dishModel = DishDao.getFirstAvailable("side", ids);

                if (dishModel == null) continue;

                ids[i - 1] = dishModel.itemId;

                dishModel = DishDao.clone(dishModel);
                dishModel.type += i;
                orderItem.items.set(i, dishModel);
            }
        }

        updateUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                BentoNowUtils.openSettingsActivity(BuildBentoActivity.this);
                break;
            case R.id.actionbar_right_btn:
                if (Order.current.OrderItems.get(orderIndex).isComplete()) {
                    onContinueOrderPressed(null);
                } else {
                    mDialog = new ConfirmationDialog(BuildBentoActivity.this, null, BackendText.get("build-not-complete-text"));
                    mDialog.addAcceptButton(BackendText.get("build-not-complete-confirmation-2"), BuildBentoActivity.this);
                    mDialog.addCancelButton(BackendText.get("build-not-complete-confirmation-1"), BuildBentoActivity.this);

                    mDialog.show();
                }
                break;
            case R.id.button_accept:
                autocomplete();

                if (Order.countCompletedOrders() > 0) {
                    onContinueOrderPressed(null);
                }
                break;
            case R.id.btn_cancel:
                if (Order.current.OrderItems.size() > 1) {
                    Order.current.OrderItems.remove(orderIndex);
                    orderIndex = Order.current.OrderItems.size() - 1;
                    onContinueOrderPressed(null);
                }
                break;
        }
    }

    public void onAddMainPressed(View view) {
        Intent intent = new Intent(this, SelectMainCustomActivity.class);
        intent.putExtra("orderIndex", orderIndex);
        startActivity(intent);
    }

    public void onAddSide1Pressed(View view) {
        Intent intent = new Intent(this, SelectSideCustomActivity.class);
        intent.putExtra("orderIndex", orderIndex);
        intent.putExtra("itemIndex", 1);
        startActivity(intent);
    }

    public void onAddSide2Pressed(View view) {
        Intent intent = new Intent(this, SelectSideCustomActivity.class);
        intent.putExtra("orderIndex", orderIndex);
        intent.putExtra("itemIndex", 2);
        startActivity(intent);
    }

    public void onAddSide3Pressed(View view) {
        Intent intent = new Intent(this, SelectSideCustomActivity.class);
        intent.putExtra("orderIndex", orderIndex);
        intent.putExtra("itemIndex", 3);
        startActivity(intent);
    }

    public void onAddSide4Pressed(View view) {
        Intent intent = new Intent(this, SelectSideCustomActivity.class);
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
                startActivity(new Intent(this, SelectMainCustomActivity.class));
            } else if (Order.current.OrderItems.get(orderIndex).items.get(1) == null) {
                Intent intent = new Intent(this, SelectSideCustomActivity.class);
                intent.putExtra("orderIndex", orderIndex);
                intent.putExtra("itemIndex", 1);
                startActivity(intent);
            } else if (Order.current.OrderItems.get(orderIndex).items.get(2) == null) {
                Intent intent = new Intent(this, SelectSideCustomActivity.class);
                intent.putExtra("orderIndex", orderIndex);
                intent.putExtra("itemIndex", 2);
                startActivity(intent);
            } else if (Order.current.OrderItems.get(orderIndex).items.get(3) == null) {
                Intent intent = new Intent(this, SelectSideCustomActivity.class);
                intent.putExtra("orderIndex", orderIndex);
                intent.putExtra("itemIndex", 3);
                startActivity(intent);
            } else if (Order.current.OrderItems.get(orderIndex).items.get(4) == null) {
                Intent intent = new Intent(this, SelectSideCustomActivity.class);
                intent.putExtra("orderIndex", orderIndex);
                intent.putExtra("itemIndex", 4);
                startActivity(intent);
            }
        } else if (BentoNowUtils.isValidCompleteOrder(BuildBentoActivity.this)) {
            String sSoldOutItems = BentoNowUtils.calculateSoldOutItems();
            if (sSoldOutItems.isEmpty()) {
                Order.current.OrderItems.get(orderIndex).bIsSoldoOut = false;
                track();
                BentoNowUtils.openCompleteOrderActivity(BuildBentoActivity.this);
            } else {
                updateUI();
                WidgetsUtils.createShortToast(String.format(getString(R.string.error_sold_out_items), sSoldOutItems));
            }
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
            DebugUtils.logError(TAG, "track(): " + e.toString());
        }
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

    private AutoFitTxtView getTxtPromoName() {
        if (txtPromoName == null)
            txtPromoName = (AutoFitTxtView) findViewById(R.id.txt_promo_name);
        return txtPromoName;
    }
}
