package com.bentonow.bentonow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.json.JSONException;
import org.json.JSONObject;


public class ErrorOutOfStockActivity extends BaseActivity {

    private static Activity activity;
    private AQuery aq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_out_of_stock);

        activity = this;
        aq = new AQuery(this);

        Bentonow.app.current_activity = this;

        initActionbar();
    }

    private void initActionbar() {
        ImageView actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);
        actionbar_right_btn.setImageResource(R.drawable.ic_ab_help);
        actionbar_right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FaqActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
            }
        });
    }
}
