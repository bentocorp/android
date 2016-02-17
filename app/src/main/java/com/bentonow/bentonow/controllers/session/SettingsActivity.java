package com.bentonow.bentonow.controllers.session;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.SocialNetworksUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.dialog.EditPhoneDialog;
import com.bentonow.bentonow.controllers.dialog.LogOutDialog;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.listener.ListenerDialog;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.ui.FontAwesomeButton;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;


public class SettingsActivity extends BaseFragmentActivity implements View.OnClickListener {
    static final String TAG = "SettingsActivity";

    String action = "";
    private String message = null;

    private LinearLayout layoutContainerPhone;
    private LinearLayout containerSettingsOrders;
    private LinearLayout containerSettingsSignIn;
    private LinearLayout containerSettingsFaq;
    private LinearLayout containerSettingsEmail;
    private LinearLayout containerSettingsCall;
    private LinearLayout containerSettingsCreditCard;
    private RelativeLayout containerUser;
    private TextView txtLogout;
    private FontAwesomeButton btnFacebook;
    private FontAwesomeButton btnTwitter;
    private FontAwesomeButton btnSms;
    private FontAwesomeButton btnEmail;

    private LogOutDialog mLogOutDialog;

    private User mCurrentUser;

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.wtf(TAG, "UTF-8 should always be supported", e);
            throw new RuntimeException("URLEncoder.encode() failed for " + s);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initActionbar();

        getBtnFacebook().setup(this);
        getBtnTwitter().setup(this);
        getBtnSms().setup(this);
        getBtnEmail().setup(this);

        getContainerSettingsSignIn().setOnClickListener(this);
        getContainerSettingsFaq().setOnClickListener(this);
        getContainerSettingsEmail().setOnClickListener(this);
        getContainerSettingsCall().setOnClickListener(this);
        getContainerSettingsCreditCard().setOnClickListener(this);
        getContainerSettingsOrders().setOnClickListener(this);
        getLayoutContainerPhone().setOnClickListener(this);
        getTxtLogout().setOnClickListener(this);
        getBtnFacebook().setOnClickListener(this);
        getBtnTwitter().setOnClickListener(this);
        getBtnSms().setOnClickListener(this);
        getBtnEmail().setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = userDao.getCurrentUser();

        updateUI();

        if (mCurrentUser != null && (mCurrentUser.coupon_code == null || mCurrentUser.coupon_code.isEmpty()))
            getUserInfo();

