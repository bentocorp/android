/**
 * @author Kokusho Torres
 * 02/09/2014
 */
package com.bentonow.bentonow.Utils;

import android.widget.Toast;

import com.bentonow.bentonow.controllers.BentoApplication;

public class WidgetsUtils {

    public static Toast mToast;

    /**
     * Method that creates a short toast based in a String
     *
     * @param message The String that is going to be show
     */
    public static void createShortToast(final String message) {
        BentoApplication.instance.handlerPost(new Runnable() {
            public void run() {
                if (!isToastShowing()) {
                    mToast = Toast.makeText(BentoApplication.instance, message, Toast.LENGTH_SHORT);
                    mToast.show();
                }
            }
        });
    }

    /**
     * Method that creates a long Toast Message based in a string
     *
     * @param message The String that is going to be show
     */
    public static void createLongToast(final String message) {
        BentoApplication.instance.handlerPost(new Runnable() {
            public void run() {
                if (!isToastShowing()) {
                    mToast = Toast.makeText(BentoApplication.instance, message, Toast.LENGTH_LONG);
                    mToast.show();
                }
            }
        });
    }

    /**
     * Method that creates a short Toast based in a string id
     *
     * @param id The Int from the String id
     */
    public static void createShortToast(final int id) {
        BentoApplication.instance.handlerPost(new Runnable() {
            public void run() {
                if (!isToastShowing()) {
                    mToast = Toast.makeText(BentoApplication.instance, BentoApplication.instance.getString(id), Toast.LENGTH_SHORT);
                    mToast.show();
                }
            }
        });
    }

    /**
     * Method that creates a long Toast Message based in a string id
     *
     * @param id
     */
    public static void createLongToast(final int id) {
        BentoApplication.instance.handlerPost(new Runnable() {
            public void run() {
                if (!isToastShowing()) {
                    mToast = Toast.makeText(BentoApplication.instance, BentoApplication.instance.getString(id), Toast.LENGTH_LONG);
                    mToast.show();
                }
            }
        });
    }

    private static boolean isToastShowing() {
        if (mToast == null)
            return false;
        else if (mToast.getView().isShown())
            return true;
        else
            return false;
    }

}
