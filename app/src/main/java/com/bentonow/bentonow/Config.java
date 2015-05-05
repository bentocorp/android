package com.bentonow.bentonow;

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
    public static String[] MenuItems_ItemLabels = {"itemId", "name", "description", "type", "image1", "max_per_order"};
    public static int aux_deduct_day = 1;

    public static enum appStatus{
        PRODUCTION,DEVELOPMENT;
    }

    public static class API{
        public static String URL = APP_DEV_STATUS.equals(appStatus.PRODUCTION) ? "http://api.bentonow.com" : "http://dev.api.bentonow.com";
        public static final String MENU_URN = "/menu";

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

    }
}
