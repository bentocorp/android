package com.bentonow.bentonow.controllers.order;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.mixpanel.android.mpmetrics.Tweak;

import org.json.JSONObject;

public class BuildBentoActivity extends BaseFragmentActivity implements View.OnClickListener, InterfaceCustomerService {

    static final String TAG = "BuildBentoActivity";

    private BackendButton btnContinue;
    private BackendAutoFitTextView btnAddAnotherBento;
    private TextView txtNumBento;
    private LinearLayout layoutAddOns;

    private int orderIndex;
    private AutoFitTxtView txtPromoName;
    private AutoFitTxtView txtEta;
    private AutoFitTxtView btnAddOn;
    private AutoFitTxtView txtDateTimeToolbar;
    private ImageView actionbarLeftBtn;
    private ImageView actionbarRightBtn;

    private ImageView imgMain;
    private ImageView imgMain4;
    private ImageView imgSide1;
    private ImageView imgSide14;
    private ImageView imgSide2;
    private ImageView imgSide24;
    private ImageView imgSide3;
    private ImageView imgSide34;
    private ImageView imgSide4;
    private ImageView imgMainSoldOut;
    private ImageView imgSide1SoldOut;
    private ImageView imgSide14SoldOut;
    private ImageView imgSide2SoldOut;
    private ImageView imgSide24SoldOut;
    private ImageView imgSide3SoldOut;
    private ImageView imgSide34SoldOut;
    private ImageView imgSide4SoldOut;
    private ImageView imgMainSoldOut4;
    private ImageView imgDropDownUp;
    private RelativeLayout containerMainTitle;
    private RelativeLayout containerMainTitle4;
    private RelativeLayout containerSide1Title;
    private RelativeLayout containerSide14Title;
    private RelativeLayout containerSide2Title;
    private RelativeLayout containerSide24Title;
    private RelativeLayout containerSide3Title;
    private RelativeLayout containerSide34Title;
    private RelativeLayout containerSide4Title;
    private RelativeLayout layoutPodFive;
    private RelativeLayout layoutPodFour;
    private FrameLayout layoutDateTime;
    private LinearLayout containerDateTime;
    private LinearLayout containerDateTimeSelection;

    private TextView txtTitleMain;
    private TextView txtTitleMain4;
    private TextView txtTitleSide1;
    private TextView txtTitleSide14;
    private TextView txtTitleSide2;
    private TextView txtTitleSide24;
    private TextView txtTitleSide3;
    private TextView txtTitleSide34;
    private TextView txtTitleSide4;

    private Spinner spinnerDate;
    private Spinner spinnerTime;


    ConfirmationDialog mDialog;

    private Menu mMenu;

    private Order mOrder;

    private boolean bSawAddOns = false;
    private boolean bHasAddOns = false;
    private boolean bShowDateTime = false;
    //private static Tweak<Boolean> showBanner = MixpanelAPI.booleanTweak("Show Banner", false);
    private static Tweak<Boolean> showAddons = MixpanelAPI.booleanTweak("Show AddOns", false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_build_bento);

        getBtnContinue().setOnClickListener(this);
        getActionbarLeftBtn().setOnClickListener(this);
        getActionbarRightBtn().setOnClickListener(this);
        getLayoutDateTime().setOnClickListener(this);

        getTxtPromoName().setText(String.format(getString(R.string.build_bento_price), BentoNowUtils.getDefaultPriceBento(DishDao.getLowestMainPrice())));
        getTxtEta().setText(String.format(getString(R.string.build_bento_eta), Settings.eta_min + "-" + Settings.eta_max));

