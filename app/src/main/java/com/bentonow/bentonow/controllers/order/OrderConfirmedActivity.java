package com.bentonow.bentonow.controllers.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BaseActivity;
import com.bentonow.bentonow.controllers.help.HelpActivity;

public class OrderConfirmedActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "OrderConfirmedActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmed);

        ImageView actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);
        actionbar_right_btn.setImageResource(R.drawable.ic_ab_help);
        actionbar_right_btn.setOnClickListener(this);
    }

    public void onFaqPressed (View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("faq", true);
        startActivity(intent);
    }

    public void onAddAnotherBentoPressed (View view) {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        onFaqPressed(null);
    }
}
