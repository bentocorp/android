package com.bentonow.bentonow.Utils;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
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
import com.bentonow.bentonow.controllers.order.BuildFixedBentoActivity;
import com.bentonow.bentonow.controllers.order.CompleteOrderActivity;
import com.bentonow.bentonow.controllers.payment.EnterCreditCardActivity;
import com.bentonow.bentonow.controllers.session.EnterPhoneNumberActivity;
import com.bentonow.bentonow.controllers.session.SettingsActivity;
import com.bentonow.bentonow.controllers.session.SignInActivity;
import com.bentonow.bentonow.dao.OrderDao;
import com.bentonow.bentonow.dao.UserDao;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.Settings;
import com.bentonow.bentonow.model.User;
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

    private static final String TAG = "BentoNowUtils";

    public static final SimpleDateFormat sdfBento = new SimpleDateFormat("yyyyMMdd");
    public static final boolean B_APPIUM_TESTING = false;
    public static final boolean B_KOKUSHO_TESTING = true;

    public static int getCurrentTime() {
        if (BuildConfig.DEBUG && BentoNowUtils.B_KOKUSHO_TESTING)
            return 200000;
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
        Menu mCurrentMenu = Menu.get();

        mContext.finish();

        Intent iBuildBento;
        if (mCurrentMenu != null)
            if (mCurrentMenu.menu_type.equals(ConstantUtils.sFixed)) {
                iBuildBento = new Intent(mContext, BuildFixedBentoActivity.class);
                iBuildBento.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(iBuildBento);
            } else {
                iBuildBento = new Intent(mContext, BuildBentoActivity.class);
                iBuildBento.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(iBuildBento);
            }
        else
            openErrorActivity(mContext);
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

    public static void openCompleteOrderActivity(Context mContext) {
        Intent intent = new Intent(mContext, CompleteOrderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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
        if (Settings.min_version != 0 && (Settings.min_version > BuildConfig.VERSION_CODE)) {
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


    public static boolean isValidCompleteOrder(Context mContext) {
        boolean bIsValid = true;

        UserDao userDao = new UserDao();
        User mUser = userDao.getCurrentUser();

        LatLng mLocation = new Gson().fromJson(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.LOCATION), LatLng.class);
        Address mAddress = new Gson().fromJson(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.ADDRESS), Address.class);

        if (mUser == null) {
            // WidgetsUtils.createShortToast(R.string.error_no_user_log_in);
            Intent mIntentSignIn = new Intent(mContext, SignInActivity.class);
            mIntentSignIn.putExtra(ConstantUtils.TAG_OPEN_SCREEN, ConstantUtils.optOpenScreen.COMPLETE_ORDER);
            mContext.startActivity(mIntentSignIn);

            bIsValid = false;
            // } else if (LocationUtils.mCurrentLocation == null || !Settings.isInServiceArea(LocationUtils.mCurrentLocation) || BentoApplication.address == null || BentoApplication.location == null) {
        } else if (mLocation == null || !Settings.isInServiceArea(mLocation) || mAddress == null) {
            // WidgetsUtils.createShortToast(mContext.getString(R.string.error_no_valid_delivery_address));

            Intent intent = new Intent(mContext, DeliveryLocationActivity.class);
            intent.putExtra(ConstantUtils.TAG_OPEN_SCREEN, ConstantUtils.optOpenScreen.COMPLETE_ORDER);
            mContext.startActivity(intent);

            bIsValid = false;
        } else if (!userDao.isCreditCardValid(mUser)) {
            //  WidgetsUtils.createShortToast(R.string.error_no_credit_card);

            Intent mIntentCredit = new Intent(mContext, EnterCreditCardActivity.class);
            mIntentCredit.putExtra(ConstantUtils.TAG_OPEN_SCREEN, ConstantUtils.optOpenScreen.COMPLETE_ORDER);
            mContext.startActivity(mIntentCredit);

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

}
