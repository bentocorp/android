package com.bentonow.bentonow.Utils;

import android.content.Context;
import android.content.Intent;

import com.bentonow.bentonow.controllers.errors.ErrorActivity;
import com.bentonow.bentonow.controllers.init.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Jose Torres on 8/17/15.
 */
public class BentoNowUtils {

    static public String getTodayDate() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }

    public static String getTomorrowDate() {
        String tomorrowDate;

        Calendar cDate = Calendar.getInstance();
        int year = cDate.get(Calendar.YEAR);
        int month = cDate.get(Calendar.MONTH);
        int day = cDate.get(Calendar.DAY_OF_MONTH);

        String monthString = month < 10 ? "0" + String.valueOf(month + 1) : String.valueOf(month);
        String TomorrowDayString = day < 10 ? "0" + String.valueOf(day) : String.valueOf(day);
        tomorrowDate = String.valueOf(year) + monthString + TomorrowDayString;

        return tomorrowDate;
    }

    public static void openMainActivity(Context mContext) {
        if (!MainActivity.bIsOpen) {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        }
    }

    public static void openErrorActivity(Context mContext) {
        if (!ErrorActivity.bIsOpen) {
            Intent intent = new Intent(mContext, ErrorActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        }
    }

    public static String getUUIDBento() {
        String sUuidBento = SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.UUID_BENTO);

        if (sUuidBento.isEmpty())
            sUuidBento = UUID.randomUUID().toString();

        return sUuidBento;
    }

}
