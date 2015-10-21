package com.bentonow.bentonow.web;


import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BentoApplication;

/**
 * Created by Jose Torres on 8/17/15.
 */
public class BentoNowApi {

    public static final String INIT = "/init";
    public static final String IOSCOPY = "/ioscopy";
    public static final String ORDER = "/order";
    public static final String COUPON_REQUEST = "/couponcode/request";

    // INIT
    public static String INIT_KEY = "key";
    public static String INIT_VALUE = "value";
    public static String INIT_TYPE = "type";

    //STATUS OVERALL
    public static final String STATUS_ALL_RUN = "/status/all";
    public static String STATUS_OVERALL_LABEL_VALUE = "value";

    // USER SIGN UP

    public static int USER_SIGNUP_200 = 200;
    public static int USER_SIGNUP_400 = 400;
    public static int USER_SIGNUP_409 = 409;

    // USER SIGN IN

    public static int DEFAULT_SUCCESS_200 = 200;
    public static int USER_LOGIN_404 = 404;
    public static int USER_LOGIN_403 = 403;


    public static String getSendCompleteOrder() {
        String sUrl = BentoApplication.instance.getResources().getString(R.string.server_api_url) + STATUS_ALL_RUN;
        return sUrl;
    }

    public static String getPlaceDetailUrl(String sPlaceId) {
        String sUrl = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + sPlaceId + "&key=" + BentoApplication.instance.getResources().getString(R.string.google_server_key);
        return sUrl;
    }


}
