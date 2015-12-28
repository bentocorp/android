package com.bentonow.bentonow.controllers.order;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.dialog.CouponDialog;
import com.bentonow.bentonow.controllers.dialog.ProgressDialog;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.controllers.payment.EnterCreditCardActivity;
import com.bentonow.bentonow.controllers.session.SignInActivity;
import com.bentonow.bentonow.dao.OrderDao;
import com.bentonow.bentonow.dao.UserDao;
import com.bentonow.bentonow.listener.InterfaceCustomerService;
import com.bentonow.bentonow.listener.ListenerDialog;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.Stock;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.service.BentoCustomerService;
import com.bentonow.bentonow.ui.BackendButton;
import com.crashlytics.android.Crashlytics;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.wsdcamp.list.LazyListAdapter;
import com.wsdcamp.list.LazyListAdapterInterface;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class CompleteOrderActivity extends BaseActivity implements View.OnClickListener, LazyListAdapterInterface, OnItemClickListener, InterfaceCustomerService {
    private static final String TAG = "CompleteOrderActivity";

    TextView txt_address;
    TextView txt_credit_card;
    TextView txt_discount;
    TextView txt_tax;
    TextView txt_delivery_price;
    TextView txt_tip;
    TextView txt_total;

    ImageView img_credit_card;

    View container_discount;

    BackendButton btn_delete;
    BackendButton btnOnLetsEatPressed;

    TextView btn_add_promo_code;
    TextView txtPromoTotal;

    private RelativeLayout layoutWrapperDiscount;
    private CouponDialog mDialogCoupon;
    private ConfirmationDialog mDialog;
    private ProgressDialog mProgressDialog;

    LayoutInflater inflater;

    private UserDao userDao = new UserDao();
    private User mCurrentUser;
    private Order mOrder;

    boolean edit = false;
    int selected = -1;
    LazyListAdapter adapter;
    String action = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_order);

        mOrder = mOrderDao.getCurrentOrder();

        txt_address = (TextView) findViewById(R.id.txt_address);
        txt_credit_card = (TextView) findViewById(R.id.txt_credit_card);
        txt_discount = (TextView) findViewById(R.id.txt_discount);
        txt_tax = (TextView) findViewById(R.id.txt_tax);
        txt_tip = (TextView) findViewById(R.id.txt_tip_percent);
        txt_total = (TextView) findViewById(R.id.txt_total);

        getBtnOnLetsEatPressed().setOnClickListener(this);

        btn_delete = (BackendButton) findViewById(R.id.btn_delete);

        img_credit_card = (ImageView) findViewById(R.id.img_credit_card);

        container_discount = findViewById(R.id.container_discount);

        adapter = new LazyListAdapter(this);

        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        initActionbar();
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(BackendText.get("complete-title"));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(this);
    }

    private void emptyOrders() {
        mOrderDao.cleanUp();
        onBackPressed();
    }

    void deleteBento() {
        if (mOrder.OrderItems.size() > 1) {
            mBentoDao.removeBento(mOrder.OrderItems.get(selected).order_pk);
            mOrder.OrderItems.remove(selected);
            selected = -1;
            adapter.notifyDataSetChanged();
            updateUI();
        } else {
            emptyOrders();
        }

    }

    @Override
    protected void onResume() {
        mCurrentUser = userDao.getCurrentUser();

        if (mOrder == null || mOrder.OrderItems == null || mOrder.OrderItems.isEmpty()) {
            Crashlytics.log(Log.ERROR, "Order", "No Items in the Order");
            emptyOrders();
        } else if (!Settings.status.equals("open")) {

        } else
            updateUI();

        super.onResume();
    }

    void track(String error) {
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
            params.put("status_error", error);

            MixpanelUtils.track("Placed An Order", params);
        } catch (Exception e) {
            DebugUtils.logError(TAG, e);
        }
    }

    void requestPromoCode(final String code) {
        DebugUtils.logDebug(TAG, "requestPromoCode " + code);
        if (code == null || code.isEmpty()) {
            mDialog = new ConfirmationDialog(CompleteOrderActivity.this, "Error", "Invalid coupon code");
            mDialog.addAcceptButton("OK", null);
            mDialog.show();
        } else {
            showLoadingDialog(getString(R.string.processing_label));

            RequestParams params = new RequestParams();
            params.put("api_token", mCurrentUser.api_token);
            BentoRestClient.get("/coupon/apply/" + code, params, new TextHttpResponseHandler() {
                @SuppressWarnings("deprecation")
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    dismissDialog();

                    DebugUtils.logDebug(TAG, "requestPromoCode failed: " + responseString + " StatusCode:" + statusCode);
                    String sError;

                    try {
                        sError = new JSONObject(responseString).getString("error");
                    } catch (Exception e) {
                        sError = "No Network";
                        DebugUtils.logError(TAG, "requestPromoCode(): " + e.getLocalizedMessage());
                    }

                    mDialog = new ConfirmationDialog(CompleteOrderActivity.this, "Error", sError);
                    mDialog.addAcceptButton("OK", null);
                    mDialog.show();
                }


                @SuppressWarnings("deprecation")
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    dismissDialog();

                    DebugUtils.logDebug(TAG, "requestPromoCode success " + responseString);

                    mCurrentUser.coupon_code = mOrder.CouponCode = code;
                    int discount = 0;

                    try {
                        discount = Integer.parseInt(new JSONObject(responseString).getString("amountOff").replace(".", ""));
                    } catch (Exception e) {
                        DebugUtils.logError(TAG, "requestPromoCode(): " + e.getLocalizedMessage());
                    }

                    DebugUtils.logDebug(TAG, "discount " + discount);

                    mOrder.OrderDetails.coupon_discount_cents = discount;
                    mOrderDao.updateOrder(mOrder);

                    BentoNowUtils.saveSettings(ConstantUtils.optSaveSettings.ALL);

                    updateUI();
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
                if (mOrder.OrderItems.size() == 1) {
                    action = "delete";
                    mDialog = new ConfirmationDialog(CompleteOrderActivity.this, null, BackendText.get("complete-remove-all-text"));
                    mDialog.addAcceptButton(BackendText.get("complete-remove-all-confirmation-2"), CompleteOrderActivity.this);
                    mDialog.addCancelButton(BackendText.get("complete-remove-all-confirmation-1"), CompleteOrderActivity.this);
                    mDialog.show();
                } else {
                    deleteBento();
                }
                break;
            case R.id.btn_edit:
                int position = (int) v.getTag();

                if (selected == position) {
                    selected = -1;
                } else {
                    selected = position;
                }

                adapter.notifyDataSetChanged();
                break;
            case R.id.button_accept:
                switch (action) {
                    case "delete":
                        deleteBento();
                        break;
                    case "closed":
                        BentoNowUtils.openErrorActivity(this);
                        break;
                    case "credit_card":
                        WidgetsUtils.createShortToast(R.string.error_no_credit_card);
                        startActivity(new Intent(this, EnterCreditCardActivity.class));
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
                                adapter.notifyDataSetChanged();
                                updateUI();
                            }
                        });
                        break;
                }
                action = "";
                break;
            case R.id.btn_on_lets_eat_pressed:
                onLetsEatPressed();
                break;
            default:
                DebugUtils.logError(TAG, "View Id wasnt found: " + v.getId());
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mOrder.currentOrderItem = position;
        mOrderDao.updateOrder(mOrder);

        onBackPressed();
    }

    public void onChangeAddressPressed(View v) {
        Intent intent = new Intent(this, DeliveryLocationActivity.class);
        intent.putExtra(ConstantUtils.TAG_OPEN_SCREEN, ConstantUtils.optOpenScreen.SUMMARY);
        startActivity(intent);
    }

    public void onChangeCreditCardPressed(View v) {
        startActivity(new Intent(this, EnterCreditCardActivity.class));
    }

    public void onAddAnotherBentoPressed(View v) {
        mOrder.OrderItems.add(mBentoDao.getNewBento());
        mOrder.currentOrderItem = mOrder.OrderItems.size() - 1;
        mOrderDao.updateOrder(mOrder);

        onBackPressed();
    }

    public void onDeleteBentoPressed(View v) {
        edit = !edit;
        adapter.notifyDataSetChanged();
        updateUI();
    }


    public void onMinusTipPressed(View v) {
        if (mOrder.OrderDetails.tip_percentage > 0) {
            mOrder.OrderDetails.tip_percentage -= 5;
        }

        updateUI();
    }

    public void onPlusTipPressed(View v) {
        if (mOrder.OrderDetails.tip_percentage < 30) {
            mOrder.OrderDetails.tip_percentage += 5;
        }

        updateUI();
    }

    public void onLetsEatPressed() {
        if (BentoNowUtils.isValidCompleteOrder(CompleteOrderActivity.this)) {
            showLoadingDialog(getString(R.string.processing_label));

            mOrder.Stripe.stripeToken = mCurrentUser.stripe_token;
            mOrder.IdempotentToken = BentoNowUtils.getUUIDBento();
            mOrder.Platform = "Android";

            RequestParams params = new RequestParams();
            params.put("data", mOrder.toString());
            params.put("api_token", mCurrentUser.api_token);

            mOrderDao.updateOrder(mOrder);

            if (mOrder.OrderItems == null || mOrder.OrderItems.isEmpty()) {
                action = "no_items";
                track(action);
                Crashlytics.log(Log.ERROR, "Order", "No Items in the Order");
                DebugUtils.logError(TAG, "Order Items 0 ");
                emptyOrders();
                return;
            }

            DebugUtils.logDebug(TAG, "Order: " + mOrder.toString());

            BentoRestClient.post("/order", params, new TextHttpResponseHandler() {
                @SuppressWarnings("deprecation")
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    String error;
                    JSONObject json;

                    DebugUtils.logError(TAG, "Order: " + statusCode + " " + responseString);

                    try {
                        json = new JSONObject(responseString);
                        if (json.has("Error")) {
                            error = json.getString("Error");
                        } else {
                            error = json.getString("error");
                        }
                    } catch (Exception ignore) {
                        error = "No Network";
                        DebugUtils.logError(TAG, "onFailure(): " + ignore.toString());
                    }

                    switch (statusCode) {
                        case 401:
                            DebugUtils.logError(TAG, "Invalid Api Token");
                            WidgetsUtils.createShortToast("You session is expired, please LogIn again");

                            if (!userDao.removeUser())
                                userDao.clearAllData();

                            startActivity(new Intent(CompleteOrderActivity.this, SignInActivity.class));
                            break;
                        case 402:
                            action = "credit_card";
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
                            Stock.set(responseString);
                            error += mOrderDao.calculateSoldOutItems();
                            break;
                        case 423:
                            action = "closed";
                            error = BackendText.get("closed-title");
                            break;
                        default:
                            Crashlytics.log(Log.ERROR, "SendOrderError", "Code " + statusCode + " : Response " + responseString + " : Parsing " + error);
                            error = getResources().getString(R.string.error_send_order);
                            break;
                    }

                    dismissDialog();

                    track(error);

                    if (!error.equals("")) {
                        mDialog = new ConfirmationDialog(CompleteOrderActivity.this, null, error);
                        mDialog.addAcceptButton("OK", null);
                        mDialog.show();
                    }
                }

                @SuppressWarnings("deprecation")
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    track(null);

                    MixpanelUtils.trackRevenue(mOrder.OrderDetails.total_cents / 100, mCurrentUser);

                    Log.i(TAG, "Order: " + responseString);

                    mCurrentUser.stripe_token = null;

                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.UUID_BENTO, "");
                    BentoNowUtils.saveSettings(ConstantUtils.optSaveSettings.ALL);

                    dismissDialog();

                    mOrderDao.cleanUp();

                    startActivity(new Intent(CompleteOrderActivity.this, OrderConfirmedActivity.class));

                    finish();
                }
            });
        }
    }

    void updateUI() {
        mOrderDao.calculateOrder(mOrder);
        mOrderDao.updateOrder(mOrder);

        if (mOrder.OrderDetails.coupon_discount_cents > 0) {
            getBtnAddPromoCode().setTextColor(getResources().getColor(R.color.orange));
            getBtnAddPromoCode().setText("REMOVE PROMO");
            getTxtPromoTotal().setText(String.format(getString(R.string.money_format), mOrder.OrderDetails.total_cents_without_coupon / 100));
            getLayoutWrapperDiscount().setVisibility(View.VISIBLE);
            container_discount.setVisibility(View.VISIBLE);
        } else {
            getBtnAddPromoCode().setText(BackendText.get("complete-add-promo"));
            getBtnAddPromoCode().setTextColor(getResources().getColor(R.color.btn_green));
            getLayoutWrapperDiscount().setVisibility(View.GONE);
            container_discount.setVisibility(View.GONE);
        }

        txt_address.setText(BentoNowUtils.getStreetAddress());
        txt_credit_card.setText(mCurrentUser.card.last4);

        getTxtDeliveryPrice().setText(String.format(getString(R.string.money_format), (double) mOrder.OrderDetails.delivery_price));
        txt_discount.setText(String.format(getString(R.string.money_format), (double) mOrder.OrderDetails.coupon_discount_cents / 100));
        txt_tax.setText(String.format(getString(R.string.money_format), mOrder.OrderDetails.tax_cents / 100));
        txt_tip.setText(String.format(getString(R.string.tip_percentage), (double) mOrder.OrderDetails.tip_percentage) + " %");
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
                card = R.drawable.card_empty;
                break;
        }

        img_credit_card.setImageResource(card);
        txt_credit_card.setText(mCurrentUser.card.last4 != null ? mCurrentUser.card.last4 : "");

        if (edit) {
            btn_delete.setTextColor(getResources().getColor(R.color.btn_green));
            btn_delete.setText(BackendText.get("complete-done"));
        } else {
            btn_delete.setTextColor(getResources().getColor(R.color.orange));
            btn_delete.setText(BackendText.get("complete-edit"));
        }
    }


    private class ItemHolder {
        public TextView txt_name;
        public TextView txt_price;
        public ImageButton btn_edit;
        public ImageButton btn_remove;

        public ItemHolder(View view) {
            txt_name = (TextView) view.findViewById(R.id.txt_name);
            txt_price = (TextView) view.findViewById(R.id.txt_price);
            btn_edit = (ImageButton) view.findViewById(R.id.btn_edit);
            btn_remove = (ImageButton) view.findViewById(R.id.btn_remove);
        }

        public void set(OrderItem mItem, int position) {
            txt_name.setTextColor(mItem.bIsSoldoOut ? getResources().getColor(R.color.orange) : getResources().getColor(R.color.btn_green));
            txt_price.setTextColor(mItem.bIsSoldoOut ? getResources().getColor(R.color.orange) : getResources().getColor(R.color.btn_green));

            txt_name.setText(mItem.items.get(0).name);

            txt_price.setText("$" + BentoNowUtils.getNumberFromPrice(OrderDao.getPriceByOrder(mItem)));

            btn_remove.setVisibility(selected == position ? View.VISIBLE : View.GONE);
            btn_edit.setVisibility(edit ? View.VISIBLE : View.GONE);
            btn_edit.setTag(position);
        }
    }

    @Override
    public int getCount() {
        return mOrder.OrderItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_order, null);

            holder = new ItemHolder(convertView);

            holder.btn_remove.setOnClickListener(this);
            holder.btn_edit.setOnClickListener(this);

            convertView.setTag(holder);
        } else {
            holder = (ItemHolder) convertView.getTag();
        }

        holder.set(mOrder.OrderItems.get(position), position);

        return convertView;
    }

    public void showAddPromoCodeDialog(View v) {
        if (mOrder.OrderDetails.coupon_discount_cents > 0) {
            mOrder.OrderDetails.coupon_discount_cents = 0;
            updateUI();
        } else {
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

    private void showLoadingDialog(String sText) {
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            mProgressDialog = new ProgressDialog(CompleteOrderActivity.this, sText);
            mProgressDialog.show();
        }
    }

    @Override
    public void openErrorActivity() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);
        onBackPressed();
    }

    @Override
    public void openMainActivity() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);
        BentoNowUtils.openMainActivity(CompleteOrderActivity.this);
    }

    @Override
    public void openBuildBentoActivity() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, Settings.status);
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

    private RelativeLayout getLayoutWrapperDiscount() {
        if (layoutWrapperDiscount == null)
            layoutWrapperDiscount = (RelativeLayout) findViewById(R.id.layout_wrapper_discount);
        return layoutWrapperDiscount;
    }

    private TextView getTxtPromoTotal() {
        if (txtPromoTotal == null)
            txtPromoTotal = (TextView) findViewById(R.id.txt_promo_total);
        return txtPromoTotal;
    }

    private BackendButton getBtnOnLetsEatPressed() {
        if (btnOnLetsEatPressed == null)
            btnOnLetsEatPressed = (BackendButton) findViewById(R.id.btn_on_lets_eat_pressed);
        return btnOnLetsEatPressed;
    }

}
