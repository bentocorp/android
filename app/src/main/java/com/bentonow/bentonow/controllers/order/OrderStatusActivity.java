package com.bentonow.bentonow.controllers.order;

import android.os.Bundle;
import android.view.View;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;

public class OrderStatusActivity extends BaseFragmentActivity implements View.OnClickListener {

    private static final String TAG = "OrderStatusActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);
    }

    @Override
    protected void onResume() {
        GoogleAnalyticsUtil.sendScreenView("Order Status");
        super.onResume();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                break;
            default:
                DebugUtils.logError(TAG, v.getId() + "");
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
