package com.bentonow.bentonow.controllers.session;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
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
import com.bentonow.bentonow.Utils.SocialNetworksUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.dialog.ProgressDialog;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.dao.UserDao;
import com.bentonow.bentonow.model.User;
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

import org.json.JSONObject;

import java.util.Collections;

import cz.msebera.android.httpclient.Header;


public class SignInActivity extends BaseFragmentActivity implements View.OnClickListener, FacebookCallback<LoginResult>, GraphRequest.GraphJSONObjectCallback {

    public static final String TAG = "SignInActivity";
    String error = "";
    //region Variables
    private View container_alert;
    private TextView txt_message;
    private EditText txt_email;
    private EditText txt_password;
    private ImageView img_email;
    private ImageView img_password;
    private Button btn_signin;
    private UserDao userDao = new UserDao();
    private User mCurrentUser;
    private ConstantUtils.optOpenScreen optOpenScreen;
    private CallbackManager callbackManager;
    private ConfirmationDialog mDialog;
    private ProgressDialog mProgressDialog;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in);

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

        txt_email = (EditText) findViewById(R.id.txt_email);
        txt_password = (EditText) findViewById(R.id.txt_password);

        img_email = (ImageView) findViewById(R.id.img_email);
        img_password = (ImageView) findViewById(R.id.img_password);

        btn_signin = (Button) findViewById(R.id.btn_signin);

        /*if (BuildConfig.DEBUG) {
            txt_email.setText("kokushos@gmail.com");
            txt_password.setText("colossus");
        }*/

        initActionbar();
        setupTextFields();
    }

    @Override
    protected void onResume() {
        if (userDao.getCurrentUser() != null)
            finish();
        else {
            GoogleAnalyticsUtil.sendScreenView("Sign In");

            if (getIntent().getStringExtra("email") != null) {
                txt_email.setText(getIntent().getStringExtra("email"));
                getIntent().removeExtra("email");
            }

        }
        super.onResume();
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
        return AndroidUtil.isEmailValid(txt_email.getText().toString()) && !error.contains("email");
    }

    boolean validPassword() {
        return AndroidUtil.isValidField(txt_password.getText().toString()) && txt_password.getText().length() >= 6 && !error.contains("password");
    }

    void onSignInSuccess(String responseString) {
        dismissDialog();

        try {
            DebugUtils.logDebug(TAG, "onSignInSuccess: " + responseString);
            mCurrentUser = new Gson().fromJson(responseString, User.class);
            userDao.insertUser(mCurrentUser);

            DebugUtils.logDebug(TAG, "After Gson: " + mCurrentUser.api_token);

            MixpanelUtils.logInUser(mCurrentUser);

            MixpanelUtils.track("Logged In");

            switch (optOpenScreen) {
                case NORMAL:
                    onBackPressed();
                    break;
                case COMPLETE_ORDER:
                    if (BentoNowUtils.isValidCompleteOrder(SignInActivity.this))
                        BentoNowUtils.openCompleteOrderActivity(SignInActivity.this, MenuDao.getCurrentMenu());
                    break;
            }
            finish();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //endregion

    //region UI

    void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(IosCopyDao.get("sign-in-title"));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.vector_navigation_left_green);
        actionbar_left_btn.setOnClickListener(this);
    }

    void updateUI() {
        DebugUtils.logDebug(TAG, "updateUI");

        boolean add_local_error = error == null || error.length() == 0;

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

    public void onSignInPressed(View view) {
        updateUI();

        if (!valid()) return;

        final User loginUser = new User();
        loginUser.email = txt_email.getText().toString();
        loginUser.password = txt_password.getText().toString();

        mProgressDialog = new ProgressDialog(SignInActivity.this, IosCopyDao.get("sign-up-sign-in-link"), true);
        mProgressDialog.show();

        UserRequest.login(loginUser, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logDebug(TAG, "onSignInPressedOnFailure statusCode:" + statusCode + " responseString: " + responseString);

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
                DebugUtils.logDebug(TAG, "onSignInPressedOnSuccess statusCode:" + statusCode + " responseString: " + responseString);
                onSignInSuccess(responseString);
            }
        });
    }

    public void onSignUpPressed(View view) {
        Intent mIntentSignUp = new Intent(this, SignUpActivity.class);
        mIntentSignUp.putExtra(ConstantUtils.TAG_OPEN_SCREEN, optOpenScreen);
        startActivity(mIntentSignUp);
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
        MixpanelUtils.track("Tapped On Forgot Password");
        SocialNetworksUtil.openWebUrl(this, IosCopyDao.get("forgot_password_url"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
            case R.id.btn_ok:
                Intent intent = new Intent(this, SignUpActivity.class);
                intent.putExtra(ConstantUtils.TAG_OPEN_SCREEN, optOpenScreen);
                intent.putExtra("email", txt_email.getText());
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        MixpanelUtils.track("Viewed Sign In Screen");
        super.onDestroy();
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

        try {
            final JSONObject user = graphResponse.getJSONObject();
            DebugUtils.logDebug(TAG, "graphResponse:" + graphResponse.toString());

            User loginUser = new User();
            loginUser.email = user.getString("email");
            loginUser.fb_token = AccessToken.getCurrentAccessToken().getToken();

            mProgressDialog = new ProgressDialog(SignInActivity.this, IosCopyDao.get("sign-up-sign-in-link"), true);
            mProgressDialog.show();

            UserRequest.login(loginUser, new TextHttpResponseHandler() {
                @SuppressWarnings("deprecation")
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    dismissDialog();

                    DebugUtils.logError(TAG, "fbLoginFailed: " + responseString + " statusCode: " + statusCode);

                    switch (statusCode) {
                        case 0:
                            showInformationDialog("Error", getString(R.string.error_no_internet_connection), null);
                            break;
                        case 404:
                            DebugUtils.logError(TAG, "onCompleted(): facebook email not found");
                            BentoNowUtils.openEnterPhoneNumberActivity(SignInActivity.this, graphResponse, optOpenScreen);
                            break;
                        default:
                            try {
                                String message = new JSONObject(responseString).getString("error");
                                showInformationDialog("Error", message, null);
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
            DebugUtils.logError(TAG, e.toString());
            dismissDialog();
            showInformationDialog("Error", "Sorry! Please share your email address through Facebook to continue.", null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        AndroidUtil.hideKeyboard(txt_message);
    }


    private void showInformationDialog(String sTitle, String sMessage, View.OnClickListener mOnClickListener) {
        if (mDialog == null || !mDialog.isShowing()) {
            mDialog = new ConfirmationDialog(SignInActivity.this, sTitle, sMessage);
            mDialog.addAcceptButton("OK", mOnClickListener);
            mDialog.show();
        }
    }

    private void dismissDialog() {
        if (mDialog != null)
            mDialog.dismiss();

        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

}
