package com.bentonow.bentonow;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class FaqActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        TextView btn_go_back = (TextView) findViewById(R.id.btn_go_back);
        btn_go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishThisActivity();
            }
        });

        TextView html_container = (TextView) findViewById(R.id.html_container);
        html_container.setText(Html.fromHtml("<h1>Frequently Asked Questions</h1><h3>Does Bento share my info?</h3><p>Lorem ipsum dolor sit amet</p>"));
    }

    /*private void addListeners() {
        actionbar_ic_back = (ImageView)findViewById(R.id.actionbar_ic_back);
        actionbar_ic_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishThisActivity();
            }
        });
    }*/

}
