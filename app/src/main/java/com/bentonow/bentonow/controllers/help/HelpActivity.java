package com.bentonow.bentonow.controllers.help;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.dao.IosCopyDao;

public class HelpActivity extends BaseFragmentActivity implements View.OnClickListener {

    private static final String TAG = "HelpActivity";
    private WebView mWebBrowser;
    private TextView info_email;
    private TextView info_phone;

    private String sScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        initActionbar();
        initElements();
        loadData();

        DebugUtils.logDebug(TAG, "HelpActivity");
    }

    @Override
    protected void onResume() {
        GoogleAnalyticsUtil.sendScreenView("Help");
        super.onResume();
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
            loadDataInWebView(IosCopyDao.get("faq-body"));
            sScreen = "Viewed FAQ Screen";
        } else if (getIntent().getBooleanExtra("tos", false)) {
            loadDataInWebView(IosCopyDao.get("terms-conditions-body"));
            sScreen = "Viewed Terms and Conditions Screen";
        } else if (getIntent().getBooleanExtra("privacy", false)) {
            loadDataInWebView(IosCopyDao.get("privacy-policy-body"));
            sScreen = "Viewed Privacy Policy Screen";
        } else {
            onBackPressed();
            sScreen = "";
        }
    }

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);

        if (getIntent().getBooleanExtra("faq", false)) {
            actionbar_title.setText(IosCopyDao.get("faq-title"));
        } else if (getIntent().getBooleanExtra("tos", false)) {
            actionbar_title.setText(IosCopyDao.get("terms-conditions-title"));
        } else if (getIntent().getBooleanExtra("privacy", false)) {
            actionbar_title.setText(IosCopyDao.get("privacy-policy-title"));
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
                ConfirmationDialog mDialog = new ConfirmationDialog(HelpActivity.this, "Call Us", "415.300.1332");
                mDialog.addAcceptButton("Call", HelpActivity.this);
                mDialog.addCancelButton("Cancel", null);
                mDialog.show();
                break;
            case R.id.button_accept:
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:4153001332"));
                startActivity(intent);
                break;
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
        }
    }


    @Override
    protected void onDestroy() {
        MixpanelUtils.track(sScreen);
        super.onDestroy();
    }

    private WebView getWebBrowser() {
        if (mWebBrowser == null)
            mWebBrowser = (WebView) findViewById(R.id.webview);

        return mWebBrowser;
    }
}
