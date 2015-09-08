/**
 * @author Kokusho Torres
 * 02/09/2014
 */
package com.bentonow.bentonow.Utils;

import android.widget.Toast;

import com.bentonow.bentonow.controllers.BentoApplication;

public class WidgetsUtils {

    /**
     * Method that creates a short toast based in a String
     *
     * @param message The String that is going to be show
     */
    public static void createShortToast(final String message) {
        BentoApplication.instance.handlerPost(new Runnable() {
            public void run() {
                Toast.makeText( BentoApplication.instance, message, Toast.LENGTH_SHORT).show();
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
                Toast.makeText( BentoApplication.instance, message, Toast.LENGTH_LONG).show();
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
                Toast.makeText( BentoApplication.instance,  BentoApplication.instance.getString(id), Toast.LENGTH_SHORT).show();
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
                Toast.makeText( BentoApplication.instance,  BentoApplication.instance.getString(id), Toast.LENGTH_LONG).show();
            }
        });
    }

}
