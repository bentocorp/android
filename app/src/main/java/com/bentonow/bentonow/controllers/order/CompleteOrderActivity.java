package com.bentonow.bentonow.controllers.order;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bentonow.bentonow.BuildConfig;
import com.bentonow.bentonow.controllers.payment.EnterCreditCardActivity;
import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.Mixpanel;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.errors.ErrorActivity;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.controllers.session.SignInActivity;
import com.bentonow.bentonow.controllers.session.SignUpActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.Stock;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.ui.BackendButton;
import com.bentonow.bentonow.ui.CustomDialog;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.wsdcamp.list.LazyListAdapter;
import com.wsdcamp.list.LazyListAdapterInterface;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class CompleteOrderActivity extends BaseActivity implements View.OnClickListener, LazyListAdapterInterface, OnItemClickListener {
    private static final String TAG = "CompleteOrderActivity";

    TextView txt_address;
    TextView txt_credit_card;
    TextView txt_discount;
    TextView txt_tax;
    TextView txt_tip;
    TextView txt_total;

    ImageView img_credit_card;

    View container_discount;

    BackendButton btn_delete;

    LayoutInflater inflater;

    boolean edit = false;
    int selected = -1;
    LazyListAdapter adapter;
    String action = "";
    CustomDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_order);

        txt_address = (TextView) findViewById(R.id.txt_address);
        txt_credit_card = (TextView) findViewById(R.id.txt_credit_card);
        txt_discount = (TextView) findViewById(R.id.txt_discount);
        txt_tax = (TextView) findViewById(R.id.txt_tax);
        txt_tip = (TextView) findViewById(R.id.txt_tip_percent);
        txt_total = (TextView) findViewById(R.id.txt_total);

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

    @Override
    protected void onResume() {
        super.onResume();

        if ( User.current == null ) {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        } else if ( Order.location == null || Order.address == null ) {
            Intent intent = new Intent(this, DeliveryLocationActivity.class);
            intent.putExtra("completeOrder", true);
            startActivity(intent);
            finish();
        } else if ( User.current.card.last4 == null || User.current.card.last4.isEmpty() ) {
            startActivity(new Intent(this, EnterCreditCardActivity.class));
        } else {
            updateUI();
        }
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(BackendText.get("complete-title"));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(this);
    }

    void deleteBento () {
        Order.current.OrderItems.remove(selected);
        selected = -1;
        adapter.notifyDataSetChanged();

        if (Order.current.OrderItems.size() == 0) {
            Order.cleanUp();
        }

        updateUI();
    }

    void track (String error) {
        try {
            JSONObject params = new JSONObject();
            params.put("quantity", Order.current.OrderItems.size());
            params.put("payment method", User.current.card.brand);
            params.put("total price", Order.current.OrderDetails.total_cents / 100);
            params.put("status", error == null ? "success" : "failure");
            params.put("status_error", error);

            Mixpanel.track(CompleteOrderActivity.this, "Placed an order", params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void requestPromoCode (final String code) {
        Log.i(TAG, "requestPromoCode " + code);
        if (code == null || code.isEmpty()) {
            dialog = new CustomDialog(this, "Invalid coupon code", null, "OK");
            dialog.show();
        } else {
            dialog = new CustomDialog(this, "Processing...", true);
            dialog.show();

            RequestParams params = new RequestParams();
            params.put("api_token", User.current.api_token);
            BentoRestClient.get("/coupon/apply/" + code, params, new TextHttpResponseHandler() {
                @SuppressWarnings("deprecation")
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    dialog.dismiss();

                    Log.i(TAG, "requestPromoCode failed " + responseString);

                    try {
                        new CustomDialog(
                                CompleteOrderActivity.this,
                                new JSONObject(responseString).getString("error"),
                                null,
                                "OK"
                        );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @SuppressWarnings("deprecation")
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    dialog.dismiss();

                    Log.i(TAG, "requestPromoCode success " + responseString);

                    User.current.coupon_code = Order.current.CouponCode = code;
                    int discount = 0;

                    try {
                        discount = Integer.parseInt(new JSONObject(responseString).getString("amountOff").replace(".", ""));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.i(TAG, "discount " + discount);

                    Order.current.OrderDetails.coupon_discount_cents = discount;
                    Settings.save(CompleteOrderActivity.this);

                    updateUI();
                }
            });
        }
    }

    //region OnClick

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
            case R.id.btn_remove:
                if (Order.current.OrderItems.size() == 1) {
                    action = "delete";
                    CustomDialog dialog = new CustomDialog(
                            this,
                            BackendText.get("complete-remove-all-text"),
                            BackendText.get("complete-remove-all-confirmation-2"),
                            BackendText.get("complete-remove-all-confirmation-1")
                    );
                    dialog.show();
                    dialog.setOnOkPressed(this);
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
            case R.id.btn_ok:
                switch (action) {
                    case "delete":
                        deleteBento();
                        onBackPressed();
                        break;
                    case "closed":
                        Intent intent = new Intent(this, ErrorActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        break;
                    case "credit_card":
                        startActivity(new Intent(this, EnterCreditCardActivity.class));
                        break;
                    case "sign_in":
                        startActivity(new Intent(this, SignInActivity.class));
                        finish();
                        break;
                    case "promo_code":
                        requestPromoCode(dialog.getText());
                        break;
                }
                action = "";
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Order.current.currentOrderItem = position;
        onBackPressed();
    }

    public void onChangeAddressPressed (View v) {
        Intent intent = new Intent(this, DeliveryLocationActivity.class);
        intent.putExtra("back", true);
        startActivity(intent);
    }

    public void onChangeCreditCardPressed (View v) {
        startActivity(new Intent(this, EnterCreditCardActivity.class));
    }

    public void onAddAnotherBentoPressed (View v) {
        Order.current.OrderItems.add(new OrderItem());
        Order.current.currentOrderItem = Order.current.OrderItems.size()-1;
        onBackPressed();
    }

    public void onDeleteBentoPressed (View v) {
        edit = !edit;
        adapter.notifyDataSetChanged();
        updateUI();
    }

    public void onAddPromoCodePressed (View v) {
        dialog = new CustomDialog(this, true);
        dialog.show();
        dialog.setOnOkPressed(this);
        action = "promo_code";

        if (BuildConfig.DEBUG) {
            dialog.setText("1121113370998kkk7");
        }
    }

    public void onMinusTipPressed (View v) {
        if (Order.current.OrderDetails.tip_percentage > 0) {
            Order.current.OrderDetails.tip_percentage -= 5;
        }

        updateUI();
    }

    public void onPlusTipPressed (View v) {
        if (Order.current.OrderDetails.tip_percentage < 30) {
            Order.current.OrderDetails.tip_percentage += 5;
        }

        updateUI();
    }

    public void onLetsEatPressed (View v) {
        dialog = new CustomDialog(this, "Processing...", true);
        dialog.show();

        Order.current.Stripe.stripeToken = User.current.stripe_token;

        RequestParams params = new RequestParams();
        params.put("data", Order.current.toString());
        params.put("api_token", User.current.api_token);

        Log.i(TAG, "Order: " + Order.current.toString());

        BentoRestClient.post("/order", params, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                String error = "";
                JSONObject json;

                Log.e(TAG, "Order: " + statusCode + " " + responseString);

                try {
                    json = new JSONObject(responseString);
                    if (json.has("Error")) {
                        error = json.getString("Error");
                    } else {
                        error = json.getString("error");
                    }
                } catch (JSONException ignore) {
                }

                switch (statusCode) {
                    case 401:
                        error = "Your session has expired, please sign in";
                        action = "sign_in";
                        break;
                    case 402:
                        action = "credit_card";
                        dialog.dismiss();
                        break;
                    case 406:
                        if (responseString.contains("You cannot use a Stripe token more than once")) {
                            User.current.stripe_token = null;
                            Settings.save(getApplicationContext());
                            error = "";
                            onLetsEatPressed(null);
                        }
                        break;
                    case 410:
                        Stock.set(responseString);
                    case 423:
                        action = "closed";
                        break;
                }

                dialog.dismiss();
                track(error);

                if (!error.equals("")) {
                    dialog = new CustomDialog(CompleteOrderActivity.this, error, "Ok", null);
                    dialog.show();
                    dialog.setOnOkPressed(CompleteOrderActivity.this);
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                track(null);

                Log.i(TAG, "Order: " + responseString);

                User.current.stripe_token = null;
                Settings.save(getApplicationContext());

                dialog.dismiss();

                startActivity(new Intent(CompleteOrderActivity.this, OrderConfirmedActivity.class));
                Order.cleanUp();
                finish();
            }
        });
    }

    //endregion

    //region UI

    void updateUI () {
        Order.calculate();

        container_discount.setVisibility(Order.current.OrderDetails.coupon_discount_cents <= 0 ? View.GONE : View.VISIBLE);

        txt_address.setText(Order.getStreetAddress());
        txt_credit_card.setText(User.current.card.last4);

        txt_discount.setText("$ " + Order.current.OrderDetails.coupon_discount_cents / 100);
        txt_tax.setText("$ " + Order.current.OrderDetails.tax_cents / 100);
        txt_tip.setText(Order.current.OrderDetails.tip_percentage + "%");
        txt_total.setText("$ " + Order.current.OrderDetails.total_cents / 100);

        int card;

        switch (User.current.card.brand) {
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
        txt_credit_card.setText(User.current.card.last4 != null ? User.current.card.last4 : "");

        if(edit){
            btn_delete.setTextColor(getResources().getColor(R.color.btn_green));
            btn_delete.setText(BackendText.get("complete-done"));
        } else {
            btn_delete.setTextColor(getResources().getColor(R.color.orange));
            btn_delete.setText(BackendText.get("complete-edit"));
        }
    }

    //endregion

    //region List

    private class ItemHolder {
        public TextView txt_name;
        public TextView txt_price;
        public ImageButton btn_edit;
        public ImageButton btn_remove;

        public ItemHolder (View view) {
            txt_name = (TextView) view.findViewById(R.id.txt_name);
            txt_price = (TextView) view.findViewById(R.id.txt_price);
            btn_edit = (ImageButton) view.findViewById(R.id.btn_edit);
            btn_remove = (ImageButton) view.findViewById(R.id.btn_remove);
        }

        public void set (OrderItem item, int position) {
            txt_name.setText(item.items.get(0).name);
            txt_price.setText("$ " + item.unit_price);

            btn_remove.setVisibility(selected == position ? View.VISIBLE : View.GONE);
            btn_edit.setVisibility(edit ? View.VISIBLE : View.GONE);
            btn_edit.setTag(position);
        }
    }

    @Override
    public int getCount() {
        return Order.current.OrderItems.size();
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

        holder.set(Order.current.OrderItems.get(position), position);

        return convertView;
    }

    //endregion
}
