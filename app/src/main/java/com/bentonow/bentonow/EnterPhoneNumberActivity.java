package com.bentonow.bentonow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
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
    }

    private void addListeners(){
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phone_number.getText().toString().equals("")) {
                    phone_number.setTextColor(getResources().getColor(R.color.orange));
                    phone_number_ico.setImageResource(R.drawable.ic_signup_phone_error);
                    return;
                } else {
                    phone_number.setTextColor(getResources().getColor(R.color.gray));
                    phone_number_ico.setImageResource(R.drawable.ic_signup_phone);
                }
                User user = User.findById(User.class,(long)1);
                user.phone = phone_number.getText().toString();
                user.save();
                postUserData();
            }
        });
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

    public void postUserData(){
        String uri = Config.API.URL + Config.API.USER.FBSIGNUP;
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


                // CASE 200 IF OK
                if (status.getCode() == Config.API.DEFAULT_SUCCESS_200) {
                    Log.i(TAG, "json: " + json.toString());
                    String apitoken = "";
                    try {
                        apitoken = json.getString("api_token");
                    } catch (JSONException e) {
                        //e.printStackTrace();
                    }

                    Log.i(TAG, "apitoken: " + apitoken);

                    User user = User.findById(User.class, (long) 1);
                    user.apitoken = apitoken;
                    user.save();

                    go igo = new go();
                    igo.fromLogin();
                } if (status.getCode() == Config.API.DEFAULT_ERROR_409) {
                    try {
                        JSONObject error_message = null;
                        error_message = new JSONObject(status.getError());
                        Toast.makeText(getApplicationContext(),error_message.getString("error"),Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    try {
                        JSONObject error_message = new JSONObject(status.getError());
                        Toast.makeText(getApplicationContext(),error_message.getString("error"),Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            }
        });
    }

}
