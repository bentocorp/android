package com.bentonow.bentonow.Utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Build;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.bentonow.bentonow.controllers.BentoApplication;
import com.google.android.gms.common.GoogleApiAvailability;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jose Torres on 19/08/15.
 */
public class AndroidUtil {

    public static final String TAG = "AndroidUtil";

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

    public static long getMillisFromSeconds(int seconds) {
        DebugUtils.logDebug(TAG, "Millis: " + TimeUnit.SECONDS.toMillis(seconds));
        return TimeUnit.SECONDS.toMillis(seconds);
    }

    public static InputFilter getInputFilterEmoji() {
        return new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                CharSequence sourceOriginal = source;
                source = replaceEmoji(source);
                end = source.toString().length();

                if (end == 0)
                    return ""; //Return empty string if the input character is already removed

                if (!sourceOriginal.toString().equals(source.toString())) {
                    char[] v = new char[end - start];
                    TextUtils.getChars(source, start, end, v, 0);

                    String s = new String(v);

                    if (source instanceof Spanned) {
                        SpannableString sp = new SpannableString(s);
                        TextUtils.copySpansFrom((Spanned) source, start, end, null, sp, 0);
                        return sp;
                    } else {
                        return s;
                    }
                } else {
                    return null; // keep original
                }
            }

            private String replaceEmoji(CharSequence source) {

                String notAllowedCharactersRegex = "[^a-zA-Z0-9@#\\$%\\&\\-\\+\\(\\)\\*;:!\\?\\~`£\\{\\}\\[\\]=\\.,_/\\\\\\s'\\\"<>\\^\\|÷×]";
                return source.toString()
                        .replaceAll(notAllowedCharactersRegex, "");
            }

        };
    }

    public static boolean isValidField(String sText) {
        return sText != null && !sText.trim().isEmpty();
    }

    public static boolean isEmailValid(String email) {
        return isValidField(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static int getRandomFromRange(int min, int max) {
        return new Random().nextInt((max - min)) + min;
    }

}
