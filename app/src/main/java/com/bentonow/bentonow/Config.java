package com.bentonow.bentonow;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Config {

    public static final float DEFAULT_ZOOM = 17.0f;
    public static final float MIN_ZOOM = 11.0f;
    public static LatLng INIT_LAT_LONG = null;
    public static LatLng DEFAULT_LAT_LONG = new LatLng(37.772492, -122.420262);
    public static int android_min_version;
    public static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    public static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    public static final String OUT_JSON = "/json";
    public static final long TIME_TO_CHECK_IF_BENTO_OPEN = 1000 * 30;
    public static final String API_MENUITEMS_TAG = "MenuItems";
    public static final String PHONE_NUMBER = "4153001332";


    public static String invalid_address_extra_label = "invalid_address";
    public static int aux_deduct_day = 0;
    public static String aux_initial_stock = "0";
    public static Location current_location;
    public static String serviceArea_dinner;
    public static int DinnerStartTime;
    public static int LunchStartTime;

    public static class CurrentOrder {
        public static int tip_percent = 15; // will be modified in the interface
        public static double tax = 0; // will be changed in the interface
        public static double item_price = 0; // will be modified in the interface
        public static int total_items = 0; // will be modified in the interface
        public static double total_tip_cost = 0;
        public static double total_items_cost = 0; //total_items*item_price;
        public static double total_tax_cost = 0; // total_items_cost*(tip_percent/100);
        public static double total_order_cost = 0; // total_tax_cost+total_items_cost;
    }

    public static class API{
        public static final String INIT = "/init";
        public static final String IOSCOPY = "/ioscopy";
        public static final String ORDER = "/order";
        public static final String COUPON_REQUEST = "/couponcode/request";

        // INIT
        public static String INIT_KEY = "key";
        public static String INIT_VALUE = "value";
        public static String INIT_TYPE = "type";

        //STATUS OVERALL
        public static final String STATUS_ALL_URN = "/status/all";
        public static String STATUS_OVERALL_LABEL_VALUE = "value";

        // USER SIGN UP

        public static int USER_SIGNUP_200 = 200;
        public static int USER_SIGNUP_400 = 400;
        public static int USER_SIGNUP_409 = 409;

        // USER SIGN IN

        public static int DEFAULT_SUCCESS_200 = 200;
        public static int USER_LOGIN_404 = 404;
        public static int USER_LOGIN_403 = 403;

        public class COUPON {
            public static final String APPLY = "/coupon/apply/";

            public class RESPONSE {
                public static final int OK200 = 200;
                public static final int INVALID_COUPON_400 = 400;
            }
        }

        public class USER {
            public static final String FBLOGIN = "/user/fblogin";
            public static final String FBSIGNUP = "/user/fbsignup";
            public static final String LOGIN = "/user/login";
            public static final String SIGNUP = "/user/signup";
        }

        public class SERVER_STATUS {
            public class ORDER {
                public class MESSAGE {
                    public static final String _402 = "No payment specified, and no payment on file.";
                }
            }
        }
    }

    public class IOSCOPY {
        public static final String PRICE = "price";
        public static final String TAX_PERCENT = "tax_percent";
        public static final String FAQ_BODY = "faq-body";
        public static final String PRIVACY_POLICY_BODY = "privacy-policy-body";
        public static final String TERMS_CONDITIONS_BODY = "terms-conditions-body";
    }

    public class DISH {
        public static final String _ID = "_id";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String TYPE = "type";
        public static final String IMAGE1 = "image1";
        public static final String MAX_PER_ORDER = "max_per_order";
        public static final String TODAY = "today";
        public static final String QTY = "qty";
        public static final String ITEM_ID = "item_id";
        public static final String itemId = "itemId";
    }

    public class ASSET {
        public class FONT {
            public static final String OPENSANS_REGULAR = "fonts/OpenSans-Regular.ttf";
        }
    }

    public class ORDER {
        public class STATUS {
            public static final String COMPLETED = "yes";
            public static final String UNCOMPLETED = "no";
        }
    }

    public class USER {
        public static final String NAME = "name";
        public static final String EMAIL = "email";
        public static final String PHONE = "phone";
        public static final String PASSWORD = "password";
        public static final String APITOKEN = "api_token";
    }

    public class FACEBOOK {
        public class SIGNUP {
            public static final String firstname = "firstname";
            public static final String lastname = "lastname";
            public static final String email = "email";
            public static final String phone = "phone";
            public static final String fb_id = "fb_id";
            public static final String fb_token = "fb_token";
            public static final String fb_profile_pic = "fb_profile_pic";
            public static final String fb_age_range = "fb_age_range";
            public static final String fb_gender = "fb_gender";
        }
    }

    public static class AppNavigateMap {
        public static from from;
    }

    enum from {
        BuildBentoActivity, SettingActivity
    }

    public class SIDE {
        public static final int MAIN = 0;
            public static final int SIDE_1 = 1;
        public static final int SIDE_2 = 2;
        public static final int SIDE_3 = 3;
        public static final int SIDE_4 = 4;
    }
}
