package com.bentonow.bentonow;

import  android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.model.Ioscopy;


public class TermAndConditionsActivity extends BaseActivity {

    private static final String TAG = "TermAndCond...Activity";
    private WebView browser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_and_conditions);
        initActionbar();
        initElements();
        loadWebView();
    }

    private void initElements() {
        browser = (WebView) findViewById(R.id.webview);
    }

    private void loadWebView() {
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
}
