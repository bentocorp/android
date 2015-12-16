package com.bentonow.bentonow.Utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.bentonow.bentonow.controllers.BentoApplication;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

}
