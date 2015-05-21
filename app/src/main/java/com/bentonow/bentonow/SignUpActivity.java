package com.bentonow.bentonow;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
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

public class SignUpActivity extends BaseActivity {

    private LinearLayout alert_container;
    private TextView alert_message_textview;
    private TextView btn_register;
    private EditText user_name, email_address, phone_number, password;
    private ImageView signup_user_ico, signup_email_ico, signup_phone_ico, signup_key_ico;
    private AQuery aq;
    private String TAG = "SignUpActivity";
    private TextView btn_go_to_sign_in_activity;
    private ImageView btn_fb_signup;
    private Activity _this;
    private CallbackManager callbackManager;
    private TextView btn_privacy_policy;
    private TextView btn_terms_conditions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        aq = new AQuery(this);
        _this = this;
        FacebookSdk.sdkInitialize(getApplicationContext());
        initElements();
        initListeners();
        initActionbar();
        initCallBackManager();
    }

    private void initElements() {
        btn_go_to_sign_in_activity = (TextView)findViewById(R.id.btn_go_to_sign_in_activity);
        alert_container = (LinearLayout)findViewById(R.id.alert_empty_input);
        alert_message_textview = (TextView)findViewById(R.id.alert_message_textview);
        //FORM INPUTS
        user_name = (EditText)findViewById(R.id.user_name);
        email_address = (EditText)findViewById(R.id.email_address);
        phone_number = (EditText)findViewById(R.id.phone_number);
        password = (EditText)findViewById(R.id.password);
        //FORM ICOS
        signup_user_ico = (ImageView)findViewById(R.id.signup_user_ico);
        signup_email_ico = (ImageView)findViewById(R.id.signup_email_ico);
        signup_phone_ico = (ImageView)findViewById(R.id.signup_phone_ico);
        signup_key_ico = (ImageView)findViewById(R.id.signup_key_ico);
        btn_register = (TextView) findViewById(R.id.btn_register);
        btn_fb_signup = (ImageView)findViewById(R.id.btn_facebook_signup);

        btn_privacy_policy = (TextView)findViewById(R.id.btn_privacy_policy);
        btn_terms_conditions = (TextView)findViewById(R.id.btn_terms_conditions);
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText("Register");

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
            }
        });
    }

    private void initListeners() {

        btn_privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),PrivacyPolicyActivity.class);
                startActivity(intent);
                overridePendingTransitionGoRight();
            }
        });

        btn_terms_conditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),TermAndConditionsActivity.class);
                startActivity(intent);
                overridePendingTransitionGoRight();
            }
        });
        // BTN REGISTER
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertReset();
                // USER NAME
                if (user_name.getText().toString().equals("")) {
                    user_name.setTextColor(getResources().getColor(R.color.orange));
                    signup_user_ico.setImageResource(R.drawable.ic_signup_profile_error);
                    alert("Please enter a valid name.");
                    return;
                }else{
                    user_name.setTextColor(getResources().getColor(R.color.gray));
                    signup_user_ico.setImageResource(R.drawable.ic_signup_profile);
                }

                // EMAIL
                if (email_address.getText().toString().equals("")) {
                    email_address.setTextColor(getResources().getColor(R.color.orange));
                    signup_email_ico.setImageResource(R.drawable.ic_signup_email_error);
                    alert("Please enter a valid email address.");
                    return;
                }else{
                    email_address.setTextColor(getResources().getColor(R.color.gray));
                    signup_email_ico.setImageResource(R.drawable.ic_signup_email);
                }

                // PHONE
                if (phone_number.getText().toString().equals("")) {
                    phone_number.setTextColor(getResources().getColor(R.color.orange));
                    signup_phone_ico.setImageResource(R.drawable.ic_signup_phone_error);
                    alert("Please enter a valid phone number.");
                    return;
                }else{
                    phone_number.setTextColor(getResources().getColor(R.color.gray));
                    signup_phone_ico.setImageResource(R.drawable.ic_signup_phone);
                }

                // PHONE
                if (password.getText().toString().equals("")) {
                    password.setTextColor(getResources().getColor(R.color.orange));
                    signup_key_ico.setImageResource(R.drawable.ic_signup_key_error);
                    alert("Please enter a password.");
                    return;
                }else{
                    password.setTextColor(getResources().getColor(R.color.gray));
                    signup_key_ico.setImageResource(R.drawable.ic_signup_key);
                }
                postUserData();
            }
        });

        btn_go_to_sign_in_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
            }
        });

        btn_fb_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                LoginManager.getInstance().logInWithReadPermissions(_this, Arrays.asList("email"));
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

    public void postUserData(){
        String uri = Config.API.URL + Config.API.USER_SIGNUP;
        Map<String, Object> params = new HashMap<String, Object>();
        //String dataJson = "{\"name\":\""+user_name.getText().toString()+"\", \"email\": \""+email_address.getText().toString()+"\", \"phone\": \""+phone_number.getText().toString()+"\", \"password\": \""+password.getText().toString()+"\"}";
        JSONObject data = new JSONObject();
        try {
            data.put(Config.USER.NAME,user_name.getText().toString());
            data.put(Config.USER.EMAIL,email_address.getText().toString());
            data.put(Config.USER.PHONE,phone_number.getText().toString());
            data.put(Config.USER.PASSWORD, password.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(TAG,"data: "+data.toString());
        params.put("data", data.toString());
        aq.ajax(uri, params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                //Toast.makeText(getApplicationContext(), status.getMessage(), Toast.LENGTH_LONG).show();
                if (status.getCode() == Config.API.USER_SIGNUP_200) {
                    Log.i(TAG, "json: " + json.toString());
                    long users = User.count(User.class, null, null);
                    User user;
                    if (users == 0) {
                        user = new User();
                    } else {
                        user = User.findById(User.class, (long) 1);
                    }
                    String apitokenTmp = null;
                    String apitoken = "";
                    try {
                        apitokenTmp = json.getString(Config.USER.APITOKEN);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (apitokenTmp != null) apitoken = apitokenTmp;
                    user.firstname = user_name.getText().toString();
                    user.phone = phone_number.getText().toString();
                    user.email = email_address.getText().toString();
                    user.apitoken = apitoken;
                    user.save();

                    // GO TO ORDER DETAIL
                    Intent intent = new Intent(getApplicationContext(), CompleteOrderActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransitionGoLeft();
                }
                if (status.getCode() == Config.API.USER_SIGNUP_400) {
                    try {
                        JSONObject error_message = new JSONObject(status.getError());
                        alert(error_message.getString("error"));
                    } catch (JSONException e) {
                        //e.printStackTrace();
                        alert("An error have ocurred.");
                    }
                }
                if (status.getCode() == Config.API.USER_SIGNUP_409) {
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

                                try {
                                    AccessToken accessToken = AccessToken.getCurrentAccessToken();
                                    Log.i(TAG,"accessToken: "+accessToken.toString());
                                    user.firstname = responseJSONObject.getString("first_name");
                                    user.lastname = responseJSONObject.getString("last_name");
                                    user.email = responseJSONObject.getString("email");
                                    user.fbid = responseJSONObject.getString("id");
                                    user.fbtoken = accessToken.getToken();
                                    user.fbprofilepic = "https://graph.facebook.com/" + responseJSONObject.getString("id") + "/picture";
                                    user.fbagerange = "";//responseJSONObject.getString("fb_age_range");
                                    user.fbgender = responseJSONObject.getString("gender");
                                    user.save();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Intent intent = new Intent(getApplicationContext(), EnterPhoneNumberActivity.class);
                                startActivity(intent);
                                finish();
                                overridePendingTransitionGoRight();
                            }
                        }
                );
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.i(TAG,"onCancel()");
            }

            @Override
            public void onError(FacebookException e) {
                Log.i(TAG,"onError(FacebookException)");
                Log.i(TAG, "error: " + e.getMessage());
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

}
