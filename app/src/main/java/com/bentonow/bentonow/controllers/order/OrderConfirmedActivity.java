package com.bentonow.bentonow.controllers.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.ui.BackendButton;

public class OrderConfirmedActivity extends BaseFragmentActivity implements View.OnClickListener {

    private static final String TAG = "OrderConfirmedActivity";

    private BackendButton btnViewAllOrders;
    private ImageView menuItemRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmed);

        MixpanelUtils.track("Viewed Order Confirmation Screen");

        getMenuItemRight().setImageResource(R.drawable.ic_ab_help);
        getMenuItemRight().setOnClickListener(this);

        getBtnViewAllOrders().setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        GoogleAnalyticsUtil.sendScreenView("Order Confirmed");
        super.onResume();
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
        switch (v.getId()) {
            case R.id.actionbar_right_btn:
                onFaqPressed(null);
                break;
            case R.id.btn_view_all_orders:
                BentoNowUtils.openOrderHistoryActivity(OrderConfirmedActivity.this);
                break;
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public BackendButton getBtnViewAllOrders() {
        if (btnViewAllOrders == null)
            btnViewAllOrders = (BackendButton) findViewById(R.id.btn_view_all_orders);
        return btnViewAllOrders;
    }


    public ImageView getMenuItemRight() {
        if (menuItemRight == null)
            menuItemRight = (ImageView) findViewById(R.id.actionbar_right_btn);
        return menuItemRight;
    }

}
