package com.bentonow.bentonow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.model.Item;
import com.bentonow.bentonow.model.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
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

    CallbackManager callbackManager;

    private ImageView btn_facebook;
    private Activity _this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        aq = new AQuery(this);
        _this = this;
        FacebookSdk.sdkInitialize(getApplicationContext());
        initActionbar();
        initElements();
        initListeners();
        initCallBackManager();
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText("Sign In");

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransitionGoLeft();
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
        btn_facebook = (ImageView)findViewById(R.id.btn_facebook);
    }

    private void initListeners() {
        // BTN REGISTER
        btn_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });

        password.setOnEditorActionListener(new EditText.OnEditorActionListener() {
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

        // BTN GON TO SIGN UP ACTIVITY
        btn_go_to_sign_up_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
                //finish();
                overridePendingTransitionGoRight();
            }
        });

        // FB
        btn_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                LoginManager.getInstance().logInWithReadPermissions(_this, Arrays.asList("email"));
            }
        });
    }

    private void submitForm() {
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

        JSONObject data = new JSONObject();
        try {
            data.put("email", email_address.getText().toString());
            data.put("password", password.getText().toString());
            postUserData(data.toString(), false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void postUserData( String dataJson, boolean facebookMethod ){
        Log.i(TAG,"postUserData( String dataJson )");
        String uri;
        if(facebookMethod) {
            uri = Config.API.URL + Config.API.USER.FBLOGIN;
        }else{
            uri = Config.API.URL + Config.API.USER.LOGIN;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("data", dataJson);

        Log.i(TAG, "dataJson: " + dataJson);

        aq.ajax(uri, params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {

                if (json != null) Log.i(TAG, "json: " + json.toString());
                else Log.i(TAG, "json IS NULL");

                Log.i(TAG, "status.getError(): " + status.getError());
                Log.i(TAG, "status.getMessage(): " + status.getMessage());
                Log.i(TAG, "status.getCode(): " + status.getCode());

                // CASE 200 IF OK
                if (status.getCode() == Config.API.DEFAULT_SUCCESS_200) {
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

                    long isUser = User.count(User.class, null, null);
                    if (isUser == 0) {
                        User user = new User(firstname, lastname, email, phone, couponcode, apitoken, card_brand, card_last4);
                        user.save();
                    } else {
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

                    if ( Config.AppNavigateMap.from != null && Config.AppNavigateMap.from.equals(Config.from.SettingActivity) ) {
                        Config.AppNavigateMap.from = null;
                        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                        startActivity(intent);
                        overridePendingTransitionGoLeft();
                    }else{
                        Intent intent = new Intent(getApplicationContext(), CompleteOrderActivity.class);
                        startActivity(intent);
                        overridePendingTransitionGoLeft();
                    }
                }

                // CASE 403 BAD PASSWORD
                if (status.getCode() == Config.API.USER_LOGIN_403) {
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
                if (status.getCode() == Config.API.USER_LOGIN_404) {
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

    //// FACEBOOK
    private void initCallBackManager() {
        callbackManager = CallbackManager.Factory.create();


        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                // Application code
                                JSONObject responseJSONObject = response.getJSONObject();
                                Log.v("LoginActivity", "graphObject: " + responseJSONObject);

                                long users = User.count(User.class, null, null);
                                User user;
                                if (users == 0) user = new User();
                                else user = User.findById(User.class, (long) 1);

                                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                                JSONObject data = new JSONObject();
                                try {
                                    data.put("email", responseJSONObject.getString("email"));
                                    data.put("fb_token", accessToken.getToken());
                                    postUserData(data.toString(), true);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                );
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                btn_facebook.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(FacebookException e) {
                btn_facebook.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onResume();

        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransitionGoLeft();
    }
}
