package com.bentonow.bentonow;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.dialog.ConfirmDialog;
import com.bentonow.bentonow.dialog.CustomDialog;
import com.bentonow.bentonow.model.Dish;
import com.bentonow.bentonow.model.Ioscopy;
import com.bentonow.bentonow.model.Item;
import com.bentonow.bentonow.model.Orders;
import com.bentonow.bentonow.model.Shop;
import com.bentonow.bentonow.model.User;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class CompleteOrderActivity extends BaseActivity {

    private static final String TAG = "CompleteOrderActivity";
    private TextView address_textview;
    private TextView btn_edit_address;
    private TextView credit_card_textview;
    private Orders current_order;
    private User current_user;
    private TextView btn_add_another_bento;
    private TextView tax_detail;
    private TextView btn_tip_negative, tip_percentTextView, btn_tip_positive;
    private TextView order_total_textview;
    private String credit_card_data = "";
    private TextView btn_confirm_pay_order;
    private AQuery aq;
    private TextView btn_add_credit_card, btn_cancel;
    private RelativeLayout overlay;
    private TextView btn_edit_credit_card;
    private Boolean editMode = false;
    private TextView btn_edit_items;
    private RelativeLayout overlay_coupon,overlay_coupon_result;
    private EditText promo_code;
    private ProgressBar progressBar;
    private LinearLayout message_box, row_discount;
    private TextView coupon_result_message;
    private TextView discount_cents;
    private boolean processing = false;

    private RelativeLayout oberlay_closed;

    private RelativeLayout oberlay_out_of_stock;

    private RelativeLayout overlay_bad_address;

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_order);
        aq = new AQuery(this);
        // INITIALS METHODS
        initAll();
    }

    private void initAll() {

        initActionbar();
        initElemnts();
        changeInitialsValues();
        addListeners();

        // TO DEBUG IF BENTO CLOSED
        if( Bentonow.pending_order_id == null ) tryGetPendingOrder();

        // INITIATE SHOW DETAILS
        if ( Bentonow.pending_order_id != null ) showOrderDetails();
        else Toast.makeText(getApplicationContext(),"There is not pending order",Toast.LENGTH_LONG).show();
    }

    private void changeInitialsValues() {
        Log.i(TAG, "changeInitialsValues()");

        //user = User.findById(User.class, (long) 1);

        // INITIATE FIELD ORDER
        current_order = Orders.findById(Orders.class,Bentonow.pending_order_id);
        if ( current_order.coords_lat == null || current_order.address_street == null ) {
            startActivity(new Intent(getApplicationContext(), DeliveryLocationActivity.class));
            finish();
            overridePendingTransitionGoLeft();
        }



        // INITIATE USER
        current_user = User.findById(User.class, (long) 1);

        // PAYMENT METHOD
        String cardbrand = "";
        String cardlast4 = "";
        if( current_user != null && current_user.cardbrand!=null ) cardbrand = current_user.cardbrand;
        if( current_user != null && current_user.cardlast4!=null ) cardlast4 = current_user.cardlast4;
        credit_card_data = cardbrand+" "+cardlast4;

        // Config
        Config.CurrentOrder.item_price = Double.parseDouble(Ioscopy.getKeyValue(Config.IOSCOPY.PRICE));
        Log.i(TAG, "Config.CurrentOrder.item_price: " + Config.CurrentOrder.item_price);
        Config.CurrentOrder.tax = Double.parseDouble(Ioscopy.getKeyValue(Config.IOSCOPY.TAX_PERCENT));
        Log.i(TAG, "Config.CurrentOrder.tax: " + Config.CurrentOrder.tax);
    }

    private void initElemnts() {
        Log.i(TAG, "initElemnts()");
        address_textview = (TextView)findViewById(R.id.address_textview);
        btn_edit_address = (TextView)findViewById(R.id.btn_edit_address);
        credit_card_textview = (TextView)findViewById(R.id.credit_card_textview);
        btn_add_another_bento = (TextView)findViewById(R.id.btn_add_another_bento);
        tax_detail = (TextView)findViewById(R.id.tax_detail);
        /// TIP
        btn_tip_negative = (TextView)findViewById(R.id.btn_tip_negative);
        btn_tip_positive = (TextView)findViewById(R.id.btn_tip_positive);
        tip_percentTextView = (TextView)findViewById(R.id.tip_percent);
        //
        order_total_textview = (TextView)findViewById(R.id.order_total_textview);
        btn_confirm_pay_order = (TextView)findViewById(R.id.btn_confirm_pay_order);

        // OVERLAY
        overlay = (RelativeLayout)findViewById(R.id.overlay);
        btn_cancel = (TextView)findViewById(R.id.btn_cancel);
        btn_add_credit_card = (TextView)findViewById(R.id.btn_add_credit_card);
        btn_edit_credit_card = (TextView)findViewById(R.id.btn_edit_credit_card);

        btn_edit_items = (TextView)findViewById(R.id.btn_edit_items);

        overlay_coupon = (RelativeLayout)findViewById(R.id.overlay_coupon);
        overlay_coupon_result = (RelativeLayout)findViewById(R.id.overlay_coupon_result);
        promo_code = (EditText)findViewById(R.id.promo_code);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        message_box = (LinearLayout)findViewById(R.id.message_box);
        coupon_result_message = (TextView)findViewById(R.id.coupon_result_message);

        row_discount = (LinearLayout)findViewById(R.id.row_discount);
        discount_cents = (TextView)findViewById(R.id.discount_cents);

        oberlay_out_of_stock = (RelativeLayout)findViewById(R.id.overlay_souldout);

        oberlay_closed = (RelativeLayout)findViewById(R.id.overlay_closed);

        overlay_bad_address = (RelativeLayout)findViewById(R.id.overlay_bad_address);
    }

    private void addListeners() {

        findViewById(R.id.btn_change_address_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overlay_bad_address.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.btn_change_address_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DeliveryLocationActivity.class);
                intent.putExtra("btnBack", true);
                startActivity(intent);
                overridePendingTransitionGoLeft();
            }
        });

        findViewById(R.id.btn_ok_closed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oberlay_closed.setVisibility(View.GONE);
                Bentonow.isOpen = false;
                startActivity(new Intent(getApplicationContext(), ErrorClosedActivity.class));
                overridePendingTransition(R.anim.bottom_slide_in, R.anim.none);
                finish();
            }
        });

        findViewById(R.id.btn_ok_souldout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oberlay_out_of_stock.setVisibility(View.GONE);
                Bentonow.pending_bento_id = Orders.itemWithDishOutOfStock();
                startActivity(new Intent(getApplicationContext(), BuildBentoActivity.class));
                overridePendingTransitionGoLeft();
            }
        });

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initAll();
                overlay_coupon_result.setVisibility(View.GONE);
            }
        });

        promo_code.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    // if shift key is down, then we want to insert the '\n' char in the TextView;
                    // otherwise, the default action is to send the message.
                    if (!event.isShiftPressed()) {
                        submitPromoCode();
                        return true;
                    }
                    return false;
                }

                submitPromoCode();
                return true;
            }
        });

        findViewById(R.id.btn_send_promo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            submitPromoCode();
            }
        });

        findViewById(R.id.btn_cancel_promo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay_coupon.setVisibility(View.GONE);
            }
        });


        findViewById(R.id.btn_add_promo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay_coupon.setVisibility(View.VISIBLE);
            }
        });

        btn_edit_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editMode){
                    editMode = false;
                    initAll();
                    btn_edit_items.setTextColor(getResources().getColor(R.color.btn_green));
                    btn_edit_items.setText("DELETE");
                }else {
                    editMode = true;
                    loadOrderItems();
                    btn_edit_items.setTextColor(getResources().getColor(R.color.orange));
                    btn_edit_items.setText("DONE");
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay.setVisibility(View.INVISIBLE);
            }
        });

        btn_add_credit_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay.setVisibility(View.INVISIBLE);
                goToAddCreditCard();
            }
        });

        btn_edit_credit_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay.setVisibility(View.INVISIBLE);
                goToAddCreditCard();
            }
        });

        // BTN EDIT ADDRESS
        btn_edit_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DeliveryLocationActivity.class);
                intent.putExtra("btnBack", true);
                startActivity(intent);
                overridePendingTransitionGoLeft();
            }
        });

        // ADD ANOTHER BENTO
        btn_add_another_bento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Bentonow.pending_bento_id!=null) {
                    Item current_betno = Item.findById(Item.class, Bentonow.pending_bento_id);
                    if (current_betno != null && current_betno.isFull()) {
                        current_betno.completed = "yes";
                        current_betno.save();
                        createNewBentoBox();
                    }
                }
                createNewBentoBox();
                Intent intent = new Intent(getApplicationContext(), BuildBentoActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransitionGoLeft();
            }
        });

        // TIP NEGATIVE
        btn_tip_negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( Config.CurrentOrder.tip_percent > 0 ) Config.CurrentOrder.tip_percent-=5;
                calculateValues();
                showOrderDetails();
            }
        });
        // TIP POSITIVE
        btn_tip_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( Config.CurrentOrder.tip_percent < 30 ) Config.CurrentOrder.tip_percent+=5;
                calculateValues();
                showOrderDetails();
            }
        });

        // CONFIRM PAY ORDER
        btn_confirm_pay_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "user: " + current_user.toString());

                if(!Orders.addressVerification(new LatLng(Double.valueOf(current_order.coords_lat),Double.valueOf(current_order.coords_long)))){
                    overlay_bad_address.setVisibility(View.VISIBLE);
                }

                if (current_user != null && current_user.cardlast4 != null && !current_user.cardlast4.isEmpty()) {
                    JSONObject data = new JSONObject();
                    Orders current_order = Orders.findById(Orders.class, Bentonow.pending_order_id);
            /*
             *  ORDER ITEMS
             */
                    JSONArray OrderItems = new JSONArray();
                    List<Item> orderItems = Item.find(Item.class, "orderid=? and completed = ?", String.valueOf(Bentonow.pending_order_id), Config.ORDER.STATUS.COMPLETED);
                    for (Item orderItem : orderItems) {
                        try {
                            JSONObject item = new JSONObject();
                            JSONObject itemBento;
                            JSONArray items = new JSONArray();

                            itemBento = new JSONObject();
                            itemBento.put("type", "main");
                            itemBento.put("id", Integer.valueOf(orderItem.main));
                            items.put(itemBento);

                            itemBento = new JSONObject();
                            itemBento.put("type", "side1");
                            itemBento.put("id", Integer.valueOf(orderItem.side1));
                            items.put(itemBento);

                            itemBento = new JSONObject();
                            itemBento.put("type", "side2");
                            itemBento.put("id", Integer.valueOf(orderItem.side2));
                            items.put(itemBento);

                            itemBento = new JSONObject();
                            itemBento.put("type", "side3");
                            itemBento.put("id", Integer.valueOf(orderItem.side3));
                            items.put(itemBento);

                            itemBento = new JSONObject();
                            itemBento.put("type", "side4");
                            itemBento.put("id", Integer.valueOf(orderItem.side4));
                            items.put(itemBento);

                            item.put("items", items);
                            item.put("item_type", "CustomerBentoBox");
                            OrderItems.put(item);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        data.put("OrderItems", OrderItems);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

            /*
             *  ORDER DETAILS
             */
                    JSONObject OrderDetails = new JSONObject();
                    try {
                        OrderDetails.put("tax_cents", Config.CurrentOrder.total_tax_cost * 100);
                        OrderDetails.put("tip_cents", Config.CurrentOrder.total_tip_cost * 100);
                        OrderDetails.put("total_cents", Config.CurrentOrder.total_order_cost * 100);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

            /*
             *  address
             */
                    JSONObject address = new JSONObject();
                    try {
                        address.put("number", current_order.address_number);
                        address.put("street", current_order.address_street);
                        address.put("city", current_order.address_city);
                        address.put("state", current_order.address_state);
                        address.put("zip", current_order.address_zip);
                        OrderDetails.put("address", address);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                /*
                 *  STRIPE
                 */
                    JSONObject Stripe = new JSONObject();
                    if (current_user.stripetoken != null && !current_user.stripetoken.isEmpty() && Config.CurrentOrder.total_order_cost > 0) {
                        try {
                            Stripe.put("stripeToken", current_user.stripetoken);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            data.put("Stripe", Stripe);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Stripe.put("stripeToken", "NULL");
                            data.put("Stripe", Stripe);
                            Log.i(TAG, "Stripe: " + Stripe.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (current_order.amountoff != null && !current_order.amountoff.isEmpty() ) {
                        try {
                            data.put("CouponCode", current_order.couponcode);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.i(TAG, "No hay promo. current_order.amountoff ");
                    }


            /*
             *  coords
             */
                    JSONObject coords = new JSONObject();
                    try {
                        coords.put("lat", current_order.coords_lat);
                        coords.put("long", current_order.coords_long);
                        OrderDetails.put("coords", coords);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        data.put("OrderDetails", OrderDetails);
                        Log.i(TAG, "data: " + data.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    postOrderData(data.toString());
                } else {
                    overlay.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void submitPromoCode () {
        if(!promo_code.getText().toString().isEmpty()) {
            overlay_coupon.setVisibility(View.GONE);
            overlay_coupon_result.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            message_box.setVisibility(View.INVISIBLE);
            postPromoCode(promo_code.getText().toString());
            promo_code.setText("");
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(promo_code.getWindowToken(), 0);
        }
    }

    private void postPromoCode(final String promo_code) {
        String uri = getResources().getString(R.string.server_api_url)+Config.API.COUPON.APPLY+promo_code+"?api_token="+current_user.apitoken;
        Log.i(TAG, "uri: " + uri);
        aq.ajax(uri, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                Log.i(TAG,"status.getCode(): "+status.getCode());
                progressBar.setVisibility(View.GONE);
                message_box.setVisibility(View.VISIBLE);
                if( status.getCode() == Config.API.COUPON.RESPONSE.OK200 ){
                    //{"amountOff": "12.00"}
                    if (json != null) {
                        try {
                            Log.i(TAG,"json: "+json.toString());
                            current_order.amountoff = json.getString("amountOff");
                            current_order.couponcode = promo_code;
                            current_order.save();
                            overlay_coupon_result.setVisibility(View.INVISIBLE);
                            initAll();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(TAG, status.getError());
                    }
                } else if( status.getCode() == Config.API.COUPON.RESPONSE.INVALID_COUPON_400 ) {
                    if(json!=null){
                        try {
                            String error = json.getString("error");
                            Log.e(TAG, "ERROR: " + error);
                            coupon_result_message.setText(error);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void createNewBentoBox() {
        Log.i(TAG,"createNewBentoBox()");
        Item item = new Item();
        item.completed = "no";
        item.orderid = String.valueOf(Bentonow.pending_order_id);
        item.save();
        Bentonow.pending_bento_id = item.getId();
    }

    private void goToAddCreditCard() {
        Log.i(TAG,"goToAddCreditCard()");
        Intent intent = new Intent(getApplicationContext(),EnterCreditCardActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransitionGoRight();
    }

    public void postOrderData(final String data){
        dialog = ProgressDialog.show(this, null, "Processing...", true);

        if (!processing) {
            processing = true;
            if (current_user != null && current_user.apitoken != null && !current_user.apitoken.isEmpty()) {
                String uri = getResources().getString(R.string.server_api_url) + Config.API.ORDER;
                Log.i(TAG, "uri: " + uri);
                Log.i(TAG, "api_token: " + current_user.apitoken);
                Map<String, Object> params = new HashMap<>();
                params.put("data", data);
                params.put("api_token", current_user.apitoken);
                Log.i(TAG, "postOrderData(data) " + data);
                aq.ajax(uri, params, String.class, new AjaxCallback<String>() {
                    @Override
                    public void callback(String url, String json, final AjaxStatus status) {
                        dialog.dismiss();

                        processing = false;

                        Log.i(TAG, "status.getCode(): " + status.getCode());
                        Log.i(TAG, "status.getError(): " + status.getError());
                        Log.i(TAG, "status.getMessage(): " + status.getMessage());
                        switch (status.getCode()) {
                            case 200:
                                Orders current_order = Orders.findById(Orders.class, Bentonow.pending_order_id);
                                current_order.completed = Config.ORDER.STATUS.COMPLETED;
                                current_order.save();
                                current_user.stripetoken = null;
                                current_user.save();
                                Bentonow.pending_order_id = null;
                                Bentonow.pending_bento_id = null;
                                Intent intent = new Intent(getApplicationContext(), OrderConfirmedActivity.class);
                                startActivity(intent);
                                finish();
                                overridePendingTransitionGoRight();
                                break;
                            // Payment failed. //{"error":"You cannot use a Stripe token more than once:tok_16DJ8QEmZcPNENoGBop6zv3A."}
                            case 406:
                                if (status.getError().contains("You cannot use a Stripe token more than once")) {
                                    Log.i(TAG, "postOrderData retry: You cannot use a Stripe token more than once");
                                    current_user.stripetoken = null;
                                    current_user.save();
                                    postOrderData(data);
                                } else {
                                    dialog = showDialogError(status.getError(), null);
                                }
                                break;
                                // if the restaurant is not open.
                            case 423:
                                dialog = showDialogError(status.getError(), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                        Shop.status = "closed";
                                        Intent intent = new Intent(getApplicationContext(), ErrorClosedActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                });
                                break;
                            // Not payment specified
                            case 402:
                                dialog = showDialogError(status.getMessage(), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                        startActivity(new Intent(getApplicationContext(), EnterCreditCardActivity.class));
                                        overridePendingTransitionGoRight();
                                    }
                                });
                                break;
                            //if the inventory is not available.
                            case 410:
                                dialog = showDialogError(status.getError(), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                        try {
                                            JSONObject error_message = new JSONObject(status.getError());
                                            JSONArray MenuStatus = error_message.getJSONArray("MenuStatus");
                                            BentoService.processMenuStock(MenuStatus);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                break;
                            // Unautorized
                            case 401:
                                dialog = showDialog("Your session has expired, please sign in", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                                        User user = User.currentUser();
                                        user.reset();
                                        finish();
                                        overridePendingTransitionGoLeft();
                                    }
                                });
                                break;
                            default:
                                showDialog(status.getMessage(), null);
                                break;
                        }
                    }
                });
            }
        }
    }

    CustomDialog showDialog (String message, View.OnClickListener listener) {
        CustomDialog _dialog = new CustomDialog(this, message, "Ok", null);
        _dialog.show();
        if (listener != null) {
            _dialog.setOnOkPressed(listener);
        }
        return _dialog;
    }

    CustomDialog showDialogError (String json, View.OnClickListener listener) {
        String message = "";

        try {
            message = new JSONObject(json).getString("Error");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            message = new JSONObject(json).getString("error");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return showDialog(message, listener);
    }

    public void loadOrderItems(){
        Log.i(TAG,"loadOrderItems()");
        final ArrayList<HashMap<String, String>> orderItemsList = new ArrayList<>();
        List<Item> items = Item.find(Item.class,"orderid = ?", String.valueOf(Bentonow.pending_order_id));
        Config.CurrentOrder.total_items = 0;
        for( Item item : items ){
            HashMap<String, String> map = new HashMap<>();
            Log.i(TAG, "item: (Bento) " + item.toString());
            if(item.main!=null) {
                long dish_id = Dish.getIdBy_id(item.main);
                if (dish_id != 0) {
                    Dish dish = Dish.findById(Dish.class, dish_id);
                    map.put(Config.DISH.NAME, dish.name);
                    map.put(Config.DISH.ITEM_ID, String.valueOf(item.getId()));
                    Config.CurrentOrder.total_items++;
                    calculateValues();
                    orderItemsList.add(map);
                }
            }
        }
        ConfirmDialog dialog = new ConfirmDialog(findViewById(R.id.overlay_remove_bento), findViewById(R.id.btn_bento_cancel), findViewById(R.id.btn_bento_ok));
        ListView itemsListView = (ListView) findViewById(R.id.orderitems);
        OrderItemsListAdapter adapter = new OrderItemsListAdapter(this, orderItemsList, editMode, dialog);
        itemsListView.setAdapter(adapter);
        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> row = orderItemsList.get(position);
                Bentonow.pending_bento_id = Long.valueOf(row.get(Config.DISH.ITEM_ID));
                Intent intent = new Intent(getApplicationContext(),BuildBentoActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransitionGoLeft();
            }
        });
    }

    private void calculateValues() {
        Log.i(TAG,"calculateValues()");
        Config.CurrentOrder.total_items_cost = 0.0;
        Config.CurrentOrder.total_tax_cost = 0.0;
        Config.CurrentOrder.total_order_cost = 0.0;

        double amountoff = 0;
        if( current_order.couponcode != null && current_order.amountoff != null ) amountoff = Double.valueOf(current_order.amountoff);


        Config.CurrentOrder.total_items_cost = (Config.CurrentOrder.total_items*Config.CurrentOrder.item_price);

        Config.CurrentOrder.total_tip_cost = round(Config.CurrentOrder.total_items_cost * Config.CurrentOrder.tip_percent / 100,2);
        Config.CurrentOrder.total_tax_cost = round((Config.CurrentOrder.total_items_cost - amountoff)*(Config.CurrentOrder.tax / 100),2);
        Config.CurrentOrder.total_order_cost = round( ( Config.CurrentOrder.total_tax_cost+Config.CurrentOrder.total_items_cost ) - amountoff , 2 );

        if ((Config.CurrentOrder.total_tax_cost + Config.CurrentOrder.total_items_cost ) - amountoff < 0) {
            Config.CurrentOrder.total_order_cost = 0;
            Config.CurrentOrder.total_tax_cost = 0;
        }

        Config.CurrentOrder.total_order_cost += Config.CurrentOrder.total_tip_cost;

        if (Config.CurrentOrder.total_order_cost < 0.5 && Config.CurrentOrder.total_order_cost >= 0.1) {
            Config.CurrentOrder.total_order_cost = 0.5;
        }
    }

    private void showOrderDetails() {
        Log.i(TAG, "showOrderDetails()");
        // ORDER ADDRESS
        address_textview.setText(current_order.getOrderAddressStreet());
        // CREDIT CARD
        credit_card_textview.setText(credit_card_data);
        // LOAD ITEMS
        loadOrderItems();
        // CHECK IF HAVE A COUPON
        if( current_order.couponcode != null && current_order.amountoff != null ){
            Log.i(TAG,"current_order.couponcode: "+current_order.couponcode+"current_order.amountoff: "+current_order.amountoff);
            row_discount.setVisibility(View.VISIBLE);
            discount_cents.setText("$"+current_order.amountoff);
        }

        //TIP
        tip_percentTextView.setText(Config.CurrentOrder.tip_percent+"%");
        // SHOW TAX
        String total_tax = String.valueOf(String.format(Locale.US,"%.2f", Config.CurrentOrder.total_tax_cost));
        tax_detail.setText("$" + total_tax);

        // SHOW TOTAL
        String total_order_cost = String.valueOf(String.format(Locale.US,"%.2f", Config.CurrentOrder.total_order_cost));
        order_total_textview.setText("$" + total_order_cost);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private void tryGetPendingOrder() {
        Log.i(TAG, "checkForPendingOrder()");
        List<Orders> pending_orders = Orders.find(Orders.class, "completed = ? AND today = ?", "no", todayDate);
        if( !pending_orders.isEmpty() ) {
            for ( Orders order : pending_orders) {
                Bentonow.pending_order_id = order.getId();
            }
        }else{
            Toast.makeText(getApplicationContext(),"There is not pending order",Toast.LENGTH_LONG).show();
        }
    }

    private void initActionbar() {
        Log.i(TAG,"initActionbar()");
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText("Summary");

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BuildBentoActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransitionGoLeft();
            }
        });
    }

}
