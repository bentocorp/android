package com.bentonow.bentonow.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.model.user.Card;
import com.bentonow.bentonow.model.user.CouponRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

public class User {
    //region STATIC VARIABLES
    static final String TAG = "model.User";
    public static User current = null;
    public static LatLng location;
    //endregion

    //region VARIABLES
    public String firstname = null;
    public String lastname = null;
    public String email = null;
    public String phone = null;
    public String coupon_code = null;
    public String api_token = null;
    public String stripe_token = null;

    public String fb_token = null;
    public String fb_id = null;
    public String fb_profile_pic = null;
    public String fb_age_range = null;
    public String fb_gender = null;

    public String password = null;

    public Card card = new Card();
    //endregion

    public static void requestCoupon (String email, String reason, TextHttpResponseHandler response) {
        CouponRequest couponRequest = new CouponRequest();
        couponRequest.email = email;
        couponRequest.reason = reason;
        couponRequest.api_token = current != null ? current.api_token : null;

        String data = new Gson().toJson(couponRequest);

        RequestParams params = new RequestParams();
        params.put("data", data);

        Log.i(TAG, "requestCoupon data: " + data);

        BentoRestClient.post("/coupon/request", params, response);
    }

    public void login (TextHttpResponseHandler responseHandler) {
        card = null;

        String endpoint = password != null ? "/user/login" : "/user/fblogin";
        String data = new Gson().toJson(this);

        RequestParams params = new RequestParams();
        params.put("data", data);

        Log.i(TAG, "login data: " + data);

        BentoRestClient.post(endpoint, params, responseHandler);
    }

    public void register (TextHttpResponseHandler responseHandler) {
        card = null;

        String endpoint = password != null ? "/user/signup" : "/user/fbsignup";
        String data = new Gson().toJson(this).replace("\"firstname\":", "\"name\":");

        RequestParams params = new RequestParams();
        params.put("data", data);

        Log.i(TAG, "register data: " + data);

        BentoRestClient.post(endpoint, params, responseHandler);
    }
}
