package com.bentonow.bentonow.model;

import com.bentonow.bentonow.model.user.Card;
import com.google.android.gms.maps.model.LatLng;

public class User {
    //region STATIC VARIABLES
    static final String TAG = "model.User";
    public static User current = null;
    public static LatLng location;
    //endregion

    //region VARIABLES
    public String firstname = "";
    public String lastname = "";
    public String email = "";
    public String phone = "";
    public String coupon_code = "";
    public String api_token = "";
    public String stripe_token = "";

    public String fb_token = "";
    public String fb_id = "";
    public String fb_profile_pic = "";
    public String fb_age_range = "";
    public String fb_gender = "";

    public String password = "";

    public Card card = new Card();
    //endregion


    @Override
    public String toString() {
        return "DataObject [firstname=" + firstname +
                ", lastname=" + lastname +
                ", email=" + email +
                ", phone=" + phone +
                ", fb_id=" + fb_id +
                ", fb_token=" + fb_token +
                ", fb_profile_pic=" + fb_profile_pic +
                ", fb_age_range=" + fb_age_range +
                ", fb_gender=" + fb_gender +
                ", password=" + password +
                ", coupon_code=" + coupon_code +
                ", api_token=" + api_token +
                ", stripe_token=" + stripe_token +
                ", card=" + card +
                "]";
    }
}
