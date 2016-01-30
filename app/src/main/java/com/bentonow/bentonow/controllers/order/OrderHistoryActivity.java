package com.bentonow.bentonow.controllers.order;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.model.User;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

public class OrderHistoryActivity extends BaseFragmentActivity implements View.OnClickListener {

    private static final String TAG = "OrderHistoryActivity";

    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);


        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ic_ab_back);
        actionbar_left_btn.setOnClickListener(OrderHistoryActivity.this);


    }

    @Override
    protected void onResume() {
        super.onResume();

        mCurrentUser = userDao.getCurrentUser();

        getOrderHistoryByUser();
    }

    private void getOrderHistoryByUser() {
        RequestParams params = new RequestParams();
        params.put("api_token", mCurrentUser.api_token);
        BentoRestClient.get("/user/orderhistory", params, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "getOrderHistoryByUser failed: " + responseString + " StatusCode: " + statusCode);
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                DebugUtils.logDebug(TAG, "getOrderHistoryByUser: " + responseString);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
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
