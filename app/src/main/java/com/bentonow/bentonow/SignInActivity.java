package com.bentonow.bentonow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SignInActivity extends BaseActivity {

    private static final String TAG = "SignInActivity";
    private TextView btn_go_to_sign_up_activity;
    private AQuery aq;
    private EditText email_address, password;
    private ImageView signup_email_ico, signup_key_ico;
    private TextView btn_sign_in;
    private LinearLayout alert_container;
    private TextView alert_message_textview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        aq = new AQuery(this);
        initActionbar();
        initElements();
        initListeners();
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText("Sign In");

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
                finish();
                overrideTransitionGoLeft();
            }
        });
    }

    private void initElements() {
        btn_go_to_sign_up_activity = (TextView)findViewById(R.id.btn_go_to_sign_up_activity);
        alert_container = (LinearLayout)findViewById(R.id.alert_empty_input);
        alert_message_textview = (TextView)findViewById(R.id.alert_message_textview);

        //FORM INPUTS
        email_address = (EditText)findViewById(R.id.email_address);
        password = (EditText)findViewById(R.id.password);
        //FORM ICOS
        signup_email_ico = (ImageView)findViewById(R.id.signup_email_ico);
        signup_key_ico = (ImageView)findViewById(R.id.signup_key_ico);
        btn_sign_in = (TextView) findViewById(R.id.btn_sign_in);

    }

    private void initListeners() {
        // BTN REGISTER
        btn_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertReset();
                // EMAIL
                if (email_address.getText().toString().equals("")) {
                    email_address.setTextColor(getResources().getColor(R.color.orange));
                    signup_email_ico.setImageResource(R.drawable.ic_signup_email_error);
                    alert("Please enter a valid email address.");
                    return;
                } else {
                    email_address.setTextColor(getResources().getColor(R.color.gray));
                    signup_email_ico.setImageResource(R.drawable.ic_signup_email);
                }

                // PHONE
                if (password.getText().toString().equals("")) {
                    password.setTextColor(getResources().getColor(R.color.orange));
                    signup_key_ico.setImageResource(R.drawable.ic_signup_key_error);
                    alert("Please enter a password.");
                    return;
                } else {
                    password.setTextColor(getResources().getColor(R.color.gray));
                    signup_key_ico.setImageResource(R.drawable.ic_signup_key);
                }
                postUserData();
            }
        });

        // BTN GON TO SIGN UP ACTIVITY
        btn_go_to_sign_up_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
                finish();
                overrideTransitionGoLeft();
            }
        });
    }

    private void overrideTransitionGoLeft() {
        overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
    }

    public void postUserData(){
        String uri = Config.API.URL + Config.API.USER_LOGIN;
        Map<String, Object> params = new HashMap<String, Object>();
        String dataJson = "{\"email\": \""+email_address.getText().toString()+"\", \"password\": \""+password.getText().toString()+"\"}";
        params.put("data", dataJson);
        aq.ajax(uri, params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                // CASE 200 IF OK
                if ( status.getCode() == Config.API.DEFAULT_SUCCESS_200) {
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

                    Log.i(TAG,"apitoken: "+apitoken);

                    long isUser = User.count(User.class, null, null);
                    if ( isUser == 0 ) {
                        User user = new User(firstname,lastname,email,phone,couponcode,apitoken, card_brand, card_last4);
                        user.save();
                    }else{
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
                    }
                    Intent intent = new Intent(getApplicationContext(),CompleteOrderActivity.class);
                    startActivity(intent);
                    finish();
                    overrideTransitionGoLeft();
                }

                // CASE 403 BAD PASSWORD
                if ( status.getCode() == Config.API.USER_LOGIN_403 ) {
                    try {
                        JSONObject error_message = new JSONObject(status.getError());
                        password.setTextColor(getResources().getColor(R.color.orange));
                        signup_key_ico.setImageResource(R.drawable.ic_signup_key_error);
                        alert(error_message.getString("error"));
                    } catch (JSONException e) {
                        //e.printStackTrace();
                        alert("An error has occurred.");
                    }
                }

                // CASE 404 EMAIL NOT FOUND
                if ( status.getCode() == Config.API.USER_LOGIN_404 ) {
                    try {
                        JSONObject error_message = new JSONObject(status.getError());
                        email_address.setTextColor(getResources().getColor(R.color.orange));
                        signup_email_ico.setImageResource(R.drawable.ic_signup_email_error);
                        alert(error_message.getString("error"));
                    } catch (JSONException e) {
                        //e.printStackTrace();
                        alert("An error has occurred.");
                    }
                }
            }
        });
    }

    private void alert(String s) {
        alert_message_textview.setText(s);
        alert_container.setVisibility(View.VISIBLE);
    }

    private void alertReset() {
        alert_message_textview.setText("");
        alert_container.setVisibility(View.INVISIBLE);
    }

}
