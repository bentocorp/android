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

import com.bentonow.bentonow.BuildConfig;
import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.Email;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SocialNetworksUtil;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.dialog.ProgressDialog;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.controllers.order.CompleteOrderActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.User;
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
import org.json.JSONObject;

import java.util.Collections;


public class SignInActivity extends BaseActivity implements View.OnClickListener, FacebookCallback<LoginResult>, GraphRequest.GraphJSONObjectCallback {

    static final String TAG = "SignInActivity";

    //region Variables
    View container_alert;
    TextView txt_message;

    EditText txt_email;
    EditText txt_password;

    ImageView img_email;
    ImageView img_password;

    Button btn_signin;

    CallbackManager callbackManager;
    private ConfirmationDialog mDialog;
    private ProgressDialog mProgressDialog;

    String error = "";
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in);

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, this);

        container_alert = findViewById(R.id.container_alert);
        txt_message = (TextView) findViewById(R.id.txt_message);

        txt_email = (EditText) findViewById(R.id.txt_email);
        txt_password = (EditText) findViewById(R.id.txt_password);

        img_email = (ImageView) findViewById(R.id.img_email);
        img_password = (ImageView) findViewById(R.id.img_password);

        btn_signin = (Button) findViewById(R.id.btn_signin);

        if (getIntent().getStringExtra("email") != null) {
            txt_email.setText(getIntent().getStringExtra("email"));
        }

        if (BuildConfig.DEBUG) {
            txt_email.setText("kokushos@gmail.com");
            txt_password.setText("colossus");
        }

        initActionbar();
        setupTextFields();
    }

    void setupTextFields() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                validate();
            }
        };

        txt_email.addTextChangedListener(watcher);
        txt_password.addTextChangedListener(watcher);

        txt_password.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    // if shift key is down, then we want to insert the '\n' char in the TextView;
                    // otherwise, the default action is to send the message.
                    if (!event.isShiftPressed()) {
                        onSignInPressed(null);
                        return true;
                    }
                    return false;
                }

                onSignInPressed(null);
                return true;
            }
        });
    }

    //region SignIn

    private void validate() {
        btn_signin.setBackgroundResource(valid() ? R.drawable.bg_green_cornered : R.drawable.btn_dark_gray);
    }

    boolean valid() {
        return validEmail() && validPassword();
    }

    boolean validEmail() {
        return Email.isValid(txt_email.getText().toString()) && !error.contains("email");
    }

    boolean validPassword() {
        return txt_password.getText().length() >= 6 && !error.contains("password");
    }

    void onSignInSuccess(String responseString) {
        dismissDialog();

        try {
            Log.i(TAG, "onSignInSuccess: " + responseString);
            User.current = new Gson().fromJson(responseString, User.class);
            Log.i(TAG, "After Gson: " + User.current.api_token);

            MixpanelUtils.logInUser();

            if (getIntent().getBooleanExtra("settings", false)) {
                onBackPressed();
            } else if (Order.location == null) {
                Intent intent = new Intent(SignInActivity.this, DeliveryLocationActivity.class);
                intent.putExtra(DeliveryLocationActivity.TAG_DELIVERY_ACTION, ConstantUtils.optDeliveryAction.COMPLETE_ORDER);
                startActivity(intent);
            } else {
                startActivity(new Intent(SignInActivity.this, CompleteOrderActivity.class));
            }

            BentoNowUtils.saveSettings(ConstantUtils.optSaveSettings.ALL);

            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //endregion

    //region UI

    void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(BackendText.get("sign-in-title"));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(this);
    }

    void updateUI() {
        Log.i(TAG, "updateUI");

        boolean add_local_error = error == null || error.length() == 0;

        // EMAIL
        if (!validEmail()) {
            txt_email.setTextColor(getResources().getColor(R.color.orange));
            img_email.setImageResource(R.drawable.ic_signup_email_error);
            if (add_local_error && txt_email.getText().length() == 0)
                error += (error.length() > 0 ? "\n" : "") + "The email field is required.";
            else if (add_local_error)
                error += (error.length() > 0 ? "\n" : "") + "The email must be a valid email address.";
        } else {
            txt_email.setTextColor(getResources().getColor(R.color.gray));
            img_email.setImageResource(R.drawable.ic_signup_email);
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

    public void onSignInPressed(View view) {
        updateUI();

        if (!valid()) return;

        final User loginUser = new User();
        loginUser.email = txt_email.getText().toString();
        loginUser.password = txt_password.getText().toString();

        mProgressDialog = new ProgressDialog(SignInActivity.this, BackendText.get("sign-up-sign-in-link"));
        mProgressDialog.show();

        loginUser.login(new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i(TAG, "onSignInPressedOnFailure statusCode:" + statusCode + " responseString: " + responseString);

                dismissDialog();

                try {
                    error = new JSONObject(responseString).getString("error");
                } catch (Exception e) {
                    error = "No Network";
                    DebugUtils.logError(TAG, "onCompleted(): " + e.getLocalizedMessage());
                }

                updateUI();
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i(TAG, "onSignInPressedOnSuccess statusCode:" + statusCode + " responseString: " + responseString);
                onSignInSuccess(responseString);
            }
        });
    }

    public void onSignUpPressed(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
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

    public void onForgotPassword(View view) {
        SocialNetworksUtil.openWebUrl(this, BackendText.get("forgot_password_url"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
            case R.id.btn_ok:
                Intent intent = new Intent(this, SignUpActivity.class);
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
        mDialog = new ConfirmationDialog(SignInActivity.this, "Error", "An error occur while trying to sign in with Facebook");
        mDialog.addAcceptButton("OK", null);
        mDialog.show();
        DebugUtils.logError("FacebookException", e);
    }

    @Override
    public void onCompleted(JSONObject jsonObject, final GraphResponse graphResponse) {
        mProgressDialog = new ProgressDialog(SignInActivity.this, BackendText.get("sign-up-sign-in-link"));
        mProgressDialog.show();

        try {
            final JSONObject user = graphResponse.getJSONObject();
            Log.i(TAG, "graphResponse:" + graphResponse.toString());

            User loginUser = new User();
            loginUser.email = user.getString("email");
            loginUser.fb_token = AccessToken.getCurrentAccessToken().getToken();

            loginUser.login(new TextHttpResponseHandler() {
                @SuppressWarnings("deprecation")
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    dismissDialog();

                    DebugUtils.logError(TAG, "fbLoginFailed: " + responseString + " statusCode: " + statusCode);

                    switch (statusCode) {
                        case 404:
                            DebugUtils.logError(TAG, "onCompleted(): facebook email not found");
                            BentoNowUtils.openEnterPhoneNumberActivity(SignInActivity.this, graphResponse);
                            break;
                        default:
                            try {
                                String message = new JSONObject(responseString).getString("error");
                                mDialog = new ConfirmationDialog(SignInActivity.this, "Error", message);
                                mDialog.addAcceptButton("OK", null);
                                mDialog.show();
                            } catch (Exception e) {
                                DebugUtils.logError(TAG, "onCompleted(): " + e.getLocalizedMessage());
                            }
                            break;
                    }

                }

                @SuppressWarnings("deprecation")
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    onSignInSuccess(responseString);
                }
            });
        } catch (Exception e) {
            dismissDialog();
            DebugUtils.logError(TAG, e.toString());
        }
    }

    private void dismissDialog() {
        if (mDialog != null)
            mDialog.dismiss();

        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

}
