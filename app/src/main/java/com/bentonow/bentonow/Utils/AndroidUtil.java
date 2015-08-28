package com.bentonow.bentonow.Utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by Jose Torres on 19/08/15.
 */
public class AndroidUtil {

    public static void showKeyboard(final Context context, final EditText view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, 0);
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

}
