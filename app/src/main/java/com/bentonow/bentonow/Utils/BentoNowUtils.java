package com.bentonow.bentonow.Utils;

import android.content.Context;
import android.content.Intent;

import com.bentonow.bentonow.BuildConfig;
import com.bentonow.bentonow.controllers.errors.ErrorActivity;
import com.bentonow.bentonow.controllers.init.MainActivity;
import com.bentonow.bentonow.controllers.order.BuildBentoActivity;
import com.bentonow.bentonow.controllers.order.BuildFixedBentoActivity;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Item;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.order.OrderItem;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Jose Torres on 8/17/15.
 */
public class BentoNowUtils {

    public static final SimpleDateFormat sdfBento = new SimpleDateFormat("yyyyMMdd");


    public static int getCurrentTime() {
        if (BuildConfig.DEBUG)
            return 183000;
           // return Integer.parseInt(new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date()).replace(":", ""));
        else
            return Integer.parseInt(new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date()).replace(":", ""));
    }

    public static String getMixpanelDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String sMixpanelDate = "";
        try {
            sMixpanelDate = dateFormat.format(Calendar.getInstance().getTime());
        } catch (Exception ex) {
            DebugUtils.logError("getMixpanelDate()", ex);
        }

        return sMixpanelDate;
    }

    public static String getTodayDate() {
        Calendar mToday = Calendar.getInstance();
        return sdfBento.format(mToday.getTime());
    }

    public static String getTomorrowDate() {
        Calendar cDate = Calendar.getInstance();
        cDate.add(Calendar.DATE, 1);

        return sdfBento.format(cDate.getTime());
    }

    public static void openMainActivity(Context mContext) {
        if (!MainActivity.bIsOpen) {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    public static void openBuildBentoActivity(Context mContext) {
        Menu mCurrentMenu = Menu.get();

        Intent iBuildBento;

        if (mCurrentMenu.menu_type.equals(ConstantUtils.sFixed))
            iBuildBento = new Intent(mContext, BuildFixedBentoActivity.class);
        else
            iBuildBento = new Intent(mContext, BuildBentoActivity.class);

        iBuildBento.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        if (!BuildBentoActivity.bIsOpen)
            mContext.startActivity(iBuildBento);
    }

    public static void openErrorActivity(Context mContext) {
        if (!ErrorActivity.bIsOpen) {
            Intent intent = new Intent(mContext, ErrorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    public static void goToDashboard(Context mContext) {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(startMain);
    }

    public static String getUUIDBento() {
        String sUuidBento = SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.UUID_BENTO);

        if (sUuidBento.isEmpty())
            sUuidBento = UUID.randomUUID().toString();

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.UUID_BENTO, sUuidBento);

        return sUuidBento;
    }

    public static String getNumberFromPhone(String sPhone) {
        if (sPhone == null)
            sPhone = "";
        return sPhone.replaceAll("\\D+", "");
    }

    public static String getPhoneFromNumber(String sNumber) {
        sNumber = getNumberFromPhone(sNumber);
        String sPhone = "(";

        for (int a = 0; a < sNumber.length(); a++) {

            switch (a) {
                case 0:
                    String sFirstNumber = sNumber.charAt(0) + "";
                    if (!sFirstNumber.equals("1"))
                        sPhone += sNumber.charAt(a);
                    break;
                case 1:
                    sPhone += sNumber.charAt(a);
                    break;
                case 2:
                    sPhone += sNumber.charAt(a) + ") ";
                    break;
                case 3:
                    sPhone += sNumber.charAt(a);
                    break;
                case 4:
                    sPhone += sNumber.charAt(a);
                    break;
                case 5:
                    sPhone += sNumber.charAt(a) + " - ";
                    break;
                case 6:
                    sPhone += sNumber.charAt(a);
                    break;
                case 7:
                    sPhone += sNumber.charAt(a);
                    break;
                case 8:
                    sPhone += sNumber.charAt(a);
                    break;
                case 9:
                    sPhone += sNumber.charAt(a);
                    break;
            }
        }

        return sPhone;
    }

    public static boolean validPhoneNumber(String sPhoneNumber) {
        String sPhone = getNumberFromPhone(sPhoneNumber);
        return sPhone.length() == 10;
    }

    public static void saveSettings(ConstantUtils.optSaveSettings optSave) {
        Gson gson = new Gson();
        String user = "";
        String location = "";
        String address = "";
        String backendText = gson.toJson(BackendText.list);

        if (User.current != null)
            user = gson.toJson(User.current);
        if (Order.location != null)
            location = gson.toJson(Order.location);
        if (Order.address != null)
            address = gson.toJson(Order.address);

        switch (optSave) {
            case ALL:
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.USER, user);
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.LOCATION, location);
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ADDRESS, address);

                if (BackendText.list.size() > 0)
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.BACKENDTEXT, backendText);
                break;
            case USER:
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.USER, user);
                break;
            case LOCATION:
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.LOCATION, location);
                break;
            case ADDRESS:
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ADDRESS, address);
                break;
            case BACKEND_TEXT:
                if (BackendText.list.size() > 0)
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.BACKENDTEXT, backendText);
                break;
        }

    }

    public static void updateUser(User mUserInfo) {
        Gson gson = new Gson();
        String user;

        if (User.current != null) {
            User.current.coupon_code = mUserInfo.coupon_code;
            user = gson.toJson(User.current);
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.USER, user);
        }
    }

    public static String calculateSoldOutItems() {
        String sSoldOutItems = "";

        for (int a = 0; a < Order.current.OrderItems.size(); a++) {
            boolean bIsSoldOut = false;
            for (Item mItem : Order.current.OrderItems.get(a).items) {
                if (mItem.isSoldOut(true)) {
                    bIsSoldOut = true;
                    if (!sSoldOutItems.contains(mItem.name))
                        sSoldOutItems += "\n- " + mItem.name;
                    DebugUtils.logDebug("calculateSoldOutItems:", mItem.name);
                }
            }

            Order.current.OrderItems.get(a).bIsSoldoOut = bIsSoldOut;
        }
        return sSoldOutItems;
    }

    public static boolean isSoldOutOrder(OrderItem mOrder) {
        boolean bIsSoldOut = false;
        for (Item mItem : mOrder.items) {
            if (mItem.isSoldOut(true)) {
                bIsSoldOut = true;
                DebugUtils.logDebug("calculateSoldOutItems:", mItem.name);
            }
        }

        return bIsSoldOut;
    }
}
