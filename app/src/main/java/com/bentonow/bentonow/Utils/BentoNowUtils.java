package com.bentonow.bentonow.Utils;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.bentonow.bentonow.BuildConfig;
import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BentoApplication;
import com.bentonow.bentonow.controllers.errors.ErrorActivity;
import com.bentonow.bentonow.controllers.errors.ErrorVersionActivity;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.controllers.help.HelpActivity;
import com.bentonow.bentonow.controllers.init.MainActivity;
import com.bentonow.bentonow.controllers.order.BuildBentoActivity;
import com.bentonow.bentonow.controllers.order.CompleteOrderActivity;
import com.bentonow.bentonow.controllers.order.OrderHistoryActivity;
import com.bentonow.bentonow.controllers.payment.EnterCreditCardActivity;
import com.bentonow.bentonow.controllers.session.EnterPhoneNumberActivity;
import com.bentonow.bentonow.controllers.session.SettingsActivity;
import com.bentonow.bentonow.controllers.session.SignInActivity;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.dao.OrderDao;
import com.bentonow.bentonow.dao.SettingsDao;
import com.bentonow.bentonow.dao.UserDao;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.menu.TimesModel;
import com.facebook.GraphResponse;
import com.google.android.gms.maps.model.LatLng;
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

    public static final SimpleDateFormat sdfBento = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat sdfBentoInit2 = new SimpleDateFormat("yyyy-MM-dd");
    public static final boolean B_APPIUM_TESTING = false;
    public static final boolean B_KOKUSHO_TESTING = true;
    private static final String TAG = "BentoNowUtils";

    public static int getCurrentTime() {
        if (BuildConfig.DEBUG && BentoNowUtils.B_KOKUSHO_TESTING)
            return 120000;
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

    public static String getTodayDateInit2() {
        Calendar mToday = Calendar.getInstance();
        return sdfBentoInit2.format(mToday.getTime());
    }

    public static String getTomorrowDate() {
        Calendar cDate = Calendar.getInstance();
        cDate.add(Calendar.DATE, 1);

        return sdfBento.format(cDate.getTime());
    }

    public static void openMainActivity(FragmentActivity mContext) {
        if (!MainActivity.bIsOpen) {
            mContext.finish();
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);
        } else
            DebugUtils.logDebug(TAG, "Cant: openMainActivity");
    }

    public static void openBuildBentoActivity(FragmentActivity mContext) {
        mContext.finish();
        Intent iBuildBento = new Intent(mContext, BuildBentoActivity.class);
        iBuildBento.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(iBuildBento);
    }

    public static void openErrorActivity(FragmentActivity mContext) {
        if (!ErrorActivity.bIsOpen) {
            mContext.finish();
            OrderDao mOrderDao = new OrderDao();
            mOrderDao.cleanUp();
            Intent intent = new Intent(mContext, ErrorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);
        }
    }

    public static void openErrorActivity(Context mContext) {
        if (!ErrorActivity.bIsOpen) {
            Intent intent = new Intent(mContext, ErrorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);
            OrderDao mOrderDao = new OrderDao();
            mOrderDao.cleanUp();
        }
    }

    public static void openDeliveryLocationScreen(FragmentActivity mContext, ConstantUtils.optOpenScreen optOpenScreen) {
        Intent intent = new Intent(mContext, DeliveryLocationActivity.class);
        intent.putExtra(ConstantUtils.TAG_OPEN_SCREEN, optOpenScreen);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mContext.startActivity(intent);
    }

    public static void openCompleteOrderActivity(Context mContext, Menu mMenu) {
        Intent intent = new Intent(mContext, CompleteOrderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Menu.TAG, mMenu);
        mContext.startActivity(intent);
    }

    public static void openCreditCardActivity(Context mContext, ConstantUtils.optOpenScreen optOpenScreen) {
        Intent mIntentCredit = new Intent(mContext, EnterCreditCardActivity.class);
        mIntentCredit.putExtra(ConstantUtils.TAG_OPEN_SCREEN, optOpenScreen);
        mContext.startActivity(mIntentCredit);
    }

    public static void openDeliveryLocationScreen(Context mContext, ConstantUtils.optOpenScreen optOpenScreen) {
        Intent intent = new Intent(mContext, DeliveryLocationActivity.class);
        intent.putExtra(ConstantUtils.TAG_OPEN_SCREEN, optOpenScreen);
        mContext.startActivity(intent);
    }

    public static void openSettingsActivity(FragmentActivity mContext) {
        Intent intent = new Intent(mContext, SettingsActivity.class);
        mContext.startActivity(intent);
        mContext.overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
    }

    public static void goToDashboard(Context mContext) {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(startMain);
    }

    public static void openOrderHistoryActivity(FragmentActivity mContext) {
        Intent mIntentOrder = new Intent(mContext, OrderHistoryActivity.class);
        mContext.startActivity(mIntentOrder);
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

    public static void rotateBanner(View mView) {
        Animation an = new RotateAnimation(0.0f, 45.0f, Animation.RELATIVE_TO_SELF, 0.75f, Animation.RELATIVE_TO_SELF, 0.75f);
        an.setDuration(0);
        an.setRepeatCount(0);
        an.setFillAfter(true);
        mView.setAnimation(an);
    }

    public static void openEnterPhoneNumberActivity(FragmentActivity mActivity, GraphResponse graphResponse, ConstantUtils.optOpenScreen optOpenScreen) {
        Intent iPhoneNumber = new Intent(mActivity, EnterPhoneNumberActivity.class);
        iPhoneNumber.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        iPhoneNumber.putExtra(EnterPhoneNumberActivity.TAG_FB_USER, graphResponse.getJSONObject().toString());
        iPhoneNumber.putExtra(ConstantUtils.TAG_OPEN_SCREEN, optOpenScreen);
        mActivity.startActivity(iPhoneNumber);
        mActivity.finish();
    }

    public static boolean isLastVersionApp(Context mContext) {
        if (MenuDao.min_version != 0 && (MenuDao.min_version > BuildConfig.VERSION_CODE)) {
            Intent intent = new Intent(mContext, ErrorVersionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return false;
        }
        return true;
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
            sPrice = String.valueOf(SettingsDao.getCurrent().price);
        }

        return sPrice;
    }

    public static String getDefaultPriceBento(double dPrice) {
        if (dPrice <= 0)
            return getNumberFromPrice(SettingsDao.getCurrent().price);
        else
            return getNumberFromPrice(dPrice);
    }


    public static boolean isValidCompleteOrder(Context mContext) {
        boolean bIsValid = true;

        UserDao userDao = new UserDao();
        User mUser = userDao.getCurrentUser();
        LatLng mLocation = null;
        Address mAddress = null;

        try {
            mLocation = new Gson().fromJson(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.LOCATION), LatLng.class);
            mAddress = new Gson().fromJson(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.ADDRESS), Address.class);
        } catch (Exception ex) {
            DebugUtils.logDebug(TAG, ex);
        }

        if (mUser == null) {
            Intent mIntentSignIn = new Intent(mContext, SignInActivity.class);
            mIntentSignIn.putExtra(ConstantUtils.TAG_OPEN_SCREEN, ConstantUtils.optOpenScreen.COMPLETE_ORDER);
            mContext.startActivity(mIntentSignIn);

            bIsValid = false;
        } else if (mLocation == null || mAddress == null) {
            openDeliveryLocationScreen(mContext, ConstantUtils.optOpenScreen.COMPLETE_ORDER);
            bIsValid = false;
        } else if (!userDao.isCreditCardValid(mUser)) {
            openCreditCardActivity(mContext, ConstantUtils.optOpenScreen.COMPLETE_ORDER);
            bIsValid = false;
        }

        return bIsValid;
    }

    public static String getFullAddress() {
        Address mAddress;
        try {
            mAddress = new Gson().fromJson(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.ADDRESS), Address.class);
        } catch (Exception ex) {
            mAddress = null;
            DebugUtils.logError(TAG, ex);
        }

        if (mAddress == null) return "";

        String sAddress = "";

        for (int i = 0; i < mAddress.getMaxAddressLineIndex(); ++i) {
            if (sAddress.length() > 0) sAddress += ", ";
            sAddress += mAddress.getAddressLine(i);
        }

        return sAddress;
    }

    public static String getStreetAddress() {
        Address mAddress;
        try {
            mAddress = new Gson().fromJson(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.ADDRESS), Address.class);
        } catch (Exception ex) {
            mAddress = null;
            DebugUtils.logError(TAG, ex);
        }

        if (mAddress == null)
            return "";
        return mAddress.getThoroughfare() + ", " + mAddress.getSubThoroughfare();
    }

    public static LatLng getOrderLocation() {
        LatLng mOrderLocation = null;

        String sLocation = SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.LOCATION);
        try {
            mOrderLocation = new Gson().fromJson(sLocation, LatLng.class);
        } catch (Exception ex) {
            DebugUtils.logError(TAG, "getOrderLocation: " + ex.toString());
        }
        return mOrderLocation;
    }

    public static void saveOrderLocation(LatLng mLocation, Address mAddress) {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.LOCATION, new Gson().toJson(mLocation));
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ADDRESS, new Gson().toJson(mAddress));
    }

    public static String getDayTimeSelected(Order mOrder) {
        String sDateTime;
        Calendar mCal = Calendar.getInstance();
        SimpleDateFormat formatOrderDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatOrderTitle = new SimpleDateFormat("EEE MM/dd");
        try {
            mCal.setTime(formatOrderDate.parse(mOrder.for_date));
            String sDate = formatOrderTitle.format(mCal.getTime());
            sDate = sDate.replace(".", "");
            String sMeal = mOrder.MealName.contains("lunch") ? "Lunch" : "Dinner";
            sDateTime = BentoApplication.instance.getString(R.string.build_bento_toolbar_title_oa, sDate, sMeal, getDateHuman(mOrder.scheduled_window_start, false),
                    getDateHuman(mOrder.scheduled_window_end, true));
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
            sDateTime = BentoApplication.instance.getString(R.string.build_bento_toolbar_title_oa, mOrder.for_date, mOrder.MealName, getDateHuman(mOrder.scheduled_window_start, false),
                    getDateHuman(mOrder.scheduled_window_end, true));
        }
        return sDateTime;
    }

    public static String getDaySelected(Order mOrder) {
        String sDateTime;
        Calendar mCal = Calendar.getInstance();
        SimpleDateFormat formatOrderDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatOrderTitle = new SimpleDateFormat("EEE MM/dd");
        try {
            mCal.setTime(formatOrderDate.parse(mOrder.for_date));
            String sDate = formatOrderTitle.format(mCal.getTime());
            sDate = sDate.replace(".", "");
            String sMeal = mOrder.MealName.contains("lunch") ? "Lunch" : "Dinner";
            sDateTime = BentoApplication.instance.getString(R.string.summary_bento_oa_delivery_day, sDate, sMeal);
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
            sDateTime = BentoApplication.instance.getString(R.string.summary_bento_oa_delivery_day, mOrder.for_date, mOrder.MealName);
        }
        return sDateTime;
    }


    public static String getTimeSelected(Order mOrder) {
        String sDateTime;
        Calendar mCal = Calendar.getInstance();
        SimpleDateFormat formatOrderDate = new SimpleDateFormat("yyyy-MM-dd");
        try {
            mCal.setTime(formatOrderDate.parse(mOrder.for_date));
            sDateTime = BentoApplication.instance.getString(R.string.summary_bento_oa_delivery_time, getDateHuman(mOrder.scheduled_window_start, false),
                    getDateHuman(mOrder.scheduled_window_end, true));
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
            sDateTime = BentoApplication.instance.getString(R.string.summary_bento_oa_delivery_time, getDateHuman(mOrder.scheduled_window_start, false),
                    getDateHuman(mOrder.scheduled_window_end, true));
        }
        return sDateTime;
    }

    public static String getDayTimeSelected(Menu mMenu) {
        String sDateTime;
        Calendar mCal = Calendar.getInstance();
        SimpleDateFormat formatOrderDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatOrderTitle = new SimpleDateFormat("EEE MM/dd");
        String sStarts = "";
        String sFinish = "";
        String sMeal = mMenu.meal_name.contains("lunch") ? "Lunch" : "Dinner";

        try {
            mCal.setTime(formatOrderDate.parse(mMenu.for_date));
            String sDate = formatOrderTitle.format(mCal.getTime());
            sDate = sDate.replace(".", "");

            for (TimesModel mTime : mMenu.listTimeModel) {
                if (mTime.isSelected) {
                    sStarts = getDateHuman(mTime.start, false);
                    sFinish = getDateHuman(mTime.end, true);
                }
            }


            sDateTime = BentoApplication.instance.getString(R.string.build_bento_toolbar_title_oa, sDate, sMeal, sStarts, sFinish);
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
            sDateTime = BentoApplication.instance.getString(R.string.build_bento_toolbar_title_oa, mMenu.for_date, sMeal, sStarts, sFinish);
        }
        return sDateTime;
    }

    public static String getDaySelected(Menu mMenu) {
        String sDateTime;
        Calendar mCal = Calendar.getInstance();
        SimpleDateFormat formatOrderDate = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatOrderTitle = new SimpleDateFormat("EEE MM/dd");
        String sMeal = mMenu.meal_name.contains("lunch") ? "Lunch" : "Dinner";

        try {
            mCal.setTime(formatOrderDate.parse(mMenu.for_date));
            String sDate = formatOrderTitle.format(mCal.getTime());
            sDate = sDate.replace(".", "");
            sDateTime = BentoApplication.instance.getString(R.string.build_bento_spinner_item_oa, sDate, sMeal);
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
            sDateTime = BentoApplication.instance.getString(R.string.build_bento_spinner_item_oa, mMenu.for_date, sMeal);
        }
        return sDateTime;
    }


    public static double getDeliveryPriceByTime(TimesModel mTime) {
        double dDeliveryPrice = SettingsDao.getCurrent().delivery_price;

        try {
            dDeliveryPrice = Double.parseDouble(mTime.delivery_price);
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }

        return dDeliveryPrice;
    }

    public static String getDateHuman(String sDate, boolean bShowAm) {
        SimpleDateFormat formatTime = new SimpleDateFormat(bShowAm ? "h:mm a" : "h:mm");
        String[] aTime = sDate.split(":");
        String sHumanTime;
        try {
            Calendar mCalToday = Calendar.getInstance();
            mCalToday.set(Calendar.HOUR_OF_DAY, Integer.parseInt(aTime[0]));
            mCalToday.set(Calendar.MINUTE, Integer.parseInt(aTime[1]));
            sHumanTime = formatTime.format(mCalToday.getTime());
            sHumanTime = sHumanTime.replace(":00", "");
        } catch (Exception ex) {
            DebugUtils.logDebug(TAG, ex);
            sHumanTime = sDate;
        }
        return sHumanTime;
    }

    public static Calendar getCalendarByTime(String sTime) {
        String[] aTime = sTime.split(":");
        Calendar mCalTime = Calendar.getInstance();
        try {
            mCalTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(aTime[0]));
            mCalTime.set(Calendar.MINUTE, Integer.parseInt(aTime[1]));
            mCalTime.set(Calendar.SECOND, 0);
        } catch (Exception ex) {
            DebugUtils.logDebug(TAG, ex);
        }
        return mCalTime;
    }

    public static long showOATimer(Menu mMenu) {
        long lSeconds = 0;
        if (MenuDao.gateKeeper != null && MenuDao.gateKeeper.getAvailableServices() != null && MenuDao.gateKeeper.getAvailableServices().mOrderAhead != null
                && !MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.isEmpty())
            if (mMenu.menu_id.equals(MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.get(0).menu_id)) {
                if (MenuDao.gateKeeper.getMealTypes() != null) {
                    if (mMenu.meal_type.equals("2") && MenuDao.gateKeeper.getMealTypes().getTwo() != null) {
                        Calendar mCutOf = getCalendarByTime(MenuDao.gateKeeper.getMealTypes().getTwo().getOaCutoff());
                        Calendar mNow = Calendar.getInstance();
                        DebugUtils.logDebug(TAG, "Hour Cut OFF: " + mCutOf.get(Calendar.HOUR_OF_DAY) + ":" + mCutOf.get(Calendar.MINUTE));
                        DebugUtils.logDebug(TAG, "Hour Now: " + mNow.get(Calendar.HOUR_OF_DAY) + ":" + mNow.get(Calendar.MINUTE));
                        if (mCutOf.after(mNow)) {
                            lSeconds = (mCutOf.getTimeInMillis() - mNow.getTimeInMillis());
                            try {
                                long lSecCutOff = Long.parseLong(SettingsDao.getCurrent().oa_countdown_remaining_mins);
                                lSecCutOff = lSecCutOff * 60 * 1000;
                                DebugUtils.logDebug(TAG, "Milliseconds Dif: " + lSecCutOff);
                                if (lSecCutOff < lSeconds)
                                    lSeconds = 0;
                            } catch (Exception ex) {
                                DebugUtils.logError(TAG, ex);
                            }
                        }
                    } else if (mMenu.meal_type.equals("3") && MenuDao.gateKeeper.getMealTypes().getThree() != null) {
                        Calendar mCutOf = getCalendarByTime(MenuDao.gateKeeper.getMealTypes().getThree().getOaCutoff());
                        Calendar mNow = Calendar.getInstance();
                        DebugUtils.logDebug(TAG, "Hour Cut OFF: " + mCutOf.get(Calendar.HOUR_OF_DAY) + ":" + mCutOf.get(Calendar.MINUTE));
                        DebugUtils.logDebug(TAG, "Hour Now: " + mNow.get(Calendar.HOUR_OF_DAY) + ":" + mNow.get(Calendar.MINUTE));
                        if (mCutOf.after(mNow)) {
                            lSeconds = (mCutOf.getTimeInMillis() - mNow.getTimeInMillis());
                            try {
                                long lSecCutOff = Long.parseLong(SettingsDao.getCurrent().oa_countdown_remaining_mins);
                                lSecCutOff = lSecCutOff * 60 * 1000;
                                DebugUtils.logDebug(TAG, "Milliseconds Dif: " + lSecCutOff);
                                if (lSecCutOff < lSeconds)
                                    lSeconds = 0;
                            } catch (Exception ex) {
                                DebugUtils.logError(TAG, ex);
                            }
                        }
                    }
                }
            }


        DebugUtils.logDebug(TAG, "Seconds Remaining: " + lSeconds);
        return lSeconds;
    }

}
