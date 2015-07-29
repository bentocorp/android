package com.bentonow.bentonow;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bentonow.bentonow.model.Ioscopy;


public class TermAndConditionsActivity extends BaseActivity {

    private static final String TAG = "TermAndCond...Activity";
    private WebView browser;
    private RelativeLayout overlay_call;
    private TextView btn_call;
    private TextView btn_cancel;
    private TextView info_phone;
    private TextView info_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_and_conditions);
        initActionbar();
        initElements();
        loadDatas();
    }

    private void initElements() {
        browser = (WebView) findViewById(R.id.webview);

        info_email = (TextView)findViewById(R.id.info_email);
        info_phone = (TextView)findViewById(R.id.info_phone);
        overlay_call = (RelativeLayout)findViewById(R.id.overlay_call);
        btn_call = (TextView)findViewById(R.id.btn_call);
        btn_cancel = (TextView)findViewById(R.id.btn_cancel);
    }

    private void loadDatas() {
        // BTN EMAIL
        info_email.setPaintFlags(info_email.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        info_email.setOnClickListener(new View.OnClickListener() {
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
                    Toast.makeText(getApplicationContext(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // BTN PHONE
        info_phone.setPaintFlags(info_email.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        info_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlay_call.setVisibility(View.VISIBLE);
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

        // BROWSER
        browser.loadUrl(Ioscopy.getKeyValue(Config.IOSCOPY.TERMS_CONDITIONS_BODY));
    }

    private void initActionbar() {
        Log.i(TAG, "initActionbar()");
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText("Terms & Conditions");

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransitionGoLeft();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransitionGoLeft();
    }

}
