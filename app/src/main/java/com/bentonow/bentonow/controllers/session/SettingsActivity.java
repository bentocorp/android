package com.bentonow.bentonow.controllers.session;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.ui.CustomDialog;
import com.bentonow.bentonow.ui.FontAwesomeButton;
import com.facebook.share.widget.ShareDialog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;


public class SettingsActivity extends BaseActivity implements View.OnClickListener {
    static final String TAG = "SettingsActivity";

    String action = "";
    private String message = null;
    private String url = "https://goo.gl/5pA0iE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initActionbar();

        Typeface font = Typeface.createFromAsset( getAssets(), "fonts/fontawesome-webfont.ttf" );
        FontAwesomeButton button = (FontAwesomeButton)findViewById( R.id.btn_facebook );
        button.setTypeface(font);

        button = (FontAwesomeButton)findViewById( R.id.btn_twitter );
        button.setTypeface(font);

        button = (FontAwesomeButton)findViewById( R.id.btn_sms );
        button.setTypeface(font);

        button = (FontAwesomeButton)findViewById( R.id.btn_email );
        button.setTypeface(font);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateUI();

        if (User.current != null && User.current.coupon_code != null && !User.current.coupon_code.isEmpty()) {
            message = BackendText.get("share-precomposed-message")
                    .replace("%@", User.current.coupon_code)
                    .replace("http://apple.co/1FPEbWY", "");
        }
    }

    void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText("Settings");

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ab_x_close);
        actionbar_left_btn.setOnClickListener(this);

    }

    void updateUI () {
        findViewById(R.id.container_user).setVisibility(User.current == null ? View.GONE : View.VISIBLE);
        findViewById(R.id.container_sig_in).setVisibility(User.current != null ? View.GONE : View.VISIBLE);

        if (User.current != null) {
            ((TextView)findViewById(R.id.txt_name)).setText(
                    User.current.lastname != null ?
                            User.current.firstname + " " + User.current.lastname : User.current.firstname
            );

            ((TextView)findViewById(R.id.txt_phone)).setText(User.current.phone);
            ((TextView)findViewById(R.id.txt_email)).setText(User.current.email);

            ((TextView)findViewById(R.id.txt_coupon)).setText(User.current.coupon_code);
            findViewById(R.id.container_coupon).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.container_coupon).setVisibility(View.GONE);
        }
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
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
                    Settings.save(this);
                }

                action = "";
                break;
        }
    }

    public void onLogoutPressed (View v) {
        Log.i(TAG, "onLogoutPressed");
        action = "logout";
        CustomDialog dialog = new CustomDialog(this, "Confirmation\nAre you sure you want to log out?", "YES", "NO");
        dialog.setOnOkPressed(this);
        dialog.show();
    }

    public void onSignInPressed (View v) {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtra("settings", true);
        startActivity(intent);
    }

    public void onFaqPressed (View v) {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("faq", true);
        startActivity(intent);
    }

    public void onEmailSupportPressed (View v) {
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

    public void onPhoneSupportPressed (View v) {
        action = "phone";
        CustomDialog dialog = new CustomDialog(this, "415.300.1332", "Call", "Cancel");
        dialog.show();
        dialog.setOnOkPressed(this);
    }

    public void onFacebookPressed (View v) {
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

    public void onTwitterPressed (View v) {
        String tweetUrl =
                String.format("https://twitter.com/intent/tweet?text=%s&url=%s",
                        urlEncode(message), urlEncode(url));
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

    public void onMessagePressed (View v) {
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
}