package com.bentonow.bentonow;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.model.Ioscopy;

import java.util.List;


public class FaqActivity extends BaseActivity {

    private static final String TAG = "FaqActivity";
    private WebView browser;
    private TextView btn_go_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        initActionbar();
        initElements();
        loadWebView();
    }

    private void initElements() {
        browser = (WebView) findViewById(R.id.webview);
    }

    private void loadWebView() {
        browser.loadUrl(Ioscopy.getKeyValue(Config.IOSCOPY.FAQ_BODY));
    }

    private void initActionbar() {
        Log.i(TAG, "initActionbar()");
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText("FAQ");

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
