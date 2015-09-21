package com.bentonow.bentonow.controllers.session;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.Mixpanel;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.controllers.order.CompleteOrderActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.ui.BackendButton;
import com.bentonow.bentonow.ui.CustomDialog;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class EnterPhoneNumberActivity extends BaseActivity implements View.OnClickListener {

    static final String TAG = "EnterPhoneNumber";

    User user;
    BackendButton btn_done;
    EditText txt_phone;
    ImageView img_phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_phone_number);
        user = new User();

        try {
            JSONObject fbuser = new JSONObject(getIntent().getStringExtra("user"));

            user.firstname = fbuser.getString("first_name");
            user.lastname = fbuser.getString("last_name");
            user.email = fbuser.getString("email");

            user.fb_id = fbuser.getString("id");
            user.fb_gender = fbuser.getString("gender");
            user.fb_profile_pic = "https://graph.facebook.com/" + user.fb_id + "/picture?width=400";
            user.fb_token = AccessToken.getCurrentAccessToken().getToken();
            user.fb_age_range = "";

        } catch (JSONException ignore) {
            onBackPressed();
        }

        btn_done = (BackendButton) findViewById(R.id.btn_done);
        txt_phone = (EditText) findViewById(R.id.txt_phone);
        img_phone = (ImageView) findViewById(R.id.img_phone);

        initActionbar();
        updateUI();
        setTextWatcher();
    }

    void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(BackendText.get("phone-confirmation-title"));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(this);

        ImageView actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);
        actionbar_right_btn.setImageResource(R.drawable.ic_ab_help);
        actionbar_right_btn.setOnClickListener(this);
    }

    void setTextWatcher () {
        txt_phone.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    // if shift key is down, then we want to insert the '\n' char in the TextView;
                    // otherwise, the default action is to send the message.
                    if (!event.isShiftPressed()) {
                        onDonePressed(null);
                        return true;
                    }
                    return false;
                }

                onDonePressed(null);
                return true;
            }
        });

        txt_phone.addTextChangedListener(new TextWatcher() {
            int oldLength;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String phone = txt_phone.getText().toString().replaceAll("[^0-9]", "");
                StringBuilder sb;

                if (oldLength != phone.length()) {
                    oldLength = phone.length();

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

                    txt_phone.setText(sb.toString());
                    txt_phone.setSelection(txt_phone.getText().length());
                }

                updateUI();
            }
        });
    }

    boolean isValid () {
        return txt_phone.getText().length() == 16;
    }

    //region onClick

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                startActivity(new Intent(this, SignUpActivity.class));
                finish();
                break;
            case R.id.actionbar_right_btn:
                Intent intent = new Intent(this, HelpActivity.class);
                intent.putExtra("faq", true);
                startActivity(intent);
                break;
        }
    }

    public void onPrivacyPolicyPressed (View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("privacy", true);
        startActivity(intent);
    }

    public void onTermAndConditionsPressed (View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("tos", true);
        startActivity(intent);
    }

    public void onDonePressed (View view) {
        if (!isValid()) return;

        user.phone = txt_phone.getText().toString().replace("(", "").replace(")", "").replace(" ", "");

        RequestParams params = new RequestParams();
        params.put("data", new Gson().toJson(user));
        BentoRestClient.post("/user/fbsignup", params, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                try {
                    JSONObject json = new JSONObject(responseString);

                    CustomDialog dialog = new CustomDialog(EnterPhoneNumberActivity.this,json.has("error") ? json.getString("error") : json.getString("Error"),"OK",null);
                    dialog.show();
                    return;
                } catch (Exception ignore) {
                    Crashlytics.log(1, TAG, responseString);
                }

                CustomDialog dialog = new CustomDialog(
                        EnterPhoneNumberActivity.this,
                        "An error occurred, please contact us",
                        "OK",
                        null
                );
                dialog.show();
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                User.current = new Gson().fromJson(responseString, User.class);
                Mixpanel.track(EnterPhoneNumberActivity.this, "Completed Registration");

                if (getIntent().getBooleanExtra("settings", false)) {
                    onBackPressed();
                } else if (Order.location == null) {
                    Intent intent = new Intent(EnterPhoneNumberActivity.this, DeliveryLocationActivity.class);
                    intent.putExtra(DeliveryLocationActivity.TAG_DELIVERY_ACTION, ConstantUtils.optDeliveryAction.COMPLETE_ORDER);
                    startActivity(intent);
                } else {
                    startActivity(new Intent(EnterPhoneNumberActivity.this, CompleteOrderActivity.class));
                }

                BentoNowUtils.saveSettings(ConstantUtils.optSaveSettings.ALL);

                finish();
            }
        });
    }

    //endregion

    //region UI

    void updateUI () {
        btn_done.setBackgroundResource(isValid() ? R.drawable.bg_green_cornered : R.drawable.btn_dark_gray);

        if (!isValid()) {
            txt_phone.setTextColor(getResources().getColor(R.color.orange));
            img_phone.setImageResource(R.drawable.ic_signup_phone_error);
        }else{
            txt_phone.setTextColor(getResources().getColor(R.color.gray));
            img_phone.setImageResource(R.drawable.ic_signup_phone);
        }
    }

    //endregion
}