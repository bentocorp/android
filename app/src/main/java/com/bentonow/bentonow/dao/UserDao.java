package com.bentonow.bentonow.dao;

import android.util.Log;

import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.user.CouponRequest;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

/**
 * Created by Jose Torres on 11/3/15.
 */
public class UserDao {
    public static final String TAG = "UserDao";


    public static void requestCoupon(User mUser, String email, String reason, TextHttpResponseHandler response) {
        CouponRequest couponRequest = new CouponRequest();
        couponRequest.email = email;
        couponRequest.reason = reason;
        couponRequest.api_token = mUser != null ? mUser.api_token : null;

        String data = new Gson().toJson(couponRequest);

        RequestParams params = new RequestParams();
        params.put("data", data);

        Log.i(TAG, "requestCoupon data: " + data);

        BentoRestClient.post("/coupon/request", params, response);
    }

    public static void login(User mUser, TextHttpResponseHandler responseHandler) {
        mUser.card = null;

        String endpoint = mUser.password != null ? "/user/login" : "/user/fblogin";
        String data = new Gson().toJson(mUser);

        RequestParams params = new RequestParams();
        params.put("data", data);

        Log.i(TAG, "login data: " + data);

        BentoRestClient.post(endpoint, params, responseHandler);
    }

    public static void register(User mUser, TextHttpResponseHandler responseHandler) {
        mUser.card = null;

        String endpoint = mUser.password != null ? "/user/signup" : "/user/fbsignup";
        String data = new Gson().toJson(mUser).replace("\"firstname\":", "\"name\":");

        RequestParams params = new RequestParams();
        params.put("data", data);

        Log.i(TAG, "register data: " + data);

        BentoRestClient.post(endpoint, params, responseHandler);
    }
}
