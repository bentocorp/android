package com.bentonow.bentonow.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.bentonow.bentonow.BuildConfig;
import com.bentonow.bentonow.controllers.errors.ErrorActivity;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.controllers.init.MainActivity;
import com.bentonow.bentonow.controllers.order.BuildBentoActivity;
import com.bentonow.bentonow.controllers.order.BuildFixedBentoActivity;
import com.bentonow.bentonow.controllers.session.EnterPhoneNumberActivity;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.service.BentoService;
import com.facebook.GraphResponse;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Jose Torres on 8/17/15.
 */
public class BentoNowUtils {

    private static final String TAG = "BentoNowUtils";

    public static final SimpleDateFormat sdfBento = new SimpleDateFormat("yyyyMMdd");
    public static final boolean B_APPIUM_TESTING = false;
    public static final boolean B_KOKUSHO_TESTING = false;


    public static int getCurrentTime() {
        if (BuildConfig.DEBUG && BentoNowUtils.B_KOKUSHO_TESTING)
            return 183000;
            //return Integer.parseInt(new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date()).replace(":", ""));
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
        if (mCurrentMenu != null)
            if (mCurrentMenu.menu_type.equals(ConstantUtils.sFixed)) {
                iBuildBento = new Intent(mContext, BuildFixedBentoActivity.class);
                iBuildBento.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                if (!BuildFixedBentoActivity.bIsOpen)
                    mContext.startActivity(iBuildBento);
            } else {
                iBuildBento = new Intent(mContext, BuildBentoActivity.class);
                iBuildBento.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                if (!BuildBentoActivity.bIsOpen)
                    mContext.startActivity(iBuildBento);
            }
        else
            openErrorActivity(mContext);
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

                if (BackendText.list != null && BackendText.list.size() > 0)
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
                if (BackendText.list != null && BackendText.list.size() > 0)
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
            for (DishModel mDishModel : Order.current.OrderItems.get(a).items) {
                if (DishDao.isSoldOut(mDishModel, true) || !DishDao.canBeAdded(mDishModel)) {
                    bIsSoldOut = true;
                    if (!sSoldOutItems.contains(mDishModel.name))
                        sSoldOutItems += "\n- " + mDishModel.name;
                    DebugUtils.logDebug("calculateSoldOutItems:", mDishModel.name);
                }
            }

            Order.current.OrderItems.get(a).bIsSoldoOut = bIsSoldOut;
        }
        return sSoldOutItems;
    }

    public static boolean isSoldOutOrder(OrderItem mOrder) {
        boolean bIsSoldOut = false;
        for (DishModel mDishModel : mOrder.items) {
            if (DishDao.isSoldOut(mDishModel, false)) {
                bIsSoldOut = true;
                DebugUtils.logDebug("calculateSoldOutItems:", mDishModel.name);
            }
        }

        return bIsSoldOut;
    }

    public static void rotateBanner(View mView) {
        Animation an = new RotateAnimation(0.0f, 45.0f, Animation.RELATIVE_TO_SELF, 0.75f, Animation.RELATIVE_TO_SELF, 0.75f);
        an.setDuration(0);
        an.setRepeatCount(0);
        an.setFillAfter(true);
        mView.setAnimation(an);
    }

    public static void openEnterPhoneNumberActivity(FragmentActivity mActivity, GraphResponse graphResponse) {
        Intent iPhoneNumber = new Intent(mActivity, EnterPhoneNumberActivity.class);
        iPhoneNumber.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        iPhoneNumber.putExtra(EnterPhoneNumberActivity.TAG_FB_USER, graphResponse.getJSONObject().toString());
        mActivity.startActivity(iPhoneNumber);
        mActivity.finish();
    }


    public static void openPolicyActivity(FragmentActivity mActivity) {
        Intent intent = new Intent(mActivity, HelpActivity.class);
        intent.putExtra("privacy", true);
        mActivity.startActivity(intent);
    }

    public static void openTermAndConditionsActivity(FragmentActivity mActivity) {
        Intent intent = new Intent(mActivity, HelpActivity.class);
        intent.putExtra("tos", true);
        mActivity.startActivity(intent);
    }

    public static void openFaqActivity(FragmentActivity mActivity) {
        Intent intent = new Intent(mActivity, HelpActivity.class);
        intent.putExtra("faq", true);
        mActivity.startActivity(intent);
    }

    public static String getNumberFromPrice(double dPrice) {
        DecimalFormat df = new DecimalFormat("###.##");
        String sPrice;

        try {
            sPrice = df.format(dPrice);
            sPrice = sPrice.replace(".00", "");
        } catch (Exception ex) {
            DebugUtils.logError("getNumberFromPrice()", ex);
            sPrice = String.valueOf(Settings.price);
        }

        return sPrice;
    }

    public static String getDefaultPriceBento(double dPrice) {
        if (dPrice <= 0)
            return getNumberFromPrice(Settings.price);
        else
            return getNumberFromPrice(dPrice);
    }

    public static void runBentoService(Context ctx) {
        if (!BentoService.isRunning()) {
            Log.i(TAG, "starting service");
            try {
                ctx.startService(new Intent(ctx, BentoService.class));
            } catch (Exception e) {
                DebugUtils.logError("BentoService: ", e);
            }
        }
    }


}
