package com.bentonow.bentonow.controllers.order;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.ImageUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.listener.InterfaceCustomerService;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.Stock;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.service.BentoCustomerService;
import com.bentonow.bentonow.ui.AutoFitTxtView;
import com.bentonow.bentonow.ui.BackendAutoFitTextView;
import com.bentonow.bentonow.ui.BackendButton;

import org.json.JSONObject;

public class BuildBentoActivity extends BaseFragmentActivity implements View.OnClickListener, InterfaceCustomerService {

    static final String TAG = "BuildBentoActivity";

    private ImageView actionbar_right_btn;
    private BackendButton btnContinue;
    private BackendAutoFitTextView btnAddAnotherBento;
    private TextView actionbar_right_badge;
    private LinearLayout layoutAddOns;

    private int orderIndex;
    private AutoFitTxtView txtPromoName;
    private AutoFitTxtView txtEta;
    private AutoFitTxtView btnAddOn;
    private ImageView imgMain;
    private ImageView imgSide1;
    private ImageView imgSide2;
    private ImageView imgSide3;
    private ImageView imgSide4;
    private ImageView imgMainSoldOut;
    private ImageView imgSide1SoldOut;
    private ImageView imgSide2SoldOut;
    private ImageView imgSide3SoldOut;
    private ImageView imgSide4SoldOut;
    private RelativeLayout containerMainTitle;
    private RelativeLayout containerSide1Title;
    private RelativeLayout containerSide2Title;
    private RelativeLayout containerSide3Title;
    private RelativeLayout containerSide4Title;

    private TextView txtTitleMain;
    private TextView txtTitleSide1;
    private TextView txtTitleSide2;
    private TextView txtTitleSide3;
    private TextView txtTitleSide4;


    ConfirmationDialog mDialog;

    private Menu mMenu;

    private Order mOrder;

    //private static Tweak<Boolean> showBanner = MixpanelAPI.booleanTweak("Show Banner", false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_build_bento);

        getBtnContinue().setOnClickListener(this);

        actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);
        actionbar_right_badge = (TextView) findViewById(R.id.actionbar_right_badge);

        initActionbar();

        getTxtPromoName().setText(String.format(getString(R.string.build_bento_price), BentoNowUtils.getDefaultPriceBento(DishDao.getLowestMainPrice())));
        getTxtEta().setText(String.format(getString(R.string.build_bento_eta), Settings.eta_min + "-" + Settings.eta_max));

        DebugUtils.logDebug(TAG, "Create: ");
    }

    @Override
    protected void onResume() {
        mMenu = Menu.get();

        if (mMenu == null) {
            openErrorActivity();
        } else {
            mOrder = mOrderDao.getCurrentOrder();

            if (mOrder == null) {
                mOrder = mOrderDao.getNewOrder();
                mOrder.MealName = mMenu.meal_name;
                mOrder.MenuType = mMenu.menu_type;
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.MEAL_NAME, mMenu.meal_name);
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.MENU_TYPE, mMenu.menu_type);

                mOrderDao.updateOrder(mOrder);
            }

            orderIndex = mOrder.currentOrderItem;

            if (mOrder.OrderItems.size() == 0) {
                mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.CUSTOM_BENTO_BOX));
                mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.ADD_ON));
            } else if (mOrder.OrderItems.size() <= orderIndex) {
                orderIndex = mOrder.OrderItems.size() - 1;
            }

            updateUI();
        }


        super.onResume();
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

    public void updateUI() {

        updateDishUI();

        if (mBentoDao.isBentoComplete(mOrder.OrderItems.get(orderIndex)) && !Stock.isSold()) {
            getBtnContinue().setBackgroundColor(getResources().getColor(R.color.btn_green));
            getBtnContinue().setText(BackendText.get("build-button-2"));
            getBtnAddAnotherBento().setTextColor(getResources().getColor(R.color.btn_green));
            getBtnAddAnotherBento().setOnClickListener(this);
            getBtnAddOn().setTextColor(getResources().getColor(R.color.btn_green));
            getBtnAddOn().setOnClickListener(this);
        } else {
            getBtnContinue().setBackgroundColor(getResources().getColor(R.color.gray));
            getBtnContinue().setText(BackendText.get("build-button-1"));
            getBtnAddAnotherBento().setTextColor(getResources().getColor(R.color.btn_green_trans));
            getBtnAddAnotherBento().setOnClickListener(null);
            getBtnAddOn().setTextColor(getResources().getColor(R.color.btn_green_trans));
            getBtnAddOn().setOnClickListener(null);
        }

        if (mOrderDao.countCompletedOrders(mOrder) == 0) {
            actionbar_right_badge.setVisibility(View.GONE);
            actionbar_right_badge.setText("0");
            actionbar_right_btn.setImageResource(R.drawable.ic_ab_bento);
        } else {
            actionbar_right_badge.setVisibility(View.VISIBLE);
            actionbar_right_badge.setText(mOrderDao.countCompletedOrders(mOrder) + "");
            actionbar_right_btn.setImageResource(R.drawable.ic_ab_bento_completed);
        }

    }

    private void updateDishUI() {
        OrderItem item = mOrder.OrderItems.get(orderIndex);

        if (item.items.get(0) == null || item.items.get(0).name.isEmpty()) {
            getImgMain().setImageBitmap(null);
            getTxtTitleMain().setText("");
            getContainerMainTitle().setVisibility(View.GONE);
            getImgMainSoldOut().setVisibility(View.GONE);
        } else {
            ImageUtils.initImageLoader().displayImage(item.items.get(0).image1, getImgMain(), ImageUtils.dishMainImageOptions());
            getTxtTitleMain().setText(item.items.get(0).name);
            getContainerMainTitle().setVisibility(View.VISIBLE);
            getImgMainSoldOut().setVisibility(DishDao.isSoldOut(item.items.get(0), false) ? View.VISIBLE : View.GONE);
        }

        if (item.items.get(1) == null || item.items.get(1).name.isEmpty()) {
            getImgSide1().setImageBitmap(null);
            getTxtTitleSide1().setText("");
            getContainerSide1Title().setVisibility(View.GONE);
            getImgSide1SoldOut().setVisibility(View.GONE);
        } else {
            ImageUtils.initImageLoader().displayImage(item.items.get(1).image1, getImgSide1(), ImageUtils.dishSideImageOptions());
            getTxtTitleSide1().setText(item.items.get(1).name);
            getContainerSide1Title().setVisibility(View.VISIBLE);
            getImgSide1SoldOut().setVisibility(DishDao.isSoldOut(item.items.get(1), false) ? View.VISIBLE : View.GONE);
        }

        if (item.items.get(2) == null || item.items.get(2).name.isEmpty()) {
            getImgSide2().setImageBitmap(null);
            getTxtTitleSide2().setText("");
            getContainerSide2Title().setVisibility(View.GONE);
            getImgSide2SoldOut().setVisibility(View.GONE);
        } else {
            ImageUtils.initImageLoader().displayImage(item.items.get(2).image1, getImgSide2(), ImageUtils.dishSideImageOptions());
            getTxtTitleSide2().setText(item.items.get(2).name);
            getContainerSide2Title().setVisibility(View.VISIBLE);
            getImgSide2SoldOut().setVisibility(DishDao.isSoldOut(item.items.get(2), false) ? View.VISIBLE : View.GONE);
        }

        if (item.items.get(3) == null || item.items.get(3).name.isEmpty()) {
            getImgSide3().setImageBitmap(null);
            getTxtTitleSide3().setText("");
            getContainerSide3Title().setVisibility(View.GONE);
            getImgSide3SoldOut().setVisibility(View.GONE);
        } else {
            ImageUtils.initImageLoader().displayImage(item.items.get(3).image1, getImgSide3(), ImageUtils.dishSideImageOptions());
            getTxtTitleSide3().setText(item.items.get(3).name);
            getContainerSide3Title().setVisibility(View.VISIBLE);
            getImgSide3SoldOut().setVisibility(DishDao.isSoldOut(item.items.get(3), false) ? View.VISIBLE : View.GONE);
        }

        if (item.items.get(4) == null || item.items.get(4).name.isEmpty()) {
            getImgSide4().setImageBitmap(null);
            getTxtTitleSide4().setText("");
            getContainerSide4Title().setVisibility(View.GONE);
            getImgSide4SoldOut().setVisibility(View.GONE);
        } else {
            ImageUtils.initImageLoader().displayImage(item.items.get(4).image1, getImgSide4(), ImageUtils.dishSideImageOptions());
            getTxtTitleSide4().setText(item.items.get(4).name);
            getContainerSide4Title().setVisibility(View.VISIBLE);
            getImgSide4SoldOut().setVisibility(DishDao.isSoldOut(item.items.get(4), false) ? View.VISIBLE : View.GONE);
        }

    }

    void autocomplete() {
        if (mOrder.OrderItems.get(orderIndex).items.get(0).name.isEmpty()) {
            mOrder.OrderItems.get(orderIndex).items.set(0, mDishDao.updateDishItem(mOrder.OrderItems.get(orderIndex).items.get(0), mDishDao.getFirstAvailable("main", null)));
        }

        int[] ids = new int[4];

        for (int i = 1; i < 5; ++i) {
            if (mOrder.OrderItems.get(orderIndex).items.get(i).name.isEmpty()) {
                DishModel dishModel = mDishDao.getFirstAvailable("side", ids);

                if (dishModel == null)
                    continue;

                ids[i - 1] = dishModel.itemId;

                dishModel = DishDao.clone(dishModel);
                dishModel.type += i;

                mOrder.OrderItems.get(orderIndex).items.set(i, mDishDao.updateDishItem(mOrder.OrderItems.get(orderIndex).items.get(i), dishModel));
            }

            updateUI();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                BentoNowUtils.openSettingsActivity(BuildBentoActivity.this);
                break;
            case R.id.actionbar_right_btn:
                // if (Order.current.OrderItems.get(orderIndex).isComplete()) {
                if (mBentoDao.isBentoComplete(mOrder.OrderItems.get(orderIndex))) {
                    onContinueOrderPressed();
                } else {
                    mDialog = new ConfirmationDialog(BuildBentoActivity.this, null, BackendText.get("build-not-complete-text"));
                    mDialog.addAcceptButton(BackendText.get("build-not-complete-confirmation-2"), BuildBentoActivity.this);
                    mDialog.addCancelButton(BackendText.get("build-not-complete-confirmation-1"), BuildBentoActivity.this);

                    mDialog.show();
                }
                break;
            case R.id.button_accept:
                autocomplete();

                if (mOrderDao.countCompletedOrders(mOrder) > 0) {
                    onContinueOrderPressed();
                }
                break;
            case R.id.btn_cancel:
                /*if (Order.current.OrderItems.size() > 1) {
                    Order.current.OrderItems.remove(orderIndex);
                    orderIndex = Order.current.OrderItems.size() - 1;
                    onContinueOrderPressed();
                }
                if (mOrder.OrderItems.size() > 1) {
                    mBentoDao.removeBento(mOrder.OrderItems.get(orderIndex).order_pk);
                    mOrder.OrderItems.remove(orderIndex);
                    orderIndex = Order.current.OrderItems.size() - 1;
                    onContinueOrderPressed();
                }
                */
                break;
            case R.id.btn_continue:
                onContinueOrderPressed();
                break;
            case R.id.btn_add_on_add_on:
                Intent mAddOnActivity = new Intent(BuildBentoActivity.this, AddOnActivity.class);
                mAddOnActivity.putExtra(AddOnActivity.TAG_OPEN_BY, ConstantUtils.optOpenAddOn.BUILDER);
                startActivity(mAddOnActivity);
                overridePendingTransition(R.anim.bottom_slide_in, R.anim.none);
                break;
            case R.id.btn_add_another_bento:
                onAddAnotherBentoPressed();
                break;
            default:
                DebugUtils.logError(TAG, String.valueOf(v.getId()));
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

    public void onAddAnotherBentoPressed() {
        mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.CUSTOM_BENTO_BOX));
        mOrder.currentOrderItem = orderIndex = mOrder.OrderItems.size() - 1;

        mOrderDao.updateOrder(mOrder);

        updateUI();
    }

    public void onContinueOrderPressed() {

        String sSoldOutItems = mOrderDao.calculateSoldOutItems(mOrder);

    /*    if (!Order.current.OrderItems.get(orderIndex).isComplete()) {
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
            }*/
        if (!mBentoDao.isBentoComplete(mOrder.OrderItems.get(orderIndex))) {
            if (mOrder.OrderItems.get(orderIndex).items.get(0) == null) {
                startActivity(new Intent(this, SelectMainCustomActivity.class));
            } else if (mOrder.OrderItems.get(orderIndex).items.get(1) == null) {
                Intent intent = new Intent(this, SelectSideCustomActivity.class);
                intent.putExtra("orderIndex", orderIndex);
                intent.putExtra("itemIndex", 1);
                startActivity(intent);
            } else if (mOrder.OrderItems.get(orderIndex).items.get(2) == null) {
                Intent intent = new Intent(this, SelectSideCustomActivity.class);
                intent.putExtra("orderIndex", orderIndex);
                intent.putExtra("itemIndex", 2);
                startActivity(intent);
            } else if (mOrder.OrderItems.get(orderIndex).items.get(3) == null) {
                Intent intent = new Intent(this, SelectSideCustomActivity.class);
                intent.putExtra("orderIndex", orderIndex);
                intent.putExtra("itemIndex", 3);
                startActivity(intent);
            } else if (mOrder.OrderItems.get(orderIndex).items.get(4) == null) {
                Intent intent = new Intent(this, SelectSideCustomActivity.class);
                intent.putExtra("orderIndex", orderIndex);
                intent.putExtra("itemIndex", 4);
                startActivity(intent);
            }
        } else if (!sSoldOutItems.isEmpty()) {
            updateUI();
            WidgetsUtils.createShortToast(String.format(getString(R.string.error_sold_out_items), sSoldOutItems));
        } else if (BentoNowUtils.isValidCompleteOrder(BuildBentoActivity.this)) {
            track();
            BentoNowUtils.openCompleteOrderActivity(BuildBentoActivity.this);
        }
    }

    private void track() {
        try {
            OrderItem item = mOrder.OrderItems.get(orderIndex);

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
    public void openErrorActivity() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);
        BentoNowUtils.openErrorActivity(BuildBentoActivity.this);
    }

    @Override
    public void openMainActivity() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);
        BentoNowUtils.openMainActivity(BuildBentoActivity.this);
    }

    @Override
    public void openBuildBentoActivity() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);

        finish();
        BentoNowUtils.openBuildBentoActivity(BuildBentoActivity.this);
    }

    @Override
    public void onConnectService() {
        DebugUtils.logDebug(TAG, "Service Connected");
        mBentoService.setServiceListener(BuildBentoActivity.this);

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


    private AutoFitTxtView getTxtPromoName() {
        if (txtPromoName == null)
            txtPromoName = (AutoFitTxtView) findViewById(R.id.txt_promo_name);
        return txtPromoName;
    }

    private AutoFitTxtView getTxtEta() {
        if (txtEta == null)
            txtEta = (AutoFitTxtView) findViewById(R.id.txt_promo_eta);
        return txtEta;
    }

    private AutoFitTxtView getBtnAddOn() {
        if (btnAddOn == null)
            btnAddOn = (AutoFitTxtView) findViewById(R.id.btn_add_on_add_on);
        return btnAddOn;
    }

    private BackendButton getBtnContinue() {
        if (btnContinue == null)
            btnContinue = (BackendButton) findViewById(R.id.btn_continue);
        return btnContinue;
    }

    private BackendAutoFitTextView getBtnAddAnotherBento() {
        if (btnAddAnotherBento == null)
            btnAddAnotherBento = (BackendAutoFitTextView) findViewById(R.id.btn_add_another_bento);
        return btnAddAnotherBento;
    }

    private LinearLayout getLayoutAddOns() {
        if (layoutAddOns == null)
            layoutAddOns = (LinearLayout) findViewById(R.id.layout_add_ons);
        return layoutAddOns;
    }

    private ImageView getImgMain() {
        if (imgMain == null)
            imgMain = (ImageView) findViewById(R.id.img_main);
        return imgMain;
    }

    private ImageView getImgSide1() {
        if (imgSide1 == null)
            imgSide1 = (ImageView) findViewById(R.id.img_side1);
        return imgSide1;
    }

    private ImageView getImgSide2() {
        if (imgSide2 == null)
            imgSide2 = (ImageView) findViewById(R.id.img_side2);
        return imgSide2;
    }

    private ImageView getImgSide3() {
        if (imgSide3 == null)
            imgSide3 = (ImageView) findViewById(R.id.img_side3);
        return imgSide3;
    }

    private ImageView getImgSide4() {
        if (imgSide4 == null)
            imgSide4 = (ImageView) findViewById(R.id.img_side4);
        return imgSide4;
    }

    private ImageView getImgMainSoldOut() {
        if (imgMainSoldOut == null)
            imgMainSoldOut = (ImageView) findViewById(R.id.img_main_sold_out);
        return imgMainSoldOut;
    }

    private ImageView getImgSide1SoldOut() {
        if (imgSide1SoldOut == null)
            imgSide1SoldOut = (ImageView) findViewById(R.id.img_side1_sold_out);
        return imgSide1SoldOut;
    }

    private ImageView getImgSide2SoldOut() {
        if (imgSide2SoldOut == null)
            imgSide2SoldOut = (ImageView) findViewById(R.id.img_side2_sold_out);
        return imgSide2SoldOut;
    }

    private ImageView getImgSide3SoldOut() {
        if (imgSide3SoldOut == null)
            imgSide3SoldOut = (ImageView) findViewById(R.id.img_side3_sold_out);
        return imgSide3SoldOut;
    }

    private ImageView getImgSide4SoldOut() {
        if (imgSide4SoldOut == null)
            imgSide4SoldOut = (ImageView) findViewById(R.id.img_side4_sold_out);
        return imgSide4SoldOut;
    }

    private TextView getTxtTitleMain() {
        if (txtTitleMain == null)
            txtTitleMain = (TextView) findViewById(R.id.txt_main_title);
        return txtTitleMain;
    }

    private TextView getTxtTitleSide1() {
        if (txtTitleSide1 == null)
            txtTitleSide1 = (TextView) findViewById(R.id.txt_side1_title);
        return txtTitleSide1;
    }

    private TextView getTxtTitleSide2() {
        if (txtTitleSide2 == null)
            txtTitleSide2 = (TextView) findViewById(R.id.txt_side2_title);
        return txtTitleSide2;
    }

    private TextView getTxtTitleSide3() {
        if (txtTitleSide3 == null)
            txtTitleSide3 = (TextView) findViewById(R.id.txt_side3_title);
        return txtTitleSide3;
    }

    private TextView getTxtTitleSide4() {
        if (txtTitleSide4 == null)
            txtTitleSide4 = (TextView) findViewById(R.id.txt_side4_title);
        return txtTitleSide4;
    }

    private RelativeLayout getContainerMainTitle() {
        if (containerMainTitle == null)
            containerMainTitle = (RelativeLayout) findViewById(R.id.container_main_title);
        return containerMainTitle;
    }

    private RelativeLayout getContainerSide1Title() {
        if (containerSide1Title == null)
            containerSide1Title = (RelativeLayout) findViewById(R.id.container_side1_title);
        return containerSide1Title;
    }

    private RelativeLayout getContainerSide2Title() {
        if (containerSide2Title == null)
            containerSide2Title = (RelativeLayout) findViewById(R.id.container_side2_title);
        return containerSide2Title;
    }

    private RelativeLayout getContainerSide3Title() {
        if (containerSide3Title == null)
            containerSide3Title = (RelativeLayout) findViewById(R.id.container_side3_title);
        return containerSide3Title;
    }

    private RelativeLayout getContainerSide4Title() {
        if (containerSide4Title == null)
            containerSide4Title = (RelativeLayout) findViewById(R.id.container_side4_title);
        return containerSide4Title;
    }


}
