package com.bentonow.bentonow.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.IntentCompat;

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

    public static final SimpleDateFormat sdfBento = new SimpleDateFormat("yyyyMMdd");


    public static int getCurrentTime() {
        return Integer.parseInt(new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date()).replace(":", ""));
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
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mContext.startActivity(intent);
        }
    }

    public static void openErrorActivity(Context mContext) {
        if (!ErrorActivity.bIsOpen) {
            Intent intent = new Intent(mContext, ErrorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mContext.startActivity(intent);
        }
    }

    public static void goToDashboard(Context mContext) {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        mContext.startActivity(startMain);
    }

    public static String getUUIDBento() {
        String sUuidBento = SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.UUID_BENTO);

        if (sUuidBento.isEmpty())
            sUuidBento = UUID.randomUUID().toString();

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.UUID_BENTO, sUuidBento);

        return sUuidBento;
    }

}
