package com.bentonow.bentonow.controllers.session;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SocialNetworksUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.dialog.EditPhoneDialog;
import com.bentonow.bentonow.controllers.dialog.LogOutDialog;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.dao.UserDao;
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

    private UserDao userDao = new UserDao();
    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initActionbar();

        ((FontAwesomeButton) findViewById(R.id.btn_facebook)).setup(this);
        ((FontAwesomeButton) findViewById(R.id.btn_twitter)).setup(this);
        ((FontAwesomeButton) findViewById(R.id.btn_sms)).setup(this);
        ((FontAwesomeButton) findViewById(R.id.btn_email)).setup(this);

        ((FontAwesomeButton) findViewById(R.id.btn_facebook)).setup(this);
        ((FontAwesomeButton) findViewById(R.id.btn_twitter)).setup(this);
        ((FontAwesomeButton) findViewById(R.id.btn_sms)).setup(this);
        ((FontAwesomeButton) findViewById(R.id.btn_email)).setup(this);

        ((FontAwesomeButton) findViewById(R.id.ico_user)).setup(this);
        ((FontAwesomeButton) findViewById(R.id.ico_faq)).setup(this);
        ((FontAwesomeButton) findViewById(R.id.ico_mail_support)).setup(this);
        ((FontAwesomeButton) findViewById(R.id.ico_phone_support)).setup(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = userDao.getCurrentUser();


        DebugUtils.logDebug(TAG, "Api Token: " + mCurrentUser.api_token);

        updateUI();

        if (mCurrentUser != null && (mCurrentUser.coupon_code == null || mCurrentUser.coupon_code.isEmpty()))
            getUserInfo();

    }

    void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText("Settings");

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ab_x_close);
        actionbar_left_btn.setOnClickListener(this);

    }

    void updateUI() {
        findViewById(R.id.container_user).setVisibility(mCurrentUser == null ? View.GONE : View.VISIBLE);
        findViewById(R.id.container_sig_in).setVisibility(mCurrentUser != null ? View.GONE : View.VISIBLE);
        getLayoutContainerPhone().setOnClickListener(this);

        if (mCurrentUser != null) {
            ((TextView) findViewById(R.id.txt_name)).setText(mCurrentUser.lastname != null ? mCurrentUser.firstname + " " + mCurrentUser.lastname : mCurrentUser.firstname);

            ((TextView) findViewById(R.id.txt_phone)).setText(BentoNowUtils.getPhoneFromNumber(mCurrentUser.phone));
            ((TextView) findViewById(R.id.txt_email)).setText(mCurrentUser.email);

            ((TextView) findViewById(R.id.txt_coupon)).setText(mCurrentUser.coupon_code);
            findViewById(R.id.container_coupon).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.container_coupon).setVisibility(View.GONE);
        }


        if (mCurrentUser != null && mCurrentUser.coupon_code != null && !mCurrentUser.coupon_code.isEmpty()) {
            message = IosCopyDao.get("share-precomposed-message").replace("%@", mCurrentUser.coupon_code).replace("http://apple.co/1FPEbWY", ConstantUtils.URL_INSTALL_ANDROID);
        }
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.wtf(TAG, "UTF-8 should always be supported", e);
            throw new RuntimeException("URLEncoder.encode() failed for " + s);
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

    public void onLogoutPressed(View v) {
        DebugUtils.logDebug(TAG, "onLogoutPressed");
        action = "logout";
        LogOutDialog mDialog = new LogOutDialog(SettingsActivity.this, "Confirmation", "Are you sure you want to log out ?");
        mDialog.addAcceptButton("YES", SettingsActivity.this);
        mDialog.addCancelButton("NO", SettingsActivity.this);
        mDialog.show();
    }

    public void onSignInPressed(View v) {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtra("settings", true);
        startActivity(intent);
    }

    public void onFaqPressed(View v) {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("faq", true);
        startActivity(intent);
    }

    public void onEmailSupportPressed(View v) {
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

    public void onPhoneSupportPressed(View v) {
        action = "phone";
        ConfirmationDialog mDialog = new ConfirmationDialog(SettingsActivity.this, "Call Us", "415.300.1332");
        mDialog.addAcceptButton("Call", SettingsActivity.this);
        mDialog.addCancelButton("Cancel", null);
        mDialog.show();
    }

    public void onFacebookPressed(View v) {
        if (!SocialNetworksUtil.postStatusFacebook(SettingsActivity.this, message, ConstantUtils.URL_INSTALL_ANDROID)) {

            Toast.makeText(getApplicationContext(), "There is no facebook client installed.", Toast.LENGTH_SHORT).show();

            onEmailPressed(v);
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

    public void onMessagePressed(View v) {
        SocialNetworksUtil.sendSms(SettingsActivity.this, message);
    }

    public void onEmailPressed(View v) {
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

                        onSignInPressed(null);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
            case R.id.button_accept:
                if (action.equals("phone")) {
                    SocialNetworksUtil.phoneCall(SettingsActivity.this, "4153001332");
                } else if (action.equals("logout")) {
                    mCurrentUser = null;
                    userDao.removeUser();
                    MixpanelUtils.clearPreferences();
                    updateUI();
                    MixpanelUtils.track("Logged Out");
                }

                action = "";
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
}