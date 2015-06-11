package com.bentonow.bentonow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bentonow.bentonow.model.User;


public class SettingActivity extends BaseActivity {

    private static final String TAG = "SettingActivity";
    private LinearLayout btn_setting_signin, btn_setting_faq, btn_setting_phone, btn_setting_email;
    private TextView btn_call, btn_cancel;
    private RelativeLayout overlay_call;
    private TextView btn_logout;
    private RelativeLayout user_general_content;
    private TextView btn_cancel_logout, btn_yes_logout;
    private RelativeLayout overlay_logout;
    private User user;
    private TextView user_name, phone, email;
    private LinearLayout btn_setting_creditcard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initActionbar();
        initElements();
        initListeners();
        checkUserSession();
        Config.AppNavigateMap.from = Config.from.SettingActivity;
    }

    private void checkUserSession() {
        btn_setting_creditcard.setVisibility(View.GONE);

        user = User.findById(User.class, (long) 1);
        if (user != null && user.apitoken != null && !user.apitoken.isEmpty()) {
            user_name.setText(user.firstname);
            phone.setText(user.phone);
            email.setText(user.email);
            user_general_content.setVisibility(View.VISIBLE);

//            if (user.cardlast4 != null && !user.cardlast4.isEmpty()) {
//                btn_setting_creditcard.setVisibility(View.VISIBLE);
//            } else {
//                btn_setting_creditcard.setVisibility(View.GONE);
//            }

            btn_setting_signin.setVisibility(View.GONE);
        } else {
//            btn_setting_creditcard.setVisibility(View.GONE);
        }
    }

    private void initElements() {
        btn_setting_signin = (LinearLayout) findViewById(R.id.btn_setting_signin);
        btn_setting_faq = (LinearLayout) findViewById(R.id.btn_setting_faq);
        btn_setting_email = (LinearLayout) findViewById(R.id.btn_setting_email);
        btn_setting_phone = (LinearLayout) findViewById(R.id.btn_setting_phone);
        btn_setting_creditcard = (LinearLayout) findViewById(R.id.btn_setting_creditcard);

        overlay_call = (RelativeLayout) findViewById(R.id.overlay_call);
        btn_call = (TextView) findViewById(R.id.btn_call);
        btn_cancel = (TextView) findViewById(R.id.btn_cancel);

        user_general_content = (RelativeLayout) findViewById(R.id.user_general_content);
        btn_logout = (TextView) findViewById(R.id.btn_logout);

        //LOG OUT OVERLAY
        overlay_logout = (RelativeLayout) findViewById(R.id.overlay_logout);
        btn_cancel_logout = (TextView) findViewById(R.id.btn_cancel_logout);
        btn_yes_logout = (TextView) findViewById(R.id.btn_yes_logout);

        user_name = (TextView) findViewById(R.id.user_name);
        phone = (TextView) findViewById(R.id.phone);
        email = (TextView) findViewById(R.id.email);
    }

    private void initListeners() {

        btn_yes_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.reset();
                Config.AppNavigateMap.from = null;
                Intent intent = new Intent(getApplicationContext(), BuildBentoActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransitionGoLeft();
            }
        });

        btn_cancel_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay_logout.setVisibility(View.GONE);
            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay_logout.setVisibility(View.VISIBLE);
            }
        });

        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Config.PHONE_NUMBER));
                startActivity(intent);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay_call.setVisibility(View.GONE);
            }
        });

        btn_setting_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
                overridePendingTransitionGoRight();
            }
        });
        btn_setting_creditcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreditCardActivity.class);
                startActivity(intent);
                overridePendingTransitionGoRight();
            }
        });
        btn_setting_faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFAQ();
            }
        });
        btn_setting_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] TO = {"help@bentonow.com"};
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(SettingActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_setting_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay_call.setVisibility(View.VISIBLE);
            }
        });
    }


    private void initActionbar() {
        Log.i(TAG, "initActionbar()");
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText("Settings");

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ab_x_close);
        actionbar_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Config.AppNavigateMap.from = null;
                finish();
                overridePendingTransitionGoLeft();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Config.AppNavigateMap.from = null;
        finish();
        overridePendingTransitionGoLeft();
    }
}