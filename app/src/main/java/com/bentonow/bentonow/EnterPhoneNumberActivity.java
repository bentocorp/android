package com.bentonow.bentonow;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.Utils.Mixpanel;
import com.bentonow.bentonow.model.Orders;
import com.bentonow.bentonow.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class EnterPhoneNumberActivity extends BaseActivity {

    private static final String TAG = "EnterPhoneNumber";
    private EditText phone_number;
    private ImageView phone_number_ico;
    private AQuery aq;
    private TextView btn_done;
    private RelativeLayout overlay;
    private TextView overlay_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_phone_number);
        aq = new AQuery(this);
        initActionbar();
        initElements();
        addListeners();
        footerLegal();
    }

    private void footerLegal() {
        //////////////////////////////////////
        TextView btn_privacy_policy = (TextView) findViewById(R.id.btn_privacy_policy);
        TextView btn_terms_conditions = (TextView) findViewById(R.id.btn_terms_conditions);

        btn_privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PrivacyPolicyActivity.class);
                startActivity(intent);
                overridePendingTransitionGoRight();
            }
        });

        btn_terms_conditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TermAndConditionsActivity.class);
                startActivity(intent);
                overridePendingTransitionGoRight();
            }
        });
        ///////////////////////////////////////////
    }

    private void initElements() {
        phone_number = (EditText)findViewById(R.id.phone_number);
        phone_number_ico = (ImageView)findViewById(R.id.phone_number_ico);
        btn_done = (TextView)findViewById(R.id.btn_done);

        phone_number.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        overlay = (RelativeLayout)findViewById(R.id.overlay);
        overlay_message = (TextView)findViewById(R.id.overlay_message);
    }

    private void addListeners(){

        findViewById(R.id.overlay_btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //overlay.setVisibility(View.GONE);
                finish();
                overridePendingTransitionGoLeft();
            }
        });

        phone_number.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    // if shift key is down, then we want to insert the '\n' char in the TextView;
                    // otherwise, the default action is to send the message.
                    if (!event.isShiftPressed()) {
                        submitForm();
                        return true;
                    }
                    return false;
                }

                submitForm();
                return true;
            }
        });

        phone_number.addTextChangedListener(new TextWatcher() {
            int oldLenght;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String phone = phone_number.getText().toString().replaceAll("[^0-9]", "");
                StringBuilder sb;

                if (oldLenght != phone.length()) {
                    oldLenght = phone.length();

                    if (phone.length() <= 3) {
                        sb = new StringBuilder(phone)
                                .insert(0, "(");
                    } else if (phone.length() <= 6) {
                        sb = new StringBuilder(phone)
                                .insert(0, "(")
                                .insert(4, ") ");
                    } else {
                        sb = new StringBuilder(phone)
                                .insert(0, "(")
                                .insert(4, ") ")
                                .insert(9, " - ");
                    }

                    phone_number.setText(sb.toString());
                    phone_number.setSelection(phone_number.getText().length());

                    if (phone.length() == 10) {
                        btn_done.setBackgroundResource(R.drawable.bg_green_cornered);
                    } else {
                        btn_done.setBackgroundResource(R.drawable.btn_dark_gray);
                    }

                    phone_number.setTextColor(getResources().getColor(R.color.gray));
                    phone_number_ico.setImageResource(R.drawable.ic_signup_phone);
                }

                if (phone_number.getText().toString().length() != 16) {
                    btn_done.setBackgroundResource(R.drawable.btn_dark_gray);
                } else {
                    btn_done.setBackgroundResource(R.drawable.bg_green_cornered);
                }
            }
        });
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }

    private void submitForm () {
        if (phone_number.getText().toString().length() != 16) {
            phone_number.setTextColor(getResources().getColor(R.color.orange));
            phone_number_ico.setImageResource(R.drawable.ic_signup_phone_error);
            return;
        } else {
            phone_number.setTextColor(getResources().getColor(R.color.gray));
            phone_number_ico.setImageResource(R.drawable.ic_signup_phone);
        }

        User user = User.findById(User.class,(long)1);
        user.phone = phone_number.getText().toString().replace("(", "").replace(")", "").replace(" ", "-");

        user.save();
        postUserData(true);
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText("Enter Phone Number");

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransitionGoLeft();
            }
        });

        ImageView actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);
        actionbar_right_btn.setImageResource(R.drawable.ic_ab_help);
        actionbar_right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFAQ();
            }
        });
    }

    public void postUserData(boolean login){
        final ProgressDialog dialog = ProgressDialog.show(this, null, "Registering...", true);

        String uri = login ? getResources().getString(R.string.server_api_url) + Config.API.USER.FBLOGIN : getResources().getString(R.string.server_api_url) + Config.API.USER.FBSIGNUP ;
        Log.i(TAG,"uri: "+uri);
        Map<String, Object> params = new HashMap<String, Object>();
        User user = User.findById(User.class, (long) 1);
        JSONObject data = new JSONObject();
        try {
            data.put(Config.FACEBOOK.SIGNUP.firstname, user.firstname);
            data.put(Config.FACEBOOK.SIGNUP.lastname, user.lastname);
            data.put(Config.FACEBOOK.SIGNUP.email, user.email);
            data.put(Config.FACEBOOK.SIGNUP.phone, user.phone);
            data.put(Config.FACEBOOK.SIGNUP.fb_id, user.fbid);
            data.put(Config.FACEBOOK.SIGNUP.fb_token, user.fbtoken);
            data.put(Config.FACEBOOK.SIGNUP.fb_profile_pic, user.fbprofilepic);
            data.put(Config.FACEBOOK.SIGNUP.fb_age_range, user.fbagerange);
            data.put(Config.FACEBOOK.SIGNUP.fb_gender, user.fbgender);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String dataJson = data.toString();

        Log.i(TAG,"dataJson: "+dataJson);

        params.put("data", dataJson);
        aq.ajax(uri, params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {

                dialog.dismiss();

                // CASE 200 IF OK
                if (status.getCode() == 200) {
                    Log.i(TAG, "json: " + json.toString());
                    String firstname = "";
                    String lastname = "";
                    String email = "";
                    String phone = "";
                    String couponcode = "";
                    String apitoken = "";
                    String card_brand = "";
                    String card_last4 = "";
                    try {
                        try {
                            JSONObject card = json.getJSONObject("card");
                            card_brand = card.getString("brand");
                            card_last4 = card.getString("last4");
                        } catch (JSONException ignore) {
                            //e1.printStackTrace();
                        }
                        firstname = json.getString("firstname");
                        lastname = json.getString("lastname");
                        email = json.getString("email");
                        phone = json.getString("phone");
                        couponcode = json.getString("coupon_code");
                        apitoken = json.getString("api_token");
                    } catch (JSONException e) {
                        //e.printStackTrace();
                    }

                    Log.i(TAG, "apitoken: " + apitoken);

                    User user = User.findById(User.class, (long) 1);
                    user.firstname = firstname;
                    user.lastname = lastname;
                    user.email = email;
                    user.phone = phone;
                    user.couponcode = couponcode;
                    user.apitoken = apitoken;
                    user.cardbrand = card_brand;
                    user.cardlast4 = card_last4;
                    user.save();

                    Mixpanel.track(EnterPhoneNumberActivity.this, "Completed Registration");

                    if ( Config.AppNavigateMap.from != null && Config.AppNavigateMap.from.equals(Config.from.SettingActivity) ) {
                        Config.AppNavigateMap.from = null;
                        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                        startActivity(intent);
                        overridePendingTransitionGoLeft();
                        finish();
                    }else{
                        Orders current_order = Orders.findById(Orders.class, Bentonow.pending_order_id);
                        if ( current_order.coords_lat == null || current_order.coords_long == null ) {
                            startActivity(new Intent(getApplicationContext(), DeliveryLocationActivity.class));
                            overridePendingTransitionGoLeft();
                            finish();
                        }else{
                            if( user.cardlast4 == null || user.cardlast4.isEmpty() ) {
                                startActivity(new Intent(getApplicationContext(), EnterCreditCardActivity.class));
                                overridePendingTransitionGoLeft();
                                finish();
                            }else {
                                startActivity(new Intent(getApplicationContext(), CompleteOrderActivity.class));
                                overridePendingTransitionGoLeft();
                                finish();
                            }
                        }
                    }

                } else if (status.getCode() == 409) {
                    try {
                        JSONObject error_message = null;
                        error_message = new JSONObject(status.getError());
                        //Toast.makeText(getApplicationContext(),error_message.getString("error"),Toast.LENGTH_LONG).show();
                        overlay_message.setText(error_message.getString("error"));
                        overlay.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        //e.printStackTrace();
                        Toast.makeText(getApplicationContext(),status.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }else if(status.getCode() == 404 ){
                    try {
                        JSONObject error_message = null;
                        error_message = new JSONObject(status.getError());
                        //Toast.makeText(getApplicationContext(),error_message.getString("error"),Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    postUserData(false);
                }else {
                    /*try {
                        JSONObject error_message = new JSONObject(status.getError());
                        Toast.makeText(getApplicationContext(),error_message.getString("error"),Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/
                    Toast.makeText(getApplicationContext(),status.getMessage(),Toast.LENGTH_LONG).show();
                }


            }
        });
    }

}