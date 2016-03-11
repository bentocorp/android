package com.bentonow.bentonow.controllers.order;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.adapter.ExpandableListOrderHistoryAdapter;
import com.bentonow.bentonow.controllers.dialog.ProgressDialog;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.listener.ListenerOrderHistory;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.order.history.OrderHistoryItemModel;
import com.bentonow.bentonow.model.order.history.OrderHistoryItemSectionModel;
import com.bentonow.bentonow.model.order.history.OrderHistoryModel;
import com.bentonow.bentonow.parse.OrderHistoryJsonParser;
import com.bentonow.bentonow.ui.BackendAutoFitTextView;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class OrderHistoryActivity extends BaseFragmentActivity implements View.OnClickListener, ListenerOrderHistory {

    private static final String TAG = "OrderHistoryActivity";

    private ImageView menuItemLeft;
    private TextView toolbarTitle;
    private ExpandableListView mExpandableListOrderHistory;
    private BackendAutoFitTextView txtEmptyOrderHistory;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ExpandableListOrderHistoryAdapter mExpandableAdapter;
    private ProgressDialog mProgressDialog;

    private User mCurrentUser;
    private OrderHistoryModel mOrderHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        getMenuItemLeft().setImageResource(R.drawable.vector_navigation_left_green);
        getMenuItemLeft().setOnClickListener(OrderHistoryActivity.this);
        getToolbarTitle().setText(IosCopyDao.get("pane-title-orders"));

        getExpandableListOrderHistory().setAdapter(getExpandableListAdapter());
        getExpandableListOrderHistory().setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                OrderHistoryItemModel mOrderHistory = (OrderHistoryItemModel) getExpandableListAdapter().getChild(groupPosition, childPosition);

                Intent mIntentOrderStatus = new Intent(OrderHistoryActivity.this, OrderStatusActivity.class);
                mIntentOrderStatus.putExtra(OrderHistoryItemModel.TAG, mOrderHistory);
                startActivity(mIntentOrderStatus);

                DebugUtils.logDebug(TAG, "Open Next Order: " + mOrderHistory.getTitle());
                return true;
            }
        });
        getExpandableListOrderHistory().setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // Doing nothing
                return true;
            }
        });

        getSwipeRefreshLayout().setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getOrderHistoryByUser();
            }
        });


        mCurrentUser = userDao.getCurrentUser();

        getOrderHistoryByUser();
    }

    @Override
    protected void onResume() {
        GoogleAnalyticsUtil.sendScreenView("Order History");

        if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.ORDER_HISTORY_FORCE_REFRESH)) {
            getOrderHistoryByUser();
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ORDER_HISTORY_FORCE_REFRESH, false);
        }

        super.onResume();
    }

    private void getOrderHistoryByUser() {
        dismissDialog();
        showLoadingDialog(getString(R.string.processing_label), false);

        RequestParams params = new RequestParams();
        params.put("api_token", mCurrentUser.api_token);

        BentoRestClient.get("/user/orderhistory", params, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "getOrderHistoryByUser failed: " + responseString + " StatusCode: " + statusCode);

                DebugUtils.logError(TAG, "getUserInfo failed: " + responseString + " StatusCode: " + statusCode);

                switch (statusCode) {
                    case 401:// Invalid Api Token
                        if (!userDao.removeUser())
                            userDao.clearAllData();

                        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ORDER_AHEAD_SUBSCRIPTION, false);

                        OrderHistoryActivity.this.finish();
                        return;
                    default:
                        break;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getTxtEmptyOrderHistory().setVisibility(View.VISIBLE);
                        getExpandableListAdapter().setListHistory(new ArrayList<OrderHistoryItemSectionModel>());
                    }
                });

                dismissDialog();
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                DebugUtils.logDebug(TAG, "getOrderHistoryByUser: " + responseString);

                mOrderHistory = OrderHistoryJsonParser.parseOrderHistory(responseString);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean bShowList = false;
                        getExpandableListAdapter().setListHistory(mOrderHistory.getListHistorySection());
                        for (int a = 0; a < mOrderHistory.getListHistorySection().size(); a++) {
                            getExpandableListOrderHistory().expandGroup(a, false);
                            if (!mOrderHistory.getListHistorySection().get(a).getListItems().isEmpty())
                                bShowList = true;
                        }

                        getTxtEmptyOrderHistory().setVisibility(bShowList ? View.GONE : View.VISIBLE);
                        getExpandableListOrderHistory().setVisibility(bShowList ? View.VISIBLE : View.GONE);
                        dismissDialog();
                    }
                });

            }
        });
    }

    private void showLoadingDialog(String sText, boolean bCancelable) {
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            mProgressDialog = new ProgressDialog(OrderHistoryActivity.this, sText, bCancelable);
            mProgressDialog.show();
        }
    }

    private void dismissDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSwipeRefreshLayout().setRefreshing(false);

                if (mProgressDialog != null)
                    mProgressDialog.dismiss();

                mProgressDialog = null;
            }
        });
    }

    @Override
    public void onRemoveOrder(int id) {

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

    private ImageView getMenuItemLeft() {
        if (menuItemLeft == null)
            menuItemLeft = (ImageView) findViewById(R.id.actionbar_left_btn);
        return menuItemLeft;
    }

    private TextView getToolbarTitle() {
        if (toolbarTitle == null)
            toolbarTitle = (TextView) findViewById(R.id.actionbar_title);
        return toolbarTitle;
    }

    private BackendAutoFitTextView getTxtEmptyOrderHistory() {
        if (txtEmptyOrderHistory == null)
            txtEmptyOrderHistory = (BackendAutoFitTextView) findViewById(R.id.txt_empty_order_history);
        return txtEmptyOrderHistory;
    }

    private ExpandableListView getExpandableListOrderHistory() {
        if (mExpandableListOrderHistory == null) {
            mExpandableListOrderHistory = (ExpandableListView) findViewById(R.id.expand_list_order_history);
            mExpandableListOrderHistory.setGroupIndicator(null);
            mExpandableListOrderHistory.setClickable(false);
        }
        return mExpandableListOrderHistory;
    }

    private ExpandableListOrderHistoryAdapter getExpandableListAdapter() {
        if (mExpandableAdapter == null)
            mExpandableAdapter = new ExpandableListOrderHistoryAdapter(OrderHistoryActivity.this, OrderHistoryActivity.this);
        return mExpandableAdapter;
    }

    private SwipeRefreshLayout getSwipeRefreshLayout() {
        if (swipeRefreshLayout == null)
            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        return swipeRefreshLayout;
    }

}
