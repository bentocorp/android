package com.bentonow.bentonow.controllers.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.help.HelpActivity;

public class OrderHistoryActivity extends BaseFragmentActivity implements View.OnClickListener {

    private static final String TAG = "OrderConfirmedActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmed);

        MixpanelUtils.track("Viewed Order Confirmation Screen");

        ImageView actionbar_right_btn = (ImageView) findViewById(R.id.actionbar_right_btn);
        actionbar_right_btn.setImageResource(R.drawable.ic_ab_help);
        actionbar_right_btn.setOnClickListener(this);
    }

    public void onFaqPressed(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        intent.putExtra("faq", true);
        startActivity(intent);
    }

    public void onAddAnotherBentoPressed(View view) {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        onFaqPressed(null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        BentoNowUtils.openBuildBentoActivity(OrderHistoryActivity.this);
    }
}
