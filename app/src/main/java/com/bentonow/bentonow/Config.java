package com.bentonow.bentonow;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by gonzalo on 07/04/2015.
 */
public class Config {

    public static final int current_version = 1;
    public static final float DEFAULT_ZOOM = 17.0f;
    public static LatLng INIT_LAT_LONG = new LatLng(37.772492, -122.420262);
    public static int android_min_version;
    public static final String API_KEY = "AIzaSyDz5MlSeWBUP1iDeSI3j6qeoLbMWDbJgQg";
    public static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    public static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    public static final String OUT_JSON = "/json";
    public static final long TIME_TO_CHECK_IF_BENTO_OPEN = 1000 * 30;
    public static final long CACHE_REST_DURATION_TIME = 5 * 60 * 1000;
    public static final String API_MENUITEMS_TAG = "MenuItems";
    public static final String PHONE_NUMBER = "4153001332";
    public static appStatus APP_DEV_STATUS = appStatus.DEVELOPMENT;

    public static String invalid_address_extra_label = "invalid_address";
    public static int aux_deduct_day = 0;
    public static String aux_initial_stock = "0";
    public static Location current_location;
    public static String serviceArea_dinner;
    public static String startTime = "16:30:00";
    public static String next_day_json;
    //public static boolean processing_stock = false;

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

    public static enum appStatus{
        PRODUCTION,DEVELOPMENT;
    }

    public static class API{
        public static final String INIT = "/init";
        public static final String IOSCOPY = "/ioscopy";
        public static final String ORDER = "/order";
        public static final String COUPON_REQUEST = "/couponcode/request";
        public static final int DEFAULT_ERROR_409 = 409;
        public static String URL = APP_DEV_STATUS.equals(appStatus.PRODUCTION) ? "https://api2.bentonow.com" : "https://api2.dev.bentonow.com";
        public static final String MENU_URN = "/menu";

        // INIT
        public static String INIT_KEY = "key";
        public static String INIT_VALUE = "value";
        public static String INIT_TYPE = "type";

        //STATUS OVERALL
        public static String STATUS_OVERALL_URN = "/status/overall";
        public static final String STATUS_ALL_URN = "/status/all";
        public static String STATUS_OVERALL_LABEL_VALUE = "value";
        public static final String STATUS_OVERALL_MESSAGE_OPEN = "open";
        public static final String STATUS_OVERALL_MESSAGE_CLOSED = "closed";
        public static final String STATUS_OVERALL_MESSAGE_SOLDOUT = "sold out";
        //STATUS MENU
        public static String STATUS_MENU = "/status/menu";
        public static String STATUS_ALL_LABEL_MENU = "menu";
        public static String STATUS_ALL_LABEL_OVERALL = "overall";

        public static String STATUS_ALL = "/status/all";

        // USER SIGN UP
        public static String USER_SIGNUP = "/user/signup";

        public static int USER_SIGNUP_200 = 200;
        public static int USER_SIGNUP_400 = 400;
        public static int USER_SIGNUP_409 = 409;

        // USER SIGN IN
        public static String USER_LOGIN = "/user/login";

        public static int DEFAULT_SUCCESS_200 = 200;
        public static int USER_LOGIN_404 = 404;
        public static int USER_LOGIN_403 = 403;
        public static String meals = "meals";
        public static String m3 = "3";

        public class FACEBOOK {
            public static final String SIGNUP = "/user/fbsignup";
            public static final int BAD_TOKEN_403 = 403;
            public static final String SIGNIN = "/user/fbsignup";
        }

        public class COUPON {
            public static final String APPLY = "/coupon/apply/";

            public class RESPONSE {
                public static final int OK200 = 200;
                public static final int INVALID_COUPON_400 = 400;
            }

            public class LABELS {
                public static final String AMOUNTOFF = "amountOff";
            }
        }

        public class USER {
            public static final String FBLOGIN = "/user/fblogin";
            public static final String FBSIGNUP = "/user/fbsignup";
            public static final String LOGIN = "/user/login";
            public static final String SIGNUP = "/user/signup";
        }

        public static class MEALS {
            public static String m3 = "3";

            public static class M3 {
                public static String startTime = "startTime";
            }
        }

        public class SERVER_STATUS {
            public class ORDER {
                public class NUMBER {
                    public static final int _200 = 200; //if ok.
                    public static final int _402 = 402; // if No payment specified, and no payment on file.
                    public static final int _410 = 410; //if the inventory is not available. The UI should be updated, and the return includes the inventory:
                }

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
        public static final String ANDROID_MIN_VERSION = "current_version";
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

    static enum from {
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
