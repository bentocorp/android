package com.bentonow.bentonow;

import android.app.Activity;

/**
 * Created by gonzalo on 04/05/2015.
 */
public class Bentonow {
    public static Boolean isOpen = false;
    public static Boolean isSolded = false;
    public static Long pending_order_id = null;
    public static Long pending_bento_id = null;
    public static int current_side;
    public static String current_dish_selected = "";

    static class app {
        public static Boolean is_first_access = false;
        public static Activity current_activity;
        public static Boolean isFocused = false;
    }
}