package com.bentonow.bentonow.controllers.session;


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
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.dao.UserDao;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.ui.BackendButton;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class EnterPhoneNumberActivity extends BaseFragmentActivity implements View.OnClickListener {

    static final String TAG = "EnterPhoneNumber";

    public static final String TAG_FB_USER = "Facebook_User";

    private BackendButton btn_done;
    private EditText txt_phone;
    private ImageView img_phone;
    private TextView textPrivacyPolicy;
    private TextView textConfirmationTerms;

    private UserDao userDao = new UserDao();
    private User mCurrentUser;

    private ConstantUtils.optOpenScreen optOpenScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_phone_number);
        mCurrentUser = new User();

        try {
            optOpenScreen = (ConstantUtils.optOpenScreen) getIntent().getExtras().getSerializable(ConstantUtils.TAG_OPEN_SCREEN);
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }

        if (optOpenScreen == null)
            optOpenScreen = ConstantUtils.optOpenScreen.NORMAL;

        try {
            JSONObject fbUser = new JSONObject(getIntent().getStringExtra(TAG_FB_USER));

            mCurrentUser.firstname = fbUser.getString("first_name");
            mCurrentUser.lastname = fbUser.getString("last_name");
            mCurrentUser.email = fbUser.getString("email");
            mCurrentUser.fb_token = AccessToken.getCurrentAccessToken().getToken();
            mCurrentUser.fb_id = fbUser.getString("id");
            mCurrentUser.fb_profile_pic = "https://graph.facebook.com/" + mCurrentUser.fb_id + "/picture?width=400";
            mCurrentUser.fb_gender = fbUser.getString("gender");
            mCurrentUser.fb_age_range = "";


        } catch (JSONException ignore) {
            DebugUtils.logError(TAG, ignore);
        }

        btn_done = (BackendButton) findViewById(R.id.btn_done);
        txt_phone = (EditText) findViewById(R.id.txt_phone);
        img_phone = (ImageView) findViewById(R.id.img_phone);

        getTextPrivacyPolicy().setOnClickListener(this);
        getTextConfirmationTerms().setOnClickListener(this);

        initActionbar();
        updateUI();
        setTextWatcher();
    }

    @Override
    protected void onResume() {
        GoogleAnalyticsUtil.sendScreenView("Phone Number");
        super.onResume();
    }

    void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(IosCopyDao.get("phone-confirmation-title"));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(this);

        ImageView actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);
        actionbar_right_btn.setImageResource(R.drawable.ic_ab_help);
        actionbar_right_btn.setOnClickListener(this);
    }

    void setTextWatcher() {
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

    boolean isValid() {
        return txt_phone.getText().length() == 16;
    }


    public void onDonePressed(View view) {
        if (!isValid())
            return;

        mCurrentUser.phone = BentoNowUtils.getNumberFromPhone(txt_phone.getText().toString());

        RequestParams params = new RequestParams();
        params.put("data", new Gson().toJson(mCurrentUser));

        BentoRestClient.post("/user/fbsignup", params, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "fbLoginFailed: " + responseString + " statusCode: " + statusCode);

                try {
                    JSONObject json = new JSONObject(responseString);

                    ConfirmationDialog mDialog = new ConfirmationDialog(EnterPhoneNumberActivity.this, "Error", json.has("error") ? json.getString("error") : json.getString("Error"));
                    mDialog.addAcceptButton("OK", null);
                    mDialog.show();
                    return;
                } catch (Exception ignore) {
                    ConfirmationDialog mDialog = new ConfirmationDialog(EnterPhoneNumberActivity.this, "Error", "An error occurred, please contact us");
                    mDialog.addAcceptButton("OK", null);
                    mDialog.show();
                    DebugUtils.logError(TAG, responseString);
                    Crashlytics.log(1, TAG, responseString);
                }

            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                try {
                    User mNewUser = new Gson().fromJson(responseString, User.class);
                    mCurrentUser.api_token = mNewUser.api_token;

                    userDao.insertUser(mCurrentUser);

                    MixpanelUtils.track("Logged In");

                    switch (optOpenScreen) {
                        case COMPLETE_ORDER:
                            if (BentoNowUtils.isValidCompleteOrder(EnterPhoneNumberActivity.this))
                                BentoNowUtils.openCompleteOrderActivity(EnterPhoneNumberActivity.this, MenuDao.getCurrentMenu());
                            break;
                    }

                    MixpanelUtils.track("Completed Registration");

                } catch (Exception ex) {
                    DebugUtils.logError(TAG, ex);
                    WidgetsUtils.createShortToast(R.string.error_sign_up_user);
                }

                finish();

            }
        });
    }

    void updateUI() {
        btn_done.setBackgroundResource(isValid() ? R.drawable.bg_green_cornered : R.drawable.btn_dark_gray);

        if (!isValid()) {
            txt_phone.setTextColor(getResources().getColor(R.color.orange));
            img_phone.setImageResource(R.drawable.ic_signup_phone_error);
        } else {
            txt_phone.setTextColor(getResources().getColor(R.color.gray));
            img_phone.setImageResource(R.drawable.ic_signup_phone);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
            case R.id.actionbar_right_btn:
                BentoNowUtils.openFaqActivity(EnterPhoneNumberActivity.this);
                break;
            case R.id.text_privacy_policy:
                BentoNowUtils.openPolicyActivity(EnterPhoneNumberActivity.this);
                break;
            case R.id.text_confirmation_terms:
                BentoNowUtils.openTermAndConditionsActivity(EnterPhoneNumberActivity.this);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        MixpanelUtils.track("Viewed Phone Number Screen");
        super.onDestroy();
    }

    private TextView getTextPrivacyPolicy() {
        if (textPrivacyPolicy == null)
            textPrivacyPolicy = (TextView) findViewById(R.id.text_privacy_policy);
        return textPrivacyPolicy;
    }

    private TextView getTextConfirmationTerms() {
        if (textConfirmationTerms == null)
            textConfirmationTerms = (TextView) findViewById(R.id.text_confirmation_terms);
        return textConfirmationTerms;
    }
}