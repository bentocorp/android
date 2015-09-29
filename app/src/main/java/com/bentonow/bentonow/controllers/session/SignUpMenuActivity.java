package com.bentonow.bentonow.controllers.session;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.Email;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.controllers.order.CompleteOrderMenuActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.ui.CustomDialog;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.Gson;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;

public class SignUpMenuActivity extends BaseFragmentActivity implements View.OnClickListener, FacebookCallback<LoginResult>, GraphRequest.GraphJSONObjectCallback {

    static final String TAG = "SignUpActivity";

    //region Variables
    View container_alert;
    TextView txt_message;

    EditText txt_name;
    EditText txt_email;
    EditText txt_phone;
    EditText txt_password;

    ImageView img_user;
    ImageView img_email;
    ImageView img_phone;
    ImageView img_password;

    Button btn_signup;

    CallbackManager callbackManager;
    CustomDialog dialog;

    String error = "";

    boolean beganRegistration = false;
    private User registerUser;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, this);

        container_alert = findViewById(R.id.container_alert);
        txt_message = (TextView) findViewById(R.id.txt_message);

        txt_name = (EditText) findViewById(R.id.txt_name);
        txt_email = (EditText) findViewById(R.id.txt_email);
        txt_phone = (EditText) findViewById(R.id.txt_phone);
        txt_password = (EditText) findViewById(R.id.txt_password);

        img_user = (ImageView) findViewById(R.id.img_user);
        img_email = (ImageView) findViewById(R.id.img_email);
        img_phone = (ImageView) findViewById(R.id.img_phone);
        img_password = (ImageView) findViewById(R.id.img_password);

        btn_signup = (Button) findViewById(R.id.btn_signup);

        if (getIntent().getStringExtra("email") != null) {
            txt_email.setText(getIntent().getStringExtra("email"));
        }

        initActionbar();
        setupTextFields();
    }

    void setupTextFields() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!beganRegistration) {
                    MixpanelUtils.track("Began Registration");
                    beganRegistration = true;
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                validate();
            }
        };

        txt_name.addTextChangedListener(watcher);
        txt_email.addTextChangedListener(watcher);
        txt_password.addTextChangedListener(watcher);

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

                if (oldLength != phone.length()) {
                    oldLength = phone.length();

                    txt_phone.setText(BentoNowUtils.getPhoneFromNumber(txt_phone.getText().toString()));
                    txt_phone.setSelection(txt_phone.getText().length());

                    txt_phone.setTextColor(getResources().getColor(R.color.gray));
                    img_phone.setImageResource(R.drawable.ic_signup_phone);
                }

                validate();
            }
        });

        txt_password.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    // if shift key is down, then we want to insert the '\n' char in the TextView;
                    // otherwise, the default action is to send the message.
                    if (!event.isShiftPressed()) {
                        onSignUpPressed(null);
                        return true;
                    }
                    return false;
                }

                onSignUpPressed(null);
                return true;
            }
        });
    }

    //region SignUp

    private void validate() {
        btn_signup.setBackgroundResource(valid() ? R.drawable.bg_green_cornered : R.drawable.btn_dark_gray);
    }

    boolean valid() {
        return validName() && validEmail() && validPhone() && validPassword();
    }

    boolean validName() {
        return !txt_name.getText().toString().equals("") && !error.contains("name");
    }

    boolean validEmail() {
        return Email.isValid(txt_email.getText().toString()) && !error.contains("email");
    }

    boolean validPhone() {
        String sPhone = BentoNowUtils.getNumberFromPhone(txt_phone.getText().toString());
        return sPhone.length() == 10 && !error.contains("phone");
    }

    boolean validPassword() {
        return txt_password.getText().length() >= 6 && !error.contains("password");
    }

    void onSignUpSuccess(String responseString) {
        dialog.dismiss();

        try {
            Log.i(TAG, "onSignUpSuccess: " + responseString);

            if (registerUser != null) {
                User mUser = new Gson().fromJson(responseString, User.class);
                registerUser.api_token = mUser.api_token;
                User.current = registerUser;
            } else {
                User.current = new Gson().fromJson(responseString, User.class);
            }

            MixpanelUtils.signUpUser();

            BentoNowUtils.saveSettings(ConstantUtils.optSaveSettings.ALL);


            if (getIntent().getBooleanExtra("settings", false)) {
                onBackPressed();
            } else if (Order.location == null) {
                Intent intent = new Intent(SignUpMenuActivity.this, DeliveryLocationActivity.class);
                intent.putExtra(DeliveryLocationActivity.TAG_DELIVERY_ACTION, ConstantUtils.optDeliveryAction.COMPLETE_ORDER);
                startActivity(intent);
            } else {
                startActivity(new Intent(SignUpMenuActivity.this, CompleteOrderMenuActivity.class));
            }

            finish();
        } catch (Exception e) {
            DebugUtils.logError(TAG, "onSignUpSuccess: " + e.toString());
        }
    }

    //endregion

    //region UI

    void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(BackendText.get("sign-up-title"));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(this);
    }

    void updateUI() {
        boolean add_local_error = error.length() == 0;

        if (!validName()) {
            txt_name.setTextColor(getResources().getColor(R.color.orange));
            img_user.setImageResource(R.drawable.ic_signup_profile_error);
            if (add_local_error) error = "The name field is required.";
        } else {
            txt_name.setTextColor(getResources().getColor(R.color.gray));
            img_user.setImageResource(R.drawable.ic_signup_profile);
        }

        // EMAIL
        if (!validEmail()) {
            txt_email.setTextColor(getResources().getColor(R.color.orange));
            img_email.setImageResource(R.drawable.ic_signup_email_error);
            if (add_local_error && txt_email.getText().length() == 0)
                error += (error.length() > 0 ? "\n" : "") + "The name field is required.";
            else if (add_local_error)
                error += (error.length() > 0 ? "\n" : "") + "The email must be a valid email address.";
        } else {
            txt_email.setTextColor(getResources().getColor(R.color.gray));
            img_email.setImageResource(R.drawable.ic_signup_email);
        }

        // PHONE
        if (!validPhone()) {
            txt_phone.setTextColor(getResources().getColor(R.color.orange));
            img_phone.setImageResource(R.drawable.ic_signup_phone_error);
            if (add_local_error && txt_phone.getText().toString().isEmpty())
                error += (error.length() > 0 ? "\n" : "") + "The phone field is required.";
            else if (add_local_error)
                error += (error.length() > 0 ? "\n" : "") + "The phone must be a valid phone number.";
        } else {
            txt_phone.setTextColor(getResources().getColor(R.color.gray));
            img_phone.setImageResource(R.drawable.ic_signup_phone);
        }

        // PASSWORD
        if (!validPassword()) {
            txt_password.setTextColor(getResources().getColor(R.color.orange));
            img_password.setImageResource(R.drawable.ic_signup_key_error);
            if (add_local_error && txt_password.getText().length() == 0)
                error += (error.length() > 0 ? "\n" : "") + "The password field is required.";
            else if (add_local_error)
                error += (error.length() > 0 ? "\n" : "") + "The password must be at least 6 characters.";
        } else {
            txt_password.setTextColor(getResources().getColor(R.color.gray));
            img_password.setImageResource(R.drawable.ic_signup_key);
        }

        txt_message.setText(error);
        container_alert.setVisibility(error.length() > 0 ? View.VISIBLE : View.INVISIBLE);
        error = "";
    }

    //endregion

    //region OnClick

    public void onFacebookPressed(View view) {
        LoginManager.getInstance().logOut();
        LoginManager.getInstance().logInWithReadPermissions(this, Collections.singletonList("email"));
    }

    public void onSignUpPressed(View view) {
        updateUI();

        if (!valid()) {
            DebugUtils.logDebug(TAG, "onSignUpPressed: " + "invalid");
            return;
        }

        registerUser = new User();
        registerUser.firstname = txt_name.getText().toString();
        registerUser.email = txt_email.getText().toString();
        registerUser.phone = BentoNowUtils.getPhoneFromNumber(txt_phone.getText().toString());
        registerUser.password = txt_password.getText().toString();

        dialog = new CustomDialog(this, BackendText.get("sign-up-sign-in-link"), true);
        dialog.show();

        registerUser.register(new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i(TAG, "onRegisterPressedOnFailure statusCode:" + statusCode + " responseString: " + responseString);

                dialog.dismiss();

                switch (statusCode) {
                    case 400:
                        error = "";

                        try {
                            JSONArray messages = new JSONArray(responseString);
                            for (int i = 0; i < messages.length(); ++i) {
                                error += (error.length() > 0 ? "\n" : "") + messages.get(i);
                            }
                        } catch (Exception e) {
                            DebugUtils.logError(TAG, "onSignUpPressed(): " + e.getLocalizedMessage());
                        }

                        updateUI();
                        break;
                    case 409:
                        dialog = new CustomDialog(SignUpMenuActivity.this, "This email is already registered.", "OK", "");
                        dialog.setOnOkPressed(SignUpMenuActivity.this);
                        break;
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i(TAG, "onRegisterPressedOnSuccess statusCode:" + statusCode + " responseString: " + responseString);
                onSignUpSuccess(responseString);
            }
        });
    }

    public void onSignInPressed(View view) {
        Intent intent = new Intent(this, SignInMenuActivity.class);
        intent.putExtra("settings", getIntent().getBooleanExtra("settings", false));
        startActivity(intent);
        finish();
    }

    public void onPrivacyPolicyPressed(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("privacy", true);
        startActivity(intent);
    }

    public void onTermAndConditionsPressed(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("tos", true);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
            case R.id.btn_ok:
                Intent intent = new Intent(this, SignInMenuActivity.class);
                intent.putExtra("email", txt_email.getText());
                startActivity(intent);
                finish();
                break;
        }
    }

    //endregion

    //region Facebook

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), this);
        request.executeAsync();
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(FacebookException e) {
        new CustomDialog(this, "An error occur while trying to register with Facebook", null, "OK").show();
        e.printStackTrace();
    }

    @Override
    public void onCompleted(JSONObject jsonObject, final GraphResponse graphResponse) {
        final JSONObject user = graphResponse.getJSONObject();

        dialog = new CustomDialog(this, BackendText.get("sign-in-sign-up-link"), true);
        dialog.show();

        try {
            Log.i(TAG, "graphResponse:" + graphResponse.toString());

            User loginUser = new User();
            loginUser.email = user.getString("email");
            loginUser.fb_token = AccessToken.getCurrentAccessToken().getToken();

            loginUser.login(new TextHttpResponseHandler() {
                @SuppressWarnings("deprecation")
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    dialog.dismiss();

                    Log.i(TAG, "fbLoginFailed: " + responseString + " statusCode: " + statusCode);

                    Intent intent = new Intent(SignUpMenuActivity.this, EnterPhoneNumberMenuActivity.class);
                    intent.putExtra("user", graphResponse.toString());
                    intent.putExtra("settings", getIntent().getBooleanExtra("settings", false));
                    startActivity(intent);
                    finish();
                }

                @SuppressWarnings("deprecation")
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    onSignUpSuccess(responseString);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //endregion
}
