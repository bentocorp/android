package com.bentonow.bentonow.controllers.help;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.ui.CustomDialog;

public class HelpActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "HelpActivity";
    private WebView mWebBrowser;
    private TextView info_email;
    private TextView info_phone;

    CustomDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        initActionbar();
        initElements();
        loadData();

        Log.i(TAG, "HelpActivity");
    }

    private void initElements() {
        info_email = (TextView) findViewById(R.id.info_email);
        info_phone = (TextView) findViewById(R.id.info_phone);
    }

    private void loadData() {
        // BTN EMAIL
        info_email.setPaintFlags(info_email.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        info_email.setOnClickListener(this);

        // BTN PHONE
        info_phone.setPaintFlags(info_email.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        info_phone.setOnClickListener(this);

        // BROWSER
        if (getIntent().getBooleanExtra("faq", false)) {
            loadDataInWebView(BackendText.get("faq-body"));
        } else if (getIntent().getBooleanExtra("tos", false)) {
            loadDataInWebView(BackendText.get("terms-conditions-body"));
        } else if (getIntent().getBooleanExtra("privacy", false)) {
            loadDataInWebView(BackendText.get("privacy-policy-body"));
        } else {
            onBackPressed();
        }
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);

        if (getIntent().getBooleanExtra("faq", false)) {
            actionbar_title.setText(BackendText.get("faq-title"));
        } else if (getIntent().getBooleanExtra("tos", false)) {
            actionbar_title.setText(BackendText.get("terms-conditions-title"));
        } else if (getIntent().getBooleanExtra("privacy", false)) {
            actionbar_title.setText(BackendText.get("privacy-policy-title"));
        }

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(this);

    }

    private void loadDataInWebView(String sHtmlString) {
        getWebBrowser().setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        getWebBrowser().loadUrl(sHtmlString);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.info_email:
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
                break;
            case R.id.info_phone:
                dialog = new CustomDialog(this, "415.300.1332", "Call", "Cancel");
                dialog.show();
                dialog.setOnOkPressed(this);
                break;
            case R.id.btn_ok:
                dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:4153001332"));
                startActivity(intent);
                break;
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BentoApplication.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BentoApplication.onPause();
    }

    private WebView getWebBrowser() {
        if (mWebBrowser == null)
            mWebBrowser = (WebView) findViewById(R.id.webview);

        return mWebBrowser;
    }
}
