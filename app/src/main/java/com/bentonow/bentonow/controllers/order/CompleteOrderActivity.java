package com.bentonow.bentonow.controllers.order;


import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.Utils.exception.ServiceException;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.adapter.ExpandableListOrderAdapter;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.dialog.CouponDialog;
import com.bentonow.bentonow.controllers.dialog.ProgressDialog;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.controllers.session.SignInActivity;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.dao.SettingsDao;
import com.bentonow.bentonow.dao.UserDao;
import com.bentonow.bentonow.listener.InterfaceCustomerService;
import com.bentonow.bentonow.listener.ListenerCompleteOrder;
import com.bentonow.bentonow.listener.ListenerDialog;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.model.order.post.OrderPost;
import com.bentonow.bentonow.parse.InitParse;
import com.bentonow.bentonow.service.BentoCustomerService;
import com.bentonow.bentonow.ui.AutoFitTxtView;
import com.bentonow.bentonow.ui.BackendButton;
import com.crashlytics.android.Crashlytics;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class CompleteOrderActivity extends BaseFragmentActivity implements View.OnClickListener, InterfaceCustomerService, ListenerCompleteOrder {

    private static final String TAG = "CompleteOrderActivity";
    private static final String TAG_ORDER_TYPE = "Order Type";

    private AutoFitTxtView txt_address;
    private TextView txt_credit_card;
    private TextView txt_discount;
    private TextView txt_tax;
    private TextView txt_delivery_price_total;
    private TextView txt_delivery_price;
    private TextView txt_tip;
    private TextView txt_total;
    private TextView txtDeliverTimeUp;
    private TextView txtDeliverTimeDown;


    private ImageView img_credit_card;
    private ImageView imgDeliverTime;

    private View container_discount;

    private BackendButton btnOnLetsEatPressed;

    private TextView btn_add_promo_code;
    private TextView txtPromoTotal;

    private RelativeLayout layoutWrapperDiscount;
    private CouponDialog mDialogCoupon;
    private ConfirmationDialog mDialog;
    private ProgressDialog mProgressDialog;
    private ExpandableListView mExpandableListOrder;

    private UserDao userDao = new UserDao();
    private User mCurrentUser;
    private Order mOrder;
    private Menu mMenu;
    private List<OrderItem> aOrder = new ArrayList<>();
    private List<DishModel> aAddOn = new ArrayList<>();
    private CountDownTimer mCountDown;

    private ExpandableListOrderAdapter mExpandableAdapter;

    private String action = "";
    private long lMilliSecondsRemaining;
    private boolean bIsMenuOD = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_order);

        txt_address = (AutoFitTxtView) findViewById(R.id.txt_address);
        txt_credit_card = (TextView) findViewById(R.id.txt_credit_card);
        txt_discount = (TextView) findViewById(R.id.txt_discount);
        txt_tax = (TextView) findViewById(R.id.txt_tax);
        txt_tip = (TextView) findViewById(R.id.txt_tip_percent);
        txt_total = (TextView) findViewById(R.id.txt_total);

        getBtnOnLetsEatPressed().setOnClickListener(this);
        getBtnAddPromoCode().setOnClickListener(this);

        img_credit_card = (ImageView) findViewById(R.id.img_credit_card);

        container_discount = findViewById(R.id.container_discount);

        bIsMenuOD = !SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_ORDER_AHEAD_MENU);

        getExpandableListOrder().setAdapter(getExpandableListAdapter());

        mMenu = getIntent().getParcelableExtra(Menu.TAG);

        initActionbar();
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(IosCopyDao.get("complete-title"));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.vector_navigation_left_green);
        actionbar_left_btn.setOnClickListener(this);
    }

    private void getCurrentOrder() {
        mOrder = mOrderDao.getCurrentOrder();

        aOrder = new ArrayList<>();

        for (int a = 0; a < mOrder.OrderItems.size(); a++) {
            if (mOrder.OrderItems.get(a).item_type.equals("CustomerBentoBox"))
                aOrder.add(mOrder.OrderItems.get(a));
        }

        aAddOn = mDishDao.getAllDishByType(ConstantUtils.optDishType.ADDON);
    }

    private void emptyOrders() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.CLEAR_ORDERS_FROM_SUMMARY, true);
        mOrderDao.cleanUp();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        mCurrentUser = userDao.getCurrentUser();

        if (mCurrentUser == null)
            onBackPressed();
        else {
            getCurrentOrder();

            if (mOrder == null || mOrder.OrderItems == null || mOrder.OrderItems.isEmpty()) {
                Crashlytics.log(Log.ERROR, "Order", "No Items in the Order");
                emptyOrders();
            } else
                updateBentoUI(false);

            if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_ORDER_AHEAD_MENU))
                setOAHashTimer(BentoNowUtils.showOATimer(mMenu));
        }

        GoogleAnalyticsUtil.sendScreenView("Complete Order");

        super.onResume();
    }

    void trackCompleteOrder(String error) {
        try {
            JSONObject params = new JSONObject();
            if (mOrder == null || mOrder.OrderItems == null) {
                Crashlytics.log(Log.ERROR, "Order", "No Items in the Order");
                params.put("quantity", 0);
            } else
                params.put("quantity", mOrder.OrderItems.size());

            if (mCurrentUser != null)
                params.put("payment method", mCurrentUser.card.brand);

            params.put("total price", mOrder.OrderDetails.total_cents / 100);
            params.put("status", error == null ? "success" : "failure");
            params.put("status_error", error == null ? "" : error);
            params.put("meal", BentoNowUtils.getMealType(mOrder.scheduled_window_start));

            MixpanelUtils.track(bIsMenuOD ? "Ordered On-demand" : "Ordered Order-ahead", params);
        } catch (Exception e) {
            DebugUtils.logError(TAG, e);
        }
    }

    private void trackError(String sError) {
        try {
            JSONObject params = new JSONObject();

            if (mCurrentUser != null)
                params.put("payment method", mCurrentUser.card.brand);

            params.put("quantity", mOrder.OrderItems.size());
            params.put("total price", mOrder.OrderDetails.total_cents / 100);
            params.put("status", sError == null ? "success" : "failure");
            params.put("status_error", sError);

            MixpanelUtils.track("Complete Order Error", params);
            Crashlytics.logException(new ServiceException(params.toString()));
        } catch (Exception e) {
            DebugUtils.logError(TAG, e);
        }
    }

    private void trackPromoCode() {
        try {
            JSONObject params = new JSONObject();
            params.put("code", mCurrentUser.coupon_code);
            params.put("discount", mOrder.OrderDetails.coupon_discount_cents);
            MixpanelUtils.track("Entered Promo Code", params);
        } catch (Exception e) {
            DebugUtils.logError(TAG, "track(): " + e.toString());
        }

    }

    void requestPromoCode(final String code) {
        DebugUtils.logDebug(TAG, "requestPromoCode " + code);
        if (code == null || code.isEmpty()) {
            mDialog = new ConfirmationDialog(CompleteOrderActivity.this, "Error", "Invalid coupon code");
            mDialog.addAcceptButton("OK", null);
            mDialog.show();
        } else {

            showLoadingDialog(getString(R.string.processing_label), false);

            RequestParams params = new RequestParams();
            params.put("api_token", mCurrentUser.api_token);
            BentoRestClient.get("/coupon/apply/" + code, params, new TextHttpResponseHandler() {
                @SuppressWarnings("deprecation")
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    dismissDialog();

                    DebugUtils.logError(TAG, "requestPromoCode failed: " + responseString + " StatusCode:" + statusCode);
                    String sError;

                    try {
                        sError = new JSONObject(responseString).getString("error");
                    } catch (Exception e) {
                        sError = getString(R.string.error_sign_up_user);
                        DebugUtils.logError(TAG, "requestPromoCode(): " + e.getLocalizedMessage());
                    }

                    switch (statusCode) {
                        case 0:// No internet Connection
                            sError = getString(R.string.error_no_internet_connection);
                            break;
                        case 401:// Invalid Api Token
                            DebugUtils.logError(TAG, "Invalid Api Token");
                            WidgetsUtils.createShortToast("You session is expired, please LogIn again");

                            if (!userDao.removeUser())
                                userDao.clearAllData();

                            startActivity(new Intent(CompleteOrderActivity.this, SignInActivity.class));
                            break;
                        default:
                            Crashlytics.log(Log.ERROR, "SendOrderError", "Code " + statusCode + " : Response " + responseString + " : Parsing " + sError);
                            break;
                    }

                    mDialog = new ConfirmationDialog(CompleteOrderActivity.this, "Error", sError);
                    mDialog.addAcceptButton("OK", null);

                    if (statusCode != 401)
                        mDialog.show();
                }


                @SuppressWarnings("deprecation")
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    dismissDialog();

                    DebugUtils.logDebug(TAG, "requestPromoCode success " + responseString);

                    mCurrentUser.coupon_code = mOrder.CouponCode = code;
                    double dAmountOff = 0;

                    try {
                        dAmountOff = Double.parseDouble(new JSONObject(responseString).getString("amountOff"));
                    } catch (Exception e) {
                        DebugUtils.logError(TAG, "requestPromoCode(): " + e.getLocalizedMessage());
                    }


                    DebugUtils.logDebug(TAG, "Coupon Discount " + dAmountOff);

                    mOrder.OrderDetails.coupon_discount_cents = (int) (dAmountOff * 100);
                    mOrderDao.updateOrder(mOrder);

                    trackPromoCode();

                    updateBentoUI(true);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
            case R.id.btn_remove:

                break;
            case R.id.button_accept:
                switch (action) {
                    case "closed":
                        BentoNowUtils.openErrorActivity(this);
                        break;
                    case "credit_card":
                        WidgetsUtils.createShortToast(R.string.error_no_credit_card);
                        onChangeCreditCardPressed(null);
                        break;
                    case "sign_in":
                        WidgetsUtils.createShortToast(R.string.error_no_user_log_in);
                        startActivity(new Intent(this, SignInActivity.class));
                        finish();
                        break;
                    case "no_items":
                        WidgetsUtils.createShortToast("There was a problem please try again");
                        onBackPressed();
                        break;
                    case "sold_out":
                        WidgetsUtils.createShortToast(R.string.error_sold_out_items);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateBentoUI(true);
                            }
                        });
                        break;
                }
                action = "";
                break;
            case R.id.btn_on_lets_eat_pressed:
                onLetsEatPressed();
                break;
            case R.id.btn_add_promo_code:
                showAddPromoCodeDialog();
                break;
            default:
                DebugUtils.logError(TAG, "View Id wasnt found: " + v.getId());
                break;
        }
    }

    public void onChangeAddressPressed(View v) {
        MixpanelUtils.track("Tapped On Change - Address");
        Intent intent = new Intent(CompleteOrderActivity.this, DeliveryLocationActivity.class);
        intent.putExtra(ConstantUtils.TAG_OPEN_SCREEN, ConstantUtils.optOpenScreen.SUMMARY);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void onChangeCreditCardPressed(View v) {
        MixpanelUtils.track("Tapped On Change - Payment");
        BentoNowUtils.openCreditCardActivity(CompleteOrderActivity.this, ConstantUtils.optOpenScreen.NORMAL);
    }

    public void onMinusTipPressed(View v) {
        if (mOrder.OrderDetails.tip_percentage > 0) {
            mOrder.OrderDetails.tip_percentage -= 5;
        }
        mOrderDao.updateOrder(mOrder);
        updateViewsUI();
    }

    public void onPlusTipPressed(View v) {
        if (mOrder.OrderDetails.tip_percentage < 30) {
            mOrder.OrderDetails.tip_percentage += 5;
        }
        mOrderDao.updateOrder(mOrder);
        updateViewsUI();
    }

    public void onLetsEatPressed() {
        MixpanelUtils.track("Tapped On Let's Eat");

        if (BentoNowUtils.isValidCompleteOrder(CompleteOrderActivity.this)) {
            showLoadingDialog(getString(R.string.processing_label), false);

            mOrder.Stripe.stripeToken = mCurrentUser.stripe_token;
            mOrder.IdempotentToken = BentoNowUtils.getUUIDBento();
            mOrder.Platform = "Android";
            mOrder.AppVersion = "1.20";

            if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.ORDER_AHEAD_SUBSCRIPTION))
                mOrder.OrderDetails.delivery_price = 0;

            mOrderDao.updateOrder(mOrder);

            for (int a = 0; a < mOrder.OrderItems.size(); a++) {
                if (mOrder.OrderItems.get(a).item_type.equals("AddonList")) {
                    mOrder.OrderItems.get(a).items = aAddOn;
                }
            }

            OrderPost mOrderPost = new OrderPost(mOrder);
            RequestParams params = new RequestParams();
            params.put("data", mOrderPost.toString());
            params.put("api_token", mCurrentUser.api_token);

            DebugUtils.logDebug(TAG, "Order: " + mOrderPost.toString());

            BentoRestClient.post("/order", params, new TextHttpResponseHandler() {
                @SuppressWarnings("deprecation")
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    String error = "";
                    JSONObject json;

                    DebugUtils.logError(TAG, "Status: " + statusCode + " Response(): " + responseString);

                    try {
                        json = new JSONObject(responseString);
                        if (json.has("Error")) {
                            error = json.getString("Error");
                        } else {
                            error = json.getString("error");
                        }
                        trackError(error);
                    } catch (Exception ignore) {
                        trackError("Order: " + statusCode + " Response(): " + responseString);
                    }

                    switch (statusCode) {
                        case 0:
                            error = "No Network";
                            break;
                        case 401:
                            DebugUtils.logError(TAG, "Invalid Api Token");
                            WidgetsUtils.createShortToast("You session is expired, please LogIn again");

                            if (!userDao.removeUser())
                                userDao.clearAllData();

                            startActivity(new Intent(CompleteOrderActivity.this, SignInActivity.class));
                            break;
                        case 402:
                            action = "credit_card";
                            if (error.isEmpty())
                                error = getString(R.string.error_no_credit_card);
                            break;
                        case 406:
                            if (responseString.contains("You cannot use a Stripe token more than once")) {
                                mCurrentUser.stripe_token = null;
                                userDao.updateUser(mCurrentUser);
                                error = "";
                                onLetsEatPressed();
                            }
                            break;
                        case 410:
                            action = "sold_out";
                            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_ORDER_SOLD_OUT, true);
                            InitParse.parseOutOfStock(responseString);
                            error += mOrderDao.calculateSoldOutItems(mOrder, bIsMenuOD);
                            break;
                        case 423:
                            action = "closed";
                            error = IosCopyDao.get("closed-title");
                            break;
                        default:
                            if (error.isEmpty())
                                error = getResources().getString(R.string.error_send_order);
                            break;
                    }

                    dismissDialog();

                    trackCompleteOrder(error);

                    if (!error.equals("")) {
                        mDialog = new ConfirmationDialog(CompleteOrderActivity.this, null, error);
                        mDialog.addAcceptButton("OK", null);
                        mDialog.show();
                    }
                }

                @SuppressWarnings("deprecation")
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    trackCompleteOrder(null);

                    MixpanelUtils.trackRevenue(mOrder.OrderDetails.total_cents / 100, mCurrentUser);

                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.CLEAR_ORDERS_FROM_SUMMARY, true);

                    Log.i(TAG, "Order: " + responseString);

                    mCurrentUser.stripe_token = null;

                    dismissDialog();

                    mOrderDao.cleanUp();

                    finish();

                    startActivity(new Intent(CompleteOrderActivity.this, OrderConfirmedActivity.class));

                }
            });
        }
    }

    private void updateBentoUI(boolean bRequestOrder) {
        if (bRequestOrder)
            getCurrentOrder();

        getExpandableListAdapter().getaAddOnList().clear();
        getExpandableListAdapter().getaOrderList().clear();
        getExpandableListAdapter().setaAddOnList(aAddOn);
        getExpandableListAdapter().setaOrderList(aOrder);

        getExpandableListAdapter().notifyDataSetChanged();
        getExpandableListOrder().expandGroup(0, false);
        getExpandableListOrder().expandGroup(1, false);

        updateViewsUI();
    }

    private void updateViewsUI() {
        mOrderDao.calculateOrder(mOrder);
        mOrderDao.updateOrder(mOrder);

        if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.IS_ORDER_AHEAD_MENU)) {
            getImgDeliverTime().setImageResource(R.drawable.vector_calendar);
            getTxtDeliverTimeUp().setText(BentoNowUtils.getDaySelected(mOrder));
            getTxtDeliverTimeDown().setText(BentoNowUtils.getTimeSelected(mOrder));
        } else {
            getImgDeliverTime().setImageResource(R.drawable.vector_schedule);
            getTxtDeliverTimeUp().setText(String.format(getString(R.string.summary_bento_od_delivery_time), MenuDao.eta_min, MenuDao.eta_max));
            getTxtDeliverTimeDown().setText("min.");
        }

        if (mOrder.OrderDetails.coupon_discount_cents > 0) {
            getBtnAddPromoCode().setTextColor(getResources().getColor(R.color.orange));
            getBtnAddPromoCode().setText("REMOVE PROMO");
            getTxtPromoTotal().setText(String.format(getString(R.string.money_format), mOrder.OrderDetails.total_cents_without_coupon / 100));
            getTxtPromoTotal().setPaintFlags(getTxtPromoTotal().getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            getTxtPromoTotal().setVisibility(View.VISIBLE);
            container_discount.setVisibility(View.VISIBLE);
        } else {
            getBtnAddPromoCode().setText(IosCopyDao.get("complete-add-promo"));
            getBtnAddPromoCode().setTextColor(getResources().getColor(R.color.btn_green));
            getTxtPromoTotal().setVisibility(View.GONE);
            container_discount.setVisibility(View.GONE);
        }

        txt_address.setText(BentoNowUtils.getStreetAddress());
        txt_credit_card.setText(mCurrentUser.card.last4);

        getTxtDeliveryPrice().setText(String.format(getString(R.string.money_format), SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.ORDER_AHEAD_SUBSCRIPTION) ? 0 : mOrder.OrderDetails.delivery_price));
        getTxtDeliveryPriceTotal().setText(String.format(getString(R.string.money_format), mOrder.OrderDetails.delivery_price));
        getTxtDeliveryPriceTotal().setVisibility(SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.ORDER_AHEAD_SUBSCRIPTION) ? View.VISIBLE : View.GONE);
        getTxtDeliveryPriceTotal().setPaintFlags(getTxtDeliveryPriceTotal().getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        double dCoupon = mOrder.OrderDetails.coupon_discount_cents;
        txt_discount.setText(String.format(getString(R.string.money_format), (dCoupon / 100.0)));
        txt_tax.setText(String.format(getString(R.string.money_format), mOrder.OrderDetails.tax_cents / 100));
        txt_tip.setText(String.format(getString(R.string.tip_percentage), mOrder.OrderDetails.tip_percentage) + " %");
        txt_total.setText(String.format(getString(R.string.money_format), mOrder.OrderDetails.total_cents / 100));


        int card;

        switch (mCurrentUser.card.brand) {
            case "American Express":
                card = R.drawable.card_amex;
                break;
            case "MasterCard":
                card = R.drawable.card_mastercard;
                break;
            case "Visa":
                card = R.drawable.card_visa;
                break;
            case "Diners Club":
                card = R.drawable.card_diners;
                break;
            case "Discover Card":
                card = R.drawable.card_discover;
                break;
            case "JCB":
                card = R.drawable.card_jcb;
                break;
            default:
                card = R.drawable.vector_credit_card_gray;
                break;
        }

        img_credit_card.setImageResource(card);
        txt_credit_card.setText(mCurrentUser.card.last4 != null ? mCurrentUser.card.last4 : "");

    }

    public void showAddPromoCodeDialog() {
        if (mOrder.OrderDetails.coupon_discount_cents > 0) {
            mOrder.OrderDetails.coupon_discount_cents = 0;
            updateViewsUI();
        } else {
            if (mDialogCoupon == null || !mDialogCoupon.isShowing()) {
                mDialogCoupon = new CouponDialog(this);
                mDialogCoupon.setmDialogListener(new ListenerDialog() {
                    @Override
                    public void btnOkClick(final String sPromoCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                requestPromoCode(sPromoCode);
                            }
                        });
                    }

                    @Override
                    public void btnOnCancel() {

                    }
                });

                mDialogCoupon.show();
            }
        }
    }

    private void dismissDialog() {
        if (mDialogCoupon != null)
            mDialogCoupon.dismiss();
        if (mDialog != null)
            mDialog.dismiss();
        if (mProgressDialog != null)
            mProgressDialog.dismiss();

        mDialog = null;
        mDialogCoupon = null;
        mProgressDialog = null;
    }

    private void showLoadingDialog(String sText, boolean bCancelable) {
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            mProgressDialog = new ProgressDialog(CompleteOrderActivity.this, sText, bCancelable);
            mProgressDialog.show();
        }
    }

    private void openAddOnActivity() {
        Intent mAddOnActivity = new Intent(CompleteOrderActivity.this, AddOnActivity.class);
        mAddOnActivity.putExtra(AddOnActivity.TAG_OPEN_BY, ConstantUtils.optOpenAddOn.SUMMARY);
        mAddOnActivity.putExtra(Menu.TAG, mMenu);
        startActivity(mAddOnActivity);
        overridePendingTransition(R.anim.bottom_slide_in, R.anim.none);
    }

    private void setOAHashTimer(final long lMilliSeconds) {
        if (lMilliSeconds == 0) {
            if (mCountDown != null) {
                mCountDown.cancel();
                mCountDown = null;
            }
        } else {
            if (mCountDown != null)
                mCountDown.cancel();
            lMilliSecondsRemaining = lMilliSeconds;

            mCountDown = new CountDownTimer(lMilliSeconds, 1000) {
                @Override
                public void onTick(long l) {
                    lMilliSecondsRemaining = lMilliSecondsRemaining - 1000;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getBtnOnLetsEatPressed().setText(getString(R.string.build_bento_btn_time_oa, IosCopyDao.get("complete-button"), IosCopyDao.get("oa-countdown-label"),
                                    AndroidUtil.getMinFromMillis(lMilliSecondsRemaining), AndroidUtil.getSecondsFromMillis(lMilliSecondsRemaining)));
                        }

                    });
                }

                @Override
                public void onFinish() {
                    finish();
                    BentoNowUtils.openDeliveryLocationScreen(CompleteOrderActivity.this, ConstantUtils.optOpenScreen.BUILD_BENTO);
                }
            };

            mCountDown.start();
        }
    }

    @Override
    public void onAddAnotherBento() {
        mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.CUSTOM_BENTO_BOX));
        mOrder.currentOrderItem = mOrder.OrderItems.size() - 1;
        mOrderDao.updateOrder(mOrder);

        onBackPressed();
    }

    @Override
    public void onAddAnotherAddOn() {
        openAddOnActivity();
    }

    @Override
    public void onEditBento(int iPk) {
        int idOrder = 0;
        for (int a = 0; a < mOrder.OrderItems.size(); a++) {
            if (mOrder.OrderItems.get(a).order_pk == iPk) {
                idOrder = a;
                break;
            }
        }
        mOrder.currentOrderItem = idOrder;
        mOrderDao.updateOrder(mOrder);

        finish();
    }

    @Override
    public void onEditAddOn() {
        openAddOnActivity();
    }

    @Override
    public void onRemoveBento(int iPk) {
        MixpanelUtils.track("Removed Bento");
        if (aOrder.size() == 1) {
            mDialog = new ConfirmationDialog(CompleteOrderActivity.this, null, IosCopyDao.get("complete-remove-all-text"));
            mDialog.addAcceptButton(IosCopyDao.get("complete-remove-all-confirmation-2"), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    emptyOrders();
                }
            });
            mDialog.addCancelButton(IosCopyDao.get("complete-remove-all-confirmation-1"), null);
            mDialog.show();
        } else {
            int idOrder = -1;

            for (int a = 0; a < mOrder.OrderItems.size(); a++) {
                if (mOrder.OrderItems.get(a).order_pk == iPk) {
                    idOrder = a;
                    break;
                }
            }

            mBentoDao.removeBento(mOrder.OrderItems.get(idOrder).order_pk);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateBentoUI(true);
                }
            });
        }
    }

    @Override
    public void onRemoveAddOn(int iPk) {
        mDishDao.removeDish(iPk);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateBentoUI(true);
            }
        });
    }

    @Override
    public void onMapNoService() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, MenuDao.gateKeeper.getAppState());
        finish();
        BentoNowUtils.openDeliveryLocationScreen(CompleteOrderActivity.this, ConstantUtils.optOpenScreen.BUILD_BENTO);
    }

    @Override
    public void onBuild() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, MenuDao.gateKeeper.getAppState());

        if (SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.POD_MODE).isEmpty()) {
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.POD_MODE, SettingsDao.getCurrent().pod_mode);
        } else if (!SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.POD_MODE).equals(SettingsDao.getCurrent().pod_mode)) {
            DebugUtils.logError(TAG, "Should change from Pod Mode " + SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.POD_MODE) + " to " + SettingsDao.getCurrent().pod_mode);
            WidgetsUtils.createLongToast(R.string.error_restarting_app);
            mOrderDao.cleanUp();
            finish();
            BentoNowUtils.openMainActivity(CompleteOrderActivity.this);
        }
    }

    @Override
    public void onClosedWall() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, MenuDao.gateKeeper.getAppState());
        finish();
        BentoNowUtils.openErrorActivity(CompleteOrderActivity.this);
    }

    @Override
    public void onSold() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, MenuDao.gateKeeper.getAppState());
        finish();
        BentoNowUtils.openErrorActivity(CompleteOrderActivity.this);
    }

    @Override
    public void onConnectService() {
        DebugUtils.logDebug(TAG, "Service Connected");
        mBentoService.setServiceListener(CompleteOrderActivity.this);

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

        if (mCountDown != null) {
            mCountDown.cancel();
            mCountDown = null;
        }
    }

    @Override
    protected void onDestroy() {
        MixpanelUtils.track("Viewed Summary Screen");
        super.onDestroy();
    }

    private TextView getTxtDeliveryPrice() {
        if (txt_delivery_price == null)
            txt_delivery_price = (TextView) findViewById(R.id.txt_delivery_price);

        return txt_delivery_price;
    }

    private TextView getBtnAddPromoCode() {
        if (btn_add_promo_code == null)
            btn_add_promo_code = (TextView) findViewById(R.id.btn_add_promo_code);
        return btn_add_promo_code;
    }

    private TextView getTxtDeliveryPriceTotal() {
        if (txt_delivery_price_total == null)
            txt_delivery_price_total = (TextView) findViewById(R.id.txt_delivery_price_total);
        return txt_delivery_price_total;
    }

    private TextView getTxtPromoTotal() {
        if (txtPromoTotal == null)
            txtPromoTotal = (TextView) findViewById(R.id.txt_promo_total);
        return txtPromoTotal;
    }

    private TextView getTxtDeliverTimeUp() {
        if (txtDeliverTimeUp == null)
            txtDeliverTimeUp = (TextView) findViewById(R.id.txt_deliver_time_up);
        return txtDeliverTimeUp;
    }

    private TextView getTxtDeliverTimeDown() {
        if (txtDeliverTimeDown == null)
            txtDeliverTimeDown = (TextView) findViewById(R.id.txt_deliver_time_down);
        return txtDeliverTimeDown;
    }

    private ImageView getImgDeliverTime() {
        if (imgDeliverTime == null)
            imgDeliverTime = (ImageView) findViewById(R.id.img_deliver_time);
        return imgDeliverTime;
    }

    private BackendButton getBtnOnLetsEatPressed() {
        if (btnOnLetsEatPressed == null)
            btnOnLetsEatPressed = (BackendButton) findViewById(R.id.btn_on_lets_eat_pressed);
        return btnOnLetsEatPressed;
    }

    private ExpandableListView getExpandableListOrder() {
        if (mExpandableListOrder == null) {
            mExpandableListOrder = (ExpandableListView) findViewById(R.id.expand_list_order);
            mExpandableListOrder.setGroupIndicator(null);
            mExpandableListOrder.setClickable(false);
        }
        return mExpandableListOrder;
    }


    private ExpandableListOrderAdapter getExpandableListAdapter() {
        if (mExpandableAdapter == null)
            mExpandableAdapter = new ExpandableListOrderAdapter(CompleteOrderActivity.this, bIsMenuOD, CompleteOrderActivity.this);
        return mExpandableAdapter;
    }
}