        GoogleAnalyticsUtil.sendScreenView("Settings");
    }

    void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText("Settings");

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ab_x_close);
        actionbar_left_btn.setOnClickListener(this);

    }

    void updateUI() {
        getContainerUser().setVisibility(mCurrentUser == null ? View.GONE : View.VISIBLE);
        getContainerSettingsSignIn().setVisibility(mCurrentUser != null ? View.GONE : View.VISIBLE);
        getContainerSettingsCreditCard().setVisibility(View.GONE);
        getContainerSettingsOrders().setVisibility(mCurrentUser != null ? View.VISIBLE : View.GONE);

        if (mCurrentUser != null) {
            ((TextView) findViewById(R.id.txt_name)).setText(mCurrentUser.lastname != null ? mCurrentUser.firstname + " " + mCurrentUser.lastname : mCurrentUser.firstname);

            ((TextView) findViewById(R.id.txt_phone)).setText(BentoNowUtils.getPhoneFromNumber(mCurrentUser.phone));
            ((TextView) findViewById(R.id.txt_email)).setText(mCurrentUser.email);

            ((TextView) findViewById(R.id.txt_coupon)).setText(mCurrentUser.coupon_code);
            findViewById(R.id.container_coupon).setVisibility(View.VISIBLE);

            DebugUtils.logDebug(TAG, "Api Token: " + mCurrentUser.api_token);
        } else {
            findViewById(R.id.container_coupon).setVisibility(View.GONE);
        }


        if (mCurrentUser != null && mCurrentUser.coupon_code != null && !mCurrentUser.coupon_code.isEmpty()) {
            message = IosCopyDao.get("share-precomposed-message").replace("%@", mCurrentUser.coupon_code).replace("http://apple.co/1FPEbWY", ConstantUtils.URL_INSTALL_ANDROID);
        }
    }

    private void updatePhoneNumber(final String sPhoneNumber) {
        RequestParams params = new RequestParams();
        params.put("api_token", mCurrentUser.api_token);
        params.put("data", "{\"new_phone\":\"" + sPhoneNumber + "\"}");
        BentoRestClient.post("/user/phone", params, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "getUserInfo:  " + responseString);
                WidgetsUtils.createShortToast(R.string.error_phone_request);
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                mCurrentUser.phone = sPhoneNumber;
                userDao.updateUser(mCurrentUser);

                MixpanelUtils.setProfileProperties(mCurrentUser);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }
        });
    }

    public void onLogoutPressed() {
        DebugUtils.logDebug(TAG, "onLogoutPressed");
        action = "logout";

        if (mLogOutDialog == null || !mLogOutDialog.isShowing()) {
            mLogOutDialog = new LogOutDialog(SettingsActivity.this, "Confirmation", "Are you sure you want to log out ?");
            mLogOutDialog.addAcceptButton("YES", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCurrentUser = null;
                    userDao.removeUser();
                    MixpanelUtils.clearPreferences();
                    updateUI();
                    MixpanelUtils.track("Logged Out");
                    GoogleAnalyticsUtil.restartEvent();
                }
            });
            mLogOutDialog.addCancelButton("NO", null);
            mLogOutDialog.show();
        }
    }

    private void onSignInPressed() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtra("settings", true);
        startActivity(intent);
    }

    private void onFaqPressed() {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("faq", true);
        startActivity(intent);
    }

    public void onEmailSupportPressed() {
        String[] TO = {"help@bentonow.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void onPhoneSupportPressed() {
        action = "phone";
        ConfirmationDialog mDialog = new ConfirmationDialog(SettingsActivity.this, "Call Us", "415.300.1332");
        mDialog.addAcceptButton("Call", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SocialNetworksUtil.phoneCall(SettingsActivity.this, "4153001332");
            }
        });
        mDialog.addCancelButton("Cancel", null);
        mDialog.show();
    }

    private void onCreditCardPressed() {
        //TODO New Credit Card Button Text
        BentoNowUtils.openCreditCardActivity(SettingsActivity.this, ConstantUtils.optOpenScreen.NORMAL);
    }

    public void onFacebookPressed() {
        if (!SocialNetworksUtil.postStatusFacebook(SettingsActivity.this, message, ConstantUtils.URL_INSTALL_ANDROID)) {

            Toast.makeText(getApplicationContext(), "There is no facebook client installed.", Toast.LENGTH_SHORT).show();

            onEmailPressed();
        }
    }

    public void onTwitterPressed(View v) {
        String tweetUrl = String.format("https://twitter.com/intent/tweet?text=%s", urlEncode(message));
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl));

        // Narrow down to official Twitter app, if available:
        List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.twitter")) {
                intent.setPackage(info.activityInfo.packageName);
            }
        }

        startActivity(intent);
    }

    public void onMessagePressed() {
        SocialNetworksUtil.sendSms(SettingsActivity.this, message);
    }

    public void onEmailPressed() {
        SocialNetworksUtil.sendEmail(SettingsActivity.this, message);
    }

    private void getUserInfo() {
        RequestParams params = new RequestParams();
        params.put("api_token", mCurrentUser.api_token);
        BentoRestClient.get("/user/info", params, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "getUserInfo:  " + responseString);

                DebugUtils.logError(TAG, "getUserInfo failed: " + responseString + " StatusCode: " + statusCode);
                String sError;

                try {
                    sError = new JSONObject(responseString).getString("error");
                } catch (Exception e) {
                    sError = getString(R.string.error_no_internet_connection);
                    DebugUtils.logError(TAG, "requestPromoCode(): " + e.getLocalizedMessage());
                }

                switch (statusCode) {
                    case 0:// No internet Connection
                        sError = getString(R.string.error_no_internet_connection);
                        break;
                    case 401:// Invalid Api Token
                        WidgetsUtils.createShortToast("You session is expired, please LogIn again");

                        if (!userDao.removeUser())
                            userDao.clearAllData();

                        onSignInPressed();
                        break;
                    default:
                        Crashlytics.log(Log.ERROR, "SendOrderError", "Code " + statusCode + " : Response " + responseString + " : Parsing " + sError);
                        break;
                }

            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                DebugUtils.logDebug(TAG, "getUserInfo: " + responseString);
                try {
                    User mUserInfo = new Gson().fromJson(responseString, User.class);

                    if (!mUserInfo.api_token.isEmpty())
                        mCurrentUser.api_token = mUserInfo.api_token;

                    if (!mUserInfo.coupon_code.isEmpty())
                        mCurrentUser.coupon_code = mUserInfo.coupon_code;

                    if (!mUserInfo.card.brand.isEmpty())
                        mCurrentUser.card = mUserInfo.card;

                    userDao.updateUser(mCurrentUser);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUI();
                        }
                    });
                } catch (Exception ex) {
                    DebugUtils.logError(TAG, "getUserInfo(): " + ex.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_SETTINGS_CLICK, hasFocus);
        DebugUtils.logDebug(TAG, "onWindowFocusChanged: " + hasFocus);
    }

    @Override
    public void onClick(View v) {
        if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.ENABLE_SETTINGS_CLICK)) {
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_SETTINGS_CLICK, false);
            switch (v.getId()) {
                case R.id.actionbar_left_btn:
                    onBackPressed();
                    break;
                case R.id.layout_container_phone:
                    EditPhoneDialog editDialog = new EditPhoneDialog();
                    editDialog.setmListenerDialog(new ListenerDialog() {
                        @Override
                        public void btnOkClick(String sPhoneNumber) {
                            DebugUtils.logDebug(TAG, "btnOkClick: " + sPhoneNumber);
                            updatePhoneNumber(sPhoneNumber);
                        }

                        @Override
                        public void btnOnCancel() {
                        }
                    });
                    editDialog.show(getFragmentManager(), EditPhoneDialog.TAG);
                    break;
                case R.id.container_sign_in:
                    onSignInPressed();
                    break;
                case R.id.container_setting_faq:
                    onFaqPressed();
                    break;
                case R.id.container_setting_email:
                    onEmailSupportPressed();
                    break;
                case R.id.container_setting_call:
                    onPhoneSupportPressed();
                    break;
                case R.id.container_setting_credit_card:
                    onCreditCardPressed();
                    break;
                case R.id.container_settings_orders:
                    BentoNowUtils.openOrderHistoryActivity(SettingsActivity.this);
                    break;
                case R.id.txt_logout:
                    onLogoutPressed();
                    break;
                case R.id.btn_facebook:
                    onFacebookPressed();
                    break;
                case R.id.btn_email:
                    onEmailPressed();
                    break;
                case R.id.btn_sms:
                    onMessagePressed();
                    break;
                default:
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_SETTINGS_CLICK, true);
                    DebugUtils.logError(TAG, "No found: " + v.getId());
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
    }

    @Override
    protected void onDestroy() {
        MixpanelUtils.track(mCurrentUser == null ? "Viewed Signed Out Settings Screen" : "Viewed Signed In Settings Screen");
        super.onDestroy();
    }

    private LinearLayout getLayoutContainerPhone() {
        if (layoutContainerPhone == null)
            layoutContainerPhone = (LinearLayout) findViewById(R.id.layout_container_phone);

        return layoutContainerPhone;
    }

    private LinearLayout getContainerSettingsOrders() {
        if (containerSettingsOrders == null)
            containerSettingsOrders = (LinearLayout) findViewById(R.id.container_settings_orders);

        return containerSettingsOrders;
    }

    private LinearLayout getContainerSettingsSignIn() {
        if (containerSettingsSignIn == null)
            containerSettingsSignIn = (LinearLayout) findViewById(R.id.container_sign_in);

        return containerSettingsSignIn;
    }

    private LinearLayout getContainerSettingsFaq() {
        if (containerSettingsFaq == null)
            containerSettingsFaq = (LinearLayout) findViewById(R.id.container_setting_faq);

        return containerSettingsFaq;
    }

    private LinearLayout getContainerSettingsEmail() {
        if (containerSettingsEmail == null)
            containerSettingsEmail = (LinearLayout) findViewById(R.id.container_setting_email);

        return containerSettingsEmail;
    }

    private LinearLayout getContainerSettingsCall() {
        if (containerSettingsCall == null)
            containerSettingsCall = (LinearLayout) findViewById(R.id.container_setting_call);

        return containerSettingsCall;
    }

    private LinearLayout getContainerSettingsCreditCard() {
        if (containerSettingsCreditCard == null)
            containerSettingsCreditCard = (LinearLayout) findViewById(R.id.container_setting_credit_card);

        return containerSettingsCreditCard;
    }

    private RelativeLayout getContainerUser() {
        if (containerUser == null)
            containerUser = (RelativeLayout) findViewById(R.id.container_user);

        return containerUser;
    }

    private TextView getTxtLogout() {
        if (txtLogout == null)
            txtLogout = (TextView) findViewById(R.id.txt_logout);

        return txtLogout;
    }

    private FontAwesomeButton getBtnFacebook() {
        if (btnFacebook == null)
            btnFacebook = (FontAwesomeButton) findViewById(R.id.btn_facebook);
        return btnFacebook;
    }

    private FontAwesomeButton getBtnEmail() {
        if (btnEmail == null)
            btnEmail = (FontAwesomeButton) findViewById(R.id.btn_email);
        return btnEmail;
    }

    private FontAwesomeButton getBtnTwitter() {
        if (btnTwitter == null)
            btnTwitter = (FontAwesomeButton) findViewById(R.id.btn_twitter);
        return btnTwitter;
    }

    private FontAwesomeButton getBtnSms() {
        if (btnSms == null)
            btnSms = (FontAwesomeButton) findViewById(R.id.btn_sms);
        return btnSms;
    }

}