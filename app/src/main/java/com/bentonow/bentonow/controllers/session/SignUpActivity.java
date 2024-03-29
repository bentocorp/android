package com.bentonow.bentonow.controllers.session;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.dialog.ProgressDialog;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.dao.UserDao;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.ui.CustomEditText;
import com.bentonow.bentonow.web.request.UserRequest;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;

import cz.msebera.android.httpclient.Header;

public class SignUpActivity extends BaseFragmentActivity implements View.OnClickListener, FacebookCallback<LoginResult>, GraphRequest.GraphJSONObjectCallback {

    static final String TAG = "SignUpActivity";

    private View container_alert;
    private TextView txt_message;

    private CustomEditText txt_name;
    private CustomEditText txt_email;
    private CustomEditText txt_phone;
    private CustomEditText txt_password;

    private ImageView img_user;
    private ImageView img_email;
    private ImageView img_phone;
    private ImageView img_password;

    private Button btn_signup;

    private ConstantUtils.optOpenScreen optOpenScreen;

    private ConfirmationDialog mDialog;
    private ProgressDialog mProgressDialog;

    private CallbackManager callbackManager;

    private UserDao userDao = new UserDao();
    private User registerUser;

    private boolean beganRegistration = false;
    private InputFilter mInputFilter;
    private String error = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);

        try {
            optOpenScreen = (ConstantUtils.optOpenScreen) getIntent().getExtras().getSerializable(ConstantUtils.TAG_OPEN_SCREEN);
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }

        if (optOpenScreen == null)
            optOpenScreen = ConstantUtils.optOpenScreen.NORMAL;

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, this);

        container_alert = findViewById(R.id.container_alert);
        txt_message = (TextView) findViewById(R.id.txt_message);


        txt_email = (CustomEditText) findViewById(R.id.txt_email);
        txt_phone = (CustomEditText) findViewById(R.id.txt_phone);
        txt_password = (CustomEditText) findViewById(R.id.txt_password);

        img_user = (ImageView) findViewById(R.id.img_user);
        img_email = (ImageView) findViewById(R.id.img_email);
        img_phone = (ImageView) findViewById(R.id.img_phone);
        img_password = (ImageView) findViewById(R.id.img_password);

        btn_signup = (Button) findViewById(R.id.btn_signup);

        if (getIntent().getStringExtra("email") != null) {
            txt_email.setText(getIntent().getStringExtra("email"));
        }

        MixpanelUtils.track("Began Registration");

        initActionbar();
        setupTextFields();
    }

    @Override
    protected void onResume() {
        if (userDao.getCurrentUser() != null)
            finish();
        else {
            GoogleAnalyticsUtil.sendScreenView("Sign Up");
        }
        super.onResume();
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

        getEditName().addTextChangedListener(watcher);
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
                    img_phone.setImageResource(R.drawable.vector_phone_gray);
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
        return AndroidUtil.isValidField(getEditName().getText().toString()) && !error.contains("name");
    }

    boolean validEmail() {
        return AndroidUtil.isEmailValid(txt_email.getText().toString()) && !error.contains("email");
    }

    boolean validPhone() {
        String sPhone = BentoNowUtils.getNumberFromPhone(txt_phone.getText().toString());
        return sPhone.length() == 10 && !error.contains("phone");
    }

    boolean validPassword() {
        return AndroidUtil.isValidField(txt_password.getText().toString()) && txt_password.getText().length() >= 6 && !error.contains("password");
    }

    void onSignUpSuccess(String responseString) {
        dismissDialog();

        try {
            DebugUtils.logDebug(TAG, "onSignUpSuccess: " + responseString);

            if (registerUser != null) {
                User mUser = new Gson().fromJson(responseString, User.class);
                registerUser.api_token = mUser.api_token;
            } else {
                registerUser = new Gson().fromJson(responseString, User.class);
            }

            userDao.insertUser(registerUser);

            MixpanelUtils.track("Logged In");

            MixpanelUtils.signUpUser(registerUser);
            MixpanelUtils.track("Completed Registration");

            switch (optOpenScreen) {
                case NORMAL:
                    Intent mIntentSettings = new Intent(SignUpActivity.this, SettingsActivity.class);
                    mIntentSettings.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mIntentSettings);
                    break;
                case COMPLETE_ORDER:
                    if (BentoNowUtils.isValidCompleteOrder(SignUpActivity.this))
                        BentoNowUtils.openCompleteOrderActivity(SignUpActivity.this, MenuDao.getCurrentMenu());
                    break;
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
        actionbar_title.setText(IosCopyDao.get("sign-up-title"));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.vector_navigation_left_green);
        actionbar_left_btn.setOnClickListener(this);
    }

    void updateUI() {
        boolean add_local_error = error.isEmpty();

        if (!validName()) {
            getEditName().setTextColor(getResources().getColor(R.color.orange));
            img_user.setImageResource(R.drawable.vector_person_orange);
            if (add_local_error)
                error = "The name field is required.";
        } else {
            getEditName().setTextColor(getResources().getColor(R.color.gray));
            img_user.setImageResource(R.drawable.vector_person_gray);
        }

        // EMAIL
        if (!validEmail()) {
            txt_email.setTextColor(getResources().getColor(R.color.orange));
            img_email.setImageResource(R.drawable.vector_email_orange);
            if (add_local_error && txt_email.getText().length() == 0)
                error += (error.length() > 0 ? "\n" : "") + "The email field is required.";
            else if (add_local_error)
                error += (error.length() > 0 ? "\n" : "") + "The email must be a valid email address.";
        } else {
            txt_email.setTextColor(getResources().getColor(R.color.gray));
            img_email.setImageResource(R.drawable.vector_email_gray);
        }

        // PHONE
        if (!validPhone()) {
            txt_phone.setTextColor(getResources().getColor(R.color.orange));
            img_phone.setImageResource(R.drawable.vector_phone_orange);
            if (add_local_error && txt_phone.getText().toString().isEmpty())
                error += (error.length() > 0 ? "\n" : "") + "The phone field is required.";
            else if (add_local_error)
                error += (error.length() > 0 ? "\n" : "") + "The phone must be a valid phone number.";
        } else {
            txt_phone.setTextColor(getResources().getColor(R.color.gray));
            img_phone.setImageResource(R.drawable.vector_phone_gray);
        }

        // PASSWORD
        if (!validPassword()) {
            txt_password.setTextColor(getResources().getColor(R.color.orange));
            img_password.setImageResource(R.drawable.vector_key_orange);
            if (add_local_error && txt_password.getText().length() == 0)
                error += (error.length() > 0 ? "\n" : "") + "The password field is required.";
            else if (add_local_error)
                error += (error.length() > 0 ? "\n" : "") + "The password must be at least 6 characters.";
        } else {
            txt_password.setTextColor(getResources().getColor(R.color.gray));
            img_password.setImageResource(R.drawable.vector_key_gray);
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
        registerUser.firstname = getEditName().getText().toString();
        registerUser.email = txt_email.getText().toString();
        registerUser.phone = BentoNowUtils.getPhoneFromNumber(txt_phone.getText().toString());
        registerUser.password = txt_password.getText().toString();

        mProgressDialog = new ProgressDialog(SignUpActivity.this, IosCopyDao.get("sign-in-sign-up-link"), true);
        mProgressDialog.show();

        UserRequest.register(registerUser, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logDebug(TAG, "onRegisterPressedOnFailure statusCode:" + statusCode + " responseString: " + responseString);

                dismissDialog();

                switch (statusCode) {
                    case 0:
                        showInformationDialog("Error", getString(R.string.error_no_internet_connection), null);
                        break;
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
                        showInformationDialog("Error", "This email is already registered.", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent mIntentLogIn = new Intent(SignUpActivity.this, SignInActivity.class);
                                mIntentLogIn.putExtra("email", txt_email.getText());
                                mIntentLogIn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(mIntentLogIn);
                                finish();
                            }
                        });
                        break;
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                DebugUtils.logDebug(TAG, "onRegisterPressedOnSuccess statusCode:" + statusCode + " responseString: " + responseString);
                onSignUpSuccess(responseString);
            }
        });
    }

    public void onSignInPressed(View view) {
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

    private void showInformationDialog(String sTitle, String sMessage, View.OnClickListener mOnClickListener) {
        if (mDialog == null || !mDialog.isShowing()) {
            mDialog = new ConfirmationDialog(SignUpActivity.this, sTitle, sMessage);
            mDialog.addAcceptButton("OK", mOnClickListener);
            mDialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
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
        DebugUtils.logError(TAG, "onCancel()");
        AndroidUtil.hideKeyboard(txt_message);
    }

    @Override
    public void onError(FacebookException e) {
        if (e.toString().contains("net::")) {
            showInformationDialog("Error", getString(R.string.error_no_internet), null);
        } else
            showInformationDialog("Error", getString(R.string.error_no_internet), null);

        DebugUtils.logError("FacebookException", e);
    }

    @Override
    public void onCompleted(JSONObject jsonObject, final GraphResponse graphResponse) {
        final JSONObject user = graphResponse.getJSONObject();


        try {
            DebugUtils.logDebug(TAG, "graphResponse:" + graphResponse.toString());

            User loginUser = new User();
            loginUser.email = user.getString("email");
            loginUser.fb_token = AccessToken.getCurrentAccessToken().getToken();

            mProgressDialog = new ProgressDialog(SignUpActivity.this, IosCopyDao.get("sign-in-sign-up-link"), true);
            mProgressDialog.show();

            UserRequest.login(loginUser, new TextHttpResponseHandler() {
                @SuppressWarnings("deprecation")
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    dismissDialog();
                    DebugUtils.logDebug(TAG, "fbLoginFailed: " + responseString + " statusCode: " + statusCode);

                    switch (statusCode) {
                        case 0:
                            showInformationDialog("Error", getString(R.string.error_no_internet_connection), null);
                            break;
                        case 404:
                            //TODO Test New Facebook User
                            DebugUtils.logError(TAG, "onCompleted(): facebook email not found");
                            BentoNowUtils.openEnterPhoneNumberActivity(SignUpActivity.this, graphResponse, optOpenScreen);
                            break;
                        case 403:
                            DebugUtils.logError(TAG, "onCompleted(): facebook bad fb_token");
                            break;
                    }
                }

                @SuppressWarnings("deprecation")
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    onSignUpSuccess(responseString);
                    //BentoNowUtils.openEnterPhoneNumberActivity(SignUpActivity.this, graphResponse);
                }
            });
        } catch (Exception e) {
            DebugUtils.logError(TAG, e);
            dismissDialog();
            showInformationDialog("Error", "Sorry! Please share your email address through Facebook to continue.", null);
        }
    }

    @Override
    protected void onDestroy() {
        MixpanelUtils.track("Viewed Register Screen");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        AndroidUtil.hideKeyboard(txt_message);
    }

    private void dismissDialog() {
        if (mDialog != null)
            mDialog.dismiss();

        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    private CustomEditText getEditName() {
        if (txt_name == null)
            txt_name = (CustomEditText) findViewById(R.id.txt_name);
        return txt_name;
    }


}
