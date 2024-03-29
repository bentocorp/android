package com.bentonow.bentonow.web.request;

import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.dao.OrderDao;
import com.bentonow.bentonow.dao.UserDao;
import com.bentonow.bentonow.listener.InterfaceWebRequest;
import com.bentonow.bentonow.listener.ListenerWebRequest;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.User;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Jose Torres on 14/05/15.
 */
public class RequestPostCompleteOrder implements InterfaceWebRequest {

    public static final String TAG = "RequestGetAllStatus";

    private ListenerWebRequest mListener;

    public RequestPostCompleteOrder(ListenerWebRequest mListener) {
        this.mListener = mListener;
    }

    @Override
    public void dispatchRequest() {
        UserDao userDao = new UserDao();
        OrderDao orderDao = new OrderDao();
        User mUser = userDao.getCurrentUser();
        Order mOrder = orderDao.getCurrentOrder();


        RequestParams params = new RequestParams();
        params.put("data", mOrder.toString());
        params.put("api_token", mUser.api_token);


        DebugUtils.logDebug(TAG, "Order: " + mOrder.toString());

        BentoRestClient.post("/order", params, new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "Order: " + statusCode + " " + responseString);

                mListener.onError(responseString, statusCode);
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                mListener.onResponse(responseString, statusCode);
            }
        });

    }
}