        String[] aDay = new String[]{"Today", "Mon, Jan 18", "Tue, Jan 19", "Wed, Jan 20", "Thu, Jan 18", "Fri, Jan 18", "Sat, Jan 18", "Sun, Jan 18"};
        String[] aTime = new String[]{"11:30 - 12:00", "01:30 - 02:00", "02:30 - 03:00", "03:30 - 04:00", "04:30 - 05:00", "05:30 - 06:00", "06:30 - 07:00", "07:30 - 08:00"};
        ArrayAdapter<String> mAdapterDay = new ArrayAdapter<>(BuildBentoActivity.this, android.R.layout.simple_spinner_item, aDay);
        ArrayAdapter<String> mAdapterTime = new ArrayAdapter<>(BuildBentoActivity.this, android.R.layout.simple_spinner_item, aTime);
        mAdapterDay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAdapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getSpinnerDate().setAdapter(mAdapterDay);
        getSpinnerTime().setAdapter(mAdapterTime);

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
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.POD_MODE, Settings.pod_mode);

                mOrderDao.updateOrder(mOrder);
            }

            orderIndex = mOrder.currentOrderItem;

            if (mOrder.OrderItems.size() == 0) {
                mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.CUSTOM_BENTO_BOX));
                mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.ADD_ON));
            } else if (mOrder.OrderItems.size() <= orderIndex) {
                orderIndex = mOrder.OrderItems.size() - 1;
            }

            for (int a = 0; a < mMenu.dishModels.size(); a++) {
                if (mMenu.dishModels.get(a).type.equals("addon")) {
                    bHasAddOns = true;
                    break;
                }
            }

            updateUI();
        }


        super.onResume();
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
            getTxtNumBento().setVisibility(View.GONE);
            getTxtNumBento().setText("0");
            getActionbarRightBtn().setImageResource(R.drawable.ic_ab_bento);
        } else {
            getTxtNumBento().setVisibility(View.VISIBLE);
            getTxtNumBento().setText(mOrderDao.countCompletedOrders(mOrder) + "");
            getActionbarRightBtn().setImageResource(R.drawable.ic_ab_bento_completed);
        }


        getBtnAddOn().setVisibility(bHasAddOns ? View.VISIBLE : View.GONE);
    }

    private void updateDishUI() {
        OrderItem item = mOrder.OrderItems.get(orderIndex);

        if (Settings.pod_mode.equals("5")) {
            getLayoutPodFive().setVisibility(View.VISIBLE);
            getLayoutPodFour().setVisibility(View.GONE);

            if (item.items.get(0) == null || item.items.get(0).name.isEmpty()) {
                getImgMain().setImageBitmap(null);
                getTxtTitleMain().setText("");
                getContainerMainTitle().setVisibility(View.GONE);
                getImgMainSoldOut().setVisibility(View.GONE);
            } else {
                if (getImgMain().getTag() == null || !getImgMain().getTag().equals(item.items.get(0).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(0).image1, getImgMain(), ImageUtils.dishMainImageOptions());
                    getImgMain().setTag(item.items.get(0).image1);
                }
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
                if (getImgSide1().getTag() == null || !getImgSide1().getTag().equals(item.items.get(1).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(1).image1, getImgSide1(), ImageUtils.dishSideImageOptions());
                    getImgSide1().setTag(item.items.get(1).image1);
                }
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
                if (getImgSide2().getTag() == null || !getImgSide2().getTag().equals(item.items.get(2).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(2).image1, getImgSide2(), ImageUtils.dishSideImageOptions());
                    getImgSide2().setTag(item.items.get(2).image1);
                }
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
                if (getImgSide3().getTag() == null || !getImgSide3().getTag().equals(item.items.get(3).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(3).image1, getImgSide3(), ImageUtils.dishSideImageOptions());
                    getImgSide3().setTag(item.items.get(3).image1);
                }
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
                if (getImgSide4().getTag() == null || !getImgSide4().getTag().equals(item.items.get(4).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(4).image1, getImgSide4(), ImageUtils.dishSideImageOptions());
                    getImgSide4().setTag(item.items.get(3).image1);
                }
                getTxtTitleSide4().setText(item.items.get(4).name);
                getContainerSide4Title().setVisibility(View.VISIBLE);
                getImgSide4SoldOut().setVisibility(DishDao.isSoldOut(item.items.get(4), false) ? View.VISIBLE : View.GONE);
            }
        } else {
            getLayoutPodFive().setVisibility(View.GONE);
            getLayoutPodFour().setVisibility(View.VISIBLE);

            if (item.items.get(0) == null || item.items.get(0).name.isEmpty()) {
                getImgMain4().setImageBitmap(null);
                getTxtTitleMain4().setText("");
                getContainerMainTitle4().setVisibility(View.GONE);
                getImgMainSoldOut4().setVisibility(View.GONE);
            } else {
                if (getImgMain4().getTag() == null || !getImgMain4().getTag().equals(item.items.get(0).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(0).image1, getImgMain4(), ImageUtils.dishMainImageOptions());
                    getImgMain4().setTag(item.items.get(0).image1);
                }
                getTxtTitleMain4().setText(item.items.get(0).name);
                getContainerMainTitle4().setVisibility(View.VISIBLE);
                getImgMainSoldOut4().setVisibility(DishDao.isSoldOut(item.items.get(0), false) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(1) == null || item.items.get(1).name.isEmpty()) {
                getImgSide14().setImageBitmap(null);
                getTxtTitleSide14().setText("");
                getContainerSide14Title().setVisibility(View.GONE);
                getImgSide14SoldOut().setVisibility(View.GONE);
            } else {
                if (getImgSide14().getTag() == null || !getImgSide14().getTag().equals(item.items.get(1).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(1).image1, getImgSide14(), ImageUtils.dishSideImageOptions());
                    getImgSide14().setTag(item.items.get(1).image1);
                }
                getTxtTitleSide14().setText(item.items.get(1).name);
                getContainerSide14Title().setVisibility(View.VISIBLE);
                getImgSide14SoldOut().setVisibility(DishDao.isSoldOut(item.items.get(1), false) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(2) == null || item.items.get(2).name.isEmpty()) {
                getImgSide24().setImageBitmap(null);
                getTxtTitleSide24().setText("");
                getContainerSide24Title().setVisibility(View.GONE);
                getImgSide24SoldOut().setVisibility(View.GONE);
            } else {
                if (getImgSide24().getTag() == null || !getImgSide24().getTag().equals(item.items.get(2).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(2).image1, getImgSide24(), ImageUtils.dishSideImageOptions());
                    getImgSide24().setTag(item.items.get(2).image1);
                }
                getTxtTitleSide24().setText(item.items.get(2).name);
                getContainerSide24Title().setVisibility(View.VISIBLE);
                getImgSide24SoldOut().setVisibility(DishDao.isSoldOut(item.items.get(2), false) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(3) == null || item.items.get(3).name.isEmpty()) {
                getImgSide34().setImageBitmap(null);
                getTxtTitleSide34().setText("");
                getContainerSide34Title().setVisibility(View.GONE);
                getImgSide34SoldOut().setVisibility(View.GONE);
            } else {
                if (getImgSide34().getTag() == null || !getImgSide34().getTag().equals(item.items.get(3).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(3).image1, getImgSide34(), ImageUtils.dishSideImageOptions());
                    getImgSide34().setTag(item.items.get(3).image1);
                }
                getTxtTitleSide34().setText(item.items.get(3).name);
                getContainerSide34Title().setVisibility(View.VISIBLE);
                getImgSide34SoldOut().setVisibility(DishDao.isSoldOut(item.items.get(3), false) ? View.VISIBLE : View.GONE);
            }
        }

    }

    void autocomplete() {
        if (mOrder.OrderItems.get(orderIndex).items.get(0).name.isEmpty()) {
            mOrder.OrderItems.get(orderIndex).items.set(0, mDishDao.updateDishItem(mOrder.OrderItems.get(orderIndex).items.get(0), mDishDao.getFirstAvailable("main", null)));
        }
        int iNumDish = mOrder.OrderItems.get(orderIndex).items.size();

        int[] ids = new int[iNumDish - 1];

        for (int i = 1; i < iNumDish; ++i) {
            if (mOrder.OrderItems.get(orderIndex).items.get(i).name.isEmpty()) {
                DishModel dishModel = mDishDao.getFirstAvailable("side", ids);

                if (dishModel == null)
                    continue;

                ids[i - 1] = dishModel.itemId;

                dishModel = DishDao.clone(dishModel);
                dishModel.type += i;

                mOrder.OrderItems.get(orderIndex).items.set(i, mDishDao.updateDishItem(mOrder.OrderItems.get(orderIndex).items.get(i), dishModel));
            }

        }
        updateUI();
    }

    private void openAddOnsActivity() {
        Intent mAddOnActivity = new Intent(BuildBentoActivity.this, AddOnActivity.class);
        mAddOnActivity.putExtra(AddOnActivity.TAG_OPEN_BY, ConstantUtils.optOpenAddOn.BUILDER);
        bSawAddOns = true;
        startActivity(mAddOnActivity);
        overridePendingTransition(R.anim.bottom_slide_in, R.anim.none);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                BentoNowUtils.openSettingsActivity(BuildBentoActivity.this);
                break;
            case R.id.actionbar_right_btn:
                if (!bShowDateTime)
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
                if (!bShowDateTime)
                    autocomplete();
                break;
            case R.id.btn_continue:
                if (!bShowDateTime)
                    onContinueOrderPressed();
                break;
            case R.id.btn_add_on_add_on:
                if (!bShowDateTime)
                    openAddOnsActivity();
                break;
            case R.id.btn_add_another_bento:
                if (!bShowDateTime)
                    onAddAnotherBentoPressed();
                break;
            case R.id.layout_date_time:
                setDateTime();
                break;
            default:
                DebugUtils.logError(TAG, String.valueOf(v.getId()));
                break;
        }
    }

    public void onAddMainPressed(View view) {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectMainCustomActivity.class);
            intent.putExtra("orderIndex", orderIndex);
            startActivity(intent);
        }
    }

    public void onAddSide1Pressed(View view) {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectSideCustomActivity.class);
            intent.putExtra("orderIndex", orderIndex);
            intent.putExtra("itemIndex", 1);
            startActivity(intent);
        }
    }

    public void onAddSide2Pressed(View view) {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectSideCustomActivity.class);
            intent.putExtra("orderIndex", orderIndex);
            intent.putExtra("itemIndex", 2);
            startActivity(intent);
        }
    }

    public void onAddSide3Pressed(View view) {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectSideCustomActivity.class);
            intent.putExtra("orderIndex", orderIndex);
            intent.putExtra("itemIndex", 3);
            startActivity(intent);
        }
    }

    public void onAddSide4Pressed(View view) {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectSideCustomActivity.class);
            intent.putExtra("orderIndex", orderIndex);
            intent.putExtra("itemIndex", 4);
            startActivity(intent);
        }
    }

    public void onAddAnotherBentoPressed() {
        mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.CUSTOM_BENTO_BOX));
        mOrder.currentOrderItem = orderIndex = mOrder.OrderItems.size() - 1;

        mOrderDao.updateOrder(mOrder);

        updateUI();
    }

    public void onContinueOrderPressed() {

        String sSoldOutItems = mOrderDao.calculateSoldOutItems(mOrder);

        if (!mBentoDao.isBentoComplete(mOrder.OrderItems.get(orderIndex))) {

            for (int a = 0; a < mOrder.OrderItems.get(orderIndex).items.size(); a++) {

                if (mOrder.OrderItems.get(orderIndex).items.get(a) == null || mOrder.OrderItems.get(orderIndex).items.get(a).name.isEmpty()) {
                    if (a == 0)
                        startActivity(new Intent(this, SelectMainCustomActivity.class));
                    else {
                        Intent intent = new Intent(this, SelectSideCustomActivity.class);
                        intent.putExtra("orderIndex", orderIndex);
                        intent.putExtra("itemIndex", a);
                        startActivity(intent);
                    }
                    break;
                }
            }
        } else if (!sSoldOutItems.isEmpty()) {
            updateUI();
            WidgetsUtils.createShortToast(String.format(getString(R.string.error_sold_out_items), sSoldOutItems));
        } else if (BentoNowUtils.isValidCompleteOrder(BuildBentoActivity.this)) {
            if (bHasAddOns && showAddons.get() && !bSawAddOns) {
                openAddOnsActivity();
            } else {
                track();
                BentoNowUtils.openCompleteOrderActivity(BuildBentoActivity.this);
            }
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

    private void setDateTime() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!bShowDateTime) {
                    bShowDateTime = true;
                    getImgDropDownUp().setImageResource(R.drawable.ic_action_navigation_arrow_drop_up);
                    getContainerDateTime().setVisibility(View.VISIBLE);
                    Animation dateInAnimation = AnimationUtils.loadAnimation(BuildBentoActivity.this, R.anim.date_time_in);
                    getContainerDateTimeSelection().startAnimation(dateInAnimation);

                } else {
                    bShowDateTime = false;
                    getImgDropDownUp().setImageResource(R.drawable.ic_action_navigation_arrow_drop_down);
                    Animation dateOutAnimation = AnimationUtils.loadAnimation(BuildBentoActivity.this, R.anim.top_slide_out);
                    dateOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getContainerDateTime().setVisibility(View.GONE);
                                }
                            });
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    getContainerDateTimeSelection().startAnimation(dateOutAnimation);
                }
            }
        });
    }


    @Override
    public void openErrorActivity() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);
        finish();
        BentoNowUtils.openErrorActivity(BuildBentoActivity.this);
    }

    @Override
    public void openBuildBentoActivity() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);

        if (!SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.MEAL_NAME).isEmpty() &&
                (!SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.MEAL_NAME).equals(mMenu.meal_name) ||
                        !SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.POD_MODE).equals(Settings.pod_mode))) {
            DebugUtils.logDebug(TAG, "Should change from MealName " + SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.MEAL_NAME) + " to " + mMenu.meal_name +
                    " Should change from Pod Mode " + SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.POD_MODE) + " to " + Settings.pod_mode);

            WidgetsUtils.createLongToast(R.string.error_restarting_app);

            mOrderDao.cleanUp();
            recreate();
        }
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
    protected void onDestroy() {
        trackViewedScreen("Viewed Custom Home Screen");
        super.onDestroy();
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

    private ImageView getActionbarLeftBtn() {
        if (actionbarLeftBtn == null)
            actionbarLeftBtn = (ImageView) findViewById(R.id.actionbar_left_btn);
        return actionbarLeftBtn;
    }

    private ImageView getActionbarRightBtn() {
        if (actionbarRightBtn == null)
            actionbarRightBtn = (ImageView) findViewById(R.id.actionbar_right_btn);
        return actionbarRightBtn;
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

    private ImageView getImgSide14() {
        if (imgSide14 == null)
            imgSide14 = (ImageView) findViewById(R.id.img_side1_four);
        return imgSide14;
    }

    private ImageView getImgSide2() {
        if (imgSide2 == null)
            imgSide2 = (ImageView) findViewById(R.id.img_side2);
        return imgSide2;
    }


    private ImageView getImgSide24() {
        if (imgSide24 == null)
            imgSide24 = (ImageView) findViewById(R.id.img_side2_four);
        return imgSide24;
    }

    private ImageView getImgSide3() {
        if (imgSide3 == null)
            imgSide3 = (ImageView) findViewById(R.id.img_side3);
        return imgSide3;
    }

    private ImageView getImgSide34() {
        if (imgSide34 == null)
            imgSide34 = (ImageView) findViewById(R.id.img_side3_four);
        return imgSide34;
    }

    private ImageView getImgSide4() {
        if (imgSide4 == null)
            imgSide4 = (ImageView) findViewById(R.id.img_side4);
        return imgSide4;
    }

    private ImageView getImgMain4() {
        if (imgMain4 == null)
            imgMain4 = (ImageView) findViewById(R.id.img_main_four);
        return imgMain4;
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


    private ImageView getImgSide14SoldOut() {
        if (imgSide14SoldOut == null)
            imgSide14SoldOut = (ImageView) findViewById(R.id.img_side1_sold_out_four);
        return imgSide14SoldOut;
    }

    private ImageView getImgSide2SoldOut() {
        if (imgSide2SoldOut == null)
            imgSide2SoldOut = (ImageView) findViewById(R.id.img_side2_sold_out);
        return imgSide2SoldOut;
    }

    private ImageView getImgSide24SoldOut() {
        if (imgSide24SoldOut == null)
            imgSide24SoldOut = (ImageView) findViewById(R.id.img_side2_sold_out_four);
        return imgSide24SoldOut;
    }

    private ImageView getImgSide3SoldOut() {
        if (imgSide3SoldOut == null)
            imgSide3SoldOut = (ImageView) findViewById(R.id.img_side3_sold_out);
        return imgSide3SoldOut;
    }

    private ImageView getImgSide34SoldOut() {
        if (imgSide34SoldOut == null)
            imgSide34SoldOut = (ImageView) findViewById(R.id.img_side3_sold_out_four);
        return imgSide34SoldOut;
    }

    private ImageView getImgSide4SoldOut() {
        if (imgSide4SoldOut == null)
            imgSide4SoldOut = (ImageView) findViewById(R.id.img_side4_sold_out);
        return imgSide4SoldOut;
    }

    private ImageView getImgMainSoldOut4() {
        if (imgMainSoldOut4 == null)
            imgMainSoldOut4 = (ImageView) findViewById(R.id.img_main_sold_out_four);
        return imgMainSoldOut4;
    }

    private ImageView getImgDropDownUp() {
        if (imgDropDownUp == null)
            imgDropDownUp = (ImageView) findViewById(R.id.img_drop_down_up);
        return imgDropDownUp;
    }

    private TextView getTxtNumBento() {
        if (txtNumBento == null)
            txtNumBento = (TextView) findViewById(R.id.actionbar_right_badge);
        return txtNumBento;
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

    private TextView getTxtTitleSide14() {
        if (txtTitleSide14 == null)
            txtTitleSide14 = (TextView) findViewById(R.id.txt_side1_title_four);
        return txtTitleSide14;
    }

    private TextView getTxtTitleSide2() {
        if (txtTitleSide2 == null)
            txtTitleSide2 = (TextView) findViewById(R.id.txt_side2_title);
        return txtTitleSide2;
    }

    private TextView getTxtTitleSide24() {
        if (txtTitleSide24 == null)
            txtTitleSide24 = (TextView) findViewById(R.id.txt_side2_title_four);
        return txtTitleSide24;
    }

    private TextView getTxtTitleSide3() {
        if (txtTitleSide3 == null)
            txtTitleSide3 = (TextView) findViewById(R.id.txt_side3_title);
        return txtTitleSide3;
    }

    private TextView getTxtTitleSide34() {
        if (txtTitleSide34 == null)
            txtTitleSide34 = (TextView) findViewById(R.id.txt_side3_title_four);
        return txtTitleSide34;
    }


    private TextView getTxtTitleSide4() {
        if (txtTitleSide4 == null)
            txtTitleSide4 = (TextView) findViewById(R.id.txt_side4_title);
        return txtTitleSide4;
    }

    private TextView getTxtTitleMain4() {
        if (txtTitleMain4 == null)
            txtTitleMain4 = (TextView) findViewById(R.id.txt_main_title_four);
        return txtTitleMain4;
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

    private RelativeLayout getContainerSide14Title() {
        if (containerSide14Title == null)
            containerSide14Title = (RelativeLayout) findViewById(R.id.container_side1_title_four);
        return containerSide14Title;
    }

    private RelativeLayout getContainerSide2Title() {
        if (containerSide2Title == null)
            containerSide2Title = (RelativeLayout) findViewById(R.id.container_side2_title);
        return containerSide2Title;
    }

    private RelativeLayout getContainerSide24Title() {
        if (containerSide24Title == null)
            containerSide24Title = (RelativeLayout) findViewById(R.id.container_side2_title_four);
        return containerSide24Title;
    }

    private RelativeLayout getContainerSide3Title() {
        if (containerSide3Title == null)
            containerSide3Title = (RelativeLayout) findViewById(R.id.container_side3_title);
        return containerSide3Title;
    }

    private RelativeLayout getContainerSide34Title() {
        if (containerSide34Title == null)
            containerSide34Title = (RelativeLayout) findViewById(R.id.container_side3_title_four);
        return containerSide34Title;
    }

    private RelativeLayout getContainerSide4Title() {
        if (containerSide4Title == null)
            containerSide4Title = (RelativeLayout) findViewById(R.id.container_side4_title);
        return containerSide4Title;
    }

    private RelativeLayout getContainerMainTitle4() {
        if (containerMainTitle4 == null)
            containerMainTitle4 = (RelativeLayout) findViewById(R.id.container_main_title_four);
        return containerMainTitle4;
    }

    private RelativeLayout getLayoutPodFive() {
        if (layoutPodFive == null)
            layoutPodFive = (RelativeLayout) findViewById(R.id.layout_pod_five);
        return layoutPodFive;
    }

    private RelativeLayout getLayoutPodFour() {
        if (layoutPodFour == null)
            layoutPodFour = (RelativeLayout) findViewById(R.id.layout_pod_four);
        return layoutPodFour;
    }

    private FrameLayout getLayoutDateTime() {
        if (layoutDateTime == null)
            layoutDateTime = (FrameLayout) findViewById(R.id.layout_date_time);
        return layoutDateTime;
    }

    private LinearLayout getContainerDateTime() {
        if (containerDateTime == null)
            containerDateTime = (LinearLayout) findViewById(R.id.container_date_time);
        return containerDateTime;
    }

    private LinearLayout getContainerDateTimeSelection() {
        if (containerDateTimeSelection == null)
            containerDateTimeSelection = (LinearLayout) findViewById(R.id.container_date_time_selection);
        return containerDateTimeSelection;
    }

    private Spinner getSpinnerDate() {
        if (spinnerDate == null)
            spinnerDate = (Spinner) findViewById(R.id.spinner_date);
        return spinnerDate;
    }

    private Spinner getSpinnerTime() {
        if (spinnerTime == null)
            spinnerTime = (Spinner) findViewById(R.id.spinner_time);
        return spinnerTime;
    }

    private AutoFitTxtView getTxtDateTimeToolbar() {
        if (txtDateTimeToolbar == null)
            txtDateTimeToolbar = (AutoFitTxtView) findViewById(R.id.txt_date_time_toolbar);
        return txtDateTimeToolbar;
    }

}
