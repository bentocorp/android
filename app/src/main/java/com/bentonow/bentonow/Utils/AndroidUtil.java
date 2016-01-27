package com.bentonow.bentonow.Utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.bentonow.bentonow.controllers.BentoApplication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jose Torres on 19/08/15.
 */
public class AndroidUtil {

    public static void showKeyboard(final Context context, final EditText view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, 0);
    }


    public static int dpToPx(float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, BentoApplication.instance.getResources().getDisplayMetrics());
        return (int) px;
    }

    public static int dpToPx(float dp, Resources resources) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getAppVersionName(Context mContext) {
        String sVersionName = "";
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            sVersionName = pInfo.versionName;
        } catch (Exception ex) {
            DebugUtils.logError("getAppVersionName", ex);
        }
        return sVersionName;
    }

    public static double round(double value, int places) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static boolean isJellyBean() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return true;
        } else {
            return false;
        }
    }

    public static String getMinFromMillis(long lMilliseconds) {
        long lMinute = TimeUnit.MILLISECONDS.toMinutes(lMilliseconds);
        return lMinute < 9 ? "0" + lMinute : String.valueOf(lMinute);
    }

    public static String getSecondsFromMillis(long lMilliseconds) {
        long lSeconds = TimeUnit.MILLISECONDS.toSeconds(lMilliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(lMilliseconds));
        return lSeconds < 9 ? "0" + lSeconds : String.valueOf(lSeconds);
    }

}
