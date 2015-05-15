package com.bentonow.bentonow;

import com.orm.SugarRecord;

import java.util.Calendar;

/**
 * Created by gonzalo on 07/04/2015.
 */
public class Config {

    public static final String API_KEY = "AIzaSyDz5MlSeWBUP1iDeSI3j6qeoLbMWDbJgQg";
    public static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    public static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    public static final String OUT_JSON = "/json";
    public static final long TIME_TO_CHECK_IF_BENTO_OPEN = 1000 * 30;
    public static final long CACHE_REST_DURATION_TIME = 5 * 60 * 1000;
    public static final String API_MENUITEMS_TAG = "MenuItems";
    public static appStatus APP_DEV_STATUS = appStatus.DEVELOPMENT;

    public static String invalid_address_extra_label = "invalid_address";
    public static String[] MenuItems_ItemLabels = {"itemId", "name", "description", "type", "image1", "max_per_order","qty"};
    public static int aux_deduct_day = 1;
    public static String aux_initial_stock = "1";

    public static class CurrentOrder {
        public static int tip_percent = 10; // will be modified in the interface
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
        public static String URL = APP_DEV_STATUS.equals(appStatus.PRODUCTION) ? "https://api.bentonow.com" : "https://dev.api.bentonow.com";
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

    }

    public class IOSCOPY {
        public static final String PRICE = "price";
        public static final String TAX_PERCENT = "tax_percent";
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
}
