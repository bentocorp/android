package com.bentonow.bentonow.controllers.session;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
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
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.dialog.EditPhoneDialog;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.listener.ListenerDialog;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.ui.CustomDialog;
import com.bentonow.bentonow.ui.FontAwesomeButton;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;


public class SettingsActivity extends BaseActivity implements View.OnClickListener {
    static final String TAG = "SettingsActivity";

    String action = "";
    private String message = null;
    private String url = "https://goo.gl/5pA0iE";

    private LinearLayout layoutCcontainerPhone;

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

        updateUI();

        if (User.current != null && (User.current.coupon_code == null || User.current.coupon_code.isEmpty()))
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
        findViewById(R.id.container_user).setVisibility(User.current == null ? View.GONE : View.VISIBLE);
        findViewById(R.id.container_sig_in).setVisibility(User.current != null ? View.GONE : View.VISIBLE);
        getLayoutContainerPhone().setOnClickListener(this);

        if (User.current != null) {
            ((TextView) findViewById(R.id.txt_name)).setText(User.current.lastname != null ? User.current.firstname + " " + User.current.lastname : User.current.firstname);

            ((TextView) findViewById(R.id.txt_phone)).setText(BentoNowUtils.getPhoneFromNumber(User.current.phone));
            ((TextView) findViewById(R.id.txt_email)).setText(User.current.email);

            ((TextView) findViewById(R.id.txt_coupon)).setText(User.current.coupon_code);
            findViewById(R.id.container_coupon).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.container_coupon).setVisibility(View.GONE);
        }


        if (User.current != null && User.current.coupon_code != null && !User.current.coupon_code.isEmpty()) {
            message = BackendText.get("share-precomposed-message").replace("%@", User.current.coupon_code).replace("http://apple.co/1FPEbWY", "");
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

    //region onClick

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
            case R.id.btn_ok:
                if (action.equals("phone")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:4153001332"));
                    startActivity(intent);
                } else if (action.equals("logout")) {
                    User.current = null;
                    updateUI();
                    BentoNowUtils.saveSettings(ConstantUtils.optSaveSettings.ALL);
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

    private void updatePhoneNumber(final String sPhoneNumber) {
        RequestParams params = new RequestParams();
        params.put("api_token", User.current.api_token);
        params.put("data", "{\"new_phone\":\"" + sPhoneNumber + "\"}");
        BentoRestClient.post("/user/phone", params, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "getUserInfo:  " + responseString);
                WidgetsUtils.createShortToast(R.string.error_web_request);
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                User.current.phone = sPhoneNumber;
                BentoNowUtils.saveSettings(ConstantUtils.optSaveSettings.USER);

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
        Log.i(TAG, "onLogoutPressed");
        action = "logout";
        CustomDialog dialog = new CustomDialog(this, "Confirmation\nAre you sure you want to log out?", "YES", "NO");
        dialog.setOnOkPressed(this);
        dialog.show();
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
        CustomDialog dialog = new CustomDialog(this, "415.300.1332", "Call", "Cancel");
        dialog.show();
        dialog.setOnOkPressed(this);
    }

    public void onFacebookPressed(View v) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Use my Bento promo code");
        intent.putExtra(Intent.EXTRA_TEXT, message + url);

        try {
            startActivity(Intent.createChooser(intent, "Share..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onTwitterPressed(View v) {
        String tweetUrl =
                String.format("https://twitter.com/intent/tweet?text=%s&url=%s", urlEncode(message), urlEncode(url));
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
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) //At least KitKat
        {
            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(this); //Need to change the build to API 19

            intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, message + url);

            if (defaultSmsPackageName != null)//Can be null in case that there is no default, then the user would be able to choose any app that support this intent.
            {
                intent.setPackage(defaultSmsPackageName);
            }

        } else { //For early versions, do what worked for you before.
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:"));
            intent.putExtra("sms_body", message + url);
        }

        startActivity(intent);
    }

    public void onEmailPressed(View v) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Use my Bento promo code");
        intent.putExtra(Intent.EXTRA_TEXT, message + url);

        try {
            startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    //endregion

    private void getUserInfo() {
        RequestParams params = new RequestParams();
        params.put("api_token", User.current.api_token);
        BentoRestClient.get("/user/info", params, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "getUserInfo:  " + responseString);

            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                DebugUtils.logDebug(TAG, "getUserInfo: " + responseString);
                try {
                    User mUserInfo = new Gson().fromJson(responseString, User.class);
                    BentoNowUtils.updateUser(mUserInfo);
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

    private LinearLayout getLayoutContainerPhone() {
        if (layoutCcontainerPhone == null)
            layoutCcontainerPhone = (LinearLayout) findViewById(R.id.layout_container_phone);

        return layoutCcontainerPhone;
    }
}